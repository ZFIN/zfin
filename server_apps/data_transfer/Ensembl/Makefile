#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, downloads data from ENSEMBL to ZFIN.
#

all:
	gradle -p $(SOURCEROOT) :server_apps:data_transfer:Ensembl:deployFiles


# ---------------  Misc Targets  ----------------------------------------

# Run the download script.
# This extracts data from ENSEMBL and places it in the local DB
# does not commit the changes unless run_commit target is made

#     1 to 1 gene mapping from ensdarG to ZDB-GENE

$(TARGETDIR)/ensdarg.csv: $(TARGETDIR)/fetchEnsdarg.groovy $(TARGETDIR)/fetch_ensdarg.sh
	cd $(TARGETDIR) && fetch_ensdarg.sh

load_ensdarG: $(TARGETDIR)/load_ensdarG.sql \
 $(TARGETDIR)/rollback.sql $(TARGETDIR)/ensdarg.csv
	cd $(TARGETDIR) && cat $< rollback.sql | ${PGBINDIR}/psql -e ${DBNAME}

load_ensdarG_commit: $(TARGETDIR)/load_ensdarG.sql \
 $(TARGETDIR)/commit.sql $(TARGETDIR)/ensdarg.csv
	cd $(TARGETDIR)/ && cat $< commit.sql | ${PGBINDIR}/psql -e ${DBNAME}

# 		fetch  Ensembl GTF and convert to gff3
run_gff: $(TARGETDIR)/fetchEnsemblGff3.groovy
	cd $(TARGETDIR) && fetchEnsemblGff3.groovy

notify_stale: $(TARGETDIR)/notifyStaleIds.sh

run_sanger_mutant_data: $(TARGETDIR)/fetch_sangerMutantInfo.sh

# 		fetch and convert Ensembl AGP to gff3
run_agp: $(TARGETDIR)/fetch_ensembl_agp.sh $(TARGETDIR)/agp_to_gff3.awk \
 $(TARGETDIR)/agp_to_gff3_trim.awk $(TARGETDIR)/clone_gff3_w_alias.awk
	cd $(TARGETDIR) && fetch_ensembl_agp.sh


run:        load_ensdarG run_gff run_agp

run_commit: load_ensdarG_commit run_gff run_agp notify_stale

run_gff3:  run_gff run_agp

run_ensembl_load: load_ensdarG_commit notify_stale

run_ensembl_load_rollback:  load_ensdarG

clean_ensembl:
	rm -f $(TARGETDIR)/load_ensdarG.sql 
	touch $(TARGETDIR)/*.sql

.PHONY += load_ensdarG load_ensdarG_commit \
	run_gff run_gff_force run_agp
