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
unload_xpat_gff.sql unload_alias_scattered.sql \
GenerateZFINGFF3.pl unload_zfin_transcript.sql unload_antibody_gff.sql \
unload_zfin_morpholino.sql unload_vega_chromosome_gff.sql \
unload_assembly_clone_gff.sql E_unload_alias_scattered.sql \
E_unload_antibody_gff.sql E_unload_pheno_gff.sql E_unload_xpat_gff.sql \
LoadVegaGFF3.pl LoadEnsemblGFF3.pl E_unload_zfin_knockdown_reagents.sql \
E_unload_transcript_gff.sql  E_unload_ensembl_contig.sql \
load_drerio_ensembl.sql  unload_vega_transcripts.sql \
E_unload_zfin_tginsertion_gff.sql unload_ZMP.sql

STATICS = gather_alias.awk rollback.sql commit.sql load_drerio_vega_id.sql \
	 update_vega_nomenclature.sql get_mo_seq.sql get_crispr_seq.sql \
	 E_zfin_ensembl_gene.sql load_BL_gff.sql get_talen_seq_1.sql \
	 get_talen_seq_2.sql sam2gff3.groovy merge_gff3.groovy gff32unl.groovy \
	 load_knockdown_reagents.sql load_burgess_lin.sh load_knockdown_reagents.sh \
	 reportMissedTalens.groovy reportMissedCRISPR.groovy reportMissedMO.groovy

# Define targets that require special handling.  This relies on the
# home directory being made before the server_apps directory.

HTTP_DATA_TRANSFER_DIR = $(TARGETROOT)/home/data_transfer/Downloads

ENDEMICTARGETS_PRE = $(HTTP_DATA_TRANSFER_DIR)

# (productions) data source is /research/zprodmore/gff3/
# can be re-set in GenerateZFINGFF3.pl for development for Ensembl & Vega

GLOBALSTORE= /research/zprodmore/gff3

LOCALSTORE = $(TARGETROOT)/home/data_transfer/Data

ifeq ($(store),local)
	STORE = $(LOCALSTORE)
else
	STORE = $(GLOBALSTORE)
endif

# ---------------  Production Rules  ------------------------------------

# use default rules for directories without app pages in them

include $(TOP)/make.default.rules


# ---------------  Endemic Targets  -------------------------------------

$(HTTP_DATA_TRANSFER_DIR) :
	$(TARGET_MKDIR) $@

# ---------------  Misc Targets  ----------------------------------------

showstore:
	@echo "using store $(STORE)"


# Run the download script.
# This extracts data from ZFIN and places it in the public http directory
# TODO: paramererize the $STORE and pass it in to the perl script.

$(TARGETDIR)/drerio_vega_id.unl: $(STORE)/drerio_vega_id.unl
	cp $< $@

load_vega: $(TARGETDIR)/load_drerio_vega_id.sql $(TARGETDIR)/LoadVegaGFF3.pl \
           $(TARGETDIR)/rollback.sql $(TARGETDIR)/unload_assembly_clone_gff.sql \
           $(TARGETDIR)/drerio_vega_id.unl $(TARGETDIR)/unload_zfin_morpholino.sql
	cd $(TARGETDIR) && LoadVegaGFF3.pl rollback

load_vega_commit: $(TARGETDIR)/load_drerio_vega_id.sql $(TARGETDIR)/LoadVegaGFF3.pl \
           $(TARGETDIR)/commit.sql $(TARGETDIR)/unload_assembly_clone_gff.sql \
           $(TARGETDIR)/drerio_vega_id.unl $(TARGETDIR)/unload_zfin_morpholino.sql
	cd $(TARGETDIR) && LoadVegaGFF3.pl commit

# $(TARGETDIR)/drerio_ensembl.unl
load_ensembl: $(TARGETDIR)/load_drerio_ensembl.sql $(TARGETDIR)/rollback.sql
	cd $(TARGETDIR) && LoadEnsemblGFF3.pl rollback

# $(TARGETDIR)/drerio_ensembl.unl
load_ensembl_commit: $(TARGETDIR)/load_drerio_ensembl.sql $(TARGETDIR)/commit.sql
	cd $(TARGETDIR) && LoadEnsemblGFF3.pl commit



########################################################################
###                     daily tracks

run: $(TARGETDIR)/GenerateZFINGFF3.pl     $(TARGETDIR)/rollback.sql \
$(TARGETDIR)/unload_zfin_genes_gff.sql    $(TARGETDIR)/E_unload_zfin_tginsertion_gff.sql \
$(TARGETDIR)/unload_alias_scattered.sql   $(TARGETDIR)/E_unload_alias_scattered.sql \
$(TARGETDIR)/unload_pheno_gff.sql         $(TARGETDIR)/E_unload_pheno_gff.sql \
$(TARGETDIR)/unload_xpat_gff.sql          $(TARGETDIR)/E_unload_xpat_gff.sql  \
$(TARGETDIR)/unload_antibody_gff.sql      $(TARGETDIR)/E_unload_antibody_gff.sql \
$(TARGETDIR)/unload_vega_transcripts.sql  $(TARGETDIR)/E_unload_transcript_gff.sql \
$(TARGETDIR)/E_unload_zfin_knockdown_reagents.sql \
$(TARGETDIR)/update_vega_nomenclature.sql $(TARGETDIR)/unload_ZMP.sql \
$(TARGETDIR)/gather_alias.awk

	cd $(TARGETDIR) && GenerateZFINGFF3.pl
	chmod a+r $(HTTP_DATA_TRANSFER_DIR)/*.gff3

########################################################################
# these are here as reminders of how the generate the mo tracks
# they do not need to be regenerated daily
# but they may be regenerated more frequently than the assemblies change


# the target bowtie indexes will need to be updated manualy
# it takes hours so do not run it unecessairly

#SANGER_FTP = ftp://ftp.sanger.ac.uk/pub/vega/zebrafish/dna/

# WARNING about a half hour download
#$(STORE)/Danio_rerio.VEGA47.47.dna.toplevel.fa.gz: $(SANGER_FTP)  $(STORE)
#	wget --timestamping "$(SANGER_FTP)/Danio_rerio.VEGA47.47.dna.toplevel.fa.gz" -o $(STORE)

#$(STORE)/Danio_rerio.VEGA47.47.dna.toplevel.fa: $(STORE)/Danio_rerio.VEGA47.47.dna.toplevel.fa.gz
#	zcat $< > $@

# In reality the sequence does not change from release to release
# just when the major version changes which takes years and is slowing.
# best to just download the new fasta and see if there are significant
# differences if not, leave the bowtie index alone

#$(STORE)/Vega_Zv9_47: $(STORE)/Danio_rerio.VEGA45.%.dna.toplevel.fa
#	cd $(STORE) && nice +10 /opt/misc/bowtie/bowtie-build --big -f $< $@

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
BOWTIE_E_IDX = $(GLOBALSTORE)/Ensembl_GRCz10.81
BOWTIE_V_IDX = $(GLOBALSTORE)/VEGA45_LG_DNA

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# BUILD BOWTIE v2 INDEX

bowtie2_idx_E: $(STORE)/Danio_rerio.GRCz10.81.dna.toplevel.fa
	@cd $(STORE) && /opt/misc/bowtie2/bowtie2-build -f $< $(BOWTIE_E_IDX)

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

$(TARGETDIR)/V_mo_seq.sam: $(TARGETDIR)/mo_seq.fa
	/opt/misc/bowtie/bowtie --best -strata --sam -f $(BOWTIE_V_IDX) $< > $@

$(TARGETDIR)/zfin_morpholino.gff3: $(TARGETDIR)/V_mo_seq.sam $(TARGETDIR)/sam2gff3.groovy
	@cd $(TARGETDIR) && ./sam2gff3.groovy < $< > $@ 2> mo_seq_V_miss.fa
	@echo "\nthe misses could be processed some other way (that allows gaps)"
	@grep -c "^>"  $(TARGETDIR)/mo_seq_V_miss.fa

run_morpholino:  $(TARGETDIR)/zfin_morpholino.gff3

clean_morpholino:
	rm -f $(TARGETDIR)/zfin_morpholino.gff3 $(TARGETDIR)/V_mo_seq.sam \
	$(TARGETDIR)/mo_seq.fa $(TARGETDIR)/mo_seq.fa_line

########################################################################
# not sure about enforcing the existence of the input files
# but want _some_ record of the end of an infrequent process
# so at least provide a reminder of their existence
# these files created as part of the vega load and
# should be used here.

$(TARGETDIR)/assembly_for_tom.tab: $(STORE)/assembly_for_tom.tab
	cp $< $@

$(TARGETDIR)/clone_acc_status.unl: $(STORE)/clone_acc_status.unl
	cp $< $@

$(TARGETDIR)/vega_chromosome.gff3: $(STORE)/vega_chromosome.gff3
	cp $< $@

# price of avoiding transactions is that if the script fails you
# need to uncomment the drop table statments at the top of the sql
# for the next attempt (and recomment when done).

load_vega_assembly: $(TARGETDIR)/assembly_for_tom.tab  $(TARGETDIR)/clone_acc_status.unl \
	$(TARGETDIR)/unload_assembly_clone_gff.sql $(TARGETDIR)/vega_chromosome.gff3
	cd $(TARGETDIR) && dbaccess -a $(DBNAME) unload_assembly_clone_gff.sql

# a similar treatment may need to be devised for
# $(TARGETDIR)/unload_zfin_morpholino.sql

