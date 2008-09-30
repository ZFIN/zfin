#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW/home/ZFIN/Workgroup_dirs 
# subdirectory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This directory exists for one file:
#   ./AnatDict/meeting_sum.html
# and that file exists only because it is pointed to by an HTTP reference in 
#   zf_info/monitor/vol7.1/vol7.1.pdf
# AND, the meeting_sum file is duplicated in the FTP tree.  I don't know why.
#
# This makefile used to create a sym link to the FTP directory, but that
# was problematic because it required the makefile to know the path to the
# FTP directory, which is not something we currently have in an environment
# of makefile variable.  Therefore it was hard coded in this makefile, and
# caused problems whenever we moved zfin.org to a different machine.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/ZFIN/Workgroup_dirs

#SUBDIRS = AnatDict

# --------------------------  Rules  ------------------------------------

# Use default rules

include $(TOP)/make.default.rules
