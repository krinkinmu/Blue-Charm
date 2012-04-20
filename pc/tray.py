#!/usr/bin/python

import gtk
import sys
import pygtk
import egg.trayicon
import threading
import counter


class Tray(threading.Thread):

	

	def __init__(self, counter):
		threading.Thread.__init__(self)
		self.counter = counter

	def changeImage(self, string):
		self.tray.remove(self.box)
		self.box.remove(self.label)
		self.label = gtk.Label(string)
		self.box.add(self.label)
		self.tray.add(self.box)
		self.tray.show_all()


	def dropCounter(self):
		self.counter.null()
		self.changeImage("BlueCharm")

	def setMessageRecievedImage(self):
		self.changeImage("AAAAAAAA");
	
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
		self.tray = egg.trayicon.TrayIcon("Tray")
		self.box = gtk.EventBox()
		self.label = gtk.Label("BlueCharm")
		self.box.add(self.label)
		self.tray.add(self.box)
		self.box.connect('button-press-event', self.tray_icon_callback)
		self.tray.show_all()

		gtk.threads_enter()
		gtk.main()
		gtk.threads_leave()

