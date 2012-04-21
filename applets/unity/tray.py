#!/usr/bin/python

import gtk
import sys
import pygtk
import threading
import counter
import appindicator


class Tray(threading.Thread):

	__path = "/home/sergey/softpract/Blue-Charm/applets/rc/"
	__noCallsImageFile = __path + "no_calls.png"
	__missedCallsImageFile = __path + "missed_call.png"
	__readImageFile = __path + "checkbox.png"
	__quitImageFile = __path + "exit.png"

	__tray = appindicator.Indicator("bluecharm", "indicator",
				appindicator.CATEGORY_APPLICATION_STATUS)


	def __init__(self, counter):
		threading.Thread.__init__(self)
		self.__counter = counter

		menu = gtk.Menu()

		readImage = gtk.Image()
		readImage.set_from_file(self.__readImageFile)
		readImage.show()

		self.readMenuItem = gtk.ImageMenuItem("Read")
		self.readMenuItem.set_image(readImage)
		self.readMenuItem.set_always_show_image(True)
		self.readMenuItem.connect('activate', self.__readMenuItemCallback)
		self.readMenuItem.show()
		menu.append(self.readMenuItem)

		quitImage = gtk.Image()
		quitImage.set_from_file(self.__quitImageFile)
		quitImage.show()

		quitMenuItem = gtk.ImageMenuItem("Quit")
		quitMenuItem.set_image(quitImage)
		quitMenuItem.set_always_show_image(True)
		quitMenuItem.connect('activate', gtk.main_quit)
		quitMenuItem.show()
		menu.append(quitMenuItem)

		self.__tray.set_menu(menu)
		self.__tray.set_icon(self.__noCallsImageFile)
		self.__tray.set_attention_icon(self.__missedCallsImageFile)
		
		self.__dropCounter()

	
	def __readMenuItemCallback(self, event):
		self.__dropCounter()

        
	def __dropCounter(self):
		self.__counter.null()
		self.readMenuItem.set_sensitive(False)
		self.__tray.set_status(appindicator.STATUS_ACTIVE)


	def setMessageRecievedImage(self):
		self.readMenuItem.set_sensitive(True)
		self.__tray.set_status(appindicator.STATUS_ATTENTION)
	

	def run(self):		
	        gtk.threads_enter()
        	gtk.main()
	        gtk.threads_leave()

