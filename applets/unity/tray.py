#!/usr/bin/python

import gtk
import sys
import pygtk
import threading
import counter
import appindicator


class Tray(threading.Thread):

	path = "/home/sergey/softpract/Blue-Charm/applets/rc/"
	noCallsImageFile = path + "no_calls.png"
	missedCallsImageFile = path + "missed_call.png"
	readImageFile = path + "checkbox.png"
	quitImageFile = path + "exit.png"

	def __init__(self, counter):
		threading.Thread.__init__(self)
		self.counter = counter

		self.tray = appindicator.Indicator("bluecharm", "indicator", appindicator.CATEGORY_APPLICATION_STATUS)
		menu = gtk.Menu()

		readImage = gtk.Image()
		readImage.set_from_file(self.readImageFile)
		readImage.show()

		self.readMenuItem = gtk.ImageMenuItem("Read")
		self.readMenuItem.set_image(readImage)
		self.readMenuItem.set_always_show_image(True)
		self.readMenuItem.connect('activate', self.dropCounter)
		self.readMenuItem.show()
		menu.append(self.readMenuItem)

		quitImage = gtk.Image()
		quitImage.set_from_file(self.quitImageFile)
		quitImage.show()

		quitMenuItem = gtk.ImageMenuItem("Quit")
		quitMenuItem.set_image(quitImage)
		quitMenuItem.set_always_show_image(True)
		quitMenuItem.connect('activate', gtk.main_quit)
		quitMenuItem.show()
		menu.append(quitMenuItem)

		self.tray.set_menu(menu)
		self.tray.set_icon(self.noCallsImageFile)
		self.tray.set_attention_icon(self.missedCallsImageFile)
		
		self.dropCounter(None)

        
	def changeImage(self, string):
		self.tray.set_from_file(string)


	def dropCounter(self, event):
		self.counter.null()
		self.readMenuItem.set_sensitive(False)
		self.tray.set_status(appindicator.STATUS_ACTIVE)

	def setMessageRecievedImage(self):
		self.readMenuItem.set_sensitive(True)
		self.tray.set_status(appindicator.STATUS_ATTENTION)
	

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

