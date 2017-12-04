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

TARGETDIR = $(TARGETROOT)/server_apps/DB_maintenance/warehouse/expressionMart
GENERICS = runExpressionMart.sh runExpressionMart_PG.sh
STATICS = begin.sql dropTables.sql populateTables.sql  commit.sql expressionMartRegen.sql refreshExpressionMart.sql expressionmart-integrity-tests.sql begin_PG.sql dropTables_PG.sql populateTables_PG.sql  commit_PG.sql expressionMartRegen_PG.sql refreshExpressionMart_PG.sql expressionmart-integrity-tests_PG.sql



# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

