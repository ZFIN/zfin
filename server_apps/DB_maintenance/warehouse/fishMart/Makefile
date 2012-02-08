#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, Database maintenance directory
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Targets made by this makefile are used to maintain the ZFIN database.


# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/DB_maintenance/warehouse/fishMart
GENERICS = runFishMart.sh
STATICS = begin.sql dropTables.sql schemaTables.sql geneGroup.sql environmentGroup.sql featureGroup.sql morpholinoGroup.sql constructGroup.sql populateFunctionalAnnotation.sql phenoFigs.sql xpatFigs.sql phenoTermGroup.sql addGroupsToFunctionalAnnotation.sql addAliasesToFunctionalAnnotation.sql addCountsToFunctionalAnnotation.sql populateFishAnnotationSearch.sql populateGeneFeatureResultView.sql populateFigureTermFishSearch.sql translateFeatureTypeForTGs.sql addBackgroundsToFish.sql populateNullOrderingColumns.sql createBtsIndexes.sql commit.sql automatedFishMart.sql dropBtsIndexes.sql

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

