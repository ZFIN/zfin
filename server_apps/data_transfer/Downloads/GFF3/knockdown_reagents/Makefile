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

TOP = ../../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Downloads/GFF3/knockdown_reagents/

STATICS =  get_crispr_seq.sql get_talen_seq_1.sql load_knockdown_reagents.sql reportMissedMO.groovy get_mo_and_crispr_seq.sql get_talen_seq_2.sql reportMissedTalens.groovy get_mo_seq.sql load_knockdown_reagents.sh reportMissedCRISPR.groovy sam2gff3.groovy merge_gff3.groovy

include $(TOP)/make.default.rules

# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@




