#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Anatomical dictionary static web 
# pages.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This directory has a logical (but not physical) association with the
# ZFIN/Workgroup_dirs/AnatDict directory.
#
# Some files in this directory were copied from the FTP pub/AnatDict directory 
# to here and the static web pages were updated to point here, at the time
# the makefile hierarchy was created (late 2000).
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/zf_info/anatomy/dict

SUBDIRS = ear hair_cell lat_line oto semi_canal sens stato caud_fin

STATICS = zfish_stages.txt sum.html meeting_sum.html Trevarrow.html \
atlas_info.html mem.html mtg2.html 


# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules
