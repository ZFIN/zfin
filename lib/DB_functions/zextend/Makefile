# Makefile to produce the production zextend.so function library.
#
# This makefile does NOT place the zextend.so file in the location
# where the server picks it up (in /research/zfin/chromix/private/lib/,
# as of 2000/06).  The only safe way we have found to replace that file
# is to:
#  1. Bring the server down.
#  2. Copy the new shared object file to /research/zfin/chromix/private/lib/
#  3. Bring the server back up to on-line mode.
#  4. Immediately run "dbaccess - ../../dataport/reload_zex_functions.sql"
#
# $Id$
# $Source$

#
#  The macros defining the paths and compiler options
#
INFORMIXDIR=/export/chromix/private/apps/Informix_A
CC= cc

INCDIR = $(INFORMIXDIR)/incl/dmi
LIBDIR = $(INFORMIXDIR)/lib 

MICFLAGS = -DPLATFORM=solaris -g -DELF -v -DPOSIX_SIGNALS -DSOLARIS -I $(INCDIR)
#MILIBS = -ldl -lsocket -lnsl -L$(LIBDIR) -lmi
MILIBS = -lsocket -lnsl -L$(LIBDIR)

#
#  The lists of files
#

SOURCEFILES = Makefile lower.c
OBJECTFILES = zextend.o

#
#  The targets, which define the various things we can do with this Makefile
#

.c.o:
	$(CC) -I $(INCDIR) $(MICFLAGS) -c $<

#.o.so:
#	$(CC) -G  $(OBJECTFILES) -o $<

all :	zextend.o md5.o zextend.so

zextend.o:	zextend.c
md5.o:		md5.c

# Clean out all object files, forcing a rebuild
clean:
	\rm -f $(OBJECTFILES) zextend.so

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

zextend.so:	$(OBJECTFILES)
	$(CC) -G $(OBJECTFILES) -o zextend.so

