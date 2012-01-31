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
STATICS = constructGroup.sql addAliasesToFunctionalAnnotation.sql addBackgroundsToFish.sql addCountsToFunctionalAnnotation.sql addGroupsToFunctionalAnnotation.sql createBtsIndexes.sql dropBtsIndexes.sql dropTables.sql environmentGroup.sql featureGroup.sql geneGroup.sql morpholinoGroup.sql phenoFigs.sql phenoTermGroup.sql populateFigureTermFishSearch.sql populateFishAnnotationSearch.sql populateFunctionalAnnotation.sql populateGeneFeatureResultView.sql populateNullOrderingColumns.sql schemaTables.sql translateFeatureTypeForTGs.sql xpatFigs.sql commit.sql


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

