#!/usr/bin/python

import bluetooth
from datetime import datetime

server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
port = 1
server_sock.bind(("", port))
server_sock.listen(0)

while True:
    client_sock,address = server_sock.accept()
    data = client_sock.recv(1024)
    print datetime.now(), "-", data
    client_sock.close()

server_sock.close()
