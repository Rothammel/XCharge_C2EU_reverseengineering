XIncludeFile "MQTT_Client.pbi"
; Zeichenvorlage | +Chr(10)+

;EnableExplicit
Declare MQTT_EventIncoming()
Declare MQTT_Helper(payload.s)
;Panel Gadget Hauptfenster
Enumeration
  #ListIcon
EndEnumeration
Enumeration  #PB_Event_FirstCustomValue
  #MQTTEvent
EndEnumeration

Global ClientID.i, sessionID.i, port.b

ExamineDesktops()
If OpenWindow(0, (DesktopWidth(0)/2)-250, (DesktopHeight(0)/2)-250, 1500, 1000, "Vorlage")


  If CreateMenu(0, WindowID(0))
    MenuTitle("File")
      ;MenuItem( 1, "&Load...")
      MenuItem( 7, "&Quit")

    MenuTitle("Edition")
      MenuItem( 8, "Cut")
      MenuItem( 9, "Copy")
      MenuItem(10, "Paste")
      
    MenuTitle("?")
      MenuItem(11, "About")
  EndIf
  
  ListIconGadget(#ListIcon, 10, 500, 1480, 500, "Meldung", 500)

  
  BindEvent(#MQTTEvent, @MQTT_EventIncoming(), #PB_All)
	Conf.MQTT_CLIENT::CLIENT_INIT
	;Config Client
	Conf\BrokerURL        = "192.168.0.5"
	Conf\Username         = "Stan"
	Conf\Password         = "rotweiss"
	Conf\ClientIdentifier = "PBClient_{" + RSet(Str(Random(999999, 101)), 6, "0") + "}"
	Conf\Window           = 0
	Conf\WindowEvent      = #MQTTEvent
	
	ClientID = MQTT_CLIENT::InitClient(@Conf, #True)
  
  
  Repeat
    Select WaitWindowEvent(50)

      Case #PB_Event_Menu

        Select EventMenu()  ; To see which menu has been selected

          Case 11 ; About
            MessageRequester("About", "cooles Tool :)", 0)
            
            
          Case 7
            End

        EndSelect
      Case #PB_Event_Gadget
        Select EventGadget()
            
          Case 1
        EndSelect

      Case #PB_Event_CloseWindow
        Quit = 1

    EndSelect

  Until Quit = 1

EndIf

End


Procedure MQTT_EventIncoming()
	;this is our contact to the MQTT Client!
	;It will inform us about things, which are going on
	Protected *Values.MQTT_Common::MQTT_EVENTDATA, a$
	Protected i, Found, Type, Payload.s, Topic.s, PacketIdentifier, ErrorText.s, Error
	Protected QoS, DUP, Retain, Shelly.s
	
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
		If *Values\PayLoadLength
			Payload = PeekS(*Values\PayLoad, *Values\PayLoadLength, #PB_UTF8)
			FreeMemory(*Values\PayLoad)
		EndIf
		FreeMemory(*Values)
		;handle it...
		Select Type
		  Case MQTT_Common::#MQTTEvent_SuccessfullyConnected
		    AddGadgetItem(#ListIcon, -1, "Successfully connected to Broker!")

				;subscribe now to (some) topic(s)
				NewList Subs.MQTT_Common::Filter()
			
				AddElement(Subs())
				Subs()\Topic = "test/up" : Subs()\QoS   = 0
				;AddElement(Subs())
				;Subs()\Topic = "test/down" : Subs()\QoS   = 0
				MQTT_CLIENT::SubscribeToTopics(ClientID, Subs())
			Case MQTT_Common::#MQTTEvent_SubscriptionSuccessfull
			  ;unused
			Case MQTT_Common::#MQTTEvent_PublishIncoming
			  AddGadgetItem(#ListIcon, -1, Topic)
			  AddGadgetItem(#ListIcon, -1, ReplaceString(Payload, Chr(10), " /n "))
			  MQTT_Helper(Payload)
			Case MQTT_Common::#MQTTEvent_PublishingSuccessfull
			  AddGadgetItem(#ListIcon, -1, "Publishing accepted!")
			Case MQTT_Common::#MQTTEvent_Error
			  AddGadgetItem(#ListIcon, -1, ErrorText)
		EndSelect
	EndIf
EndProcedure

Procedure MQTT_Helper(payload.s)
  Debug "payload: " + payload
  If FindString(payload, Chr(10))
    payload = ReplaceString(Payload, Chr(10), " ")
  EndIf
  message.s = StringField(payload, 1, " ")
  Debug "message: " + message
  version.s = StringField(payload, 2, " ")
  Debug "version: " + version
  irgendein_counter.s = StringField(payload, 3, " ")
  Debug "counter: " + irgendein_counter
  json_string.s = StringField(payload, 4, " ")
  Debug "JSON: " + json_string
  If message = "RequestChargeQRCode"
    If ParseJSON(0, json_string)
      ;If GetJSONMember(JSONValue(0), "sid")
        sessionID = Val(GetJSONString(GetJSONMember(JSONValue(0), "sid")))
        Debug "current sessionID: " + sessionID
      ;EndIf
      ;If GetJSONMember(JSONValue(0), "port")
        port = GetJSONInteger(GetJSONMember(JSONValue(0), "port"))
        Debug "port: " + port
      ;EndIf
      ;If GetJSONMember(JSONValue(0), "time")
        time.s = GetJSONString(GetJSONMember(JSONValue(0), "time"))
        Debug "time: " + time
      ;EndIf
    Else
      Debug "JSONErrorMessage: " + JSONErrorMessage() + "on position: " + JSONErrorPosition()
    EndIf
  EndIf
  
EndProcedure
; IDE Options = PureBasic 6.00 LTS (Windows - x64)
; CursorPosition = 159
; FirstLine = 118
; Folding = -
; EnableThread
; EnableXP