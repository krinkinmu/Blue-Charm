#!/usr/bin/python

import gtk
import sys
import pygtk
import threading
import counter


class Tray(threading.Thread):

	__path = "/home/sergey/softpract/Blue-Charm/applets/rc/"
	__noCallsImageFile = __path + "no_calls.png"
	__missedCallsImageFile = __path + "missed_call.png"
	__quitImageFile = __path + "exit.png"

	__tray = gtk.StatusIcon()
	__menu = gtk.Menu()


	def __init__(self, counter):
		threading.Thread.__init__(self)
		self.__counter = counter

		self.__tray.connect('button-press-event', self.__tray_icon_callback)
		self.__dropCounter()

		quitImage = gtk.Image()
		quitImage.set_from_file(self.__quitImageFile)
		quitImage.show()

		quitMenuItem = gtk.ImageMenuItem("exit")
		quitMenuItem.set_image(quitImage)
		quitMenuItem.set_always_show_image(True)
		quitMenuItem.connect('activate', gtk.main_quit)
		quitMenuItem.show()

		self.__menu.append(quitMenuItem)


	def __dropCounter(self):
		self.__counter.null()
		self.__tray.set_from_file(self.__noCallsImageFile)


	def setMessageRecievedImage(self):
		self.__tray.set_from_file(self.__missedCallsImageFile)


	def __openMenu(self):
		self.__menu.popup(None, None, None, 0, 0)


	def __tray_icon_callback(self, widget, event):
		self.__counter.acquire()
		if (self.__counter.value() != 0):
			self.__dropCounter()
		else:
			self.__openMenu()
		self.__counter.release()


	def run(self):
	        gtk.threads_enter()
        	gtk.main()
	        gtk.threads_leave()

