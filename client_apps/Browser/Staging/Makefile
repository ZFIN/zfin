#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Anatomical dictionary browser
# applet staging subdirectory
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
# anatomical dictionary browser applet after all of the Java source files 
# have been made specific.
# 
# The parent directory contains the generic versions of the file. The
# parent directory's makefile produces the specific versions of all files,
# puts them in this directory and then invokes this makefile.
# 
# This makefile uses some makefile variables inherited from the parent.
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

TARGETDIR = $(TARGETROOT)/client_apps/Browser

# ---- Java variables

# Use the standard JAVAC_11, and JAVACFLAGS.  Still need to define classpath.

CLASSPATH = .:$(JAVAHOME_11)/lib/classes.zip:$(TOP)/lib/Java

# ---- Java source and class file variables

# Java related Makefile variables inherited from parent:
#  JAVAS JAR IMPORTPATH IMPORTDIR

CLASSES = $(foreach JVS, $(JAVAS), $(patsubst %.java,%.class,$(JVS)))

TARGETJAR = $(TARGETDIR)/$(JAR)

# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber

all : $(TARGETDIR) $(TARGETJAR)

$(TARGETDIR) :
	$(TARGET_MKDIR) $@

$(IMPORTDIR) :
	ln -s $(IMPORTPATH)/$(IMPORTDIR)

# ----  Rules for creating Java classes.

$(TARGETJAR) : $(JAR)
	$(TARGET_CP) $(JAR) $@
	$(TOUCH) $@
	chmod o+r $@

$(JAR) : $(CLASSES) $(IMPORTDIR)
	$(JAVAHOME_11)/bin/jar -cvf $(JAR) $(CLASSES) $(IMPORTDIR)

# When any class file needs to be remade, remake all of them.

$(CLASSES) : $(JAVAS)
	$(JAVAC_11) $(JAVACFLAGS) -classpath $(CLASSPATH) $(JAVAS)



# ---------------  Maintenance Rules  -----------------------------------

clean :			# remove almost everything in the directory
	rm -f $(JAVAS) $(CLASSES) $(JAR) $(IMPORTDIR)


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETJAR)


sanitycheck :		# nothing to do, all done by parent.

