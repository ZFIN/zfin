#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Java Server staging subdirectory
#
# $Source$
# $Id$
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# THIS MAKEFILE SHOULD NEVER BE INVOKED DIRECTLY, ONLY RECURSIVELY THROUGH
# ITS PARENT MAKEFILE.
#
# This staging subdirectory is used by the parent directory to build the
# java server after all of the C & Java source files have been made specific.
# The parent directory contains the generic versions of the file. The
# parent directory's makefile produces the specific versions of all files,
# puts them in this directory and then invokes this makefile.
# 
# This makefile uses makefile variables inherited from the parent.
#
# THE ONLY FILE IN THIS DIRECTORY THAT SHOULD EVER BE EDITED IS THIS 
# MAKEFILE.
#

# First check if we are being called recursively.

ifeq (0, $(MAKELEVEL))
$(error ERROR: THIS MAKEFILE CANNOT BE INVOKED DIRECTLY.  INVOKE IT FROM ITS PARENT MAKEFILE.)
endif

# If you got the above error. cd up a directory and remake from that 
# directory.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include


# ---- Variables inherited from parent makefile
#
# JAVASRCS, MICSRCS, TARGETDIR


# ---- Java variables

# NOTE:  The version of Java used in this Makefile must be the same
#	as the version invoked in the shell script that starts the
#	Java server.  This is because we are building C functions that
#	link directly into the JVM, and so the version of the Java
#	support libraries we compile with must match the version
#	of the JVM this code will be linked into at runtime.

JAVAHOME  = /local/apps/jdk-1.1
CLASSLIB  = $(JAVAHOME)/lib/classes.zip
JAVABIN   = $(JAVAHOME)/bin
JAVAC     = $(JAVABIN)/javac
JAVAH     = $(JAVABIN)/javah
JAVAFLAGS = -g -classpath .:$(CLASSLIB)


# ---- C Variables

CC = cc
CINCLUDES = -I $(JAVAHOME)/include -I $(JAVAHOME)/include/solaris



# ---- Informix variables

#  Get INFORMIXDIR from environment 

MIINCDIR   = $(INFORMIXDIR)/incl/dmi
MILIBDIR = $(INFORMIXDIR)/lib/dmi
MICFLAGS = -DPLATFORM=solaris -g -DELF -v -DPOSIX_SIGNALS -DSOLARIS -I $(MIINCDIR)

#  esql doesn't add this library to the list, so specify it explicitly
MILIBS = -L$(MILIBDIR) -lifdmi



# ---- Java source and class file variables

# Java related Makefile variables inherited from parent:
#  JAVASRCS

JAVACLASSES = $(foreach JVS, $(JAVASRCS), $(patsubst %.java,%.class,$(JVS)))
JAVATARGETS = $(foreach JVC, $(JAVACLASSES), $(TARGETDIR)/$(JVC))



# ---- C source, object, and library variables

LIBTARGET = $(TARGETDIR)/libMISQL.so

# Currently 3 types of C files: 
# 1. .h files produced by the javah processor which do not need the MI defines
# 2. .c files produced by the javah processor which do not need the MI defines
# 3. Preexisting C files that do noeed the MI defines.  This list is inherited from
#    the parent makefile in the MICSRCS variable.

JAVACHDRS = MIConnection.h Statement.h ResultSet.h
JAVACSRCS = MIConnection.c Statement.c ResultSet.c
JAVACOBJS = $(foreach JC, $(JAVACSRCS), $(patsubst %.c,%.o,$(JC)))

MICOBJS   = $(foreach MIC, $(MICSRCS), $(patsubst %.c,%.o,$(MIC)))
CSRCS     = $(JAVACSRCS) $(MICSRCS)
OBJS      = $(foreach CF, $(CSRCS), $(patsubst %.c,%.o,$(CF)))


# define target list

TARGETS = $(JAVATARGETS) $(LIBTARGET)


# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber depend $(SUBDIRS)

all : $(TARGETS)




# ----  Rules for creating Java classes.

# When any class file needs to be remade, remake all of them.

$(JAVATARGETS) : $(TARGETDIR)/%.class : %.class
	$(TARGET_CP) $^ $@

$(JAVACLASSES) : $(JAVASRCS)
	$(JAVAC) $(JAVAFLAGS) $(JAVASRCS)



# ----  Rules for creating shared object library

$(LIBTARGET) : $(JAVACHDRS) $(OBJS)
#	precc is a shell script that passes a -G option to cc (see precc for reason)
	INFORMIXC=../precc esql -o $(LIBTARGET) $(OBJECS) $(MILIBS)


# Make the .h files from the class files

$(JAVACHDRS) : %.h : %.class
	$(JAVAH) $*


# Make the .c files from the class files
$(JAVACSRCS) : %.c : %.class
	$(JAVAH) -stubs $*


# make the .o files from the .c files

$(JAVACOBJS) : %.o : %.c
	$(CC) -G $(CINCLUDES) -c $<

$(MICOBJS) : %.o : %.c
	$(CC) -G $(MICFLAGS) $(CINCLUDES) -c $<


# ---------------  Maintenance Rules  -----------------------------------

clean :			# remove almost everything in the directory
	rm -f $(JAVACLASSES) $(JAVACSRCS) $(JAVACHDRS) $(OBJS)


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)


sanitycheck :		# nothing to do, all done by parent.

depend : $(CSRCS)
	makedepend $(CINCLUDES) -I $(MIINCDIR) $(CSRCS)




