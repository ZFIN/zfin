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

GENERICS = data_mgr.apg doc_title.apg \
	 jump_to.apg \
	start_select.apg htmlpageheader.apg \
	htmlpagefooter.apg your_input_welcome.apg input_button_generic.apg \
	input_welcome_generic.apg zdb_history.apg new_gene.apg new_line.apg \
	ZDB_home_leftcolumn.apg ZDB_home_rightcolumn.apg 
STATICS=ZDB_home.apg docframes.apg stub.html helpframes.html spiffy_corners.apg blast.apg

# --------------------------  Rules  ------------------------------------

# use defuault rules for app page directories.

include $(TOP)/make.default.apg.rules

