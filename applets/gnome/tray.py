#!/usr/bin/python

import gtk
import sys
import pygtk
import threading
import counter


class Tray(threading.Thread):

	__path = "../rc/"
	__noCallsImageFile = __path + "no_calls.png"
	__missedCallsImageFile = __path + "missed_call.png"
	__readImageFile = __path + "checkbox.png"
	__quitImageFile = __path + "exit.png"

	__tray = gtk.StatusIcon()
	__menu = gtk.Menu()


	def __init__(self, counter):
		threading.Thread.__init__(self)
		self.__counter = counter

		self.__tray.connect('button-press-event', self.__openMenu)

		quitImage = gtk.Image()
		quitImage.set_from_file(self.__quitImageFile)
		quitImage.show()

		quitMenuItem = gtk.ImageMenuItem("exit")
		quitMenuItem.set_image(quitImage)
		quitMenuItem.set_always_show_image(True)
		quitMenuItem.connect('activate', gtk.main_quit)
		quitMenuItem.show()

		readImage = gtk.Image()
		readImage.set_from_file(self.__readImageFile)
		readImage.show()

		self.__readMenuItem = gtk.ImageMenuItem("Read")
		self.__readMenuItem.set_image(readImage)
		self.__readMenuItem.set_always_show_image(True)
		self.__readMenuItem.connect('activate', self.__readMenuItemCallback)
		self.__readMenuItem.show()

		self.__menu.append(self.__readMenuItem)
		self.__menu.append(quitMenuItem)

		self.__dropCounter()


	def __readMenuItemCallback(self, event):
		self.__dropCounter()

	def __dropCounter(self):
		self.__counter.null()
		self.__readMenuItem.set_sensitive(False)
		self.__tray.set_from_file(self.__noCallsImageFile)


	def setMessageRecievedImage(self):
		self.__readMenuItem.set_sensitive(True)
		self.__tray.set_from_file(self.__missedCallsImageFile)


	def __openMenu(self, widget, event):
		self.__menu.popup(None, None, None, 0, 0)


	def run(self):
	        gtk.threads_enter()
        	gtk.main()
	        gtk.threads_leave()

