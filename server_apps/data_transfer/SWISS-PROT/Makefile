
#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, SWISS-PROT automated data load directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# See the README file. 


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/SWISS-PROT

GENERICS = loadsp.pl sp_check.pl sp_parser.pl  index.html \
	create_file_for_swiss_prot.pl sp_badgo_report.pl 	

STATICS = sptogo.pl iptogo.pl ectogo.pl sp_delete.sql \
	sp_load.sql create_file_for_swiss_prot.sql README  

# Define targets that require special handling.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer/SWISS-PROT

ENDEMICTARGETS_PRE = $(HTTP_DATA_TRANSFER_DIR)


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@


# ---------------  Misc Targets  ----------------------------------------

run :
	$(TARGETDIR)/loadsp.pl

