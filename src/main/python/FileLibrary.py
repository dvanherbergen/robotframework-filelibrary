import FileLibrary_Keywords
import json
import os
import time

from robot.libraries.BuiltIn import BuiltIn
from robot.utils.normalizing import NormalizedDict
from robot.libraries.Remote import Remote

class FileLibrary:
	
	ROBOT_LIBRARY_SCOPE = 'GLOBAL'
	remoteLib = None
	remoteURL = 'http://127.0.0.1: 9889'

	def _initialize_remote_library(self):
		# TODO remove hardcoded path...
		os.startfile('C:/robot-tests/resources/lib/start-filelibrary.bat')
		time.sleep(1)
		self.remoteLib = Remote(self.remoteURL)

	def get_robot_variables(self):
		values = []
		for name, value in BuiltIn().get_variables(no_decoration=True).items():
			item = name, value
			if not isinstance(value, NormalizedDict):
				values.append(item)
		return json.dumps(dict(values), ensure_ascii=False)
		
	def get_keyword_names(self):
		return FileLibrary_Keywords.keywords

	def get_keyword_arguments(self, name):
		return FileLibrary_Keywords.keyword_arguments[name]

	def get_keyword_documentation(self, name):
		return FileLibrary_Keywords.keyword_documentation[name]

	def run_keyword(self, name, arguments, kwargs):
		if self.remoteLib == None:
			self._initialize_remote_library()
		if name == 'resetTemplateData':
			if (len(arguments) > 0 and arguments[0].lower() == 'true'):
				# inject all variables from the test case context
				self.remoteLib.run_keyword(name, (self.get_robot_variables(),), kwargs)	
			else:
				self.remoteLib.run_keyword(name, (), kwargs)			
		else:
			return self.remoteLib.run_keyword(name, arguments, kwargs)
