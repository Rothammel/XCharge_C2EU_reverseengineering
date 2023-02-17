;/---------------------------
;|
;| MQTT Client
;|      V1.09
;|      20.10.2022
;|
;| Supports MQTT <= 3.1.1
;| http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html
;|
;| (c)HeX0R
;|
;|
;| New in V1.08    (13.10.2022)
;| -unsubscribe handling added
;|  seems, I've forgotten that
;|
;| New in V1.09
;| -added #MQTTInfo events
;|
;\---------------------------

; ----------------------------------------------------------------------------
; "THE BEER-WARE LICENSE":
; <HeX0R@coderbu.de> wrote this file. as long as you retain this notice you
; can do whatever you want with this stuff. If we meet some day, and you think
; this stuff is worth it, you can buy me a beer in return
; (see address on https://hex0rs.coderbu.de/).
; Or send money for a beer via =>
; https://www.paypal.com/paypalme/hex0r
; Or just go out and drink a few on your own/with your friends ;)
;=============================================================================

CompilerIf #PB_Compiler_Thread = 0
	CompilerError "Please enable Thread-Safe Options!"
CompilerEndIf

XIncludeFile "MQTT_Common.pbi"

DeclareModule MQTT_CLIENT
	
	Structure LAST_WILL
		Topic.s
		Message.s
		Retain.b
		QoS.b
	EndStructure
	
	Structure CLIENT_INIT
		BrokerURL.s             ;URL or IP of the broker
		ClientIdentifier.s			;The (unique) identifier of our client
		Port.i									;if empty, 1883 will be used
		Window.i								;Window, where the thread will send messages to
		WindowEvent.i						;WindowEvent, which will be used for those messages
		Username.s							;Username for authentication, if empty, broker needs to allow annonymous connections
		Password.s							;Password for authentication
		KeepAlive.i							;The time (in seconds), where we are considered to be offline
		AltPingInterval.i       ;
		InitialBufferSize.i			;if empty, initial buffer size will be 65536Bytes, the buffer will increase anyway, if packets are bigger
		Will.LAST_WILL
	EndStructure
	
	Declare InitClient(*Config.CLIENT_INIT, RunAlso.i = #False)
	;                                 use that to initialize a new client
	;                                 MQTT_Client supports more than just one Client, it will return the ID of the new initialized Client
	;                                 set the CLIENT_INIT structure to fit your needs.
	;                                 When RunAlso is #True, it will also start-up the client (no need to call StartClient())
	Declare DeInitClient(ClientID.i) ;all resources of the client will be removed, it will also stop the client (in case it is still running)
	Declare StartClient(ClientID.i, CleanSession = 1)
	;                                 Start the client
	;                                 if CleanSession = 0, the broker will try to reanimate a previous session (if there is any)
	Declare StopClient(ClientID)     ;Stop Client
	Declare SubscribeToTopics(ClientID, List Topics.MQTT_Common::Filter())
	;                                 Subscribe to topics to receive published messages
	Declare PublishTopic(ClientID, Topic.s, *Payload, PayLoadLength, QoS = 0, PayLoadType = MQTT_Common::#PayloadType_UnicodeString)
	;                                 Publish to a topic
	Declare UnsubscribeFromTopics(ClientID, List Topics.MQTT_Common::Filter())
	
EndDeclareModule


Module MQTT_CLIENT
	EnableExplicit
	UseModule MQTT_Common
	
	;{ internal Structures
	Structure Thread
		ThreadID.i
		StopIt.i
		Port.i
		Username.s
		Password.s
		URL.s
		Error.i
		CleanSession.i
		KeepAlive.i
		InitBufferSize.i
	EndStructure
	
	Structure _CLIENT_
		ClientID.i
		Mutex.i
		Accepted.i
		Identifier.s
		Window.i
		WindowEvent.i
		LastActivity.q
		AltPingInterval.i
		*Buffer
		BufferPos.i
		Will.LAST_WILL
		T.Thread
		Map PacketIdentifiersUsed.b()
		List Subscriptions.Filter()
		List Packets.PACKET()
	EndStructure
	;}
	
	Global Init
	Global NewList Clients._CLIENT_()
	
	Procedure IsSocketBlocked()
		Protected Result
		
		;in heavy network traffic times, we might see a WSAEWOULDBLOCK
		CompilerSelect #PB_Compiler_OS
			CompilerCase #PB_OS_Windows
				If WSAGetLastError_() = #WSAEWOULDBLOCK
					Result = #True
				EndIf
			CompilerCase #PB_OS_Linux
; 				If PeekL(errno_location()) = #EWOULDBLOCK
; 					Result = #True
; 				EndIf
		CompilerEndSelect
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure FindClient(ClientID.i)
		;we could also simply use the ClientID, which is already a pointer to the correct Clients() element
		;That would be much faster of course, but you can't validate then, if it is still an existing element
		;Therefore I went that way.
		Protected *C._CLIENT_
		
		ForEach Clients()
			If @Clients() = ClientID
				*C = ClientID
				Break
			EndIf
		Next
		
		ProcedureReturn *C
	EndProcedure
	
	Procedure SetGetUniqePacketIdentifiers(*C._CLIENT_, Set = -1, Remove = #False)
		Protected Result.u, i, Size
		
		;Multi procedure for unique packet identifiers.
		;If Set > -1 it will add a packet identifier (sent by broker)
		;If Set > -1 AND Remove = #true, it will remove an identifier
		;If Set = -1 and Remove = #false, it will return the next unused packet identifier (and store it in the map)
		;packet identifiers have to be unique between broker and client, but NOT accross all clients!

		With *C
			If Set <> -1
				If Remove
					DeleteMapElement(\PacketIdentifiersUsed(), Str(Set))
				Else
					\PacketIdentifiersUsed(Str(Set)) = #True
				EndIf
			Else
				;we need the smallest available unused packet identifier
				Size = MapSize(\PacketIdentifiersUsed()) + 1
				Dim w.u(Size)
				ForEach \PacketIdentifiersUsed()
					If Val(MapKey(\PacketIdentifiersUsed())) < Size
						w(Val(MapKey(\PacketIdentifiersUsed()))) = #True
					EndIf
				Next
				For i = 1 To Size
					If W(i) = #False
						Result = i
						Break
					EndIf
				Next i
				\PacketIdentifiersUsed(Str(Result)) = #True
			EndIf
		EndWith
		
		ProcedureReturn Result
	EndProcedure
		
	Procedure SendDataToWindow(*C._CLIENT_, Type, Topic.s = "", *Payload = 0, PayLoadLength = 0, ErrorText.s = "", PacketIdentifier = 0, Error = 0, QoS = 0, DUP = 0, Retain = 0)
		Protected *send.MQTT_EVENTDATA, Size, Strings.s
		
		;this is to send data to an [optional] Window
		
		If IsWindow(*C\Window) And *C\WindowEvent
			Strings                = Topic + #ESC$ + ErrorText
			Size                   = SizeOf(MQTT_EVENTDATA) + StringByteLength(Strings, #PB_UTF8) + 1
			*send                  = AllocateMemory(Size)
			*send\Type             = Type
			*send\PacketIdentifier = PacketIdentifier
			*send\Error            = Error
			*send\QoS              = QoS
			*send\DUP              = DUP
			*send\Retain           = Retain
			*send\PayLoadLength    = PayLoadLength
			If PayLoadLength
				*send\PayLoad = AllocateMemory(PayLoadLength)
				CompilerIf #USE_BASE64_PAYLOAD
					Base64Decoder(PeekS(*Payload), *send\PayLoad, PayLoadLength)
				CompilerElse
					CopyMemory(*Payload, *send\PayLoad, PayLoadLength)
				CompilerEndIf
			EndIf
			PokeS(*send + OffsetOf(MQTT_EVENTDATA\D), Strings, -1, #PB_UTF8)
			PostEvent(*C\WindowEvent, *C\Window, 0, 0, *send)
		EndIf
		
	EndProcedure
	
	Procedure GetPacketSize(*Stream.HEADER, Size, *i.INTEGER = #False, *reallength.INTEGER = #False)
		Protected i, Roll, Result
		
		;get length of a received packet, or -1 if it is not complete, yet
		;*Stream              => is the Buffer received
		;Size                 => is the length we received until now
		;*i.INTEGER           => will be set to the amount of bytes needed to store the length information
		;                        MQTT uses a dynamical amount of bytes (1...4) to store the packetlength
		;*reallength.INTEGER  => will be set to the packet length in case Result = -1
		;                        so, if Result = -1, but *reallength\i would be e.g. 100000
		;                        we know, that this packet has 100000bytes, but is not complete, yet.
		;                        we can also check then, if our receiving buffer would be huge enough for that packet and resize it if needed.
		
		For i = 0 To 3
			If Size < SizeOf(Header) + 1 + i
				Result = -1
				Break
			EndIf
			Result | ((*Stream\bytes[i] & $7F) << Roll)
			If *Stream\bytes[i] & $80 = 0
				i + 1
				Break
			EndIf
			Roll + 7
		Next i
		
		If Result <> -1
			If Size < SizeOf(HEADER) + i + Result
				If *reallength
					*reallength\i = Result
				EndIf
				Result = -1
			EndIf
		EndIf
		
		If Result <> -1 And *i
			*i\i = i
		EndIf
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure PacketDataReceived(*Stream.HEADER, Size, *P.PACKET, *C._CLIENT_)
		Protected RemainingLength, CurrPos, L, Topic.s, Length, Result = #True
		
		;This procedure will pre-handle all packets, which have been received from the broker
		
		*P\Flags        = *Stream\PacketType & $F
		*P\Type         = (*Stream\PacketType & $F0) >> 4
		*P\PacketState  = #PacketState_Incoming
		Length          = GetPacketSize(*Stream, Size, @CurrPos)
		If Length = -1
			ProcedureReturn 0
		EndIf
		*P\PayLoad\PayLoadBase64 = ""
		
		RemainingLength = Length
		
		Select *P\Type
			Case #CONNACK
				Select *Stream\bytes[CurrPos + 1]
					Case 0
						;connection accepted
						*C\Accepted = #True
						SendDataToWindow(*C, #MQTTEvent_SuccessfullyConnected)
					Case 1
						*C\T\Error = #Error_UnsupportedProtocolVersion
						SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(*C\T\Error), 0, *C\T\Error)
						Result = #False
					Case 2
						*C\T\Error = #Error_UnsupportedIdentifier
						SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(*C\T\Error), 0, *C\T\Error)
						Result = #False
					Case 3
						*C\T\Error = #Error_MQTTServiceUnavailable
						SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(*C\T\Error), 0, *C\T\Error)
						Result = #False
					Case 4
						*C\T\Error = #Error_BadUsernameOrPassword
						SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(*C\T\Error), 0, *C\T\Error)
						Result = #False
					Case 5
						*C\T\Error = #Error_NotAuthorizedToConnect
						SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(*C\T\Error), 0, *C\T\Error)
						Result = #False
					Default
						Result = #False
				EndSelect
			Case #PINGRESP
				;do nothing, we are still alive!
			Case #PUBLISH
				;paket got published
				*P\Retain = *P\Flags  & $01
				*P\QoS    = (*P\Flags & $06) >> 1
				*P\DUP    = (*P\Flags & $08) >> 3
				L         = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
				CurrPos + 2
				RemainingLength - 2
				*P\TopicName = PeekS(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L, #PB_UTF8 | #PB_ByteLength)
				CurrPos + L
				RemainingLength - L
				If *P\QoS > 0
					*P\PacketIdentifier = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
					SetGetUniqePacketIdentifiers(*C, *P\PacketIdentifier)
					CurrPos + 2
					RemainingLength - 2
				EndIf
				If RemainingLength > 0
					*P\PayLoad\BufferLengh = RemainingLength
					CompilerIf #USE_BASE64_PAYLOAD
						*P\PayLoad\PayLoadBase64 = Base64Encoder(*Stream + OffsetOf(HEADER\bytes) + CurrPos, RemainingLength)
					CompilerElse
						*P\PayLoad\Buffer      = AllocateMemory(RemainingLength)
						CopyMemory(*Stream + OffsetOf(HEADER\bytes) + CurrPos, *P\PayLoad\Buffer, RemainingLength)
					CompilerEndIf
				EndIf
			Case #SUBACK
				;successfully subscribed
				*P\PacketIdentifier = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
				CurrPos + 2
				RemainingLength - 2
				If RemainingLength > 0
					*P\PayLoad\BufferLengh = RemainingLength
					CompilerIf #USE_BASE64_PAYLOAD
						*P\PayLoad\PayLoadBase64 = Base64Encoder(*Stream + OffsetOf(HEADER\bytes) + CurrPos, RemainingLength)
					CompilerElse
						*P\PayLoad\Buffer      = AllocateMemory(RemainingLength)
						CopyMemory(*Stream + OffsetOf(HEADER\bytes) + CurrPos, *P\PayLoad\Buffer, RemainingLength)
					CompilerEndIf
				EndIf
			Case #PUBACK, #PUBREC, #PUBREL, #PUBCOMP, #UNSUBACK
				*P\PacketIdentifier = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
				;do nothing here
		EndSelect
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure SetStreamLength(*Stream.HEADER, Length)
		Protected Result, More
		
		;more or less the opposite of GetPacketSize() to set the length of a packet (when sending packets)
		
		Repeat
			If Length > 127
				More = $80
			Else
				More = 0
			EndIf
			*Stream\bytes[Result] = (Length & $7F) | More
			Length >> 7
			Result + 1
		Until Length = 0
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure SendConnectPacket(*C._CLIENT_)
		Protected Pos, OverallLengh, Length, i, Result, *Buffer.HEADER
		
		;send the #CONNECT package to the broker
		
		
		Length = 10 + 2 + StringByteLength(*C\Identifier, #PB_UTF8)
		If *C\T\Username
			Length + 2 + StringByteLength(*C\T\Username, #PB_UTF8)
		EndIf
		If *C\T\Password
			Length + 2 + StringByteLength(*C\T\Password, #PB_UTF8)
		EndIf
		If *C\Will\Topic
			Length + 2 + StringByteLength(*C\Will\Message, #PB_UTF8) + 2 + StringByteLength(*C\Will\Topic, #PB_UTF8)
		EndIf
		
		*Buffer = AllocateMemory(SizeOf(HEADER) + 8 + Length)
		If *Buffer
			*Buffer\PacketType = #CONNECT << 4
			Pos = SetStreamLength(*Buffer, Length)
			*Buffer\bytes[Pos]     = 0
			*Buffer\bytes[Pos + 1] = 4
			PokeS(*Buffer + OffsetOf(HEADER\bytes) + Pos + 2, "MQTT", -1, #PB_UTF8)
			*Buffer\bytes[Pos + 6] = 4 ;MQTT Protocol Version
			i = *C\T\CleanSession << 1
			If *C\T\Username
				i = i | $80
			EndIf
			If *C\T\Password
				i = i | $40
			EndIf
			If *C\Will\Topic
				i = i | $04
				i = i | (*C\Will\QoS << 3)
				i = i | (*C\Will\Retain << 5)
			EndIf
			*Buffer\bytes[Pos + 7] = i
			*Buffer\bytes[Pos + 8]     = (*C\T\KeepAlive & $FF00) >> 8
			*Buffer\bytes[Pos + 9] = *C\T\KeepAlive & $FF
			Pos + 10
			i = StringByteLength(*C\Identifier, #PB_UTF8)
			*Buffer\bytes[Pos]     = (i & $FF00) >> 8
			*Buffer\bytes[Pos + 1] = i & $FF
			Pos + 2
			PokeS(*Buffer + OffsetOf(HEADER\bytes) + Pos, *C\Identifier, -1, #PB_UTF8)
			Pos + i
			If *C\Will\Topic
				i = StringByteLength(*C\Will\Topic, #PB_UTF8)
				*Buffer\bytes[Pos]     = (i & $FF00) >> 8
				*Buffer\bytes[Pos + 1] = i & $FF
				Pos + 2
				PokeS(*Buffer + OffsetOf(HEADER\bytes) + Pos, *C\Will\Topic, -1, #PB_UTF8)
				Pos + i
				i = StringByteLength(*C\Will\Message, #PB_UTF8)
				*Buffer\bytes[Pos]     = (i & $FF00) >> 8
				*Buffer\bytes[Pos + 1] = i & $FF
				Pos + 2
				If i > 0
					PokeS(*Buffer + OffsetOf(HEADER\bytes) + Pos, *C\Will\Message, -1, #PB_UTF8)
					Pos + i
				EndIf
			EndIf
			If *C\T\Username
				i = StringByteLength(*C\T\Username, #PB_UTF8)
				*Buffer\bytes[Pos]     = (i & $FF00) >> 8
				*Buffer\bytes[Pos + 1] = i & $FF
				Pos + 2
				PokeS(*Buffer + OffsetOf(HEADER\bytes) + Pos, *C\T\Username, -1, #PB_UTF8)
				Pos + i
			EndIf
			If *C\T\Password
				i = StringByteLength(*C\T\Password, #PB_UTF8)
				*Buffer\bytes[Pos]     = (i & $FF00) >> 8
				*Buffer\bytes[Pos + 1] = i & $FF
				Pos + 2
				PokeS(*Buffer + OffsetOf(HEADER\bytes) + Pos, *C\T\Password, -1, #PB_UTF8)
				Pos + i
			EndIf
			Result = SendNetworkData(*C\ClientID, *Buffer, OffsetOf(HEADER\bytes) + Pos)
			
			FreeMemory(*Buffer)
		EndIf
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure SendCommand(*C._CLIENT_, *P.PACKET)
		Protected CP.MINI_PACKET, Result
		Protected *Buff.HEADER, *Buff2, CurrPos, i, Size
		Protected Length, LengthShould, LengthIs
		
		;main sending procedure
		;will make sure, that packets get send completely
		
		With *P
			If \NYS\Buffer
				*Buff        = \NYS\Buffer
				\NYS\Buffer  = 0
				LengthIs     = SendNetworkData(*C\ClientID, *Buff, \NYS\BufferLength)
				LengthShould = \NYS\BufferLength
			Else
				Select \Type
					Case #SUBSCRIBE
						ForEach \tmpSubsc()
							Length + StringByteLength(\tmpSubsc()\Topic, #PB_UTF8) + 3
						Next
						If Length > 0
							Length + 2
							*Buff            = AllocateMemory(64 + Length)
							*Buff\PacketType = 2 | (\Type << 4)
							CurrPos          = SetStreamLength(*Buff, Length)
							*Buff\bytes[CurrPos + 1] = \PacketIdentifier & $FF
							*Buff\bytes[CurrPos]     = (\PacketIdentifier & $FF00) >> 8
							CurrPos + 2
							ForEach \tmpSubsc()
								i = StringByteLength(\tmpSubsc()\Topic, #PB_UTF8)
								*Buff\bytes[CurrPos + 1] = i & $FF
								*Buff\bytes[CurrPos]     = (i & $FF00) >> 8
								CurrPos + 2
								PokeS(*Buff + OffsetOf(HEADER\bytes) + CurrPos, \tmpSubsc()\Topic, -1, #PB_UTF8)
								CurrPos + i
								*Buff\bytes[CurrPos] = \tmpSubsc()\QoS
								CurrPos + 1
							Next
							LengthIs     = SendNetworkData(*C\ClientID, *Buff, CurrPos + 1)
							LengthShould = CurrPos + 1
							;FreeMemory(*Buff)
						EndIf
					Case #UNSUBSCRIBE
						ForEach \tmpSubsc()
							Length + StringByteLength(\tmpSubsc()\Topic, #PB_UTF8) + 2
						Next
						If Length > 0
							Length + 2
							*Buff            = AllocateMemory(64 + Length)
							*Buff\PacketType = 2 | (\Type << 4)
							CurrPos          = SetStreamLength(*Buff, Length)
							*Buff\bytes[CurrPos + 1] = \PacketIdentifier & $FF
							*Buff\bytes[CurrPos]     = (\PacketIdentifier & $FF00) >> 8
							CurrPos + 2
							ForEach \tmpSubsc()
								i = StringByteLength(\tmpSubsc()\Topic, #PB_UTF8)
								*Buff\bytes[CurrPos + 1] = i & $FF
								*Buff\bytes[CurrPos]     = (i & $FF00) >> 8
								CurrPos + 2
								PokeS(*Buff + OffsetOf(HEADER\bytes) + CurrPos, \tmpSubsc()\Topic, -1, #PB_UTF8)
								CurrPos + i
							Next
							LengthIs     = SendNetworkData(*C\ClientID, *Buff, CurrPos + 1)
							LengthShould = CurrPos + 1
							;FreeMemory(*Buff)
						EndIf
					Case #PUBREL
						CP\a[0]      = 2 | (\Type << 4)
						CP\a[1]      = 2
						CP\a[2]      = (\PacketIdentifier & $FF00) >> 8
						CP\a[3]      = \PacketIdentifier & $FF
						LengthIs     = SendNetworkData(*C\ClientID, @CP, 4)
						LengthShould = 4
					Case #PUBACK, #PUBREC, #PUBCOMP
						CP\a[0]      = \Type << 4
						CP\a[1]      = 2
						CP\a[2]      = (\PacketIdentifier & $FF00) >> 8
						CP\a[3]      = \PacketIdentifier & $FF
						LengthIs     = SendNetworkData(*C\ClientID, @CP, 4)
						LengthShould = 4
					Case #PUBLISH
						i      = \PayLoad\BufferLengh
						Length = StringByteLength(\TopicName, #PB_UTF8) + i + 2
						If \QoS > 0
							Length + 2
						EndIf
						*Buff = AllocateMemory(64 + Length)
						If *Buff
							*Buff\PacketType         = \Type << 4
							*Buff\PacketType         = *Buff\PacketType | (\QoS << 1)
							*Buff\PacketType         = *Buff\PacketType | (\DUP << 3)
							*Buff\PacketType         = *Buff\PacketType | \Retain
							CurrPos                  = SetStreamLength(*Buff, Length)
							Length                   = StringByteLength(\TopicName, #PB_UTF8)
							*Buff\bytes[CurrPos + 1] = Length & $FF
							*Buff\bytes[CurrPos]     = (Length & $FF00) >> 8
							CurrPos + 2
							CurrPos + PokeS(*Buff + OffsetOf(HEADER\bytes) + CurrPos, \TopicName, -1, #PB_UTF8)
							If \QoS > 0
								*Buff\bytes[CurrPos + 1] = \PacketIdentifier & $FF
								*Buff\bytes[CurrPos]     = (\PacketIdentifier & $FF00) >> 8
								CurrPos + 2
							EndIf
							If i
								CompilerIf #USE_BASE64_PAYLOAD
									Base64Decoder(\PayLoad\PayLoadBase64, *Buff + OffsetOf(HEADER\bytes) + CurrPos, i)
								CompilerElse
									CopyMemory(\PayLoad\Buffer, *Buff + OffsetOf(HEADER\bytes) + CurrPos, i)
								CompilerEndIf
								CurrPos + i
							EndIf
							LengthIs     = SendNetworkData(*C\ClientID, *Buff, CurrPos + 1)
							LengthShould = CurrPos + 1
							;FreeMemory(*Buff)
						EndIf
				EndSelect
			EndIf
			If LengthIs = LengthShould
					If LengthIs > 4
						FreeMemory(*Buff)
					EndIf
					\NYS\Buffer     = 0
					Result          = #SendFinished
					*C\LastActivity = ElapsedMilliseconds()
				ElseIf LengthIs = -1
					If IsSocketBlocked()
						\NYS\Buffer       = AllocateMemory(LengthShould)
						\NYS\BufferLength = LengthShould
						If LengthShould <= 4
							CopyMemory(@CP, \NYS\Buffer, \NYS\BufferLength)
						Else
							CopyMemory(*Buff, \NYS\Buffer, \NYS\BufferLength)
							FreeMemory(*Buff)
						EndIf
						Result = #SendNotFinished
					Else
						;freak out
						Result = #SendFailed
						If LengthShould > 4
							FreeMemory(*Buff)
						EndIf
						\NYS\Buffer = 0
					EndIf
				Else
					;packet not finished
					\NYS\Buffer       = AllocateMemory(LengthShould - LengthIs)
					\NYS\BufferLength = LengthShould - LengthIs
					*C\LastActivity   = ElapsedMilliseconds()
					If LengthShould <= 4
						CopyMemory(@CP + LengthIs, \NYS\Buffer, \NYS\BufferLength)
					Else
						CopyMemory(*Buff + LengthIs, \NYS\Buffer, \NYS\BufferLength)
						FreeMemory(*Buff)
					EndIf
					Result = #SendNotFinished
				EndIf
			
		EndWith
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure ClearPacket(*C._CLIENT_)
		
		;make sure the packet is cleared gracefully
		
		CompilerIf #USE_BASE64_PAYLOAD = #False
			If *C\Packets()\PayLoad\Buffer
				FreeMemory(*C\Packets()\PayLoad\Buffer)
			EndIf
			If *C\Packets()\NYS\Buffer
				FreeMemory(*C\Packets()\NYS\Buffer)
			EndIf
		CompilerEndIf
		DeleteElement(*C\Packets())
	EndProcedure
	
	Procedure ClientThread(*C._CLIENT_)
		Protected Found, Pos, *P.PACKET
		Protected i, j, k, l, Add, Length
		Protected State, CP.MINI_PACKET, *Buff2
		Protected LogTimer.q, RealLength
		
		;here is all the magic :)
		
		*C\ClientID = OpenNetworkConnection(*C\T\URL, *C\T\Port, #PB_Network_TCP, 2000)
		If *C\ClientID = 0
			*C\T\Error = #Error_CantConnect
			SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(*C\T\Error), 0, *C\T\Error)
			ProcedureReturn
		EndIf
		
		LogTimer = ElapsedMilliseconds()
		
		
		Repeat
			
			CompilerIf #PB_Compiler_Debugger And #DEEP_DEBUG
				If LogTimer + 2000 < ElapsedMilliseconds()
					LogTimer = ElapsedMilliseconds()
					Debug "Identifiers: " + Str(MapSize(*C\PacketIdentifiersUsed()))
				EndIf
			CompilerEndIf
			
			If State = 0
				;send welcome packet to broker
				SendConnectPacket(*C)
				State = 1
			EndIf
			
			With *C
				Select NetworkClientEvent(*C\ClientID)
					Case #PB_NetworkEvent_None
						Delay(20)
					Case #PB_NetworkEvent_Disconnect
						\T\Error = #Error_BeingDisconnected
						SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(*C\T\Error), 0, *C\T\Error)
						Break
					Case #PB_NetworkEvent_Data
						;get packet, here we only receive the packet, we will handle it down below outside of the network loop
						Pos = ReceiveNetworkData(\ClientID, \Buffer + \BufferPos, MemorySize(\Buffer) - \BufferPos)
						If Pos > -1
							\BufferPos + Pos
							LockMutex(\Mutex)
							Repeat
								RealLength = -1
								Length     = GetPacketSize(\Buffer, \BufferPos, @Add, @RealLength)
								If Length = -1
									If RealLength <> -1
										If MemorySize(\Buffer) < SizeOf(HEADER) + Add + RealLength
											\Buffer = ReAllocateMemory(\Buffer, SizeOf(HEADER) + Add + RealLength + 64)
										EndIf
									EndIf
								Else
									LastElement(\Packets())
									AddElement(\Packets())
									If PacketDataReceived(\Buffer, \BufferPos, @\Packets(), *C)
										MoveMemory(\Buffer + Length + SizeOf(HEADER) + Add, \Buffer, \BufferPos - Length - SizeOf(HEADER) - Add)
										\BufferPos - Length - SizeOf(HEADER) - Add
									Else
										;error, close connection
										CloseNetworkConnection(\ClientID)
										\T\Error = #Error_WrongAnswerReceived
										SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(\T\Error), 0, *C\T\Error)
										UnlockMutex(\Mutex)
										Break 2
									EndIf
								EndIf
							Until Length = -1
							UnlockMutex(\Mutex)
						Else
							;network error? handle it?
						EndIf
				EndSelect
				
				If \LastActivity + (\AltPingInterval * 1000) < ElapsedMilliseconds()
					If \Accepted = 0
						;resend welcome message
						State = 0
						\LastActivity = ElapsedMilliseconds()
					Else
						CP\a[0] = #PINGREQ << 4
						CP\a[1] = 0
						If SendNetworkData(\ClientID, @CP, 2) = 2
							\LastActivity = ElapsedMilliseconds()
						Else
							\T\Error = #Error_TimedOut
							SendDataToWindow(*C, #MQTTEvent_Error, "", 0, 0, ErrorDescription(\T\Error), 0, *C\T\Error)
							Break
						EndIf
					EndIf
				EndIf
				
				;packet handling
				LockMutex(\Mutex)
				ForEach \Packets()
					If \Packets()\PacketState  = #PacketState_Incoming
						Select \Packets()\Type
							Case #CONNACK, #PINGRESP
								;nothing more to do
								ClearPacket(*C)
							Case #PUBLISH
								CompilerIf #USE_BASE64_PAYLOAD
									SendDataToWindow(*C, #MQTTEvent_PublishIncoming, \Packets()\TopicName, @\Packets()\PayLoad\PayLoadBase64, \Packets()\PayLoad\BufferLengh, "", \Packets()\PacketIdentifier, 0, \Packets()\QoS, \Packets()\DUP, \Packets()\Retain)
								CompilerElse
									SendDataToWindow(*C, #MQTTEvent_PublishIncoming, \Packets()\TopicName, \Packets()\PayLoad\Buffer, \Packets()\PayLoad\BufferLengh, "", \Packets()\PacketIdentifier, 0, \Packets()\QoS, \Packets()\DUP, \Packets()\Retain)
								CompilerEndIf
								If \Packets()\QoS = 1
									\Packets()\Type = #PUBACK
									\Packets()\PacketState = #PacketState_OutgoingNotSendYet
								ElseIf \Packets()\QoS = 2
									\Packets()\Type = #PUBREC
									\Packets()\PacketState = #PacketState_OutgoingNotSendYet
								Else
									ClearPacket(*C)
								EndIf
							Case #PUBACK
								*P = @\Packets()
								ForEach \Packets()
									If @\Packets() <> *P And \Packets()\Type = #PUBLISH And \Packets()\PacketIdentifier = *P\PacketIdentifier
										SendDataToWindow(*C, #MQTTEvent_PublishingSuccessfull, \Packets()\TopicName, 0, 0, "", \Packets()\PacketIdentifier)
										SetGetUniqePacketIdentifiers(*C, \Packets()\PacketIdentifier, #True)
										ClearPacket(*C)
										Break
									EndIf
								Next
								ChangeCurrentElement(\Packets(), *P)
								ClearPacket(*C)
							Case #PUBREC, #PUBREL
								*P = @\Packets()
								ForEach \Packets()
									If @\Packets() <> *P And \Packets()\Type = #PUBLISH And \Packets()\PacketIdentifier = *P\PacketIdentifier
										ClearPacket(*C)
										Break
									EndIf
								Next
								ChangeCurrentElement(\Packets(), *P)
								If \Packets()\Type = #PUBREC
									\Packets()\Type = #PUBREL
								Else
									\Packets()\Type = #PUBCOMP
								EndIf
								\Packets()\PacketState = #PacketState_OutgoingNotSendYet
							Case #PUBCOMP
								SetGetUniqePacketIdentifiers(*C, \Packets()\PacketIdentifier, #True)
								ForEach \Packets()
									If \Packets()\PacketIdentifier = *P\PacketIdentifier
										ClearPacket(*C)
									EndIf
								Next
							Case #SUBACK
								*P = @\Packets()
								ForEach \Packets()
									If @\Packets() <> *P And \Packets()\Type = #SUBSCRIBE And \Packets()\PacketIdentifier = *P\PacketIdentifier
										;here we are
										i = 0
										CompilerIf #USE_BASE64_PAYLOAD
											*Buff2 = AllocateMemory(StringByteLength(*P\PayLoad\PayLoadBase64))
											Base64Decoder(*P\PayLoad\PayLoadBase64, *Buff2, *p\PayLoad\BufferLengh)
										CompilerElse
											*Buff2 = *P\PayLoad\Buffer
										CompilerEndIf
										ForEach \Packets()\tmpSubsc()
											j = PeekB(*Buff2 + i)
											If j >= 0 And j <= 2
												AddElement(\Subscriptions())
												\Subscriptions()\Topic = \Packets()\tmpSubsc()\Topic
												\Subscriptions()\QoS   = j
											EndIf
											i + 1
										Next
										CompilerIf #USE_BASE64_PAYLOAD
											FreeMemory(*Buff2)
										CompilerEndIf
										ClearList(\Packets()\tmpSubsc())
										SetGetUniqePacketIdentifiers(*C, \Packets()\PacketIdentifier, #True)
										CompilerIf #USE_BASE64_PAYLOAD = #False
											If \Packets()\PayLoad\Buffer
												FreeMemory(\Packets()\PayLoad\Buffer)
											EndIf
										CompilerEndIf
										DeleteElement(\Packets())
										Break
									EndIf
								Next
								ChangeCurrentElement(\Packets(), *P)
								CompilerIf #USE_BASE64_PAYLOAD
									SendDataToWindow(*C, #MQTTEvent_SubscriptionSuccessfull, \Packets()\TopicName, @\Packets()\PayLoad\PayLoadBase64, \Packets()\PayLoad\BufferLengh, "", \Packets()\PacketIdentifier)
								CompilerElse
									SendDataToWindow(*C, #MQTTEvent_SubscriptionSuccessfull, \Packets()\TopicName, \Packets()\PayLoad\Buffer, \Packets()\PayLoad\BufferLengh, "", \Packets()\PacketIdentifier)
								CompilerEndIf
								ClearPacket(*C)
							Case #UNSUBACK
								*P = @\Packets()
								ForEach \Packets()
									If @\Packets() <> *P And \Packets()\Type = #UNSUBSCRIBE And \Packets()\PacketIdentifier = *P\PacketIdentifier
										;here we are
										;#UNSUBACK has no Payload!
										ClearList(\Packets()\tmpSubsc())
										SetGetUniqePacketIdentifiers(*C, \Packets()\PacketIdentifier, #True)
										DeleteElement(\Packets())
										Break
									EndIf
								Next
								ChangeCurrentElement(\Packets(), *P)
								SendDataToWindow(*C, #MQTTEvent_UnsubscriptionSuccessfull, \Packets()\TopicName, 0, 0, "", \Packets()\PacketIdentifier)
								ClearPacket(*C)
								
								
						EndSelect
					ElseIf \Packets()\PacketState = #PacketState_OutgoingNotSendYet
						Select \Packets()\Type
							Case #SUBSCRIBE
								Select SendCommand(*C, @\Packets())
									Case #SendFinished
										\Packets()\PacketState = #PacketState_WaitForAnswer
									Case #SendFailed
										ClearPacket(*C)
										Break
									Case #SendNotFinished
										Break
								EndSelect
							Case #UNSUBSCRIBE
								Select SendCommand(*C, @\Packets())
									Case #SendFinished
										\Packets()\PacketState = #PacketState_WaitForAnswer
									Case #SendFailed
										ClearPacket(*C)
										Break
									Case #SendNotFinished
										Break
								EndSelect
							Case #PUBLISH
								Select SendCommand(*C, @\Packets())
									Case #SendFinished
										If \Packets()\QoS > 0
											\Packets()\PacketState = #PacketState_WaitForAnswer
										Else
											SendDataToWindow(*C, #MQTTEvent_PublishingSuccessfull, \Packets()\TopicName, 0, 0, "", \Packets()\PacketIdentifier)
											ClearPacket(*C)
										EndIf
									Case #SendFailed
										ClearPacket(*C)
										Break
									Case #SendNotFinished
										Break
								EndSelect
							Case #PUBREL
								Select SendCommand(*C, @\Packets())
									Case #SendFinished
										\Packets()\PacketState = #PacketState_WaitForAnswer
									Case #SendFailed
										ClearPacket(*C)
										Break
									Case #SendNotFinished
										Break
								EndSelect
							Case #PUBACK
								Select SendCommand(*C, @\Packets())
									Case #SendFinished
										SetGetUniqePacketIdentifiers(*C, \Packets()\PacketIdentifier, #True)
										ClearPacket(*C)
									Case #SendFailed
										ClearPacket(*C)
										Break
									Case #SendNotFinished
										Break
								EndSelect
							Case #PUBREC
								Select SendCommand(*C, @\Packets())
									Case #SendFinished
										\Packets()\PacketState = #PacketState_WaitForAnswer
									Case #SendFailed
										ClearPacket(*C)
										Break
									Case #SendNotFinished
										Break
								EndSelect
						EndSelect
					EndIf
				Next
				UnlockMutex(\Mutex)
			EndWith
			
		Until *C\T\StopIt = #True
		
		SendDataToWindow(*C, #MQTTEvent_Info, "", 0, 0, "Thread has been ended", 0, #Info_ThreadEnded)
		
	EndProcedure
	
	Procedure PublishTopic(ClientID, Topic.s, *Payload, PayLoadLength, QoS = 0, PayLoadType = #PayloadType_UnicodeString)
		Protected *Buff, Result, *Client._CLIENT_
		
		;publish to a topic
		
		*Client = FindClient(ClientID)
		If *Client = #False
			ProcedureReturn 0
		EndIf
		If PayLoadLength > 268435455
			*Client\T\Error = #Error_LengthOfPacketIncorrect
			ProcedureReturn 0
		EndIf
		
		With *Client
			LockMutex(\Mutex)
			LastElement(\Packets())
			AddElement(\Packets())
			\Packets()\Type                = #PUBLISH
			\Packets()\PacketState         = #PacketState_OutgoingNotSendYet
			\Packets()\TopicName           = Topic
			\Packets()\QoS                 = QoS
			\Packets()\PayLoad\BufferLengh = PayLoadLength
			If PayLoadLength
				CompilerIf #USE_BASE64_PAYLOAD
					Select PayLoadType
						Case #PayloadType_base64
							\Packets()\PayLoad\PayLoadBase64 = PeekS(*Payload)
						Case #PayloadType_Buffer
							\Packets()\PayLoad\PayLoadBase64 = Base64Encoder(*Payload, PayLoadLength)
						Case #PayloadType_UnicodeString
							*Buff                          = UTF8(PeekS(*Payload))
							\Packets()\PayLoad\BufferLengh   = MemorySize(*Buff)
							\Packets()\PayLoad\PayLoadBase64 = Base64Encoder(*Buff, \Packets()\PayLoad\BufferLengh)
							FreeMemory(*Buff)
						Case #PayloadType_UTF8String
							\Packets()\PayLoad\BufferLengh   = MemoryStringLength(*Payload, #PB_UTF8)
							\Packets()\PayLoad\PayLoadBase64 = Base64Encoder(*Payload, MemoryStringLength(*Payload, #PB_UTF8))
					EndSelect
				CompilerElse
					\Packets()\PayLoad\Buffer = AllocateMemory(PayLoadLength)
					Select PayLoadType
						Case #PayloadType_base64
							Base64Decoder(PeekS(*Payload), \Packets()\PayLoad\Buffer, PayLoadLength)
						Case #PayloadType_Buffer
							CopyMemory(*Payload, \Packets()\PayLoad\Buffer, PayLoadLength)
						Case #PayloadType_UnicodeString
							*Buff                        = UTF8(PeekS(*Payload))
							\Packets()\PayLoad\BufferLengh = MemorySize(*Buff)
							\Packets()\PayLoad\Buffer      = *Buff
						Case #PayloadType_UTF8String
							\Packets()\PayLoad\BufferLengh = MemoryStringLength(*Payload, #PB_UTF8)
							CopyMemory(*Payload, \Packets()\PayLoad\Buffer, \Packets()\PayLoad\BufferLengh)
					EndSelect
				CompilerEndIf
			EndIf
			If QoS = 1 Or QoS = 2
				Result                      = SetGetUniqePacketIdentifiers(*Client)
				\Packets()\PacketIdentifier = Result
			Else
				Result = #True
			EndIf
			UnlockMutex(\Mutex)
		EndWith
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure SubscribeToTopics(ClientID, List Topics.Filter())
		Protected Result, *Client._CLIENT_
		
		;subscribe to a list of topics
		
		*Client = FindClient(ClientID)
		If *Client = #False
			ProcedureReturn 0
		EndIf
		
		With Clients()
			
			LockMutex(\Mutex)
			Result = SetGetUniqePacketIdentifiers(*Client)
			LastElement(\Packets())
			AddElement(\Packets())
			\Packets()\PacketState      = #PacketState_OutgoingNotSendYet
			\Packets()\PacketIdentifier = Result
			\Packets()\Type             = #SUBSCRIBE
			ForEach Topics()
				AddElement(\Packets()\tmpSubsc())
				\Packets()\tmpSubsc()\Topic = Topics()\Topic
				\Packets()\tmpSubsc()\QoS   = Topics()\QoS
			Next
			UnlockMutex(\Mutex)
		EndWith
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure UnsubscribeFromTopics(ClientID, List Topics.Filter())
		Protected Result, *Client._CLIENT_
		
		;unsubscribe to a list of topics
		
		*Client = FindClient(ClientID)
		If *Client = #False
			ProcedureReturn 0
		EndIf
		
		With Clients()
			
			LockMutex(\Mutex)
			Result = SetGetUniqePacketIdentifiers(*Client)
			LastElement(\Packets())
			AddElement(\Packets())
			\Packets()\PacketState      = #PacketState_OutgoingNotSendYet
			\Packets()\PacketIdentifier = Result
			\Packets()\Type             = #UNSUBSCRIBE
			ForEach Topics()
				AddElement(\Packets()\tmpSubsc())
				\Packets()\tmpSubsc()\Topic = Topics()\Topic
				\Packets()\tmpSubsc()\QoS   = Topics()\QoS
			Next
			UnlockMutex(\Mutex)
		EndWith
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure StopClient(ClientID.i)
		Protected *Client._CLIENT_
		
		*Client = FindClient(ClientID)
		If *Client
			*Client\T\StopIt = #True
			If *Client\T\ThreadID And IsThread(*Client\T\ThreadID)
				WaitThread(*Client\T\ThreadID)
			EndIf
			ForEach *Client\Packets()
				ClearPacket(*Client)
			Next
			ClearList(*Client\Packets())
			ClearMap(*Client\PacketIdentifiersUsed())
		EndIf
		
		ProcedureReturn *Client
	EndProcedure
	
	Procedure StartClient(ClientID.i, CleanSession = 1)
		Protected *Client._CLIENT_, Result
		
		*Client = FindClient(ClientID)
		If *Client = 0
			ProcedureReturn 0
		EndIf
		
		With *Client
			
			If CleanSession
				\T\CleanSession = 1
			EndIf
			\T\StopIt         = #False
			\T\Error          = #Error_None
			\BufferPos        = 0
			\T\ThreadID       = CreateThread(@ClientThread(), *Client)
			Result            = \T\ThreadID
			
		EndWith
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure _InitDefault(Value.i, Def.i)
		If Value = 0
			ProcedureReturn Def
		EndIf
		ProcedureReturn Value
	EndProcedure
	
	Procedure InitClient(*Config.CLIENT_INIT, RunAlso.i = #False)
		Protected Result
		
		If Init = #False
			CompilerIf #PB_Compiler_Version < 600
				Init = InitNetwork()
			CompilerElse
				Init = #True
			CompilerEndIf
		EndIf
		If Init = #False
			ProcedureReturn #False
		EndIf
		
		Result = AddElement(Clients())
		With Clients()
			;some plausibility checks
			If *Config\Will\QoS < 0
				*Config\Will\QoS = 0
			ElseIf *Config\Will\QoS > 2
				*Config\Will\QoS = 2
			EndIf
			If *Config\Will\Retain
				*Config\Will\Retain = 1
			EndIf
			\Identifier       = *Config\ClientIdentifier
			\Window           = *Config\Window
			\WindowEvent      = *Config\WindowEvent
			\Will\QoS         = *Config\Will\QoS
			\Will\Retain      = *Config\Will\Retain
			\Will\Topic       = *Config\Will\Topic
			\Will\Message     = *Config\Will\Message
			\T\InitBufferSize = _InitDefault(*Config\InitialBufferSize, $10000)
			\T\KeepAlive      = _InitDefault(*Config\KeepAlive, 60)
			\T\URL            = *Config\BrokerURL
			\T\Port           = _InitDefault(*Config\Port, 1883)
			\T\Username       = *Config\Username
			\T\Password       = *Config\Password
			\Buffer           = AllocateMemory(\T\InitBufferSize)
			\Mutex            = CreateMutex()
			If *Config\AltPingInterval > 0
				\AltPingInterval = *Config\AltPingInterval
			Else
				\AltPingInterval = \T\KeepAlive
			EndIf
			If \WindowEvent = 0
				\Window = -1
			EndIf
		EndWith
		
		If Result And RunAlso
			StartClient(Result)
		EndIf
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure DeInitClient(ClientID)
		Protected *Client._CLIENT_
		
		*Client = FindClient(ClientID)
		If *Client
			StopClient(*Client)
			If *Client\Buffer
				FreeMemory(*Client\Buffer)
			EndIf
			FreeMutex(Clients()\Mutex)
			CompilerIf #USE_BASE64_PAYLOAD
				ForEach Clients()\Packets()
					ClearPacket(@Clients())
				Next
			CompilerEndIf
			DeleteElement(Clients())
		EndIf
		
		ProcedureReturn *Client
	EndProcedure
	
EndModule

;--------------###########---------------
;                  EOF
;--------------###########---------------



CompilerIf #PB_Compiler_IsMainFile
	Enumeration #PB_Event_FirstCustomValue
		#MyEvent
	EndEnumeration
	
	Enumeration
		#Window
	EndEnumeration
	
	Enumeration
		#Editor
	EndEnumeration
	
	;not really needed, but the reply from a broker to a subscription request doesn't contain the topic we tried to subscribe to.
	;therefore it might make sense to store our topics we requested
	;in this case I only used it to create log messages, like "successfully subscribed to Topic"
	Structure _MySubscriptions_
		PacketIdentifier.u
		List Topics.MQTT_Common::Filter() ;<- needed to store the topics we subscribed to
	EndStructure
	
	Global ClientID.i
	Global NewList MySubscriptions._MySubscriptions_()
	
	Procedure LogIT(Text.s)
		If Text
			Text = FormatDate("%hh:%ii:%ss", Date()) + " " + Text
		EndIf
		AddGadgetItem(#Editor, -1, Text)
		CompilerSelect #PB_Compiler_OS
			CompilerCase #PB_OS_Windows
				Select GadgetType(#Editor)
					Case #PB_GadgetType_ListView
						SendMessage_(GadgetID(#Editor), #LB_SETTOPINDEX, CountGadgetItems(#Editor) - 1, #Null)
					Case #PB_GadgetType_ListIcon
						SendMessage_(GadgetID(#Editor), #LVM_ENSUREVISIBLE, CountGadgetItems(#Editor) - 1, #False)
					Case #PB_GadgetType_Editor
						SendMessage_(GadgetID(#Editor), #EM_SCROLLCARET, #SB_BOTTOM, 0)
				EndSelect
			CompilerCase #PB_OS_Linux
				Protected *Adjustment.GtkAdjustment
				*Adjustment       = gtk_scrolled_window_get_vadjustment_(gtk_widget_get_parent_(GadgetID(#Editor)))
				*Adjustment\value = *Adjustment\upper
				gtk_adjustment_value_changed_(*Adjustment)
		CompilerEndSelect
		
	EndProcedure
	
	Procedure MQTT_EventIncoming()
		Protected *Values.MQTT_Common::MQTT_EVENTDATA, a$
		Protected *Buffer, i, no, Type, Payload.s, Topic.s, PacketIdentifier.i, ErrorText.s, Error
		Protected QoS, DUP, Retain
		
		*Values = EventData()
		If *Values
			a$               = PeekS(*Values + OffsetOf(MQTT_Common::MQTT_EVENTDATA\D), -1, #PB_UTF8)
			Topic            = StringField(a$, 1, #ESC$)
			ErrorText        = ReplaceString(StringField(a$, 2, #ESC$), "{CLIENT}", "BROKER")
			Type             = *Values\Type
			PacketIdentifier = *Values\PacketIdentifier
			Error            = *Values\Error
			QoS              = *Values\QoS
			DUP              = *Values\DUP
			Retain           = *Values\Retain
			If *Values\PayLoad
				Payload = PeekS(*Values\PayLoad, *Values\PayLoadLength, #PB_UTF8 | #PB_ByteLength)
				FreeMemory(*Values\PayLoad)
			EndIf
			FreeMemory(*Values)
			;handle it...
			Select Type
				Case MQTT_Common::#MQTTEvent_SuccessfullyConnected
					LogIT("connection o.k.!")
					;subscribe now to some shelly plug topics
					AddElement(MySubscriptions())
					AddElement(MySubscriptions()\Topics())
					MySubscriptions()\Topics()\Topic = "shellies/+/online"      : MySubscriptions()\Topics()\QoS   = 0
					AddElement(MySubscriptions()\Topics())
					MySubscriptions()\Topics()\Topic = "shellies/+/relay/#"     : MySubscriptions()\Topics()\QoS   = 0
					AddElement(MySubscriptions()\Topics())
					MySubscriptions()\Topics()\Topic = "shellies/+/temperature" : MySubscriptions()\Topics()\QoS   = 0
					MySubscriptions()\PacketIdentifier = MQTT_CLIENT::SubscribeToTopics(ClientID, MySubscriptions()\Topics())
				Case MQTT_Common::#MQTTEvent_SubscriptionSuccessfull
					ForEach MySubscriptions()
						If MySubscriptions()\PacketIdentifier = PacketIdentifier
							i = 1
							ForEach MySubscriptions()\Topics()
								If Val(StringField(Payload, i, ",")) <> $80
									LogIT("Subscribed successfully to '" + MySubscriptions()\Topics()\Topic + "'")
								Else
									LogIT("Unable to subscribe to '" + MySubscriptions()\Topics()\Topic + "'!")
								EndIf
								i + 1
							Next
							ClearList(MySubscriptions()\Topics())
							DeleteElement(MySubscriptions())
						EndIf
					Next
				Case MQTT_Common::#MQTTEvent_PublishIncoming
					LogIT("Published Topic: '" + Topic + "'->" + Payload + " (Q=" + Str(Qos) + ", D=" + Str(DUP) + ", R=" + Str(Retain) + ", M=" + Str(PacketIdentifier) + ")")
				Case MQTT_Common::#MQTTEvent_Error
					LogIT(ErrorText)
			EndSelect
		EndIf
	EndProcedure
	
	
	Procedure main()
		Protected ClientConf.MQTT_CLIENT::CLIENT_INIT
		
		OpenWindow(#Window, 0, 0, 800, 400, "MQTT Client", #PB_Window_SystemMenu | #PB_Window_SizeGadget | #PB_Window_ScreenCentered)
		EditorGadget(#Editor, 5, 5, 790, 360, #PB_Editor_ReadOnly)
		BindEvent(#MyEvent, @MQTT_EventIncoming(), #Window)
		
		;Config Client
		ClientConf\BrokerURL        = "127.0.0.1"
		ClientConf\Username         = "aaa"
		ClientConf\Password         = "bbb"
		ClientConf\ClientIdentifier = "PBClient_{" + RSet(Str(Random(999999, 101)), 6, "0") + "}"
		ClientConf\Window           = #Window
		ClientConf\WindowEvent      = #MyEvent
		ClientConf\Will\Topic       = "Booom/We/are/Dead"
		ClientConf\Will\Message     = "disconnected!"
		
		
		ClientID = MQTT_CLIENT::InitClient(@ClientConf, #True)
		If ClientID
			Repeat : Until WaitWindowEvent() = #PB_Event_CloseWindow
			MQTT_CLIENT::DeInitClient(ClientID)
		EndIf
	EndProcedure
	
	main()
CompilerEndIf



; IDE Options = PureBasic 6.00 LTS (Windows - x64)
; CursorPosition = 1103
; FirstLine = 106
; Folding = HkR0LA-
; EnableThread
; EnableXP
; EnableUser
; Executable = MQTT_Client.exe
; CompileSourceDirectory
; EnableCompileCount = 198
; EnableBuildCount = 4
; EnableExeConstant