#
#  The macros defining the paths and compiler options
#
BIN= /local/apps/jdk/bin
CLASSLIB= /local/apps/jdk/lib/classes.zip
JAVAHOME= /local/apps/jdk

JAVAC = $(BIN)/javac
JAVAH = $(BIN)/javah
JAR= $(BIN)/jar
TOSOURCE=/home/grads/ted/bin/toSource

JAVAFLAGS = -g -classpath .:$(CLASSLIB)

.SUFFIXES: .java .class

.java.class:
	$(JAVAC) $(JAVAFLAGS) $<

MI_HOME= /export/fish/Illustra
CC= /local/bin/cc

INCDIR = $(MI_HOME)/h
LIBDIR = $(MI_HOME)/lib 

MICFLAGS = -DPLATFORM=solaris -g -DELF -v -DPOSIX_SIGNALS -DSOLARIS -I $(INCDIR)
MILIBS = -ldl -lsocket -lnsl -L$(LIBDIR) -lmi

#
#  The lists of files
#

CONTROLFILES = Makefile enddef S90javaserver
CLASSFILES = Server.class MIConnection.class Statement.class ResultSet.class SQLException.class
OBJECTFILES = MIConnection.o Statement.o ResultSet.o MIConnectionImp.o

#
#  The targets, which define the various things we can do with this Makefile
#

# Build the server
Server:	$(CLASSFILES) libMISQL.so

# Clean out all object files, forcing a rebuild
clean:
	\rm $(CLASSFILES)
	\rm $(OBJECTFILES)

#  Checkin a new version to RCS
#   -tfile:  file contains initial text (if new file) -mstring:  message string, -nname:  name of version
#   -zLT:  use the local time when substituting time for keywords
#VERSION_MESSAGE=
VERSION_MESSAGE= -m"Initial version"
#VERSION_NAME=
VERSION_NAME= -NInitial
checkin:
	ci -zLT -l -tinitial.text $(VERSION_MESSAGE) $(VERSION_NAME) $(CONTROLFILES) `$(TOSOURCE) $(CLASSFILES)` `$(TOSOURCE) $(OBJFILES)`

#
#  The actual commands for creating the targets
#

libMISQL.so:	enddef $(OBJECTFILES)
	$(CC) -G -M enddef $(OBJECTFILES) $(MILIBS) -o libMISQL.so

MIConnection.h:	MIConnection.class
	$(JAVAH) MIConnection

MIConnection.c:	MIConnection.class
	$(JAVAH) -stubs MIConnection

MIConnection.o:	MIConnection.c MIConnection.h
	$(CC) -G -I $(JAVAHOME)/include -I $(JAVAHOME)/include/solaris -c MIConnection.c 

Statement.h:	Statement.class
	$(JAVAH) Statement

Statement.c:	Statement.class
	$(JAVAH) -stubs Statement

Statement.o:	Statement.c Statement.h
	$(CC) -G -I $(JAVAHOME)/include -I $(JAVAHOME)/include/solaris -c Statement.c 

ResultSet.h:	ResultSet.class
	$(JAVAH) ResultSet

ResultSet.c: ResultSet.class
	$(JAVAH) -stubs ResultSet

ResultSet.o:	ResultSet.c ResultSet.h
	$(CC) -G -I $(JAVAHOME)/include -I $(JAVAHOME)/include/solaris -c ResultSet.c 

MIConnectionImp.o:	MIConnectionImp.c MIConnection.h Statement.h ResultSet.h
	$(CC) -G $(MICFLAGS) -I $(JAVAHOME)/include -I $(JAVAHOME)/include/solaris -c MIConnectionImp.c 
