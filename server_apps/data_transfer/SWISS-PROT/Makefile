
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

GENERICS = pre_loadsp.pl loadsp.pl sp_check.pl sp_parser.pl \
	create_file_for_swiss_prot.pl create_file_for_swiss_prot.sql \
	sp_badgo_report.pl loadsp_ec2go_part.pl sp_badgo_report_ec2gopart.pl sp_adbackattr.sql

STATICS = sp_match.pl sptogo.pl iptogo.pl ectogo.pl sp_delete.sql sp_delete_ec2gopart.sql\
	sp_load.sql sp_load_ec2gopart.sql README   index.html

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

runpre:
	$(TARGETDIR)/pre_loadsp.pl

run:
	$(TARGETDIR)/loadsp.pl

runec2go:
	$(TARGETDIR)/loadsp_ec2go_part.pl

