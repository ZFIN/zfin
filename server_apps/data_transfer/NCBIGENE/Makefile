#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW SVN Project, NCBIGENE automated data load directory
#
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

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/NCBIGENE

GENERICS = NCBI_gene_load.pl prepareNCBIgeneLoad.sql loadNCBIgeneAccs.sql NCBI_gene_load_PG.pl prepareNCBIgeneLoad_PG.sql loadNCBIgeneAccs_PG.sql

STATICS = README

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

