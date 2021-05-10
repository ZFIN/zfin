#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW  make_thumbnail
# directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This builds the make_thumbnail.sh executable.  

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/sysexecs/make_thumbnail

# The script is static.  It is the only file in this directory.

STATICS = make_thumbnail.sh

STATICTARGETS = $(foreach STAT, $(STATICS), $(TARGETDIR)/$(STAT))
TARGETS = $(SPECIFICTARGETS) $(STATICTARGETS)

include $(TOP)/make.default.rules
