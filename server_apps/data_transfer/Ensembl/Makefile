#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, downloads data from ENSEMBL to ZFIN.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Ensembl

GENERICS = fetch_ensdarg.sh fetch_ensdarT_dbacc.sh

STATICS  = readme fetch_ensdarg.sql load_ensdarg.sql readme.ensembl_mysql\
	rollback.sql commit.sql load_ensdarT_dbacc.sql fetch_ensdarT_dbacc.mysql

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer/Ensembl

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@

# ---------------  Misc Targets  ----------------------------------------

# Run the download script.
# This extracts data from ENSEMBL and places it in the local DB
# does not commit the changes unless run_commit target is made

#     1 to 1 gene mapping from ensdarG to ZDB-GENE
run : $(TARGETDIR)/fetch_ensdarg.sh
	$(TARGETDIR)/fetch_ensdarg.sh rollback

run_commit : $(TARGETDIR)/fetch_ensdarg.sh
	- touch $(TARGETDIR)/fetch_ensembl.log
	$(TARGETDIR)/fetch_ensdarg.sh commit

#         all ensdarT that map to an ottdarT in ZFIN
run_transcript : fetch_ensdarT_dbacc.sh
	cd $(TARGETDIR) && fetch_ensdarT_dbacc.sh rollback

run_transcript_commit : fetch_ensdarT_dbacc.sh
	- touch $(TARGETDIR)/fetch_ensembl.log
	cd $(TARGETDIR) && fetch_ensdarT_dbacc.sh commit
