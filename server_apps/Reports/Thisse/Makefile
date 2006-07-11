#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, WWW server apps reports ZGC directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# 
# Targets made by this makefile generate ZGC related nomenclature and 
# expression data statistics report.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/Reports/Thisse

GENERICS = getSiGeneVegaFasta.sh
STATICS = siGene_wo_xpat.sql

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

# ---------------  Misc Targets  ----------------------------------------

run : $(TARGETDIR)/getSiGeneVegaFasta.sh
	$(TARGETDIR)/getSiGeneVegaFasta.sh
