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
# ZFIN/Workgroup_dirs/AnatDict directory.  That directory is a symbolic
# link to an FTP directory on Chromix.  Members of the Anatomical
# Dictionary Workgroup have access to that FTP directory.
#
# Some files in this directory were copied from the FTP directory to here
# and the static web pages were updated to point here, at the time
# the makefile hierarchy was created (late 2000).
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/zf_info/anatomy/dict

SUBDIRS = ear hair_cell lat_line oto semi_canal sens stato caud_fin

GENERICS = mem.html sum.html mtg2.html
STATICS  = current.html meeting_sum.html zfish_stages.txt Trevarrow.html


# ---------------  Production Rules  ------------------------------------

# Use default rules for directories without app pages in them.

include $(TOP)/make.default.rules
