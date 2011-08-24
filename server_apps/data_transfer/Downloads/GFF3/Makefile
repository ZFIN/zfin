#------------------------------------------------------------------------
#
# Makefile for ZFIN_WWW CVS Project, creates data files for public downloads
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# !! See $(TOP)/Makfile and $(TOP)/make.include for a full explanation !!
# !! of the makefile hierarchy this makefile is a part of, and of the  !!
# !! format and conventions used in this makefile.                     !!
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# This makefile produces the script that creates data files for public
# download.
# The script is run by cron every week.  It invokes the sql script.
# The dump uses the HTTP directory structure.

# ---------------  Variable Definitions  --------------------------------

TOP = ../../../..
include $(TOP)/make.include

TARGETDIR = $(TARGETROOT)/server_apps/data_transfer/Downloads/GFF3

GENERICS = unload_pheno_gff.sql unload_zfin_genes_gff.sql \
unload_mutant_gff.sql unload_xpat_gff.sql unload_alias_scattered.sql \
GenerateZFINGFF3.pl unload_zfin_transcript.sql unload_antibody_gff.sql \
unload_zfin_morpholino.sql unload_vega_chromosome_gff.sql \
unload_assembly_clone_gff.sql E_unload_alias_scattered.sql \
E_unload_antibody_gff.sql E_unload_pheno_gff.sql E_unload_xpat_gff.sql \
LoadVegaGFF3.pl LoadEnsemblGFF3.pl E_unload_zfin_morpholino.sql \
E_unload_transcript_gff.sql  E_unload_ensembl_contig.sql \
load_drerio_ensembl.sql  unload_vega_transcripts.sql

STATICS = gather_alias.awk rollback.sql commit.sql load_drerio_vega_id.sql \
	 update_vega_nomenclature.sql get_mo_seq.sql sam2gff3.awk increment_id.awk \
	 E_zfin_ensembl_gene.sql

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer/Downloads

ENDEMICTARGETS_PRE = $(HTTP_DATA_TRANSFER_DIR)

# (production) data source is /research/zprodmore/gff3/
# can be re-set in GFF3Files.pl for development


# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@

# ---------------  Misc Targets  ----------------------------------------

# Run the download script.  This extracts data from ZFIN and places it in
# the public http directory

# Note: explanation per request from SM.
# As with all make targets, items following the colon after the target
# (on the same logical line) are the targets depencencies.
# commands follow the set of depencencies on new lines with a mandatory 
#	tab as their first character.
# the effect is, if any dependicies are false (i.e. do not exist)
# evoking "gmake <target>" will not result in the comands being run
# note that empty dependicies are vacously true. 
# but being empty is not the only path to truth. HTH

# $(TARGETDIR)/unload_zfin_morpholino.sql 
# $(TARGETDIR)/unload_assembly_clone_gff.sql

load_vega:  $(TARGETDIR)/load_drerio_vega_id.sql $(TARGETDIR)/rollback.sql        # $(TARGETDIR)/drerio_vega_id.unl 
	cd $(TARGETDIR) && LoadVegaGFF3.pl rollback 
       
load_vega_commit: $(TARGETDIR)/load_drerio_vega_id.sql $(TARGETDIR)/commit.sql    # $(TARGETDIR)/drerio_vega_id.unl
	cd $(TARGETDIR) && LoadVegaGFF3.pl commit
       
load_ensembl: $(TARGETDIR)/load_drerio_ensembl.sql $(TARGETDIR)/rollback.sql      # $(TARGETDIR)/drerio_ensembl.unl
	cd $(TARGETDIR) && LoadEnsemblGFF3.pl rollback 
       
load_ensembl_commit: $(TARGETDIR)/load_drerio_ensembl.sql $(TARGETDIR)/commit.sql # $(TARGETDIR)/drerio_ensembl.unl
	cd $(TARGETDIR) && LoadEnsemblGFF3.pl commit
       
run : $(TARGETDIR)/GenerateZFINGFF3.pl 	  $(TARGETDIR)/rollback.sql \
$(TARGETDIR)/unload_zfin_genes_gff.sql \
$(TARGETDIR)/unload_alias_scattered.sql   $(TARGETDIR)/E_unload_alias_scattered.sql \
$(TARGETDIR)/unload_pheno_gff.sql  		  $(TARGETDIR)/E_unload_pheno_gff.sql \
$(TARGETDIR)/unload_xpat_gff.sql 		  $(TARGETDIR)/E_unload_xpat_gff.sql  \
$(TARGETDIR)/unload_antibody_gff.sql	  $(TARGETDIR)/E_unload_antibody_gff.sql \
$(TARGETDIR)/unload_vega_transcripts.sql  $(TARGETDIR)/E_unload_transcript_gff.sql \
$(TARGETDIR)/unload_zfin_morpholino.sql   $(TARGETDIR)/E_unload_zfin_morpholino.sql \
$(TARGETDIR)/update_vega_nomenclature.sql $(TARGETDIR)/gather_alias.awk
	cd $(TARGETDIR) && GenerateZFINGFF3.pl
	chmod a+r $(HTTP_DATA_TRANSFER_DIR)/*.gff3


# these are here as reminders of how the generate the mo tracks
# they do not need to be regenerated daily
# but they may be regenerated more frequently than the assemblies change
# the target bowtie indexes will need to be updated manualy

run_morpholino_E : get_mo_seq.sql
	cd $(TARGETDIR) && dbaccess -a $(DBNAME) get_mo_seq.sql && \
	/private/bin/bowtie --best -strata --sam -f /research/zprodmore/gff3/Ensembl_Zv9.62 mo_seq.fa > E_mo_seq.sam && \
	nawk -f sam2gff3.awk < E_mo_seq.sam > E_mo_seq.gff3 && \
	sort -k9,9 -k 1,1n -k 4,4n  E_mo_seq.gff3 | ./increment_id.awk > E_zfin_morpholino.gff3

run_morpholino : get_mo_seq.sql
	cd $(TARGETDIR) && dbaccess -a $(DBNAME) get_mo_seq.sql && \
	/private/bin/bowtie --best -strata --sam -f /research/zprodmore/gff3/VEGA43_LG_DNA mo_seq.fa > mo_seq.sam && \
	nawk -f sam2gff3.awk < mo_seq.sam > mo_seq.gff3 && \
	sort -k9,9 -k 1,1n -k 4,4n  mo_seq.gff3 | ./increment_id.awk > zfin_morpholino.gff3
