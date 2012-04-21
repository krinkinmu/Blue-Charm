#!/usr/bin/python

import bluetooth
import pynotify
import sys
import threading



class Server(threading.Thread):

	DELIMITER = '\3'
	MAGIC = "BLUECHARM"
	SMS = "IN_SMS"
	CALL = "IN_CALL"
	APP_NAME = "Blue Charm"
	PORT_NUMBER = 10

	def __init__(self, counter, tray):
		threading.Thread.__init__(self)
		self.counter = counter
		self.tray = tray
	

	def parseSms(self, data):
		if (len(data) < 4):
			return None
		return "SMS from: " + data[2] + "\n Text: " + data[3]

	def parseCall(self, data):
		if (len(data) < 3):
			return None
		return "Call from: " + data[2]
    
                                                        
	def info (self, data):
		data_splitted = data.split(self.DELIMITER)
		if (len(data_splitted) >= 3 and data_splitted[0] == self.MAGIC):
			typeM = data_splitted[1]

			self.counter.acquire()
			self.counter.inc()
			self.counter.release()

			self.tray.setMessageRecievedImage()

			if (typeM == self.SMS):
				return "SMS", self.parseSms(data_splitted)
			elif (typeM == self.CALL):
				return "Call", self.parseCall(data_splitted) 

		else:
			return None, None
         

	def run(self):
		server_sock=bluetooth.BluetoothSocket(bluetooth.RFCOMM)

		port = self.PORT_NUMBER
		server_sock.bind(("",port))
		server_sock.listen(1)
		pynotify.init(self.APP_NAME)
		while ( True ) :
			print "wait"
			client_sock,address = server_sock.accept()
			print "recieve"

			data = client_sock.recv(1024)

			typeM,contentM = self.info(data)
			if (typeM != None and contentM != None):
				n = pynotify.Notification(typeM, contentM)
				n.show()
								

			client_sock.close()

		server_sock.close()
