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

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/DB_maintenance/warehouse
SUBDIRS = chromosomeMartPostgres expressionMart phenotypeMart
GENERICS = who_is.sh who_is_not.sh switch.sh  regenChromosomeMart.sh regenExpressionMart.sh regenPhenotypeMart.sh
STATICS = getUnloadDir.sh char regenChromosomeMartReport.txt runChromosomeMartReport.txt regenExpressionMartReport.txt runExpressionMartReport.txt regenChromosomeMartReportPostgres.txt runChromosomeMartReportPostgres.txt regenExpressionMartReportPostgres.txt runExpressionMartReportPostgres.txt 


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

