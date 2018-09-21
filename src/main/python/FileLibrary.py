import FileLibrary_Keywords
import json
import os
import sys
import time
import inspect
import uuid
import datetime
import tempfile

from robot.libraries.BuiltIn import BuiltIn
from robot.utils.normalizing import NormalizedDict
from robot.libraries.Remote import Remote
from robot.libraries.Process import Process
from robot.api import logger

class FileLibrary:
  
  ROBOT_LIBRARY_SCOPE = 'TEST SUITE'
  AGENT_PATH = os.path.abspath(os.path.dirname(__file__))
  KEYWORDS = ['get_file_library_server_pid' , 'get_random_UUID']
  

  def __init__(self, path='', debug=False):
    self.driverPath = path
    self.debug = debug
    self.remoteLib = None
    self.started = datetime.datetime.now()
    self.process = Process()  


  def __init_remote_process__(self):
    debugArg = '-agentlib:jdwp=transport=dt_socket,address=8001,server=y,suspend=n'
    jars = set(filter(lambda k: k.endswith('.jar'), sys.path))
    jars.add(self.driverPath)
    jars.add(FileLibrary.AGENT_PATH)
    jars.add(os.environ.get('CLASSPATH',''))
    jars.add(os.environ.get('PYTHONPATH',''))
    jars.add(os.environ.get('JYTHONPATH',''))
    jarSet = set(filter (None, jars ))
    classPath = os.pathsep.join(jars)
    mainClass = 'io.github.dvanherbergen.filelibrary.remote.RPCServer'
    pidFilename = os.path.join(tempfile.gettempdir(), (str(uuid.uuid4()) + '.pid'))
    logFile = os.path.join(tempfile.gettempdir(), 'filelibrary-' + (str(uuid.uuid4())) + '.log')
    print("Logging to " + logFile)
    if self.debug:
      print("starting process ", 'java', debugArg, "-cp", classPath, mainClass, pidFilename) 
      self.process.start_process('java', debugArg, "-cp", classPath, mainClass, pidFilename, shell=True, cwd='', alias='rpcServer', stdout=logFile, stderr=None)
    else:
      print("starting process ", 'java', "-cp", classPath, mainClass, pidFilename) 
      self.process.start_process('java', "-cp", classPath, mainClass, pidFilename, shell=True, cwd='', alias='rpcServer', stdout=logFile, stderr=None)
    i = 0
    # Wait till file is created max wait = 10 seconds
    while not os.path.exists(pidFilename) and i < 400:
      time.sleep(.05)
      i += 1
    # Wait some more to make sure the port is written to the file 
    time.sleep(.1)
    pid_file = open(pidFilename, "r")
    port = pid_file.read()
    print("Received file library port: " + port + ", waited " + str(i*0.05)  +" s")
    pid_file.close()
    os.remove(pidFilename)    
    self.remoteLib = Remote('http://127.0.0.1:' + port)


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
    if self.process and self.remoteLib:
      return self.process.get_process_id('rpcServer')
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
    if ( name == 'stop' or  name == 'disconnect' ) and  self.remoteLib == None:
      return
    if (self.remoteLib == None):
      self.__init_remote_process__()
    if name in FileLibrary.KEYWORDS:
      return getattr(self, name)(*arguments, **kwargs)
    if name == 'resetTemplateData':
      if (len(arguments) > 0 and arguments[0].lower() == 'true'):
        # inject all variables from the test case context
        self.remoteLib.run_keyword(name, (self.get_robot_variables(),), kwargs) 
      else:
        self.remoteLib.run_keyword(name, (), kwargs)
    else:
      return self.remoteLib.run_keyword(name, arguments, kwargs)

