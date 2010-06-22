#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Anatomy Static web pages
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/zf_info/anatomy

SUBDIRS =  dict

# Bulk process directories that only contain static files
# this does mean all Makefiles below these could be droped
RSYNCDIR = 24hrs 48hrs 72hrs 120hrs highres lowres stages

STATICS =  24hrs.gif 48hrs.gif 72hrs.gif 120hrs.gif dev_atlas.html

# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules

all :
	rsync -r --delete -C --exclude=Makefile --exclude=.svn $(RSYNCDIR) $(TARGETDIR)
