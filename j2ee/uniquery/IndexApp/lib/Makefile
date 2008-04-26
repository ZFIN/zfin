#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project,  IndexApp/lib directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

UNIQUERYDIR = $(TARGETROOT)/j2ee/uniquery
TARGETDIR   = $(UNIQUERYDIR)/IndexApp/lib
SUPPORTDIR  = $(UNIQUERYDIR)/Support/lib

STATICS = cvu.jar

# Also need to get libs from support directory into target directory.

SUPPORTJARS = commons-lang-2.3.jar lucene-core-2.3.1.jar UniquerySupport.jar
SUPPORTJARSRCPATHS = $(foreach SUPPORTJAR,$(SUPPORTJARS),$(SUPPORTDIR)/$(SUPPORTJAR))
SUPPORTJARDESTPATHS = $(foreach SUPPORTJAR,$(SUPPORTJARS),$(TARGETDIR)/$(SUPPORTJAR))

ENDEMICTARGETS_POST = $(SUPPORTJARDESTPATHS)


# ---------------  Production Rules  ------------------------------------

# Use defualt rules for directories without app pages in them.

include $(TOP)/make.default.rules



# ---------------  Endemic Rules  ------------------------------------

$(SUPPORTJARDESTPATHS) : $(TARGETDIR)/% : $(SUPPORTDIR)/%
	$(TARGET_CP) $^ $@
	$(TOUCH) $@
