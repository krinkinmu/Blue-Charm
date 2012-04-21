#!/usr/bin/python

import gtk
import sys
import pygtk
import threading
import counter
import appindicator


class Tray(threading.Thread):

	noCallsImageFile = "/home/sergey/softpract/Blue-Charm/rc/no_calls.png"
	missedCallsImageFile = "/home/sergey/softpract/Blue-Charm/rc/missed_call.png"

	def __init__(self, counter):
		threading.Thread.__init__(self)
		self.counter = counter
        
	def changeImage(self, string):
		self.tray.set_from_file(string)


	def dropCounter(self):
		self.counter.null()
		self.tray.set_status(appindicator.STATUS_ACTIVE)

	def setMessageRecievedImage(self):
		self.tray.set_status(appindicator.STATUS_ATTENTION)
	
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
		self.tray = appindicator.Indicator("bluecharm", "indicator", appindicator.CATEGORY_APPLICATION_STATUS)
		menu = gtk.Menu()

		readMenuItem = gtk.MenuItem("Read")
		readMenuItem.connect('activate', self.dropCounter)
		readMenuItem.show()
		menu.append(readMenuItem)

		quitMenuItem = gtk.MenuItem("Quit")
		quitMenuItem.connect('activate', gtk.main_quit)
		quitMenuItem.show()
		menu.append(quitMenuItem)

		self.tray.set_menu(menu)
		self.tray.set_status(appindicator.STATUS_ACTIVE)
		self.tray.set_icon(self.noCallsImageFile)
		self.tray.set_attention_icon(self.missedCallsImageFile)
#		self.tray.set_icon("indicator-messages-new")

	        gtk.threads_enter()
        	gtk.main()
	        gtk.threads_leave()

