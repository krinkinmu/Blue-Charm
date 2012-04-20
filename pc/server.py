#!/usr/bin/python

import bluetooth
import pynotify
import sys
import threading



class Server(threading.Thread):

	splitter = '\3'
	magic = "BLUECHARM"
	sms = "IN_SMS"
	call = "IN_CALL"

	def __init__(self, counter, tray):
		threading.Thread.__init__(self)
		self.counter = counter
		self.tray = tray
	

	def parseSms(self, data ) :
		return "Sender: " +  data[3] \
	            + "\n Phone: " +  data[2] + " number of messages: \n" +\
        	    "\n"

	def parseCall(self, data ) :
	     return " Phone: " +  data[2] + "\n is calling \n"
    
                                                        
	def info (self, data )  :
	    print data
	    data_splitted = data.split( self.splitter )
	    print data_splitted
	    if ( data_splitted[0] == self.magic ) :
        	typeM = data_splitted[1]
		self.counter.acquire()
		self.counter.inc()
		self.counter.release()
		self.tray.setMessageRecievedImage()
	        if ( typeM == self.sms ) :
        	    return "SMS", self.parseSms( data_splitted )   
    
	        elif ( typeM == self.call ) :
        	    return "Call", self.parseCall( data_splitted ) 

	    else :
        	return None, None
         
	def callback_function(notification=None, action=None, data=None):
         	print "It worked!"



	def run(self):
                                                            
		server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )

		port = 10
		server_sock.bind(("",port))
		server_sock.listen(1)

		while ( True ) :
		    print "wait"
		    client_sock,address = server_sock.accept()
		    print "recieve"

		    data = client_sock.recv(1024)

		    typeM,contentM = self.info( data )

    
		    n = pynotify.Notification( typeM, contentM )
		    n.show()                                                            

		    client_sock.close()

		server_sock.close()
