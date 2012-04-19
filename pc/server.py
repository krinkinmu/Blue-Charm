import bluetooth
import pynotify
import gtk
import sys

if not pynotify.init("init"):
	sys.exit(1)

server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )

port = 10
server_sock.bind(("",port))
server_sock.listen(1)

print "before"
client_sock,address = server_sock.accept()
#print "after"

data = client_sock.recv(1024)
n = pynotify.Notification("New message", data)
n.show()
#print "received [%s]" % data

client_sock.close()
server_sock.close()
