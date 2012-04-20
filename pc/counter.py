import threading

class Counter(object):
	counter = 0

	def __init__(self):
		self.lock = threading.Lock()

	def acquire(self):
		print "lock"
		self.lock.acquire()

	def release(self):
		print "unlock"
		self.lock.release()

	def inc(self):
		print "increase"
		self.counter += 1

	def null(self):
		print "null"
		self.counter = 0

	def value(self):
		print self.counter
		return self.counter
