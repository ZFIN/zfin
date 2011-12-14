#!/private/bin/perl -w
# GenerateZFINGFF3.pl
#-----------------------------------------------------------------------
# Runs script to create GFF3 files for public download.
# and internal gbrowse use
#
# We query ZFIN for:
# Vega Assembly clone lengths
# ZFIN Genes related to Vega transcripts & their Alias'
#
# Subset of genes with Vega transcripts that have:
#   Expression Pattern
#   Phenotype images
#   Antibodies
# We generate locations for Morpholinos
#
# define GLOBALS
# set environment variables

$ENV{"DBDATE"}="Y4MD-";
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/GFF3";
print "starting here: \n". `pwd`. "\n";

print "generating tracks\n";

my $cmd = "cat ".
	"update_vega_nomenclature.sql " . # full nomenclature transaction
    "unload_zfin_genes_gff.sql " .
    "unload_alias_scattered.sql " .
    "unload_xpat_gff.sql " .
    "unload_pheno_gff.sql " .
    "unload_antibody_gff.sql " .
    "unload_vega_chromosome_gff.sql " .
    "unload_vega_transcripts.sql ".
	"unload_zfin_morpholino.sql " .
	#"";
###############################################
###  Ensembl
#my $cmd = "cat ".
	"E_load_unload_BL_gff.sql " .                # full transaction
    "E_unload_transcript_gff.sql " .
    "E_unload_ensembl_contig.sql " .
 	"E_unload_zfin_morpholino.sql " .
    "E_zfin_ensembl_gene.sql " .                  # begin gene transaction
    "E_unload_alias_scattered.sql " .
    "E_unload_xpat_gff.sql " .
    "E_unload_pheno_gff.sql " .
    "E_unload_antibody_gff.sql " .
    "rollback.sql | $ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|-->";

system($cmd);
