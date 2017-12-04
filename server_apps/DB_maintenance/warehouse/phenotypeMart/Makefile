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

TARGETDIR = $(TARGETROOT)/server_apps/DB_maintenance/warehouse/phenotypeMart
GENERICS = runPhenotypeMart.sh runPhenotypeMart_PG.sh
STATICS = begin.sql dropTables.sql populateTables.sql  commit.sql phenotypeMartRegen.sql refreshPhenotypeMart.sql phenotypemart-integrity-tests.sql truncatePGTable.sql truncatePGSTable.sql truncateTempMappingTable.sql  begin_PG.sql dropTables_PG.sql populateTables_PG.sql  commit_PG.sql phenotypeMartRegen_PG.sql refreshPhenotypeMart_PG.sql phenotypemart-integrity-tests_PG.sql truncatePGTable_PG.sql truncatePGSTable_PG.sql truncateTempMappingTable_PG.sql


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

