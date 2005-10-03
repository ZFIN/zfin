#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW/home/ZFIN subdirectory
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
# Makefile for the web-database interface of the ZFIN project.
#
# 


# ---------------  Variable Definitions  --------------------------------

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/ZFIN

SUBDIRS = APP_PAGES help_files misc_html Workgroup_dirs Methods

GENERICS = index.html


# --------------------------  Rules  ------------------------------------

# Use default rules

include $(TOP)/make.default.rules


# ---------------  Special Targets  ------------------------------------

postloaddb :
	$(MAKE) -C APP_PAGES $@
