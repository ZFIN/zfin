#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Miscellaneous app pages (and HTML
# pages that are loaded in the DB).
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

# betainfo.html,stub.html & helpframes.html are unusual.  
# They are HTML files, but they are loaded in the database 
# like an app page.  Treat them like app pages.

GENERICS = ZDB_home.apg data_mgr.apg doc_title.apg \
	docframes.apg jump_to.apg blast.apg\
	start_select.apg stub.html helpframes.html htmlpageheader.apg \
	htmlpagefooter.apg your_input_welcome.apg input_button_generic.apg \
	input_welcome_generic.apg zdb_history.apg new_gene.apg new_line.apg


# --------------------------  Rules  ------------------------------------

# use defuault rules for app page directories.

include $(TOP)/make.default.apg.rules

