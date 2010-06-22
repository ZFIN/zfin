#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, ZIRC load and unload directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile produces scripts that pull data from ZIRC, and scripts
# that push data to ZIRC.  We send a list of known fish lines to ZIRC,
# and we get back the statuses for fish lines at ZIRC.  Files produced
# by this makefile are run by cron.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/ZIRC

GENERICS = pushToZirc.pl pushToZirc.sql pullFromZirc.pl

STATICS = pullEstsFromZirc.pl pullGenoFromZirc.pl pullAtbFromZirc.pl

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer/ZIRC

ENDEMICTARGETS_PRE = $(HTTP_DATA_TRANSFER_DIR)

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@


# ---------------  Misc Targets  ----------------------------------------

# Run the pull script.  This gets data from the ZIRC FTP site and loads
# it into ZFIN.

pull : $(TARGETDIR)/pullFromZirc.pl $(TARGETDIR)/pullEstsFromZirc.pl
	(cd $(TARGETDIR); pullFromZirc.pl)

# Run the push script.  This gets data from ZFIN and makes it available
# to ZIRC on the ZFIN FTP site.

push : $(TARGETDIR)/pushToZirc.pl
	(cd $(TARGETDIR); pushToZirc.pl)
