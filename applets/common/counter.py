import threading

class Counter(object):
	counter = 0

	def __init__(self):
		self.lock = threading.Lock()

	def acquire(self):
		self.lock.acquire()

	def release(self):
		self.lock.release()

	def inc(self):
		self.counter += 1

	def null(self):
		self.counter = 0

	def value(self):
		return self.counter
