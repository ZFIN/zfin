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

TOP = ../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/DB_maintenance

GENERICS = unload_production.sh validatedata.pl


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Misc Targets  ----------------------------------------

runorphans :
	$(TARGETDIR)/validatedata.pl $(DBNAME) -o

rundaily :
	$(TARGETDIR)/validatedata.pl $(DBNAME) -d

runweekly :
	$(TARGETDIR)/validatedata.pl $(DBNAME) -w

runmonthly :
	$(TARGETDIR)/validatedata.pl $(DBNAME) -m

runyearly :
	$(TARGETDIR)/validatedata.pl $(DBNAME) -y
