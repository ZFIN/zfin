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

GENERICS = regen.sh scrubscan.pl unload_production.sh validatedata.pl


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Misc Targets  ----------------------------------------

runregen : regen.sh
	$(TARGETDIR)/regen.sh

runscrub : scrubscan.pl
	$(TARGETDIR)/scrubscan.pl

runorphans : validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -o

rundaily : validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -d

runweekly : validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -w

runmonthly : validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -m

runyearly : validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -y
