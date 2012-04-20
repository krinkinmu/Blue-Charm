#!/usr/bin/python

import gtk
import sys
import threading
import server
import tray
import counter
 

gtk.gdk.threads_init()

counter = counter.Counter()

tray = tray.Tray(counter)
tray.start()

server = server.Server(counter, tray)
server.setDaemon(True)
server.start()


