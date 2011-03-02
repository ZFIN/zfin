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

GENERICS = load_drerio_vega_id.sql unload_pheno_gff.sql unload_zfin_genes_gff.sql \
unload_mutant_gff.sql unload_xpat_gff.sql unload_alias_scattered.sql GFF3Files.pl \
unload_zfin_transcript.sql unload_antibody_gff.sql unload_zfin_morpholino.sql \
unload_vega_chromosome_gff.sql unload_assembly_clone_gff.sql

STATICS = gather_alias.awk drop_table_gff3.sql

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

run : $(TARGETDIR)/GFF3Files.pl         $(TARGETDIR)/load_drerio_vega_id.sql \
$(TARGETDIR)/unload_pheno_gff.sql       $(TARGETDIR)/unload_zfin_genes_gff.sql \
$(TARGETDIR)/unload_mutant_gff.sql      $(TARGETDIR)/unload_xpat_gff.sql \
$(TARGETDIR)/unload_alias_scattered.sql $(TARGETDIR)/gather_alias.awk   \
$(TARGETDIR)/drop_table_gff3.sql \
$(TARGETDIR)/unload_zfin_morpholino.sql $(TARGETDIR)/unload_assembly_clone_gff.sql
	$(TARGETDIR)/GFF3Files.pl
	chmod a+r $(HTTP_DATA_TRANSFER_DIR)/*.gff3

