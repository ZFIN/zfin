#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, downloads data from ENSEMBL to ZFIN.
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#

# ---------------  Variable Definitions  --------------------------------

TOP = ../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Ensembl

GENERICS = fetch_ensembl_agp.sh pullFromBioMart.pl notifyStaleIds.sh reportEnsembleStatistics.sh fetch_ensdargOttdargTable.sh fetch_sangerMutantInfo.sh

STATICS  = fetch_ensdarTG.sh fetch_ensdarG.mysql fetch_ensdarT_dbacc.mysql \
	load_ensdarG.sql load_ensdarT_dbacc.sql fetch_ensdarpInfo.mysql \
	fetch_ensembl_gff.sh gtf2gff3.pl gtf2gff3.cfg unload_zfin_DNA_clone.sql \
	 agp_to_gff3.awk agp_to_gff3_trim.awk clone_gff3_w_alias.awk  \
	  rollback.sql commit.sql readme readme.ensembl_mysql fetch_sangerMutantInfo.mysql fetch_ensdargOttdargTable.mysql loadEnsdarPMapping.sql loadSangerMutantData.sql loadEnsdargOttdarg.sql updateSequenceFeatureChromosomeLocation.sql \
	load-missing-uniprot-records.sql log4j.xml report.properties build.xml

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer/Ensembl

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules

# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@

# ---------------  Misc Targets  ----------------------------------------

# Run the download script.
# This extracts data from ENSEMBL and places it in the local DB
# does not commit the changes unless run_commit target is made

#     1 to 1 gene mapping from ensdarG to ZDB-GENE

$(TARGETDIR)/ensdarT_dbacc.unl:  $(TARGETDIR)/fetch_ensdarT_dbacc.mysql \
                                 $(TARGETDIR)/ensdarG.unl

$(TARGETDIR)/ensdarG.unl: $(TARGETDIR)/fetch_ensdarG.mysql 
	cd $(TARGETDIR) && fetch_ensdarTG.sh
	 
load_ensdarG: $(TARGETDIR)/load_ensdarG.sql \
 $(TARGETDIR)/rollback.sql $(TARGETDIR)/ensdarG.unl 
	cd $(TARGETDIR) && cat $< rollback.sql | dbaccess -a $$DBNAME
	
load_ensdarG_commit: $(TARGETDIR)/load_ensdarG.sql \
 $(TARGETDIR)/commit.sql $(TARGETDIR)/ensdarG.unl
	cd $(TARGETDIR)/ && cat $< commit.sql | dbaccess -a $$DBNAME

load_ensdarT: $(TARGETDIR)/load_ensdarT_dbacc.sql \
 $(TARGETDIR)/rollback.sql $(TARGETDIR)/ensdarT_dbacc.unl
	cd $(TARGETDIR)/ && cat $< rollback.sql | dbaccess -a $$DBNAME
	
load_ensdarT_commit: $(TARGETDIR)/load_ensdarT_dbacc.sql \
 $(TARGETDIR)/commit.sql $(TARGETDIR)/ensdarT_dbacc.unl
	cd $(TARGETDIR)/ && cat $< commit.sql | dbaccess -a $$DBNAME

# 		fetch  Ensembl GTF and convert to gff3
run_gff: $(TARGETDIR)/fetch_ensembl_gff.sh
	cd $(TARGETDIR) && fetch_ensembl_gff.sh

notify_stale: $(TARGETDIR)/notifyStaleIds.sh

run_sanger_mutant_data: $(TARGETDIR)/fetch_sangerMutantInfo.sh

# pre delete any versions of the file to be downloaded
run_gff_force: $(TARGETDIR)/fetch_ensembl_gff.sh
	cd $(TARGETDIR) && fetch_ensembl_gff.sh -f

# 		fetch and convert Ensembl AGP to gff3
run_agp: $(TARGETDIR)/fetch_ensembl_agp.sh $(TARGETDIR)/agp_to_gff3.awk \
 $(TARGETDIR)/agp_to_gff3_trim.awk $(TARGETDIR)/clone_gff3_w_alias.awk
	cd $(TARGETDIR) && fetch_ensembl_agp.sh

########################################################################
run:        load_ensdarG        load_ensdarT        run_gff run_agp

run_commit: load_ensdarG_commit load_ensdarT_commit run_gff run_agp notify_stale

run_gff3:  run_gff run_agp

run_ensembl_load: load_ensdarG_commit load_ensdarT_commit notify_stale

run_ensembl_load_rollback:  load_ensdarG load_ensdarT

clean_ensembl:
	rm -f $(TARGETDIR)/load_ensdarG.sql $(TARGETDIR)/ensdarT_dbacc.unl
	touch $(TARGETDIR)/*.mysql $(TARGETDIR)/*.sql 
	
.PHONY += load_ensdarG load_ensdarG_commit load_ensdarT load_ensdarT_commit \
	run_gff run_gff_force run_agp
