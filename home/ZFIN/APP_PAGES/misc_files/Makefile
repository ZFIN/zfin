#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Miscellaneous app pages (and HTML
# pages that are loaded in the DB).
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
#


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/home/ZFIN/APP_PAGES/misc_files

# betainfo.html and stub.html are unusual.  They are HTML files, but they
# are loaded in the database like an app page.  Treat them like app pages.

GENERICS = ZDB_home.apg betainfo.html data_mgr.apg doc_title.apg \
	docframes.apg jump_to.apg process_notification.apg startframes.apg \
	startframes_select.apg stub.html


# --------------------------  Rules  ------------------------------------

# use defuault rules for app page directories.

include $(TOP)/make.default.apg.rules

