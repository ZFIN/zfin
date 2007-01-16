#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Anatomy data load directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# The AO load runs as needed, normal once a month. Curator provides OBO
# format anatomy file. After each load, an unload is performed to 
# start another round.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Anatomy

GENERICS = unloadAO.pl anatitem_2_obo.pl stg_2_obo.pl err_report.pl \
	loadMaster.sh unloadAOFile.sql

STATICS = README parseOBO.pl loadAO.sql make_annot_translation.sql \
	batch_xpat_annot_adjust.sql parse_stage_keywords.pl \
	check_thisse_keywords.sql check_annotation.sql 

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Misc Targets  ----------------------------------------

# Run script to create file for OBO site
unloadshare : 
	$(TARGETDIR)/unloadAO.pl share

# Run script to create file for continous curation
unload : 
	$(TARGETDIR)/unloadAO.pl

