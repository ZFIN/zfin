#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project,  Client Apps (applets) directory
#
# $Source$
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
#


# ---------------  Variable Definitions  --------------------------------

TOP = ..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/client_apps

# do not include gbrowse/ as a SUBDIR
# it will force it to be built before it's Makefile is generated
SUBDIRS = Map


all:  
	
# ---------------  Production Rules  ------------------------------------

# Use defualt rules for directories without app pages in them.

include $(TOP)/make.default.rules
