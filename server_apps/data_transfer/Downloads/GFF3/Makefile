#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, creates data files for public downloads
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile produces the script that creates data files for public
# download.
# The script is run by cron every week.  It invokes the sql script.
# The dump uses the HTTP directory structure.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Downloads/GFF3

STATICS = gather_alias.awk runSqlFiles.groovy

SUBDIRS = knockdown_reagents

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer/Downloads

ENDEMICTARGETS_PRE = $(HTTP_DATA_TRANSFER_DIR)

# (productions) data source is /research/zprodmore/gff3/
# can be re-set in GenerateZFINGFF3.pl for development for Ensembl & Vega

GLOBALSTORE= /research/zprodmore/gff3

LOCALSTORE = $(TARGETROOT)/home/data_transfer/Data

ifeq ($(store),local)
	STORE = $(LOCALSTORE)
else
	STORE = $(GLOBALSTORE)
endif

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@

# ---------------  Misc Targets  ----------------------------------------

showstore:
	@echo "using store $(STORE)"


load_ensembl:
	loadEnsemblGff3.groovy


