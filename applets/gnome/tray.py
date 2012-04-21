#!/usr/bin/python

import gtk
import sys
import pygtk
import threading
import counter


class Tray(threading.Thread):


	def __init__(self, counter):
		threading.Thread.__init__(self)
		self.counter = counter

		path = "/home/sergey/softpract/Blue-Charm/applets/rc/"
		self.noCallsImageFile = path + "no_calls.png"
		self.missedCallsImageFile = path + "missed_call.png"
		print self.noCallsImageFile
		print self.missedCallsImageFile

		
		self.tray = gtk.StatusIcon()
		self.tray.connect('button-press-event', self.tray_icon_callback)
		self.changeImage(self.noCallsImageFile)

        
	def changeImage(self, string):
		self.tray.set_from_file(string)


	def dropCounter(self):
		self.counter.null()
		self.changeImage(self.noCallsImageFile)

	def setMessageRecievedImage(self):
		self.changeImage(self.missedCallsImageFile);
	
	def openMenu(self):
		menu = gtk.Menu()
		quit = gtk.MenuItem("exit")
		quit.connect('activate', gtk.main_quit)
		quit.show()

		menu.append(quit)
		menu.popup(None, None, None, 0, 0)


	def tray_icon_callback(self, widget, event):
		self.counter.acquire()
		if (self.counter.value() != 0):
			self.dropCounter()
		else:
			self.openMenu()
		self.counter.release()

	

	def run(self):
	        gtk.threads_enter()
        	gtk.main()
	        gtk.threads_leave()

