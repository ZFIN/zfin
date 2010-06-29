#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Locus app pages.
#
# $Id$
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/ZFIN/APP_PAGES/locus_files

# locusselect.html is loaded in the DB with the app pages.  Treat it like
# an app page.

STATICS = do-locusupdate.apg locuslister.apg locusselect.html \
	locusupdate.apg locusview.apg process_locus.apg


# Use default rules for app page directories.

include $(TOP)/make.default.apg.rules

