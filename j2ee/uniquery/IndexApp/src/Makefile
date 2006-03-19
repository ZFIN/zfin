#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project,  IndexApp/src directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Produces the class files using ANT.  However, the hard parts are:
# 1. Getting the clobber target to work.
# 2. Figuring out when to call ant.  (We could just call ant all the time,
#    but ant is gosh darn slow.)
# 
# And we want to be able to do this without complicating this file or 
# making this file high-maintenance.  

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIRPARENT = $(TARGETROOT)/j2ee/uniquery/IndexApp
TARGETDIR = $(TARGETDIRPARENT)/classes

CLASS_DIR = $(TARGETDIR)
SOURCE_DIR = .

include ../../../make.include.j2ee		# get JAVAS and CLASSES

ENDEMICTARGETS_POST = $(CLASSES)


# ---------------  Production Rules  ------------------------------------

# Use defualt rules for directories without app pages in them.

include $(TOP)/make.default.rules


# ---------------  Endemic Rules  ------------------------------------

# Target to make the class files if we need to
$(CLASSES) : $(CLASS_DIR)/%.class : $(SOURCE_DIR)/%.java
	/local/bin/ant -Dbase.dir=$(TARGETDIRPARENT)
	/bin/chmod o+r $(CLASSES)

# ---------------  Misc Rules  ------------------------------------

# run the indexer  MAYBE TAKE THIS OUT:

run :
	/local/bin/ant -Dbase.dir=$(TARGETDIRPARENT) run

# run the searcher (and what is that?)

run_searcher :
	/local/bin/ant -Dbase.dir=$(TARGETDIRPARENT) runsearcher
