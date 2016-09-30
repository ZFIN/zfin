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

SUBDIRS = loadUp/ warehouse/ pheno/ extentMonitoring/ queryMonitoring/ gafLoad/ lib/

GENERICS = regen.sh scrubscan.pl unload_production.sh checkUnloadProduction.sh dumpServer.pl dumpLogsContinuous.pl fx_permission_check.pl backupBlastDbsAndRsyncAlmostBlastDbs.sh checkVarcharOctetLength.pl orphanChecks.pl disable_updates.pl pub_check_and_addback_volpg.pl pushDataToPublic.sh check_undefined_environment.pl checkSOZFINMapping.sh reportLabAddressChanges.sh feature_genotype_association_check.pl rotateInformixLog.sh set_unload_timestamp.sql moveFiles.py


STATICS = loadDb.sh makeDeveloperBlastDbs.sh build.xml 
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
