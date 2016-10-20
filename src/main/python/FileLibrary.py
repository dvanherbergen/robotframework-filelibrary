import FileLibrary_Keywords
import json
import os
import time
import inspect
import uuid

from robot.libraries.BuiltIn import BuiltIn
from robot.utils.normalizing import NormalizedDict
from robot.libraries.Remote import Remote
from robot.libraries.Process import Process
from robot.api import logger

class FileLibrary:
	
	ROBOT_LIBRARY_SCOPE = 'GLOBAL'
	remoteLib = None
	remoteURL = 'http://127.0.0.1:'
	driverPath = ''
	PROCESS = None
	KEYWORDS = ['get_file_library_server_pid' , 'get_random_UUID']

	def __init__(self, path='', debug=False):
		self.driverPath = path
		self.debug = debug
		self.PROCESS = Process()
		
	def _initialize_remote_library(self):
		print 'starting file libary...'
		debugArg = '-agentlib:jdwp=transport=dt_socket,address=8001,server=y,suspend=n'
		classPath = self.driverPath + os.pathsep + os.environ['PYTHONPATH']
		mainClass = 'org.robotframework.filelibrary.remote.RPCServer'
		pidUUID = str(uuid.uuid4())
		if self.debug:
			print("starting process ", 'java', debugArg, "-cp", classPath, mainClass, pidUUID) 
			self.PROCESS.start_process('java', debugArg, "-cp", classPath, mainClass, pidUUID, shell=True, cwd='', alias='rpcServer')
		else:
			print("starting process ", 'java', "-cp", classPath, mainClass, pidUUID) 
			self.PROCESS.start_process('java', "-cp", classPath, mainClass, pidUUID, shell=True, cwd='', alias='rpcServer')
		i = 0
# Wait till file is created max wait = 10 seconds
		while not os.path.exists(pidUUID+'.pid') and i < 200:
			time.sleep(.05)
			i += 1
# Wait some more to make sure the port is written to the file 
		time.sleep(.1)
		pid_file = open(pidUUID+'.pid', "r")
		port = pid_file.read()
		print("receiving port from file library port= " + port + " waited " + str(i*0.05)  +" s") 
		pid_file.close()
		os.remove(pidUUID+'.pid')
		
		self.remoteLib = Remote(self.remoteURL + port)

	def get_robot_variables(self):
		values = []
		for name, value in BuiltIn().get_variables(no_decoration=True).items():
			item = name, value
			if not isinstance(value, NormalizedDict):
				values.append(item)
		return json.dumps(dict(values), ensure_ascii=False)
		
	def get_keyword_names(self):
	 	return FileLibrary.KEYWORDS + [kw for kw in FileLibrary_Keywords.keywords]
		
	def get_file_library_server_pid(self):
		if self.PROCESS and self.remoteLib:
			return self.PROCESS.get_process_id('rpcServer')
 		return None

	def get_random_UUID(self):
		return str(uuid.uuid4())

    	
	def get_keyword_arguments(self, name):
		if name in FileLibrary.KEYWORDS:
			return self._get_args(name)
		return FileLibrary_Keywords.keyword_arguments[name]

	def _get_args(self, method_name):
		spec = inspect.getargspec(getattr(self, method_name))
		args = spec[0][1:]
		if spec[3]:
			for i, item in enumerate(reversed(spec[3])):
				args[-i-1] = args[-i-1]+'='+str(item)
		if spec[1]:
			args += ['*'+spec[1]]
		if spec[2]:
			args += ['**'+spec[2]]
		return args

	def get_keyword_documentation(self, name):
		if name == '__init__':
			return FileLibrary_Keywords.keyword_documentation['__intro__']
		if name in FileLibrary.KEYWORDS:
			return getattr(self, name).__doc__
		return FileLibrary_Keywords.keyword_documentation[name]

	def run_keyword(self, name, arguments, kwargs):
		if ( name == 'stopFileLibraryProcess' or  name == 'disconnect' ) and  self.remoteLib == None:
			return
		if name in FileLibrary.KEYWORDS:
			return getattr(self, name)(*arguments, **kwargs)
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
