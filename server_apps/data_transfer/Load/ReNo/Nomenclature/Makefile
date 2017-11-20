#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, ReNo Nomenclature run generator directory
#
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Load/ReNo

SUBDIRS =

GENERICS =

STATICS =  select_nomemclature_candidates.sql select_sanger_nomemclature_candidates.sql generate_SangerMutant_Nomenclature_run.sh moveEnsdarTToZMP.sql select_special_sanger_nomen_candidate.sql generate_special_Sanger_uniprot_nomenclature_run.sh generate_UniProt_Nomenclature_run.sh

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Misc Targets  ----------------------------------------


run:  
	generate_UniProt_Nomenclature_run.sh

runSanger:   
	generate_SangerMutant_Nomenclature_run.sh