;/---------------------------
;|
;| MQTT Broker Framework
;|      V1.09
;|      20.10.2022
;|
;|      Added MQTTInfo Messages to signal, when thread has been started and when it has been ended
;|
;| Supports MQTT <= 3.1.1
;| http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html
;|
;| (c)HeX0R
;|
;| Had been done because of boredom within three days.
;| I saw too late, that there is MQTT V5.00 already.
;| => http://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html
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

DeclareModule MQTT_BROKER
	UseMD5Fingerprint()
	Enumeration AccessFlags
		#AccessFlag_NoEncryption   ;passwords in clean text
		#AccessFlag_PasswordMD5		 ;passwords are md5 hashes
		#AccessFlag_BothMD5        ;passwords and username are md5 hashes
		                           ;add more if needed to IsClientAllowed()
	EndEnumeration
	
	Structure SERVER_ACCESS
		Username.s
		Password.s
		Flag.b
	EndStructure
	
	Structure LAST_WILL_SERVER
		Flag.b
		Topic.s
		MessageBase64.s           ;The WillMessage will be stored as base64, no matter if #Use_Base64_PayLoad is #True or #False
		                          ;                I don't expect very huge WillMessages...
		QoS.b
		Retain.b
	EndStructure
	
	Structure SERVER_INIT
		Port.i                      ;if empty, 1883 will be used
		BindIP.s								    ;if empty, broker will listen on ALL ips
		ClientTimeOUT.i					    ;if empty, new clients will timeout after 5s
		LogWindow.i							    ;Window, where broker will send log data to
		LogWindowEvent.i				    ;WindowEvent, broker will use, when sending data to LogWindow
														    ;if both LogWindow and LogWindowEvent are empty, no messages will be send to anywhere
		LogFile.s								    ;Broker will use that to log data into a file, or leave empty to log nothing
		PersistantStoragePath.s	    ;Broker will store persistant data (when it went offline) in this path, or nowhere if nothing provided
		InitialBufferSize.i					;if empty, initial buffer size will be 65536Bytes, the buffer will increase anyway, if packets are bigger
		List Access.SERVER_ACCESS()	;a list which contains allowed usernames/passwords.
																;See SERVER_ACCESS structure and AccessFlags above.
		                            ;Keep the list empty to allow anonymous connections
	EndStructure
	
	;Public procedures
	Declare InitServer(*Config.SERVER_INIT, RunServerAlso.i = #False)
	;                              use that to initialize the server first
	;                              set the SERVER_INIT structure to fit your needs.
	;                              When RunServerAlso is #True, it will also start-up the server (no need to call StartServer())
	Declare StartServer()         ;Broker will be started with that command
	Declare StopServer()          ;Broker will be stopped with that command
	Declare DeInitServer()        ;all resources of the server will be removed, it will also stop the server (in case it is still running)
	Declare PublishViaServer(Topic.s, *Payload, PayLoadLength.i, QoS = 0, PayLoadType = MQTT_Common::#PayloadType_UnicodeString)
	;                             for testing purposes only!
	;                             a broker usually doesn't publish on its own, only clients publish topics
	Declare ClearServerData()     ;will remove resources, which had been stored for persistant sessions
	;                              can be only called, when server is deinitialized.
	Declare GetLastError()        ;get the last occured error, see Errors Enumeration in MQTT_Common.pbi
	
EndDeclareModule

Module MQTT_BROKER
	EnableExplicit
	
	UseModule MQTT_Common
	
	;{ internal Structures
	Structure connPACKETs Extends PACKET
		;                          connPACKETS are only valid while a new client connected, until it had authenticated itself correctly.
		;
		Level.b                   ;revision level of the protocol, this module here can work with level <= 4, where 4 equals V3.1.1 of the protocol
		ConnFlags.a								;The Connect Flags byte contains a number of parameters specifying the behavior of the MQTT connection.
															;                It also indicates the presence or absence of fields in the payload.
		CleanSession.b						;This byte specifies the handling of the Session state.
															;                the Client and Server can store Session state to enable reliable messaging to continue across a sequence of Network Connections.
															;                This bit is used to control the lifetime of the Session state.
		KeepAlive.l								;The Keep Alive is a time interval measured in seconds.
															;                Expressed as a 16-bit word, it is the maximum time interval that is permitted to elapse between
															;                the point at which the Client finishes transmitting one Control Packet and the point it starts sending the next.
															;                It is the responsibility of the Client to ensure that the interval between 
															;                control packets being sent does not exceed the Keep Alive value.
		ClientIdentifier.s				;The Client Identifier (ClientId) identifies the Client to the Server.
															;                It is part of the payload
		Will.LAST_WILL_SERVER 		;Willxxx... WTF?? => https://www.hivemq.com/blog/mqtt-essentials-part-9-last-will-and-testament/
		Password.s                ;Password transmitted by Client to make sure it can connect to our server
		Username.s								;Username transmitted by Client to make sure it can connect to our server
		ClientID.i								;That's the OS network ID
		TimeOUT.i									;Make sure, there are no corpses of connPACKETs
		*Buffer										;Memory Buffer
		BufferPos.i								;Memory Buffer current position
	EndStructure
	
	Structure Publishes
		Topic.s
		*Payload
		PayLoadLength.i
		PayLoadBase64.s
		Qos.b
	EndStructure
	
	Structure Session          ;one Session equals one MQTT client
		ClientID.i
		ClientIdentifier.s
		Level.b                  ;<- might be useful in future, if we decide to support MQTT version 5 also
		*Buffer
		BufferPos.i
		Persistant.b
		Will.LAST_WILL_SERVER
		KeepAlive.l
		ConnFlags.a
		LastActivity.q
		SessionState.b
		Map PacketIdentifiersUsed.b()
		List Subscriptions.Filter()
		List Packets.PACKET()
	EndStructure
	
	Structure _SERVER_
		InitNetwork.i
		IsInit.i
		LogWindow.i
		LogWindowEvent.i
		LogFile.s
		Mutex.i
		Semaphore.i
		PersistantStoragePath.s
		ThreadID.i
		StopIt.i
		Port.i
		BindIP.s
		Error.i
		TimeOUT.i
		InitBufferSize.i
		List PublishToClient.Publishes()  ;<- with those messages you can publish topics from the server to clients
																			;                not really part of MQTT, it is used for testing purposes
		List Sessions.Session()						;<- those are the established sessions from clients
		List RetainedMessages.Publishes()	;<- store Published messages, which should retain
		List Access.SERVER_ACCESS()				;<- keeps userdata and passwords which are allowed to connect
	EndStructure
	;}
	
	Global SERVER._SERVER_
	
	Procedure LogServerAction(ClientIdentifier.s, Type.i, Topic.s = "", *Payload = 0, PayLoadLength = 0, ErrorText.s = "", Error = 0, QoS = 0, Retain = 0, DUP = 0, PacketIdentifier = 0)
		Protected *send.MQTT_EVENTDATA, Size, Strings.s, FID, i, Payload.s, *Buffer
		
		;Will log incoming/outgoing events
		;Can send messages to a window, and/or into a log file
		
		If IsWindow(SERVER\LogWindow) And SERVER\LogWindowEvent
			Strings                = ClientIdentifier + #ESC$ + Topic + #ESC$ + ErrorText
			Size                   = SizeOf(MQTT_EVENTDATA) + StringByteLength(Strings, #PB_UTF8) + 1
			*send                  = AllocateMemory(Size)
			*send\Type             = Type
			*send\Error            = Error
			*send\QoS              = QoS
			*send\Retain           = Retain
			*send\DUP              = DUP
			*send\PacketIdentifier = PacketIdentifier
			*send\PayLoadLength    = PayLoadLength
			If PayLoadLength
				*send\PayLoad = AllocateMemory(PayLoadLength)
				CompilerIf #USE_BASE64_PAYLOAD
					Base64Decoder(PeekS(*Payload), *send\PayLoad, PayLoadLength)
				CompilerElse
					CopyMemory(*Payload, *send\PayLoad, *send\PayLoadLength)
				CompilerEndIf
			EndIf
			PokeS(*send + OffsetOf(MQTT_EVENTDATA\D), Strings, -1, #PB_UTF8)
			PostEvent(SERVER\LogWindowEvent, SERVER\LogWindow, 0, 0, *send)
		EndIf
		If SERVER\LogFile
			FID = OpenFile(#PB_Any, SERVER\LogFile, #PB_UTF8)
			If FID
				FileSeek(FID, Lof(FID))
				Select Type
					Case #MQTTEvent_ClientConnected
						Strings = "Client [" + ClientIdentifier + "] connected!"
					Case #MQTTEvent_ClientDisconnected
						Strings = "Client [" + ClientIdentifier + "] disconnected!"
					Case #MQTTEvent_InfoPublished
						If PayLoadLength
							CompilerIf #USE_BASE64_PAYLOAD
								*Buffer = AllocateMemory(PayLoadLength)
								If *Buffer
									i       = Base64Decoder(PeekS(*Payload), *Buffer, MemorySize(*Buffer))
									Payload = PeekS(*Buffer, i, #PB_UTF8 | #PB_ByteLength)
									FreeMemory(*Buffer)
								EndIf
							CompilerElse
								Payload = PeekS(*Payload, PayLoadLength, #PB_UTF8 | #PB_ByteLength)
							CompilerEndIf
						EndIf
						Strings = "Client [" + ClientIdentifier + "] Published Topic: " + Topic + ", Payload: " + Payload
					Case #MQTTEvent_Subscription
						Strings = "Client [" + ClientIdentifier + "] Subscribed to Topic: " + Topic
					Case #MQTTEvent_Error
						Strings = "[ERROR!] " + ErrorText
					Case #MQTTEvent_Info
						Strings = "[INFO!] " + ErrorText
				EndSelect
				WriteStringN(FID, FormatDate("[%dd.%mm %hh:%ii:%ss] ", Date()) + Strings)
				CloseFile(FID)
			EndIf
		EndIf
		
	EndProcedure
	
	Procedure SetGetUniqePacketIdentifiers(Set = -1, Remove = #False)
		Protected Result.u, i, Size
		
		;Multi procedure for unique packet identifiers.
		;If Set > -1 it will add a packet identifier (sent by client)
		;If Set > -1 AND Remove = #true, it will remove an identifier
		;If Set = -1 and Remove = #false, it will return the next unused packet identifier (and store it in the map)
		;packet identifiers have to be unique between broker and client, but NOT accross all clients!
		
		With SERVER\Sessions()
			If Set <> -1
				If Remove
					DeleteMapElement(\PacketIdentifiersUsed(), Str(Set))
				Else
					\PacketIdentifiersUsed(Str(Set)) = #True
				EndIf
			Else
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
	
	Procedure ConnPacketDataReceived(*Stream.HEADER, Size, *P.connPACKETs)
		Protected RemainingLength, CurrPos, L, Length
		
		;When a MQTT client connected to a broker, the broker doesn't respond yet.
		;It waits for the CONNECT packet from the client, which will contain username/password for authentication and some other flags
		;This procedure will do nothing but handle those CONNECT packets.
		
		*P\Flags        = *Stream\PacketType & $F
		*P\Type         = (*Stream\PacketType & $F0) >> 4
		*P\PacketState  = #PacketState_Incoming
		Length          = GetPacketSize(*Stream, Size, @CurrPos)
		If Length = -1
			ProcedureReturn 0
		EndIf
		RemainingLength = Length
		
		If *P\Type <> #CONNECT
			;New clients MUST send a connect packet first, and NOTHING else
			ProcedureReturn 0
		EndIf
		
		If *P\Flags <> 0 Or *Stream\bytes[CurrPos + 1] <> 4 Or *Stream\bytes[CurrPos + 2] <> Asc("M") Or *Stream\bytes[CurrPos + 3] <> Asc("Q") Or
		   *Stream\bytes[CurrPos + 4] <> Asc("T") Or *Stream\bytes[CurrPos + 5] <> Asc("T")
			ProcedureReturn 0
		EndIf
		*P\Level        = *Stream\bytes[CurrPos + 6]
		*P\ConnFlags    = *Stream\bytes[CurrPos + 7]
		*P\CleanSession = (*P\ConnFlags & $02) >> 1
		*P\Will\Flag    = (*P\ConnFlags & $04) >> 2
		
		If *P\Will\Flag
			If *P\ConnFlags & $18
				;Will QoS
				*P\Will\QoS = *P\ConnFlags >> 3
				*P\Will\QoS = *P\Will\QoS & $03
			EndIf
			If *P\ConnFlags & $20
				;Will Retain
				*P\Will\Retain = *P\ConnFlags >> 5
				*P\Will\Retain = *P\Will\Retain & $01
			EndIf
		EndIf
		CurrPos + 8
		RemainingLength - 8
		*P\KeepAlive = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
		RemainingLength - 2
		CurrPos + 2
		L = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
		CurrPos + 2
		RemainingLength - 2
		*P\ClientIdentifier = PeekS(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L, #PB_UTF8 | #PB_ByteLength)
		CurrPos + L
		RemainingLength - L
		If *P\ConnFlags & $04
			;Will Flag
			L = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
			CurrPos + 2
			RemainingLength - 2
			*P\Will\Topic = PeekS(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L, #PB_UTF8 | #PB_ByteLength)
			CurrPos + L
			RemainingLength - L
			L = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
			CurrPos + 2
			RemainingLength - 2
			If L > 0
				*P\Will\MessageBase64 = Base64Encoder(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L)
				CurrPos + L
				RemainingLength - L
			EndIf
		EndIf
		If *P\ConnFlags & $80
			;Username
			L = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
			CurrPos + 2
			RemainingLength - 2
			If L > 0
				*P\Username = PeekS(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L, #PB_UTF8 | #PB_ByteLength)
				CurrPos + L
				RemainingLength - L
			EndIf
		EndIf
		If *P\ConnFlags & $40
			;Password
			L = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
			CurrPos + 2
			RemainingLength - 2
			If L > 0
				*P\Password = PeekS(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L, #PB_UTF8 | #PB_ByteLength)
				CurrPos + L
				RemainingLength - L
			EndIf
		EndIf
		
		ProcedureReturn #True
	EndProcedure
	
	Procedure PacketDataReceived(*Stream.HEADER, Size, *P.PACKET)
		Protected RemainingLength, CurrPos, L, Topic.s, Length
		
		;This procedure will pre-handle all other packets, which have been received from a client
		
		*P\Flags        = *Stream\PacketType & $F
		*P\Type         = (*Stream\PacketType & $F0) >> 4
		*P\PacketState  = #PacketState_Incoming
		Length          = GetPacketSize(*Stream, Size, @CurrPos)
		If Length = -1
			ProcedureReturn 0
		EndIf
		*P\PayLoad\PayLoadBase64 = ""
		
		
		
		RemainingLength = Length
		ClearList(*P\tmpSubsc())
		
		Select *P\Type
			Case #PINGREQ, #DISCONNECT
				;nothing to do
				
			Case #PUBACK, #PUBREC, #PUBREL, #PUBCOMP, #SUBSCRIBE, #SUBACK, #UNSUBSCRIBE, #UNSUBACK
				*P\PacketIdentifier = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
				CurrPos + 2
				RemainingLength - 2
				Select *P\Type
					Case #SUBSCRIBE
						While RemainingLength > 0
							AddElement(*P\tmpSubsc())
							*P\tmpSubsc()\Add = 1
							L                 = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
							CurrPos + 2
							RemainingLength - 2
							*P\tmpSubsc()\Topic = PeekS(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L, #PB_UTF8 | #PB_ByteLength)
							CurrPos + L
							RemainingLength - L
							*P\tmpSubsc()\QoS = *Stream\bytes[CurrPos] & $03
							CurrPos + 1
							RemainingLength - 1
						Wend
					Case #UNSUBSCRIBE
						While RemainingLength > 0
							L = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
							CurrPos + 2
							RemainingLength - 2
							Topic = PeekS(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L, #PB_UTF8 | #PB_ByteLength)
							CurrPos + L
							RemainingLength - L
							AddElement(*P\tmpSubsc())
							*P\tmpSubsc()\Topic = Topic
						Wend
				EndSelect
			Case #PUBLISH
				*P\Retain = *P\Flags & $01
				*P\QoS    = (*P\Flags & $06) >> 1
				*P\DUP    = (*P\Flags & $08) >> 3
				L         = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
				CurrPos + 2
				RemainingLength - 2
				*P\TopicName = PeekS(*Stream + OffsetOf(HEADER\bytes) + CurrPos, L, #PB_UTF8 | #PB_ByteLength)
				CurrPos + L
				RemainingLength - L
				If *P\QoS = 1 Or *P\QoS = 2
					*P\PacketIdentifier = *Stream\bytes[CurrPos + 1] + (*Stream\bytes[CurrPos] * 256)
					CurrPos + 2
					RemainingLength - 2
				EndIf
				If RemainingLength > 0
					CompilerIf #USE_BASE64_PAYLOAD
						*P\PayLoad\BufferLengh   = RemainingLength
						*P\PayLoad\PayLoadBase64 = Base64Encoder(*Stream + OffsetOf(HEADER\bytes) + CurrPos, RemainingLength)
					CompilerElse
						*P\PayLoad\BufferLengh = RemainingLength
						*P\PayLoad\Buffer      = AllocateMemory(RemainingLength)
						CopyMemory(*Stream + OffsetOf(HEADER\bytes) + CurrPos, *P\PayLoad\Buffer, RemainingLength)
					CompilerEndIf
				EndIf
				
			Default
				;no other types should be possible here
				ProcedureReturn 0
		EndSelect
		
		ProcedureReturn #True
	EndProcedure
	
	Procedure Send_Connack(CID, SP, ReturnCode)
		Protected CP.MINI_PACKET
		
		;simple procedure to send a CONNACK (connection acknoledged) back to the clients
		
		CP\a[0] = #CONNACK << 4
		CP\a[1] = 2
		CP\a[2] = SP
		CP\a[3] = ReturnCode
		SendNetworkData(CID, @CP, 4)
	EndProcedure
	
	Procedure PublishViaServer(Topic.s, *Payload, PayLoadLength.i, QoS = 0, PayLoadType = #PayloadType_UnicodeString)
		Protected *Buff, s.s
		
		;for debugging purposes only
		
		LockMutex(SERVER\Mutex)
		LastElement(SERVER\PublishToClient())
		AddElement(SERVER\PublishToClient())
		SERVER\PublishToClient()\Topic = Topic
		If PayLoadLength
			CompilerIf #USE_BASE64_PAYLOAD
				Select PayLoadType
					Case #PayloadType_base64
						SERVER\PublishToClient()\PayLoadBase64 = PeekS(*Payload)
						SERVER\PublishToClient()\PayLoadLength = PayLoadLength
					Case #PayloadType_UnicodeString
						;MQTT uses UTF8 strings!
						s       = PeekS(*Payload, PayLoadLength)
						*Buff   = UTF8(s)
						SERVER\PublishToClient()\PayLoadBase64 = Base64Encoder(*Buff, MemorySize(*Buff))
						SERVER\PublishToClient()\PayLoadLength = StringByteLength(s, #PB_UTF8)
						FreeMemory(*Buff)
					Case #PayloadType_UTF8String
						SERVER\PublishToClient()\PayLoadLength = MemoryStringLength(*Payload, #PB_UTF8)
						SERVER\PublishToClient()\PayLoadBase64 = Base64Encoder(*Payload, SERVER\PublishToClient()\PayLoadLength)
					Case #PayloadType_Buffer
						SERVER\PublishToClient()\PayLoadLength = PayLoadLength
						SERVER\PublishToClient()\PayLoadBase64 = Base64Encoder(*Payload, PayLoadLength)
				EndSelect
			CompilerElse
				Select PayLoadType
					Case #PayloadType_base64
						SERVER\PublishToClient()\Payload       = AllocateMemory(PayLoadLength)
						SERVER\PublishToClient()\PayLoadLength = Base64Decoder(PeekS(*Payload), SERVER\PublishToClient()\Payload, PayLoadLength)
					Case #PayloadType_UnicodeString
						;MQTT uses UTF8 strings!
						s       = PeekS(*Payload, PayLoadLength)
						*Buff   = UTF8(s)
						SERVER\PublishToClient()\PayLoad       = *Buff
						SERVER\PublishToClient()\PayLoadLength = StringByteLength(s, #PB_UTF8)
					Case #PayloadType_UTF8String
						SERVER\PublishToClient()\PayLoadLength = MemoryStringLength(*Payload, #PB_UTF8)
						SERVER\PublishToClient()\PayLoad       = AllocateMemory(SERVER\PublishToClient()\PayLoadLength)
						CopyMemory(*Payload, SERVER\PublishToClient()\PayLoad, SERVER\PublishToClient()\PayLoadLength)
					Case #PayloadType_Buffer
						SERVER\PublishToClient()\PayLoadLength = PayLoadLength
						SERVER\PublishToClient()\PayLoad       = AllocateMemory(PayLoadLength)
						CopyMemory(*Payload, SERVER\PublishToClient()\PayLoad, PayLoadLength)
				EndSelect
			CompilerEndIf
			SERVER\PublishToClient()\QoS           = QoS
		EndIf
		SignalSemaphore(SERVER\Semaphore)
		UnlockMutex(SERVER\Mutex)
	EndProcedure
	
	Procedure TopicMatches(Topic.s, FilterContainingWildcards.s)
		Protected *T.CHARACTER, *F.CHARACTER, Result
		
		;procedure to check if a subscription matches a published topic
		;it includes the MQTT wildcard handling
		
		If FilterContainingWildcards = "" Or Topic = ""
			ProcedureReturn 0
		EndIf
		
		*T     = @Topic
		*F     = @FilterContainingWildcards
		While *T\c <> 0
			If *F\c = '#'
				Result = #True
				Break
			ElseIf *F\c = '+'
				;skip Topic word
				*F + SizeOf(CHARACTER)
				While *T\c <> '/' And *T\c <> 0
					*T + SizeOf(CHARACTER)
				Wend
				If *T\c = 0
					Break
				EndIf
			EndIf
			If *F\c <> *T\c
				Break
			EndIf
			*F + SizeOf(CHARACTER)
			*T + SizeOf(CHARACTER)
		Wend
		
		If Result = #False And *F\c = *T\c
			Result = #True
		EndIf
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure ClearPacket()
		
		;make sure the packet is cleared gracefully
		
		CompilerIf #USE_BASE64_PAYLOAD = #False
			If SERVER\Sessions()\Packets()\PayLoad\Buffer
				FreeMemory(SERVER\Sessions()\Packets()\PayLoad\Buffer)
			EndIf
		CompilerEndIf
		If SERVER\Sessions()\Packets()\NYS\Buffer
			FreeMemory(SERVER\Sessions()\Packets()\NYS\Buffer)
		EndIf
		DeleteElement(SERVER\Sessions()\Packets())
	EndProcedure
	
	Procedure AddSessionPacket(Type, Topic.s = "", *Payload = 0, PayloadLength = 0, QoS = 0, PacketIdentifier = 0, DUP = 0, Retain = 0)
		;simple procedure to add an outgoing packet to the queue
		
		PushListPosition(SERVER\Sessions()\Packets())
		LastElement(SERVER\Sessions()\Packets())
		AddElement(SERVER\Sessions()\Packets())
		With SERVER\Sessions()\Packets()
			\PacketState      = #PacketState_OutgoingNotSendYet
			\Type             = Type
			\PacketIdentifier = PacketIdentifier
			\DUP              = DUP
			\TopicName        = Topic
			If PayloadLength > 0
				CompilerIf #USE_BASE64_PAYLOAD
					\PayLoad\BufferLengh   = PayloadLength
					\PayLoad\PayLoadBase64 = PeekS(*Payload)
				CompilerElse
					\PayLoad\Buffer      = AllocateMemory(PayloadLength)
					\PayLoad\BufferLengh = PayloadLength
					CopyMemory(*Payload, \PayLoad\Buffer, PayloadLength)
				CompilerEndIf
			EndIf
			\QoS              = QoS
			\Retain           = Retain
		EndWith
		PopListPosition(SERVER\Sessions()\Packets())
		
	EndProcedure
	
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
	
	Procedure SendCommand()
		Protected CP.MINI_PACKET, Result
		Protected *Buff.HEADER, *Buff2, CurrPos, i, Size, *Payload
		Protected Length, LengthShould, LengthIs
		
		;main sending procedure
		;will make sure, that packets get send completely
		
		If SERVER\Sessions()\SessionState = #SessionState_inactive
			If SERVER\Sessions()\Packets()\QoS = 0
				;run out of luck
				ClearPacket()
			EndIf
		Else
			With SERVER\Sessions()\Packets()
				If \NYS\Buffer
					*Buff        = \NYS\Buffer
					\NYS\Buffer  = 0
					LengthIs     = SendNetworkData(SERVER\Sessions()\ClientID, *Buff, \NYS\BufferLength)
					LengthShould = \NYS\BufferLength
				Else
					Select \Type
						Case #PINGRESP
							CP\a[0]      = #PINGRESP << 4
							CP\a[1]      = 0
							LengthIs     = SendNetworkData(SERVER\Sessions()\ClientID, @CP, 2)
							LengthShould = 2
						Case #SUBACK
							If \PayLoad\BufferLengh
								CompilerIf #USE_BASE64_PAYLOAD
									*Buff2 = AllocateMemory(StringByteLength(\PayLoad\PayLoadBase64))
									If *Buff2
										Size   = Base64Decoder(\PayLoad\PayLoadBase64, *Buff2, 1024)
										*Buff  = AllocateMemory(6 + Size)
										If *Buff
											*Buff\PacketType         = #SUBACK << 4
											CurrPos                  = SetStreamLength(*Buff, 2 + Size)
											*Buff\bytes[CurrPos + 1] = \PacketIdentifier & $FF
											*Buff\bytes[CurrPos]     = (\PacketIdentifier & $FF00) >> 8
											CurrPos + 2
											i = CurrPos
											CopyMemory(*Buff2, *Buff + OffsetOf(HEADER\bytes) + CurrPos, Size)
											LengthIs     = SendNetworkData(SERVER\Sessions()\ClientID, *Buff, 1 + CurrPos + Size)
											LengthShould = 1 + CurrPos + Size
											;FreeMemory(*Buff)
										EndIf
										FreeMemory(*Buff2)
									EndIf
								CompilerElse
									Size   = \PayLoad\BufferLengh
									*Buff  = AllocateMemory(6 + Size)
									If *Buff
										*Buff\PacketType         = #SUBACK << 4
										CurrPos                  = SetStreamLength(*Buff, 2 + Size)
										*Buff\bytes[CurrPos + 1] = \PacketIdentifier & $FF
										*Buff\bytes[CurrPos]     = (\PacketIdentifier & $FF00) >> 8
										CurrPos + 2
										i = CurrPos
										CopyMemory(\PayLoad\Buffer, *Buff + OffsetOf(HEADER\bytes) + CurrPos, Size)
										LengthIs     = SendNetworkData(SERVER\Sessions()\ClientID, *Buff, 1 + CurrPos + Size)
										LengthShould = 1 + CurrPos + Size
										; 									FreeMemory(*Buff)
									EndIf
								CompilerEndIf
							EndIf
						Case #PUBACK, #PUBREC, #PUBCOMP, #UNSUBACK
							CP\a[0]      = \Type << 4
							CP\a[1]      = 2
							CP\a[2]      = (\PacketIdentifier & $FF00) >> 8
							CP\a[3]      = \PacketIdentifier & $FF
							LengthIs     = SendNetworkData(SERVER\Sessions()\ClientID, @CP, 4)
							LengthShould = 4
						Case #PUBREL
							CP\a[0]      = 2 | (\Type << 4)
							CP\a[1]      = 2
							CP\a[2]      = (\PacketIdentifier & $FF00) >> 8
							CP\a[3]      = \PacketIdentifier & $FF
							LengthIs     = SendNetworkData(SERVER\Sessions()\ClientID, @CP, 4)
							LengthShould = 4
						Case #PUBLISH
							If \PayLoad\BufferLengh > 0
								i = \PayLoad\BufferLengh
							EndIf
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
								PokeS(*Buff + OffsetOf(HEADER\bytes) + CurrPos, \TopicName, -1, #PB_UTF8)
								CurrPos + StringByteLength(\TopicName, #PB_UTF8)
								If \Qos > 0
									*Buff\bytes[CurrPos + 1] = \PacketIdentifier & $FF
									*Buff\bytes[CurrPos]     = (\PacketIdentifier & $FF00) >> 8
									CurrPos + 2
								EndIf
								CompilerIf #USE_BASE64_PAYLOAD
									Base64Decoder(\PayLoad\PayLoadBase64, *Buff + OffsetOf(HEADER\bytes) + CurrPos, i)
								CompilerElse
									CopyMemory(\PayLoad\Buffer, *Buff + OffsetOf(HEADER\bytes) + CurrPos, i)
								CompilerEndIf
								CurrPos + i
								LengthIs     = SendNetworkData(SERVER\Sessions()\ClientID, *Buff, CurrPos + 1)
								LengthShould = CurrPos + 1
								; 							FreeMemory(*Buff)
							EndIf
					EndSelect
				EndIf
				If LengthIs = LengthShould
					If LengthIs > 4
						FreeMemory(*Buff)
					EndIf
					\NYS\Buffer = 0
					Result      = #SendFinished
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
					If LengthShould <= 4
						CopyMemory(@CP + LengthIs, \NYS\Buffer, \NYS\BufferLength)
					Else
						CopyMemory(*Buff + LengthIs, \NYS\Buffer, \NYS\BufferLength)
						FreeMemory(*Buff)
					EndIf
					Result = #SendNotFinished
				EndIf
			EndWith
		EndIf
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure CheckSessionPackets()
		Protected *Session, *P.PACKET, Topic.s, *Payload, PayLoadLength, QoS, QoSR, Identifier, Found, a$
		Protected *Buffer, i, l, OwnIdentifier, DUP, Retain, PayLoadBase64.s, R
		
		;quite huge procedure, where all the packets will be handled.
		
		ForEach SERVER\Sessions()
			If SERVER\Sessions()\SessionState = #SessionState_inactive
				Continue
			EndIf
			ForEach SERVER\Sessions()\Packets()
				With SERVER\Sessions()\Packets()
					If \ReSendAt > 0 And \ReSendAt > ElapsedMilliseconds()
						;not yet
						Continue
					EndIf
					If \WaitForAnswerSince > 0
						If \WaitForAnswerSince > ElapsedMilliseconds() + 2000
							\WaitForAnswerSince = 0
							\PacketState        = #PacketState_OutgoingNotSendYet
						Else
							Continue
						EndIf
					EndIf
					
					Select \Type
						Case #SUBACK
							If \PacketState = #PacketState_OutgoingNotSendYet
								R = SendCommand()
								Select R
									Case #SendFinished, #SendFailed
										*P = @SERVER\Sessions()\Packets()
										ForEach SERVER\Sessions()\Packets()
											If @SERVER\Sessions()\Packets() <> *P And \PacketIdentifier = *P\PacketIdentifier
												ClearPacket()
											EndIf
										Next
										ChangeCurrentElement(SERVER\Sessions()\Packets(), *P)
										SetGetUniqePacketIdentifiers(\PacketIdentifier, #True)
										ClearPacket()
										If R = #SendFailed
											Break
										EndIf
									Case #SendNotFinished
										\ReSendAt = ElapsedMilliseconds() + 2000
										Break
								EndSelect
							EndIf
						Case #UNSUBACK
							If \PacketState = #PacketState_OutgoingNotSendYet
								R = SendCommand()
								Select R
									Case #SendFinished, #SendFailed
										*P = @SERVER\Sessions()\Packets()
										ForEach SERVER\Sessions()\Packets()
											If @SERVER\Sessions()\Packets() <> *P And \PacketIdentifier = *P\PacketIdentifier
												ClearPacket()
											EndIf
										Next
										ChangeCurrentElement(SERVER\Sessions()\Packets(), *P)
										SetGetUniqePacketIdentifiers(\PacketIdentifier, #True)
										ClearPacket()
										If R = #SendFailed
											Break
										EndIf
									Case #SendNotFinished
										\ReSendAt = ElapsedMilliseconds() + 2000
										Break
								EndSelect
								
							EndIf
						Case #PINGREQ
							If SERVER\Sessions()\SessionState = #SessionState_active And \PacketState = #PacketState_Incoming
								\Type        = #PINGRESP
								\PacketState = #PacketState_OutgoingNotSendYet
								SendCommand()
								ClearPacket()
							EndIf
							
						Case #PUBLISH
							Select \PacketState
								Case #PacketState_Incoming
									Topic         = \TopicName
									*Payload      = \PayLoad\Buffer
									PayLoadLength = \PayLoad\BufferLengh
									QoS           = \QoS
									DUP           = \DUP
									Retain        = \Retain
									Identifier    = \PacketIdentifier
									CompilerIf #USE_BASE64_PAYLOAD
										PayLoadBase64 = \PayLoad\PayLoadBase64
										*Payload      = @PayLoadBase64
									CompilerEndIf
									If QoS > 0
										SetGetUniqePacketIdentifiers(Identifier)
									EndIf
									If \Retain
										;find and delete existing message
										ForEach SERVER\RetainedMessages()
											If SERVER\RetainedMessages()\Topic = Topic
												CompilerIf #USE_BASE64_PAYLOAD = #False
													If SERVER\RetainedMessages()\PayLoadLength
														FreeMemory(SERVER\RetainedMessages()\Payload)
													EndIf
												CompilerEndIf
												DeleteElement(SERVER\RetainedMessages())
											EndIf
										Next
										If PayLoadLength > 0
											AddElement(SERVER\RetainedMessages())
											SERVER\RetainedMessages()\Topic         = Topic
											SERVER\RetainedMessages()\Qos           = Qos
											SERVER\RetainedMessages()\PayLoadLength = PayLoadLength
											CompilerIf #USE_BASE64_PAYLOAD
												SERVER\RetainedMessages()\PayLoadBase64 = PayLoadBase64
											CompilerElse
												SERVER\RetainedMessages()\Payload = AllocateMemory(PayLoadLength)
												CopyMemory(*Payload, SERVER\RetainedMessages()\Payload, PayLoadLength)
											CompilerEndIf
										EndIf
									EndIf
									
									If Qos = 0 Or Qos = 1
										;we can publish that directly
										PushListPosition(SERVER\Sessions()\Packets())
										*Session   = @SERVER\Sessions()
										ForEach SERVER\Sessions()
											If @SERVER\Sessions() <> *Session
												ForEach SERVER\Sessions()\Subscriptions()
													If SERVER\Sessions()\SessionState = #SessionState_active Or SERVER\Sessions()\Persistant
														If TopicMatches(Topic, SERVER\Sessions()\Subscriptions()\Topic)
															;send publish command to client
															QoSR = SERVER\Sessions()\Subscriptions()\QoS
															If QoSR > QoS
																QoSR = QoS
															EndIf
															If QoSR > 0
																OwnIdentifier = SetGetUniqePacketIdentifiers()
															EndIf
															AddSessionPacket(#PUBLISH, Topic, *Payload, PayLoadLength, QoSR, OwnIdentifier)
														EndIf
													EndIf
												Next
											EndIf
										Next
										ChangeCurrentElement(SERVER\Sessions(), *Session)
										PopListPosition(SERVER\Sessions()\Packets())
										If QoS = 0
											LogServerAction(SERVER\Sessions()\ClientIdentifier, #MQTTEvent_InfoPublished, Topic, *PayLoad, PayLoadLength, "", 0, QoS, Retain, DUP)
										Else
											AddSessionPacket(#PUBACK, Topic, *Payload, PayLoadLength, QoS, Identifier, 0, 0)
											LogServerAction(SERVER\Sessions()\ClientIdentifier, #MQTTEvent_InfoPublished, Topic, *PayLoad, PayLoadLength, "", 0, QoS, Retain, DUP, Identifier)
										EndIf
										ClearPacket()
									ElseIf QoS = 2
										\PacketState = #PacketState_WaitForAnswer
										;keep the original publish message
										AddSessionPacket(#PUBREC, "", 0, 0, 0, Identifier, 0, 0)
										LogServerAction(SERVER\Sessions()\ClientIdentifier, #MQTTEvent_InfoPublished, Topic, *PayLoad, PayLoadLength, "", 0, QoS, Retain, DUP, Identifier)
									EndIf
								Case #PacketState_OutgoingNotSendYet
									QoS           = \QoS
									Identifier    = \PacketIdentifier
									
									Select QoS
										Case 0
											Select SendCommand()
												Case #SendFinished
													ClearPacket()
												Case #SendFailed
													ClearPacket()
													Break
											EndSelect
											;nothing more to do
										Case 1
											Select SendCommand()
												Case #SendFinished
													;send answer
													\PacketState        = #PacketState_WaitForAnswer
													\WaitForAnswerSince = ElapsedMilliseconds()
													AddSessionPacket(#PUBACK, "", 0, 0, QoS, Identifier)
												Case #SendNotFinished
													;try again next time
													Break
												Case #SendFailed
													ClearPacket()
													Break
											EndSelect
										Case 2
											R = SendCommand()
											Select R
												Case #SendFinished
													\PacketState        = #PacketState_WaitForAnswer
													\WaitForAnswerSince = ElapsedMilliseconds()
													AddSessionPacket(#PUBREC, "", 0, 0, QoS, Identifier)
												Case #SendNotFinished
													\ReSendAt = ElapsedMilliseconds() + 2000
												Case #SendFailed
													ClearPacket()
													Break
											EndSelect
									EndSelect
									
							EndSelect
						Case #PUBACK ;QoS 1 reply
							Select \PacketState
								Case #PacketState_OutgoingNotSendYet
									R = SendCommand()
									Select R
										Case #SendFinished
											SetGetUniqePacketIdentifiers(\PacketIdentifier, #True)
											ClearPacket()
										Case #SendNotFinished
											Break
										Case #SendFailed
											Break
									EndSelect
								Case #PacketState_Incoming
									;finished
									;search for all packets with this identifier and remove them
									*P = @SERVER\Sessions()\Packets()
									ForEach SERVER\Sessions()\Packets()
										If @SERVER\Sessions()\Packets() <> *P And \PacketIdentifier = *P\PacketIdentifier
											ClearPacket()
										EndIf
									Next
									ChangeCurrentElement(SERVER\Sessions()\Packets(), *P)
									SetGetUniqePacketIdentifiers(\PacketIdentifier, #True)
									ClearPacket()
							EndSelect
						Case #PUBREC ;first QoS 2 reply
							Select \PacketState
								Case #PacketState_OutgoingNotSendYet
									Select SendCommand()
										Case #SendFinished
											\PacketState        = #PacketState_WaitForAnswer
											\WaitForAnswerSince = ElapsedMilliseconds()
										Case #SendFailed, #SendNotFinished
											Break
									EndSelect
									
								Case #PacketState_Incoming
									*P = @SERVER\Sessions()\Packets()
									ForEach SERVER\Sessions()\Packets()
										If @SERVER\Sessions()\Packets() <> *P And \PacketIdentifier = *P\PacketIdentifier
											ClearPacket()
										EndIf
									Next
									ChangeCurrentElement(SERVER\Sessions()\Packets(), *P)
									\Type        = #PUBREL
									\PacketState = #PacketState_OutgoingNotSendYet
							EndSelect
							
						Case #PUBREL ;second QoS 2 reply
							If \PacketState = #PacketState_Incoming
								Identifier = \PacketIdentifier
								;search for main publish packet
								ForEach SERVER\Sessions()\Packets()
									If \Type = #PUBLISH And \PacketIdentifier = Identifier And \PacketState = #PacketState_WaitForAnswer
										Topic         = \TopicName
										*Payload      = \PayLoad\Buffer
										PayLoadLength = \PayLoad\BufferLengh
										QoS           = \QoS
										Identifier    = \PacketIdentifier
										DUP           = \DUP
										Retain        = \Retain
										*P            = @SERVER\Sessions()\Packets()
										CompilerIf #USE_BASE64_PAYLOAD
											PayLoadBase64 = \PayLoad\PayLoadBase64
											*Payload      = @PayLoadBase64
										CompilerEndIf
										Break
									EndIf
								Next
								*Session   = @SERVER\Sessions()
								If *P
									;now publish it to subscribers
									ForEach SERVER\Sessions()
										If @SERVER\Sessions() <> *Session
											ForEach SERVER\Sessions()\Subscriptions()
												If SERVER\Sessions()\SessionState = #SessionState_active Or SERVER\Sessions()\Persistant
													If TopicMatches(Topic, SERVER\Sessions()\Subscriptions()\Topic)
														;send publish command to client
														QoSR          = SERVER\Sessions()\Subscriptions()\QoS
														OwnIdentifier = SetGetUniqePacketIdentifiers()
														If QoSR > QoS
															QoSR = QoS
														EndIf
														AddSessionPacket(#PUBLISH, Topic, *Payload, PayLoadLength, QoSR, OwnIdentifier)
													EndIf
												EndIf
											Next
										EndIf
									Next
								EndIf
								ChangeCurrentElement(SERVER\Sessions(), *Session)
								ForEach SERVER\Sessions()\Packets()
									If \PacketIdentifier = Identifier
										ClearPacket()
									EndIf
								Next
								AddSessionPacket(#PUBCOMP, "", 0, 0, QoS, Identifier)
							ElseIf \PacketState = #PacketState_OutgoingNotSendYet
								Select SendCommand()
									Case #SendFinished
										\PacketState = #PacketState_WaitForAnswer
									Case #SendNotFinished
										\ReSendAt = ElapsedMilliseconds() + 2000
										Break
									Case #SendFailed
										Break
								EndSelect
							EndIf
							
						Case #PUBCOMP ;final QoS 2 reply
							Identifier = \PacketIdentifier
							If \PacketState = #PacketState_OutgoingNotSendYet
								R = SendCommand()
							Else
								R = #SendFinished
							EndIf
							If R = #SendFinished
								ForEach SERVER\Sessions()\Packets()
									If \PacketIdentifier = Identifier
										ClearPacket()
									EndIf
								Next
								SetGetUniqePacketIdentifiers(Identifier, #True)
							Else
								Break
							EndIf
							
						Case #DISCONNECT
							;remove all packets
							ForEach SERVER\Sessions()\Packets()
								ClearPacket()
							Next
							ClearList(SERVER\Sessions()\Packets())
							CloseNetworkConnection(SERVER\Sessions()\ClientID)
							SERVER\Sessions()\SessionState = #SessionState_inactive
							SERVER\Sessions()\ClientID     = #Null
							Break
							
						Case #SUBSCRIBE
							;check if allowed, but who will decide that??
							;we allow any description now
							If \PacketState = #PacketState_Incoming
								If ListSize(\tmpSubsc()) > 0
									ForEach \tmpSubsc()
										If \tmpSubsc()\Add
											LogServerAction(SERVER\Sessions()\ClientIdentifier, #MQTTEvent_Subscription, \tmpSubsc()\Topic, 0, 0, "", 0, \tmpSubsc()\QoS)
											;check if subscription is known already
											Found = #False
											ForEach SERVER\Sessions()\Subscriptions()
												If \tmpSubsc()\Topic = SERVER\Sessions()\Subscriptions()\Topic
													;yes, udate QoS only
													SERVER\Sessions()\Subscriptions()\QoS = \tmpSubsc()\QoS
													Found                                 = #True
													Break
												EndIf
											Next
											If Found = 0
												;no, add it
												AddElement(SERVER\Sessions()\Subscriptions())
												SERVER\Sessions()\Subscriptions()\Topic    = \tmpSubsc()\Topic
												SERVER\Sessions()\Subscriptions()\QoS      = \tmpSubsc()\QoS
											EndIf
										EndIf
									Next
									;now send answer
									*Buffer = AllocateMemory(ListSize(\tmpSubsc()))
									i       = 0
									ForEach \tmpSubsc()
										PokeB(*Buffer + i, \tmpSubsc()\QoS)
										i + 1
									Next
									CompilerIf #USE_BASE64_PAYLOAD
										a$ = Base64Encoder(*Buffer, MemorySize(*Buffer))
										AddSessionPacket(#SUBACK, "", @a$, MemorySize(*Buffer), 0, \PacketIdentifier)
									CompilerElse
										AddSessionPacket(#SUBACK, "", *Buffer, MemorySize(*Buffer), 0, \PacketIdentifier)
									CompilerEndIf
									FreeMemory(*Buffer)
									SetGetUniqePacketIdentifiers(\PacketIdentifier)
									;Successfully subscribed, now check for existing retained messages for those subscriptions
									ForEach SERVER\RetainedMessages()
										ForEach \tmpSubsc()
											If TopicMatches(SERVER\RetainedMessages()\Topic, \tmpSubsc()\Topic)
												OwnIdentifier = 0
												If SERVER\RetainedMessages()\QoS > 0
													OwnIdentifier = SetGetUniqePacketIdentifiers()
												EndIf
												CompilerIf #USE_BASE64_PAYLOAD
													*Payload = @SERVER\RetainedMessages()\PayLoadBase64
												CompilerElse
													*Payload = SERVER\RetainedMessages()\Payload
												CompilerEndIf
												LogServerAction(SERVER\Sessions()\ClientIdentifier, #MQTTEvent_InfoPublished, SERVER\RetainedMessages()\Topic, 
												                *Payload, SERVER\RetainedMessages()\PayLoadLength, "", 0, \tmpSubsc()\QoS, 1, 0, OwnIdentifier)
												AddSessionPacket(#PUBLISH, SERVER\RetainedMessages()\Topic, *Payload, SERVER\RetainedMessages()\PayLoadLength, SERVER\RetainedMessages()\Qos, OwnIdentifier, 0, 1)
												Break
											EndIf
										Next
									Next
									ClearList(\tmpSubsc())
								EndIf
							EndIf
							ClearPacket()
						Case #UNSUBSCRIBE
							If \PacketState = #PacketState_Incoming
								Identifier = \PacketIdentifier
								ForEach \tmpSubsc()
									If \tmpSubsc()\Add = 0
										ForEach SERVER\Sessions()\Subscriptions()
											If TopicMatches(\tmpSubsc()\Topic, SERVER\Sessions()\Subscriptions()\Topic)
												DeleteElement(SERVER\Sessions()\Subscriptions())
												Break
											EndIf
										Next
									EndIf
								Next
								SetGetUniqePacketIdentifiers(\PacketIdentifier)
								;now send answer
								ClearList(\tmpSubsc())
								AddSessionPacket(#UNSUBACK, "", 0, 0, 0, Identifier)
							EndIf
							ClearPacket()
						Default
							If \PacketState = #PacketState_OutgoingNotSendYet
								Select SendCommand()
									Case #SendFinished
										SetGetUniqePacketIdentifiers(\PacketIdentifier, #True)
										ClearPacket()
									Default
										Break
								EndSelect
							EndIf
					EndSelect
					
				EndWith
			Next
		Next
	EndProcedure
	
	Procedure IsClientAllowed(Username.s, Password.s)
		Protected Result
		
		;check if the client, who connected and has sent the CONNACK, is accepted
		
		If ListSize(SERVER\Access()) = 0
			;no access data provided, anyone is allowed to connect!
			ProcedureReturn #True
		EndIf
		
		ForEach SERVER\Access()
			Select SERVER\Access()\Flag
				Case #AccessFlag_NoEncryption
					;not hashed
					If SERVER\Access()\Username = Username And SERVER\Access()\Password = Password
						Result = #True
						Break
					EndIf
				Case #AccessFlag_PasswordMD5
					;password is a md5 hash
					If SERVER\Access()\Username = Username 
						If SERVER\Access()\Password = ""
							Result = #True
							Break
						ElseIf Fingerprint(@Password, StringByteLength(Password), #PB_Cipher_MD5) = Fingerprint(@SERVER\Access()\Password, StringByteLength(SERVER\Access()\Password), #PB_Cipher_MD5)
							Result = #True
							Break
						EndIf
					EndIf
				Case #AccessFlag_BothMD5
					If Fingerprint(@Username, StringByteLength(Username), #PB_Cipher_MD5) = Fingerprint(@SERVER\Access()\Username, StringByteLength(SERVER\Access()\Username), #PB_Cipher_MD5)
						If SERVER\Access()\Password = ""
							Result = #True
							Break
						ElseIf Fingerprint(@Password, StringByteLength(Password), #PB_Cipher_MD5) = Fingerprint(@SERVER\Access()\Password, StringByteLength(SERVER\Access()\Password), #PB_Cipher_MD5)
							Result = #True
							Break
						EndIf
					EndIf
			EndSelect
		Next
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure CheckConnPackets(List ConnPackets.ConnPackets())
		Protected Found, Found2
		
		;handle CONNACK packets
		;if anything is fine, that client will be added as a new Sessions() element
		
		With ConnPackets()
			ForEach ConnPackets()
				If ConnPackets()\TimeOUT < ElapsedMilliseconds()
					;timed out
					CloseNetworkConnection(ConnPackets()\ClientID)
					If ConnPackets()\Buffer
						FreeMemory(ConnPackets()\Buffer)
					EndIf
					ClearList(ConnPackets()\tmpSubsc())
					DeleteElement(ConnPackets())
				ElseIf \PacketState = #PacketState_Incoming
					Select \Type
						Case #CONNECT
							If IsClientAllowed(\Username, \Password) = #False
								Send_Connack(\ClientID, 0, #ConnRefused_BadUsernameOrPassword)
								CloseNetworkConnection(\ClientID)
								FreeMemory(\Buffer)
								DeleteElement(ConnPackets())
							ElseIf \Level > 4
								Send_Connack(\ClientID, 0, #ConnRefused_UnacceptableProtocolVersion)
								CloseNetworkConnection(\ClientID)
								FreeMemory(\Buffer)
								DeleteElement(ConnPackets())
							Else
								Found  = 0
								Found2 = 0
								ForEach SERVER\Sessions()
									If SERVER\Sessions()\ClientIdentifier = \ClientIdentifier
										;we have an existing session!
										If \CleanSession = 0
											Found = 1
										Else
											Found2 = 1
											ForEach SERVER\Sessions()\Packets()
												ClearPacket()
											Next
											ClearList(SERVER\Sessions()\Packets())
											ClearList(SERVER\Sessions()\Subscriptions())
										EndIf
										Break
									EndIf
								Next
								
								If Found = 0 And Found2 = 0
									AddElement(SERVER\Sessions())
								EndIf
								SERVER\Sessions()\LastActivity       = ElapsedMilliseconds()
								SERVER\Sessions()\ClientID           = \ClientID
								SERVER\Sessions()\ClientIdentifier   = \ClientIdentifier
								SERVER\Sessions()\SessionState       = #SessionState_active
								SERVER\Sessions()\ConnFlags          = \ConnFlags
								SERVER\Sessions()\KeepAlive          = \KeepAlive
								SERVER\Sessions()\Will\QoS           = \Will\QoS
								SERVER\Sessions()\Will\Flag          = \Will\Flag
								SERVER\Sessions()\Will\MessageBase64 = \Will\MessageBase64
								SERVER\Sessions()\Will\Retain        = \Will\Retain
								SERVER\Sessions()\Will\Topic         = \Will\Topic
								SERVER\Sessions()\Level              = \Level
								SERVER\Sessions()\Persistant         = 1 - \CleanSession
								If SERVER\Sessions()\Buffer = 0
									SERVER\Sessions()\Buffer = AllocateMemory(SERVER\InitBufferSize)
								EndIf
								SERVER\Sessions()\BufferPos = 0
								Send_Connack(SERVER\Sessions()\ClientID, Found2, #ConnAccepted)
								LogServerAction(SERVER\Sessions()\ClientIdentifier, #MQTTEvent_ClientConnected)
								FreeMemory(\Buffer)
								DeleteElement(ConnPackets())
							EndIf
						Default
							CloseNetworkConnection(ConnPackets()\ClientID)
							If ConnPackets()\Buffer
								FreeMemory(ConnPackets()\Buffer)
							EndIf
							ClearList(ConnPackets()\tmpSubsc())
							DeleteElement(ConnPackets())
					EndSelect
				EndIf
			Next
		EndWith
	EndProcedure
	
	Procedure CheckForLastWill()
		Protected *S.Session, Identifier.i, *PayLoad, L, *Buffer
		
		;check if the disconnected client has a last will stored, and if so, publish it to all subscribers
		
		If SERVER\Sessions()\Will\Topic
			*S = SERVER\Sessions()
			If *S\Will\MessageBase64
				*Buffer = AllocateMemory(StringByteLength(*S\Will\MessageBase64))
				L       = Base64Decoder(*S\Will\MessageBase64, *Buffer, MemorySize(*Buffer))
				CompilerIf #USE_BASE64_PAYLOAD
					*PayLoad = @*S\Will\MessageBase64
				CompilerElse
					*PayLoad = *Buffer
				CompilerEndIf
			EndIf
			ForEach SERVER\Sessions()
				If @SERVER\Sessions() <> *S And SERVER\Sessions()\SessionState = #SessionState_active
					ForEach SERVER\Sessions()\Subscriptions()
						If TopicMatches(*S\Will\Topic, SERVER\Sessions()\Subscriptions()\Topic)
							If SERVER\Sessions()\Subscriptions()\QoS > 0
								Identifier = SetGetUniqePacketIdentifiers()
							Else
								Identifier = 0
							EndIf
							AddSessionPacket(#PUBLISH, *S\Will\Topic, *PayLoad, L, SERVER\Sessions()\Subscriptions()\QoS, Identifier, 0, *S\Will\Retain)
						EndIf
					Next
				EndIf
			Next
			ChangeCurrentElement(SERVER\Sessions(), *S)
			If *Buffer
				FreeMemory(*Buffer)
			EndIf
		EndIf
	EndProcedure
	
	Procedure ServerThread(Dummy)
		Protected SID, CID, Found, Pos, *P.PACKET
		Protected i, j, k, l, Add, Length, *S.Session, *Payload
		Protected LogTimer.q, RealLength
		
		;here is all the magic :)
		
		If SERVER\BindIP
			SID = CreateNetworkServer(#PB_Any, SERVER\Port, #PB_Network_TCP, SERVER\BindIP)
		Else
			SID = CreateNetworkServer(#PB_Any, SERVER\Port)
		EndIf
		
		If SID = 0
			SERVER\Error = #Error_CantStartServer
			LogServerAction("BROKER", #MQTTEvent_Error, "", 0, 0, ErrorDescription(SERVER\Error), SERVER\Error)
			ProcedureReturn
		EndIf
		LogServerAction("", #MQTTEvent_Info, "", 0, 0, "Server Thread Started", #Info_ThreadStarted)
		
		NewList ConnPackets.ConnPackets()  ;<- those are packets not (yet) belonging to an established session
																			 ;   we will do the whole connecting-handshake with those
																			 ;   when finished, they will create a new session element
		
		LogTimer = ElapsedMilliseconds()
		
		Repeat
			
			Select NetworkServerEvent(SID)
				Case #PB_NetworkEvent_None
					Delay(20)
				Case #PB_NetworkEvent_Connect
					;ConnPackets are more or less temp. packets
					;As soon as they are logged in it will be integrated into a session
					AddElement(ConnPackets())
					ConnPackets()\ClientID    = EventClient()
					ConnPackets()\TimeOUT     = ElapsedMilliseconds() + SERVER\TimeOUT
					ConnPackets()\Buffer      = AllocateMemory(SERVER\InitBufferSize)
					ConnPackets()\PacketState = #PacketState_WaitForAnswer
				Case #PB_NetworkEvent_Disconnect
					Found = #False
					ForEach ConnPackets()
						If ConnPackets()\ClientID = EventClient()
							LogServerAction(ConnPackets()\ClientIdentifier, #MQTTEvent_ClientDisconnected)
							FreeMemory(ConnPackets()\Buffer)
							DeleteElement(ConnPackets())
							Found = #True
							Break
						EndIf
					Next
					If Found = #False
						ForEach SERVER\Sessions()
							If SERVER\Sessions()\ClientID = EventClient()
								LogServerAction(SERVER\Sessions()\ClientIdentifier, #MQTTEvent_ClientDisconnected)
								CheckForLastWill()
								;error, close connection
								;(removed, seems PB made the eventclient() invalid already)
								;CloseNetworkConnection(SERVER\Sessions()\ClientID)
								If SERVER\Sessions()\Persistant
									SERVER\Sessions()\SessionState = #SessionState_inactive
									SERVER\Sessions()\ClientID     = #Null
								Else
									FreeMemory(SERVER\Sessions()\Buffer)
									ForEach SERVER\Sessions()\Packets()
										ClearPacket()
									Next
									DeleteElement(SERVER\Sessions())
								EndIf
								Break
							EndIf
						Next
					EndIf
				Case #PB_NetworkEvent_Data
					CID   = EventClient()
					Found = 0
					;check not yet authorized clients first
					With ConnPackets()
						
						ForEach ConnPackets()
							If \ClientID = CID
								Found = 1
								Break
							EndIf
						Next
						
						If Found = 1
							;get packet, here we only receive the packet, we will handle it down below outside of the network loop
							Pos = ReceiveNetworkData(CID, \Buffer, MemorySize(\Buffer) - \BufferPos)
							If Pos > -1
								\BufferPos + Pos
								;dynamically increase buffer if needed
								If \BufferPos > MemorySize(\Buffer) - 256
									\Buffer = ReAllocateMemory(\Buffer, MemorySize(\Buffer) + SERVER\InitBufferSize)
								EndIf
								
								Repeat
									Length = GetPacketSize(\Buffer, \BufferPos, @Add)
									If Length = -1
										
									Else
										If ConnPacketDataReceived(\Buffer, \BufferPos, @ConnPackets())
											If \BufferPos - Length - 1 - Add < 0
												;client sends rubbish, we better freak out
												SERVER\Error = #Error_LengthOfPacketIncorrect
												LogServerAction(\ClientIdentifier, #MQTTEvent_Error, "", 0, 0, ErrorDescription(SERVER\ERROR), SERVER\Error)
												CloseNetworkConnection(CID)
												FreeMemory(\Buffer)
												DeleteElement(ConnPackets())
												Break
											EndIf
											MoveMemory(\Buffer + Length + 1 + Add, \Buffer, \BufferPos - Length - 1 - Add)
											\BufferPos - Length - 1 - Add
										Else
											;error, close connection
											SERVER\Error = #Error_CorruptedPacketReceived
											LogServerAction(\ClientIdentifier, #MQTTEvent_Error, "", 0, 0, ErrorDescription(SERVER\ERROR), SERVER\Error)
											CloseNetworkConnection(CID)
											FreeMemory(\Buffer)
											DeleteElement(ConnPackets())
											Break
										EndIf
									EndIf
								Until Length = -1
							Else
								;network error? handle it?
							EndIf
						EndIf
					EndWith
					
					With SERVER\Sessions()
						If Found = 0
							;now check for known clients
							ForEach SERVER\Sessions()
								If \SessionState = #SessionState_active
									If \ClientID = CID
										\LastActivity = ElapsedMilliseconds()
										;this client is known and accepted already
										Found = 2
										Break
									EndIf
								EndIf
							Next
						EndIf
						
						If Found = 0
							;???
							CloseNetworkConnection(CID)
						ElseIf Found = 2
							;get packet, here we only receive the packet, we will handle it down below outside of the network loop
							Pos = ReceiveNetworkData(CID, \Buffer + \BufferPos, MemorySize(\Buffer) - \BufferPos)
							If Pos > -1
								\BufferPos + Pos
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
										LastElement(SERVER\Sessions()\Packets())
										AddElement(SERVER\Sessions()\Packets())
										If PacketDataReceived(\Buffer, \BufferPos, @SERVER\Sessions()\Packets())
											If \BufferPos - Length - 1 - Add < 0
												;client sends rubbish, we better freak out
												SERVER\Error = #Error_LengthOfPacketIncorrect
												LogServerAction(\ClientIdentifier, #MQTTEvent_Error, "", 0, 0, ErrorDescription(SERVER\ERROR), SERVER\Error)
												CloseNetworkConnection(CID)
												FreeMemory(SERVER\Sessions()\Buffer)
												ForEach SERVER\Sessions()\Packets()
													ClearPacket()
												Next
												DeleteElement(SERVER\Sessions())
												Break
											EndIf
											MoveMemory(\Buffer + Length + SizeOf(HEADER) + Add, \Buffer, \BufferPos - Length - SizeOf(HEADER) - Add)
											\BufferPos - Length - SizeOf(HEADER) - Add
										Else
											;error, close connection
											SERVER\Error = #Error_CorruptedPacketReceived
											LogServerAction(\ClientIdentifier, #MQTTEvent_Error, "", 0, 0, ErrorDescription(SERVER\ERROR), SERVER\Error)
											CloseNetworkConnection(CID)
											If SERVER\Sessions()\Persistant
												SERVER\Sessions()\SessionState = #SessionState_inactive
												SERVER\Sessions()\ClientID     = #Null
											Else
												FreeMemory(SERVER\Sessions()\Buffer)
												ForEach SERVER\Sessions()\Packets()
													ClearPacket()
												Next
												DeleteElement(SERVER\Sessions())
											EndIf
											Break
										EndIf
									EndIf
								Until Length = -1
							Else
								;network error? handle it?
							EndIf
						EndIf
					EndWith
					
			EndSelect
			
			
			;first check connection packets
			CheckConnPackets(ConnPackets())
			;next, check packets from established sessions
			CheckSessionPackets()
			
			;check for timed out sessions
			ForEach SERVER\Sessions()
				CompilerIf #PB_Compiler_Debugger And #DEEP_DEBUG
					If LogTimer + 2000 < ElapsedMilliseconds()
						LogTimer = ElapsedMilliseconds()
						Debug "Identifiers: " + Str(MapSize(SERVER\Sessions()\PacketIdentifiersUsed()))
						Debug "Packets: " + Str(ListSize(SERVER\Sessions()\Packets()))
						Debug "Retained Packets: " + Str(ListSize(SERVER\RetainedMessages()))
					EndIf
				CompilerEndIf
				If SERVER\Sessions()\SessionState = #SessionState_active And SERVER\Sessions()\LastActivity + (1500 * SERVER\Sessions()\KeepAlive) < ElapsedMilliseconds()
					;timed out
					CheckForLastWill()
					SERVER\Error = #Error_TimedOut
					LogServerAction(SERVER\Sessions()\ClientIdentifier, #MQTTEvent_Error, "", 0, 0, ErrorDescription(SERVER\ERROR), SERVER\Error)
					CloseNetworkConnection(SERVER\Sessions()\ClientID)
					SERVER\Sessions()\ClientID     = #Null
					SERVER\Sessions()\SessionState = #SessionState_inactive
				EndIf
			Next
			
			;Broker should not publish directly to clients.
			;I've added that only for testing purpose
			If TrySemaphore(SERVER\Semaphore)
				LockMutex(SERVER\Mutex)
				If FirstElement(SERVER\PublishToClient())
					ForEach SERVER\Sessions()
						If SERVER\Sessions()\ClientID And SERVER\Sessions()\SessionState = #SessionState_active
							ForEach SERVER\Sessions()\Subscriptions()
								If TopicMatches(SERVER\PublishToClient()\Topic, SERVER\Sessions()\Subscriptions()\Topic)
									If SERVER\PublishToClient()\QoS > 0
										l = SetGetUniqePacketIdentifiers()
									EndIf
									CompilerIf #USE_BASE64_PAYLOAD
										*Payload = @SERVER\PublishToClient()\PayLoadBase64
									CompilerElse
										*Payload = @SERVER\PublishToClient()\Payload
									CompilerEndIf
									AddSessionPacket(#PUBLISH, SERVER\PublishToClient()\Topic, *Payload, SERVER\PublishToClient()\PayLoadLength, SERVER\PublishToClient()\QoS, l)
								EndIf
							Next
						EndIf
					Next
					CompilerIf #USE_BASE64_PAYLOAD = #False
						If SERVER\PublishToClient()\Payload
							FreeMemory(SERVER\PublishToClient()\Payload)
						EndIf
					CompilerEndIf
					DeleteElement(SERVER\PublishToClient())
				EndIf
				UnlockMutex(SERVER\Mutex)
			EndIf
			
		Until SERVER\StopIt = #True
		
		;save session data
		ForEach SERVER\Sessions()
			If SERVER\Sessions()\ClientID
				CloseNetworkConnection(SERVER\Sessions()\ClientID)
				SERVER\Sessions()\ClientID = #Null
			EndIf
			If SERVER\Sessions()\Persistant = #False
				FreeMemory(SERVER\Sessions()\Buffer)
				ForEach SERVER\Sessions()\Packets()
					ClearPacket()
				Next
				ClearList(SERVER\Sessions()\Subscriptions())
				ClearList(SERVER\Sessions()\Packets())
				ClearMap(SERVER\Sessions()\PacketIdentifiersUsed())
				DeleteElement(SERVER\Sessions())
			EndIf
		Next
		
		ForEach ConnPackets()
			If ConnPackets()\ClientID
				CloseNetworkConnection(ConnPackets()\ClientID)
			EndIf
			If ConnPackets()\Buffer
				FreeMemory(ConnPackets()\Buffer)
			EndIf
		Next
		ClearList(ConnPackets())
		CloseNetworkServer(SID)
		LogServerAction("", #MQTTEvent_Info, "", 0, 0, "Server Thread Ended", #Info_ThreadEnded)
		
	EndProcedure
	
	Procedure ClearServerData()
		;can only be used, when no server is currently running.
		;it will delete all persistant messages and stored sessions.
		;the server will then behave as been started for the first time
		
		If SERVER\ThreadID And IsThread(SERVER\ThreadID)
			SERVER\Error = #Error_UseStopServerFirst
			ProcedureReturn #False
		EndIf
		
		If SERVER\PersistantStoragePath = "" Or FileSize(SERVER\PersistantStoragePath) > -2
			ProcedureReturn #True
			;that directory does not exist, which means, we are clean already, therefore we return #True
		EndIf
		
		If FileSize(SERVER\PersistantStoragePath + "sessions.json") > 0
			DeleteFile(SERVER\PersistantStoragePath + "sessions.json")
		EndIf
		If FileSize(SERVER\PersistantStoragePath + "retainmsg.json") > 0
			DeleteFile(SERVER\PersistantStoragePath + "retainmsg.json")
		EndIf
		
		ProcedureReturn #True
	EndProcedure
	
	Procedure _InitDefault(Value.i, Def.i)
		If Value = 0
			ProcedureReturn Def
		EndIf
		ProcedureReturn Value
	EndProcedure
	
	Procedure StartServer()
		Protected i, a$, JSON
		
		
		If SERVER\isInit = #False
			SERVER\Error = #Error_UseInitServerFirst
			ProcedureReturn #False ;InitServer first
		EndIf
		
		If SERVER\ThreadID And IsThread(SERVER\ThreadID)
			SERVER\Error = #Error_UseStopServerFirst
			ProcedureReturn #False ;StopServer first
		EndIf
		
		If ListSize(SERVER\Sessions()) > 0
			ForEach SERVER\Sessions()\Packets()
				ClearPacket()
			Next
			ClearList(SERVER\Sessions()\Packets())
			ClearList(SERVER\Sessions())
		EndIf
		ClearList(SERVER\RetainedMessages())
		;read old Session data (if exists)
		If SERVER\PersistantStoragePath
			If FileSize(SERVER\PersistantStoragePath + "sessions.json") > 0
				JSON = LoadJSON(#PB_Any, SERVER\PersistantStoragePath + "sessions.json")
				If JSON
					ExtractJSONList(JSONValue(JSON), SERVER\Sessions())
					FreeJSON(JSON)
				EndIf
			EndIf
			ForEach SERVER\Sessions()
				SERVER\Sessions()\SessionState = #SessionState_inactive
				SERVER\Sessions()\Buffer       = AllocateMemory(SERVER\InitBufferSize)
				SERVER\Sessions()\BufferPos    = 0
				SERVER\Sessions()\ClientID     = 0
				CompilerIf #USE_BASE64_PAYLOAD = #False
					ForEach SERVER\Sessions()\Packets()
						If SERVER\Sessions()\Packets()\PayLoad\BufferLengh
							SERVER\Sessions()\Packets()\PayLoad\Buffer = AllocateMemory(SERVER\Sessions()\Packets()\PayLoad\BufferLengh)
							Base64Decoder(SERVER\Sessions()\Packets()\PayLoad\PayLoadBase64, SERVER\Sessions()\Packets()\PayLoad\Buffer, SERVER\Sessions()\Packets()\PayLoad\BufferLengh)
							SERVER\Sessions()\Packets()\PayLoad\PayLoadBase64 = ""
						EndIf
					Next
				CompilerEndIf
			Next
			If FileSize(SERVER\PersistantStoragePath + "retainmsg.json") > 0
				JSON = LoadJSON(#PB_Any, SERVER\PersistantStoragePath + "retainmsg.json")
				If JSON
					ExtractJSONList(JSONValue(JSON), SERVER\RetainedMessages())
					FreeJSON(JSON)
					CompilerIf #USE_BASE64_PAYLOAD = #False
						ForEach SERVER\RetainedMessages()
							If SERVER\RetainedMessages()\PayLoadBase64
								SERVER\RetainedMessages()\Payload = AllocateMemory(SERVER\RetainedMessages()\PayLoadLength)
								Base64Decoder(SERVER\RetainedMessages()\PayLoadBase64, SERVER\RetainedMessages()\Payload, SERVER\RetainedMessages()\PayLoadLength)
								SERVER\RetainedMessages()\PayLoadBase64 = ""
							EndIf
						Next
					CompilerEndIf
				EndIf
			EndIf
		EndIf
		
		SERVER\StopIt   = #False
		SERVER\ThreadID = CreateThread(@ServerThread(), 0)
		
		ProcedureReturn SERVER\ThreadID
	EndProcedure
	
	Procedure InitServer(*Config.SERVER_INIT, RunServerAlso.i = #False)
		Protected a$, i, Result
		
		If SERVER\InitNetwork = #False
			If InitNetwork() = 0
				SERVER\Error = #Error_NoNetworkAvailable
				ProcedureReturn 0
			EndIf
			SERVER\InitNetwork = #True
		EndIf
		
		If SERVER\isInit = #True
			SERVER\Error = #Error_UseDeInitServerFirst
			ProcedureReturn 0
		EndIf
		SERVER\isInit                = #True
		SERVER\Port                  = _InitDefault(*Config\Port, 1883)
		SERVER\BindIP                = *Config\BindIP
		SERVER\InitBufferSize        = _InitDefault(*Config\InitialBufferSize, $10000)
		SERVER\LogFile               = *Config\LogFile
		SERVER\PersistantStoragePath = *Config\PersistantStoragePath
		SERVER\TimeOUT               = _InitDefault(*Config\ClientTimeOUT, 5000)
		SERVER\LogWindowEvent        = *Config\LogWindowEvent
		SERVER\LogWindow             = *Config\LogWindow
		SERVER\Mutex                 = CreateMutex()
		SERVER\Semaphore             = CreateSemaphore()
		
		If *Config\LogWindowEvent = 0
			SERVER\LogWindow = -1
		EndIf
		If SERVER\PersistantStoragePath And FileSize(SERVER\PersistantStoragePath) <> -2
			;Make sure directory exists
			a$ = StringField(SERVER\PersistantStoragePath, 1, #PS$)
			For i = 1 To CountString(SERVER\PersistantStoragePath, #PS$) - 1
				a$ + #PS$
				If FileSize(a$) <> -2
					CreateDirectory(a$)
				EndIf
				a$ + StringField(SERVER\PersistantStoragePath, i + 1, #PS$)
			Next i
		EndIf
		CopyList(*Config\Access(), SERVER\Access())
		
		Result = #True
		If RunServerAlso
			Result = StartServer()
		EndIf
		
		ProcedureReturn Result
	EndProcedure
	
	Procedure StopServer()
		Protected JSON
		
		If SERVER\ThreadID And IsThread(SERVER\ThreadID)
			SERVER\StopIt = #True
			If WaitThread(SERVER\ThreadID, 1000) = 0
				KillThread(SERVER\ThreadID)
			EndIf
			SERVER\ThreadID = 0
		EndIf
		
		If SERVER\PersistantStoragePath
			If ListSize(SERVER\Sessions()) = 0
				If FileSize(SERVER\PersistantStoragePath + "sessions.json") > 0
					DeleteFile(SERVER\PersistantStoragePath + "sessions.json")
				EndIf
			Else
				CompilerIf #USE_BASE64_PAYLOAD = #False
					ForEach SERVER\Sessions()
						ForEach SERVER\Sessions()\Packets()
							If SERVER\Sessions()\Packets()\PayLoad\Buffer
								SERVER\Sessions()\Packets()\PayLoad\PayLoadBase64 = Base64Encoder(SERVER\Sessions()\Packets()\PayLoad\Buffer, SERVER\Sessions()\Packets()\PayLoad\BufferLengh)
								FreeMemory(SERVER\Sessions()\Packets()\PayLoad\Buffer)
								SERVER\Sessions()\Packets()\PayLoad\Buffer = 0
							EndIf
						Next
					Next
				CompilerEndIf
				JSON = CreateJSON(#PB_Any)
				InsertJSONList(JSONValue(JSON), SERVER\Sessions())
				SaveJSON(JSON, SERVER\PersistantStoragePath + "sessions.json")
				FreeJSON(JSON)
			EndIf
			If ListSize(SERVER\RetainedMessages()) = 0
				CompilerIf #USE_BASE64_PAYLOAD = #False
					ForEach SERVER\RetainedMessages()
						If SERVER\RetainedMessages()\Payload
							SERVER\RetainedMessages()\PayLoadBase64 = Base64Encoder(SERVER\RetainedMessages()\Payload, SERVER\RetainedMessages()\PayLoadLength)
							FreeMemory(SERVER\RetainedMessages()\Payload)
							SERVER\RetainedMessages()\Payload = 0
						EndIf
					Next
				CompilerEndIf
				If FileSize(SERVER\PersistantStoragePath + "retainmsg.json") > 0
					DeleteFile(SERVER\PersistantStoragePath + "retainmsg.json")
				EndIf
			Else
				JSON = CreateJSON(#PB_Any)
				InsertJSONList(JSONValue(JSON), SERVER\RetainedMessages())
				SaveJSON(JSON, SERVER\PersistantStoragePath + "retainmsg.json")
				FreeJSON(JSON)
			EndIf
		EndIf
		
		;clean up
		ForEach SERVER\Sessions()
			If SERVER\Sessions()\Buffer
				FreeMemory(SERVER\Sessions()\Buffer)
			EndIf
			ClearList(SERVER\Sessions()\Subscriptions())
			ForEach SERVER\Sessions()\Packets()
				ClearPacket()
			Next
			ClearList(SERVER\Sessions()\Packets())
		Next
		ClearList(SERVER\Sessions())
		ClearList(SERVER\RetainedMessages())
	EndProcedure
	
	Procedure DeInitServer()
		StopServer()
		SERVER\isInit = #False
		If SERVER\Mutex
			FreeMutex(SERVER\Mutex)
			SERVER\Mutex = 0
		EndIf
		If SERVER\Semaphore
			FreeSemaphore(SERVER\Semaphore)
			SERVER\Semaphore = 0
		EndIf
	EndProcedure
	
	Procedure GetLastError()
		ProcedureReturn SERVER\Error
	EndProcedure
	
EndModule

;--------------###########---------------
;                  EOF
;--------------###########---------------



CompilerIf #PB_Compiler_IsMainFile
	
	; Quick example, with a log window
	; for all who own those cool shelly plugs (https://www.amazon.de/gp/product/B07TCQ7BFN/ref=ppx_yo_dt_b_asin_title_o06_s00?ie=UTF8&psc=1)
	; => they can be switched on and off with this example.
	;    (need to activate MQTT support in their settings)
	
	Enumeration #PB_Event_FirstCustomValue
		#MQTT_Event
	EndEnumeration
	
	Enumeration
		#MQTT_LogWindow
	EndEnumeration
	
	Enumeration
		#Editor_Log
		#Combo_Shellies
		#Button_SwitchShelly
	EndEnumeration
	
	Procedure LogIT(Text.s)
		If Text
			Text = FormatDate("%hh:%ii:%ss", Date()) + " " + Text
		EndIf
		
		AddGadgetItem(#Editor_Log, -1, Text)
		CompilerSelect #PB_Compiler_OS
			CompilerCase #PB_OS_Windows
				Select GadgetType(#Editor_Log)
					Case #PB_GadgetType_ListView
						SendMessage_(GadgetID(#Editor_Log), #LB_SETTOPINDEX, CountGadgetItems(#Editor_Log) - 1, #Null)
					Case #PB_GadgetType_ListIcon
						SendMessage_(GadgetID(#Editor_Log), #LVM_ENSUREVISIBLE, CountGadgetItems(#Editor_Log) - 1, #False)
					Case #PB_GadgetType_Editor
						SendMessage_(GadgetID(#Editor_Log), #EM_SCROLLCARET, #SB_BOTTOM, 0)
				EndSelect
			CompilerCase #PB_OS_Linux
				Protected *Adjustment.GtkAdjustment
				*Adjustment       = gtk_scrolled_window_get_vadjustment_(gtk_widget_get_parent_(GadgetID(#Editor_Log)))
				*Adjustment\value = *Adjustment\upper
				gtk_adjustment_value_changed_(*Adjustment)
		CompilerEndSelect
		
	EndProcedure
	
	Procedure OnEvent_ComboShellies()
		Protected i
		
		i = GetGadgetState(#Combo_Shellies)
		If i = -1
			DisableGadget(#Button_SwitchShelly, 1)
			SetGadgetText(#Button_SwitchShelly, "")
		Else
			DisableGadget(#Button_SwitchShelly, 0)
			If GetGadgetItemData(#Combo_Shellies, i)
				SetGadgetText(#Button_SwitchShelly, "Switch off")
			Else
				SetGadgetText(#Button_SwitchShelly, "Switch on")
			EndIf
		EndIf
	EndProcedure
	
	
	Procedure OnEvent_MQTT_LogData()
		Protected *Values.MQTT_Common::MQTT_EVENTDATA, a$
		Protected *Buffer, i, no, ClientIdentifier.s, Type, Payload.s
		Protected Topic.s, ErrorText.s, Error, QoS, DUP, Retain, RetMsg.s, Identifier
		
		*Values = EventData()
		If *Values
			a$               = PeekS(*Values + OffsetOf(MQTT_Common::MQTT_EVENTDATA\D), -1, #PB_UTF8)
			ClientIdentifier = StringField(a$, 1, #ESC$)
			Topic            = StringField(a$, 2, #ESC$)
			ErrorText        = ReplaceString(StringField(a$, 3, #ESC$), "{CLIENT}", ClientIdentifier)
			Type             = *Values\Type
			Error            = *Values\Error
			QoS              = *Values\QoS
			DUP              = *Values\DUP
			Retain           = *Values\Retain
			Identifier       = *Values\PacketIdentifier
			If *Values\PayLoadLength
				Payload = PeekS(*Values\PayLoad, *Values\PayLoadLength, #PB_UTF8 | #PB_ByteLength)
				FreeMemory(*Values\PayLoad)
			EndIf
			FreeMemory(*Values)
			
			;handle it...
			Select Type
				Case MQTT_Common::#MQTTEvent_ClientConnected
					LogIT("Client [" + ClientIdentifier + "] connected!")
				Case MQTT_Common::#MQTTEvent_ClientDisconnected
					LogIT("Client [" + ClientIdentifier + "] disconnected!")
				Case MQTT_Common::#MQTTEvent_InfoPublished
					;for shelly plugs
					If Topic = "shellies/" + ClientIdentifier + "/relay/0"
						;this is a shelly plug!
						no = #True
						For i = 0 To CountGadgetItems(#Combo_Shellies) - 1
							If GetGadgetItemText(#Combo_Shellies, i) = ClientIdentifier
								no = #False
								If Payload = "on"
									SetGadgetItemData(#Combo_Shellies, i, 1)
								Else
									SetGadgetItemData(#Combo_Shellies, i, 0)
								EndIf
								Break
							EndIf
						Next i
						If no
							i = CountGadgetItems(#Combo_Shellies)
							AddGadgetItem(#Combo_Shellies, -1, ClientIdentifier)
							If Payload = "on"
								SetGadgetItemData(#Combo_Shellies, i, 1)
							Else
								SetGadgetItemData(#Combo_Shellies, i, 0)
							EndIf
						EndIf
						OnEvent_ComboShellies()
					EndIf
					If Retain
						RetMsg = "(Retained) "
					EndIf
					LogIT("Client [" + ClientIdentifier + "] Published " + RetMsg + "Topic: " + Topic + ", Payload: " + Payload + 
					      " (Q=" + Str(QoS) + ", R=" + Str(Retain) + ", D=" + Str(DUP) + ", M=" + Str(Identifier) + ")")
				Case MQTT_Common::#MQTTEvent_Subscription
					LogIT("Client [" + ClientIdentifier + "] Subscribed to Topic: " + Topic + " (Q=" + Str(QoS) + ")")
				Case MQTT_Common::#MQTTEvent_Error
					LogIT("[ERROR] " + ErrorText)
			EndSelect
		EndIf
	EndProcedure
	
	Procedure OnEvent_ResizeWindow()
		ResizeGadget(#Editor_Log, #PB_Ignore, #PB_Ignore, WindowWidth(#MQTT_LogWindow) - 10, WindowHeight(#MQTT_LogWindow) - 40)
		ResizeGadget(#Button_SwitchShelly, WindowWidth(#MQTT_LogWindow) / 2 - 40, WindowHeight(#MQTT_LogWindow) - 30, #PB_Ignore, #PB_Ignore)
		ResizeGadget(#Combo_Shellies, #PB_Ignore, WindowHeight(#MQTT_LogWindow) - 30, WindowWidth(#MQTT_LogWindow) / 2 - 60, #PB_Ignore)
	EndProcedure
	
	Procedure OnEvent_Button()
		Protected i, Topic.s, *Payload, L
		
		i = GetGadgetState(#Combo_Shellies)
		If i > -1
			Topic = "shellies/" + GetGadgetItemText(#Combo_Shellies, i) + "/relay/0/command"
			If GetGadgetItemData(#Combo_Shellies, i) = 1
				*Payload = UTF8("off")
				L        = 3
			Else
				*Payload = UTF8("on")
				L        = 2
			EndIf
			MQTT_BROKER::PublishViaServer(Topic, *Payload, L, 0, MQTT_Common::#PayloadType_Buffer)
			FreeMemory(*Payload)
		EndIf
	EndProcedure
	
	Procedure main()
		Protected BrokerConfig.MQTT_BROKER::SERVER_INIT
		
		
		OpenWindow(#MQTT_LogWindow, 0, 0, 800, 400, "MQTT Broker Log", #PB_Window_SystemMenu | #PB_Window_SizeGadget | #PB_Window_ScreenCentered | #PB_Window_MinimizeGadget)
		EditorGadget(#Editor_Log, 5, 5, 790, 360, #PB_Editor_ReadOnly)
		ComboBoxGadget(#Combo_Shellies, 5, 370, 150, 25)
		ButtonGadget(#Button_SwitchShelly, 360, 370, 80, 25, "")
		DisableGadget(#Button_SwitchShelly, 1)
		BindGadgetEvent(#Button_SwitchShelly, @OnEvent_Button())
		BindGadgetEvent(#Combo_Shellies, @OnEvent_ComboShellies())
		BindEvent(#PB_Event_SizeWindow, @OnEvent_ResizeWindow())
		
		;Set Broker parameters
		BrokerConfig\LogWindow             = #MQTT_LogWindow
		BrokerConfig\LogWindowEvent        = #MQTT_Event
		;BrokerConfig\PersistantStoragePath = GetUserDirectory(#PB_Directory_ProgramData) + "hex0r" + #PS$ + "MQTT" + #PS$
		;BrokerConfig\LogFile               = "I:\mqtt.log"
		AddElement(BrokerConfig\Access())
		BrokerConfig\Access()\Username = "aaa"
		BrokerConfig\Access()\Password = "bbb"
		
		If MQTT_BROKER::InitServer(@BrokerConfig, #True)
			LogIT("Listen Server started, waiting for incoming data...")
			BindEvent(#MQTT_Event, @OnEvent_MQTT_LogData(), #MQTT_LogWindow)
			OnEvent_ResizeWindow()
			Repeat : Until WaitWindowEvent() = #PB_Event_CloseWindow
			MQTT_BROKER::DeInitServer()
		EndIf
		
	EndProcedure
	
	main()
CompilerEndIf


; IDE Options = PureBasic 6.00 LTS (Windows - x64)
; CursorPosition = 1731
; FirstLine = 464
; Folding = 4Bpl0jPm66
; EnableThread
; EnableXP
; EnableUser
; Executable = MQTT_Broker.exe
; CompileSourceDirectory
; EnablePurifier = 1,1,1,1
; EnableCompileCount = 174
; EnableBuildCount = 3
; EnableExeConstant