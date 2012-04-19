import bluetooth


nearby_devices = bluetooth.discover_devices()

for bdaddr in nearby_devices:
        print bdaddr
        port=1
        sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
        try:
            sock.connect((bdaddr,port))                                     
            sock.send("hallo")
        except bluetooth.btcommon.BluetoothError:
            print "socket connect error"
                                                                                                
        sock.close() 
