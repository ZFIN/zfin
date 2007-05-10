#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Java Lib directories
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
# This makefile populates the /lib/java directories in a rather wholesale
# fashion.  First it scans the tree below it for directories, removes 
# certain directories from the list, creates the remaining directories in 
# the specific tree, and then copies over all class files to the specific tree.
#
# There are lots of subdirectories below this one, but none of them 
# contains a Makefile.  All makefile work is done by this makefile.
#
# The class files created by this makefile are not directly accessable
# via the web.  They are either used by locally running java code or
# are bundled in JAR files before they are sent out.

# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/lib/Java


# ---- Generate list of directories to create.

# Directories to exclude.  These dirs don't have class files in them
# and/or below them so don't create them in the target directory.

EXCLUDEDIRS = . CVS CVS/% %/CVS %/CVS/% ifc111/documentation% ifc111/examples%

# get all subdirectories
FINDSRCDIRS := $(shell find . -type d)
ALLSRCDIRS   = $(patsubst ./%,%,$(FINDSRCDIRS))

# Remove directories to be excluded.
SRCDIRS = $(filter-out $(EXCLUDEDIRS),$(ALLSRCDIRS))

TARGETDIRS = $(TARGETDIR) $(foreach SDIR, $(SRCDIRS), $(TARGETDIR)/$(SDIR))


# ---- Generate list of class files

# The redirect of standard error discards error messages when the directory
# doesn't contain any class files.

STATICS := $(foreach SDIR, $(SRCDIRS), $(shell ls $(SDIR)/*.class 2>/dev/null)) \
	$(foreach SDIR, $(SRCDIRS), $(shell ls $(SDIR)/*.jar 2>/dev/null))
STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))


TARGETS = $(STATICTARGETS)



# ---------------  Production Rules  ------------------------------------

.PHONY : all sanitycheck clean clobber onetimeonly

all : $(TARGETDIRS) $(TARGETS)

$(TARGETDIRS) : 
	$(TARGET_MKDIR) $@

$(STATICTARGETS) : $(TARGETDIR)/% : %
	$(TARGET_CP) $^ $@



# ---------------  Maintenance Rules  -----------------------------------

clean :		# No intermediate files to get rid of.


clobber :			# Remove targets, but not target dir.
	rm -f $(TARGETS)


sanitycheck :
	$(SPECIFICCHECK) $(STATICS)


# define one time only target

onetimeonly :
	$(ONETIMEONLY)
