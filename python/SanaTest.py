import json, requests, os
import tempfile
from CytoSANA.CyCaller import CyCaller
from CytoSANA.CyRESTInstance import CyRESTInstance
import numpy as np

class CytoSANA:

	def __init__(self, cy_rest_instance=None):
		""" Constructor remembers CyREST location """
		self._cy_caller = CyCaller(cy_rest_instance)

	def load_session(self, f):
		self._cy_caller.execute_get("/v1/session", {"file": f})

	def align(self, data):
		self._cy_caller.execute_post("/sana/v1/align", json.dumps(data))
		
	def visualize(self, data):
		self._cy_caller.execute_post("/sana/v1/visualize", json.dumps(data))

def main():
	sana = CytoSANA()
	f = os.path.join(os.getcwd(), "doubleYeast.cys")
	print(f)
	sana.load_session(f)
	sana.align()

if __name__ == '__main__':
	main()