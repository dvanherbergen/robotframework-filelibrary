import FileLibrary_Keywords

class FileLibrary:

	ROBOT_LIBRARY_SCOPE = 'GLOBAL'

	def get_keyword_names(self):
		print "getting all names"
		return FileLibrary_Keywords.keywords

	def get_keyword_arguments(self, name):
		print "getting args for " + name
		return FileLibrary_Keywords.keyword_arguments[name]

	def get_keyword_documentation(self, name):
		print "getting doc for " + name
		return FileLibrary_Keywords.keyword_documentation[name]

	def run_keyword(self, name, arguments, kwargs):
		print "Running keyword " + name