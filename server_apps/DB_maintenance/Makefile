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

SUBDIRS = loadUp/

GENERICS = regen.sh scrubscan.pl unload_production.sh validatedata.pl \
	   load_and_index_almost.pl dumpServer.pl dumpLogsContinuous.pl \
           fx_permission_check.pl pnas_report.pl updatePNAS.sql \
	   performance.pl

STATICS = postloaddb_almost.csh \


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules



# ---------------  Misc Targets  ----------------------------------------

runregen : $(TARGETDIR)/regen.sh
	$(TARGETDIR)/regen.sh

runscrub : $(TARGETDIR)/scrubscan.pl
	$(TARGETDIR)/scrubscan.pl

runloadindex : $(TARGETDIR)/load_and_index_almost.pl
	$(TARGETDIR)/load_and_index_almost.pl -i

dumpserver : $(TARGETDIR)/dumpServer.pl
	$(TARGETDIR)/dumpServer.pl

dumplogscontinuous : $(TARGETDIR)/dumpLogsContinuous.pl
	$(TARGETDIR)/dumpLogsContinuous.pl

runorphans : $(TARGETDIR)/validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -o

rundaily : $(TARGETDIR)/validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -d

runweekly : $(TARGETDIR)/validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -w

runmonthly : $(TARGETDIR)/validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -m

runyearly : $(TARGETDIR)/validatedata.pl
	$(TARGETDIR)/validatedata.pl $(DBNAME) -y
