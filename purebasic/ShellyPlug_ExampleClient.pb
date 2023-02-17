XIncludeFile "MQTT_Client.pbi"

Enumeration #PB_Event_FirstCustomValue
	#MQTTEvent
EndEnumeration

;we store all Shelly plugs and their current states in a list
Structure _Shellies_
	State.s
	Temp.s
	Overtemp.s
	Power.s
	Energy.s
EndStructure
	
Global ClientID.i
Global NewMap Shellies._Shellies_()

Procedure.s GetXMLString()
	Protected XML$

	XML$ + "<?xml version='1.0' encoding='UTF-16'?>"
	XML$ + ""
	XML$ + "<dialogs>"
	XML$ + "  <window flags='#PB_Window_SystemMenu | #PB_Window_MinimizeGadget | #PB_Window_MaximizeGadget | #PB_Window_SizeGadget | #PB_Window_ScreenCentered' text='Shelly Plugs Example MQTT Client' minwidth='400' minheight='360' name='window_main' xpos='1283' ypos='519'>"
	XML$ + "    <vbox expand='item:3'>"
	XML$ + "      <frame text='Shelly Plugs Available' name='frame_1'>"
	XML$ + "        <combobox name='combo_shellies' onchange='OnChange_ComboShellies()'/> "
	XML$ + "      </frame>"
	XML$ + "      <frame text='Shelly Plug State' name='frame_2'>"
	XML$ + "        <gridbox columns='2'>"
	XML$ + "          <text text='Current Temperature:' name='text_1'/>"
	XML$ + "          <string flags='#PB_String_ReadOnly' name='string_temp'/>"
	XML$ + "          <text text='Current Power:' name='text_2'/>"
	XML$ + "          <string flags='#PB_String_ReadOnly' name='string_power'/>"
	XML$ + "          <text text='Overtemperature:' name='text_3'/>"
	XML$ + "          <string flags='#PB_String_ReadOnly' name='string_overtemp'/>"
	XML$ + "          <text text='Energy:' name='text_5'/>"
	XML$ + "          <string flags='#PB_String_ReadOnly' name='string_energy'/>"
	XML$ + "          <text text='Current State: [unknown]' name='text_4'/>"
	XML$ + "          <button disabled='yes' name='button_state' text='???' onevent='OnClick_ButtonState()'/>"
	XML$ + "        </gridbox> "
	XML$ + "      </frame>"
	XML$ + "      <frame text='Log:' name='frame_3'>"
	XML$ + "        <editor name='editor_log'/> "
	XML$ + "      </frame> "
	XML$ + "    </vbox> "
	XML$ + "  </window>"
	XML$ + "</dialogs><!--DDesign0R Definition: PureBasic|1|1|1|__|-|0-->"

	ProcedureReturn XML$
EndProcedure

Procedure LogIT(Text.s)
	Protected Gadget
	
	Gadget = DialogGadget(0, "editor_log")
	If Text
		Text = FormatDate("%hh:%ii:%ss", Date()) + " " + Text
	EndIf
	AddGadgetItem(Gadget, -1, Text)
	CompilerSelect #PB_Compiler_OS
		CompilerCase #PB_OS_Windows
			Select GadgetType(Gadget)
				Case #PB_GadgetType_ListView
					SendMessage_(GadgetID(Gadget), #LB_SETTOPINDEX, CountGadgetItems(Gadget) - 1, #Null)
				Case #PB_GadgetType_ListIcon
					SendMessage_(GadgetID(Gadget), #LVM_ENSUREVISIBLE, CountGadgetItems(Gadget) - 1, #False)
				Case #PB_GadgetType_Editor
					SendMessage_(GadgetID(Gadget), #EM_SCROLLCARET, #SB_BOTTOM, 0)
			EndSelect
		CompilerCase #PB_OS_Linux
			Protected *Adjustment.GtkAdjustment
			*Adjustment       = gtk_scrolled_window_get_vadjustment_(gtk_widget_get_parent_(GadgetID(Gadget)))
			*Adjustment\value = *Adjustment\upper
			gtk_adjustment_value_changed_(*Adjustment)
	CompilerEndSelect
	
EndProcedure

Runtime Procedure OnChange_ComboShellies()
	If FindMapElement(Shellies(), GetGadgetText(DialogGadget(0, "combo_shellies")))
		DisableGadget(DialogGadget(0, "button_state"), 0)
		SetGadgetText(DialogGadget(0, "string_temp"), Shellies()\Temp)
		SetGadgetText(DialogGadget(0, "string_power"), Shellies()\Power)
		SetGadgetText(DialogGadget(0, "string_overtemp"), Shellies()\Overtemp)
		SetGadgetText(DialogGadget(0, "string_energy"), Shellies()\Energy)
		If Shellies()\State = "off"
			SetGadgetText(DialogGadget(0, "button_state"), "Switch On")
			DisableGadget(DialogGadget(0, "button_state"), 0)
		ElseIf Shellies()\State = "on"
			SetGadgetText(DialogGadget(0, "button_state"), "Switch Off")
			DisableGadget(DialogGadget(0, "button_state"), 0)
		Else
			DisableGadget(DialogGadget(0, "button_state"), 1)
		EndIf
	Else
		DisableGadget(DialogGadget(0, "button_state"), 1)
	EndIf
EndProcedure

Runtime Procedure OnClick_ButtonState()
	Protected Payload.s
	If GetGadgetText(DialogGadget(0, "button_state")) = "Switch On"
		Payload = "on"
	Else
		Payload = "off"
	EndIf
	MQTT_CLIENT::PublishTopic(ClientID, "shellies/" + GetGadgetText(DialogGadget(0, "combo_shellies")) + "/relay/0/command", @Payload, Len(Payload))
	DisableGadget(DialogGadget(0, "button_state"), 1)
EndProcedure

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
				LogIT("Successfully connected to Broker!")
				;subscribe now to some shelly plug topics
				NewList Subs.MQTT_Common::Filter()
			
				AddElement(Subs())
				Subs()\Topic = "test/up" : Subs()\QoS   = 0
				MQTT_CLIENT::SubscribeToTopics(ClientID, Subs())
			Case MQTT_Common::#MQTTEvent_SubscriptionSuccessfull
				;unused
			Case MQTT_Common::#MQTTEvent_PublishIncoming
			  Debug Topic
			  Debug Payload
			Case MQTT_Common::#MQTTEvent_PublishingSuccessfull
				LogIT("Publishing accepted!")
			Case MQTT_Common::#MQTTEvent_Error
				LogIT(ErrorText)
		EndSelect
	EndIf
EndProcedure



Procedure main()
	Protected a$, Conf.MQTT_CLIENT::CLIENT_INIT
	a$ = GetXMLString()
	If ParseXML(0, a$) And XMLStatus(0) = #PB_XML_Success
		CreateDialog(0)
		OpenXMLDialog(0, 0, "window_main")
		BindEvent(#MQTTEvent, @MQTT_EventIncoming(), DialogWindow(0))
		
		;Config Client
		Conf\BrokerURL        = "192.168.0.5"
		Conf\Username         = "Stan"
		Conf\Password         = "rotweiss"
		Conf\ClientIdentifier = "PBClient_{" + RSet(Str(Random(999999, 101)), 6, "0") + "}"
		Conf\Window           = DialogWindow(0)
		Conf\WindowEvent      = #MQTTEvent
		
		ClientID = MQTT_CLIENT::InitClient(@Conf, #True)
		If ClientID
			Repeat : Until WaitWindowEvent() = #PB_Event_CloseWindow
			MQTT_CLIENT::DeInitClient(ClientID)
		EndIf
	EndIf
		
EndProcedure

main()
	
; IDE Options = PureBasic 6.00 LTS (Windows - x64)
; CursorPosition = 161
; FirstLine = 80
; Folding = y-
; EnableThread
; EnableXP
; EnableUser
; CompileSourceDirectory
; EnableCompileCount = 86
; EnableBuildCount = 0
; EnableExeConstant