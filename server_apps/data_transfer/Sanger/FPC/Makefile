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
# This makefile produces the script that downloads data files from
# Sanger FPC .
# The script is run by hand when FPC updates occur. (gmake run/commit)
# It invokes the update_fpc.sql sql script.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Sanger/FPC

GENERICS = fetch_load_fpc.sh

STATICS  = update_fpc.sql rollback.sql commit.sql marker-to-fpc_zdb.r README

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

# ---------------  Misc Targets  ----------------------------------------

# Run the download script.
# This extracts data from Sanger and places it in ZFIN

run : $(TARGETDIR)/fetch_load_fpc.sh 
	cd $(TARGETDIR);fetch_load_fpc.sh rollback

commit : $(TARGETDIR)/fetch_load_fpc.sh 
	cd $(TARGETDIR);fetch_load_fpc.sh commit
