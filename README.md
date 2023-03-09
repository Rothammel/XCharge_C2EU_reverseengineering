I have a wallbox from XCharge called C2EU. It's incredibly cheap and nice to look at.

Unfortunately, there is no information about the protocol for controlling it. OCPP does not work properly. It is probably not implemented accurately.

In a few night shifts I reverse-engineered the APK of the wallbox. I found out that the protocol with the name xfamiliy is only MQTT via Websocket.

Therefore, the address of the own MQTT broker must look like this:
ws://192.168.0.1:8084

The wallbox then sends messages and asks for the QR code and the settings. Node Red replies to the wallbox and forwards the status of the wallbox to Home Assistant.

Have fun - friends of electromobility
