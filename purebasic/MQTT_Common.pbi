; MQTTCommon.pbi
; (c)HeX0R 20.10.2022
; Version 1.02
;
;     1.01:   (13.10.2022)
;     Added unsubscribe events for Client
;
;     1.02:
;     Added #MQTTEvent_Info signals
;     Removed #Error_ThreadEnded and replaced it with #Info_ThreadEnded
;
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

DeclareModule MQTT_Common
	
	#USE_BASE64_PAYLOAD = #True
	;the framework can use (internally) base64 decoded payloads, or memory buffers.
	;while base64 should be o.k. usually, MQTT also allows to send huge binary data, even files are allowed (max.: 268,435,455bytes).
	;in that case the base64 strings the broker will store, can get quite huge and the en/decoding procedure might also need much time.
	;then you better switch to binary storage, maybe also, when you run it on a RaspBi due to it's less memory capacities.
	;binary storage is not fully tested, it might lead to memory leaks (although I didn't recognize it until now).
	
	
	#DEEP_DEBUG = #False   ;for debugging purposes only, to see, if all packets and identifiers are being removed successfully
	
	Enumeration ControlPacketTypes
		#Reserved1
		#CONNECT
		#CONNACK
		#PUBLISH
		#PUBACK
		#PUBREC
		#PUBREL
		#PUBCOMP
		#SUBSCRIBE
		#SUBACK
		#UNSUBSCRIBE
		#UNSUBACK
		#PINGREQ
		#PINGRESP
		#DISCONNECT
		#Reserved2
	EndEnumeration
	
	CompilerIf #PB_Compiler_Debugger
		Global Dim DTypes.s(#Reserved2) ;only used for debugging purposes, not needed for final product
		DTypes(#Reserved1)   = "Reserved!"
		DTypes(#CONNECT)     = "CONNECT"
		DTypes(#CONNACK)     = "CONNACK"
		DTypes(#PUBLISH)     = "PUBLISH"
		DTypes(#PUBACK)      = "PUBACK"
		DTypes(#PUBREC)      = "PUBREC"
		DTypes(#PUBREL)      = "PUBREL"
		DTypes(#PUBCOMP)     = "PUBCOMP"
		DTypes(#SUBSCRIBE)   = "SUBSCRIBE"
		DTypes(#SUBACK)      = "SUBACK"
		DTypes(#UNSUBSCRIBE) = "UNSUBSCRIBE"
		DTypes(#UNSUBACK)    = "UNSUBACK"
		DTypes(#PINGREQ)     = "PINGREQ"
		DTypes(#PINGRESP)    = "PINGRESP"
		DTypes(#DISCONNECT)  = "DISCONNECT"
	CompilerEndIf
	
	Enumeration ConnectionReturnValues         ;values are defined in the MQTT description, I just wanted to give them more understandable names
		#ConnAccepted
		#ConnRefused_UnacceptableProtocolVersion
		#ConnRefused_IdentifierRejected
		#ConnRefused_ServerUnavailable
		#ConnRefused_BadUsernameOrPassword
		#ConnRefused_NotAuthorized
	EndEnumeration
	
	Enumeration SessionStates
		#SessionState_inactive
		#SessionState_active
	EndEnumeration
	
	Enumeration PacketState
		#PacketState_Incoming
		#PacketState_WaitForAnswer
		#PacketState_OutgoingNotSendYet
	EndEnumeration
	
	Enumeration Errors
		#Error_None
		#Error_CantStartServer
		#Error_CantConnect
		#Error_BeingDisconnected
		#Error_WrongAnswerReceived
		#Error_TimedOut
		#Error_UnsupportedIdentifier
		#Error_UseStopServerFirst
		#Error_UseDeInitServerFirst
		#Error_NoNetworkAvailable
		#Error_UseInitServerFirst
		#Error_MQTTServiceUnavailable
		#Error_UnsupportedProtocolVersion
		#Error_BadUsernameOrPassword
		#Error_NotAuthorizedToConnect
		#Error_CorruptedPacketReceived
		#Error_LengthOfPacketIncorrect
	EndEnumeration
	
	Enumeration Info
		#Info_ThreadStarted
		#Info_ThreadEnded     ;<- can be used for an easy way to know that a servcer/client shut-off/disconnected
	EndEnumeration
	
	Enumeration EventTypes
		#MQTTEvent_ClientConnected           ;<- Broker only
		#MQTTEvent_ClientDisconnected				 ;<- Broker only
		#MQTTEvent_InfoPublished						 ;<- Broker only
		#MQTTEvent_Subscription							 ;<- Broker only
		#MQTTEvent_SuccessfullyConnected		 ;<- Client only
		#MQTTEvent_SuccessfullyDisconnected	 ;<- Client only
		#MQTTEvent_SubscriptionSuccessfull	 ;<- Client only
		#MQTTEvent_SubscriptionDenied				 ;<- Client only
		#MQTTEvent_UnsubscriptionSuccessfull ;<- Client only
		#MQTTEvent_UnsibscriptionDenied      ;<- Client only
		#MQTTEvent_PublishIncoming					 ;<- Client only
		#MQTTEvent_PublishingSuccessfull		 ;<- Client only
		#MQTTEvent_PublishingDenied					 ;<- Client only
		#MQTTEvent_Error										 ;<- Client AND Broker
		#MQTTEvent_Info                      ;<- Client and Broker
	EndEnumeration
	
	Enumeration
		#PayloadType_UnicodeString
		#PayloadType_UTF8String
		#PayloadType_base64
		#PayloadType_Buffer
	EndEnumeration
	
	Enumeration SendResults
		#SendFailed = -1
		#SendNotFinished
		#SendFinished
	EndEnumeration
	
	Structure HEADER
		PacketType.b
		bytes.a[0]
	EndStructure
	
	Structure MQTT_EVENTDATA   ;used to communicate with the Event Window
		Type.u
		PacketIdentifier.u
		Error.u
		QoS.b
		DUP.b
		Retain.b
		*PayLoad
		PayLoadLength.i
		D.b[0]
	EndStructure
	
	Structure Filter
		Topic.s
		QoS.b
	EndStructure
	
	Structure Filter_tmp
		Topic.s
		QoS.b
		Add.b
	EndStructure
	
	Structure PAYLOAD
		*Buffer
		BufferLengh.i
		PayLoadBase64.s
	EndStructure
	
	Structure NOTYETSENT
		*Buffer
		BufferLength.i
	EndStructure
	
	Structure PACKET
		Type.w                      ;part of the header of any packet => type of this packet, see enumeration ControlPacketTypes above
		Flags.w											;part of the header of any packet => not often really used in MQTT, just make sure, it fits the requirements
		PacketIdentifier.u					;used in PUBLISH packets with higher QoS and in SUBSCRIBE packets
		QoS.b												;Quality of Service code
		DUP.b												;used in PUBLISH packets, to indicate this is not the first time the server was sending that packet (DUPlicate)
		Retain.b										;when set to 1 the server should keep the message, to send it to clients which are currently disconnected.
																;                    as soon as they connect and re-subscribe, the retained messages will be sent to them.
		TopicName.s									;Topic name of Published items
		PacketState.b								;incoming, outgoing or waiting for a reply, see enumeration PacketState above
		ReSendAt.q									;in case we didn't got a usable answer, we set a new time for resending it
		WaitForAnswerSince.q				;holds the time, since when we are waiting for a reply
		NYS.NOTYETSENT							;added this to handle huge payloads also.
		;                            huge payloads can not be sent in one piece, therefore we have to store the remaining data here
		PayLoad.PAYLOAD									  
		;                            Content of ... whatever type this packet is
		;                            MQTT supports binary content also, although most clients are sending UTF8 strings.
		;                            to remain compatible, this framework will store the Payload as base64 encoded strings
		;                            because the MQTT protocol V3.1.1 has no flag to show us, what kind of data it really is.
		List tmpSubsc.Filter_tmp()  ;temp. Filters being transmitted for subscriptions
																;           They will be integrated into Sessions()\Subscriptions() then
	EndStructure
	
	Structure MINI_PACKET         ;comes in handy, for very small packets we have to send (like #PINGRESP e.g.)
		a.a[4]
	EndStructure
	
	Declare.s ErrorDescription(Error)
	
	CompilerIf #PB_Compiler_OS = #PB_OS_Linux
		ImportC ""
			errno_location() As "__errno_location"
		EndImport
	CompilerEndIf

EndDeclareModule

Module MQTT_Common
	Procedure.s ErrorDescription(Error)
		Result.s = ""
		Select Error
			Case #Error_None
				Result = "No error!"
			Case #Error_CantStartServer
				Result = "Unable to start the Broker! (Listenport in use?)"
			Case #Error_CantConnect
				Result = "Can't connect to Broker!"
			Case #Error_BeingDisconnected
				Result = "Somehow we have been disconnected!"
			Case #Error_WrongAnswerReceived
				Result = "Wrong Answer from {CLIENT} received!"
			Case #Error_TimedOut
				Result = "{CLIENT} Timed out!"
			Case #Error_UnsupportedIdentifier
				Result = "This ClientIdentifier ({CLIENT}) is not allowed!"
			Case #Error_UseStopServerFirst
				Result = "Use StopServer() first!"
			Case #Error_UseDeInitServerFirst
				Result = "Use DeInitServer() first!"
			Case #Error_NoNetworkAvailable
				Result = "Can't initialize network!"
			Case #Error_UseInitServerFirst
				Result = "Use InitServer() first!"
			Case #Error_MQTTServiceUnavailable
				Result = "MQTT Service is unavailable!"
			Case #Error_UnsupportedProtocolVersion
				Result = "MQTT Protocol version not accepted!"
			Case #Error_BadUsernameOrPassword
				Result = "Username and/or Password wrong!"
			Case #Error_NotAuthorizedToConnect
				Result = "You are not authorized to connect to that broker!"
			Case #Error_CorruptedPacketReceived
				Result = "Corrupted Packet from {CLIENT} received!"
			Case #Error_LengthOfPacketIncorrect
				Result = "Invalid Packetlength from {CLIENT} received!"
		EndSelect
		
		ProcedureReturn Result
	EndProcedure
EndModule

; IDE Options = PureBasic 6.00 LTS (Windows - x64)
; CursorPosition = 9
; Folding = -
; EnableXP
; EnableUser
; CompileSourceDirectory
; EnableCompileCount = 0
; EnableBuildCount = 0
; EnableExeConstant