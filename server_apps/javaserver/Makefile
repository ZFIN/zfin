#
#  The macros defining the paths and compiler options
#
# NOTE:  The version of Java used in this Makefile must be the same
#	as the version invoked in the shell script that starts the
#	Java server.  This is because we are building C functions that
#	link directly into the JVM, and so the version of the Java
#	support libraries we compile with must match the version
#	of the JVM this code will be linked into at runtime.
JAVAHOME= /local/apps/jdk-1.1
BIN= $(JAVAHOME)/bin
CLASSLIB= $(JAVAHOME)/lib/classes.zip

JAVAC = $(BIN)/javac
JAVAH = $(BIN)/javah
JAR= $(BIN)/jar
TOSOURCE=/home/grads/ted/bin/toSource

JAVAFLAGS = -g -classpath .:$(CLASSLIB)

.SUFFIXES: .java .class

.java.class:
	$(JAVAC) $(JAVAFLAGS) $<

#  Get INFORMIXDIR from environment 
CC= cc

INCDIR = $(INFORMIXDIR)/incl/dmi
MILIBDIR = $(INFORMIXDIR)/lib/dmi

MICFLAGS = -DPLATFORM=solaris -g -DELF -v -DPOSIX_SIGNALS -DSOLARIS -I $(INCDIR)

#  esql doesn't add this library to the list, so specify it explicitly
MILIBS = -L$(MILIBDIR) -lifdmi

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
VERSION_MESSAGE= -m"Improved synchronization"
#VERSION_NAME=
VERSION_NAME= -NSynchronized
checkin:
	ci -zLT -l -tinitial.text $(VERSION_MESSAGE) $(VERSION_NAME) $(CONTROLFILES) `$(TOSOURCE) $(CLASSFILES)` `$(TOSOURCE) $(OBJECTFILES)`

#
#  The actual commands for creating the targets
#

libMISQL.so:	$(OBJECTFILES)
#	$(CC) -G $(OBJECTFILES) $(MILIBS) -o libMISQL.so
#	precc is a shell script that passes a -G option to cc (see precc for reason)
	INFORMIXC=./precc esql -o libMISQL.so $(OBJECTFILES) $(MILIBS)

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
