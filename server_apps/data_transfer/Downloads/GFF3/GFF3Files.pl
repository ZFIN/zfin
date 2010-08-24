#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create GFF3 files for public download.
# and internal gbrowse use
#
# We extract several different kinds of information:
#
# ZFIN Genes related to vega Transcripts
#
# Alias of those genes
#
# Subset of those genes with Expression Pattern
#
# Subset of those genes with Phenotype images
#
# define GLOBALS
# set environment variables

$ENV{"DBDATE"}="Y4MD-";

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";

$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";

$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";

$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/GFF3";

# generate the sequence regions gff3
print "get_final_gene.awk -> vega_chromosome.gff3\n";

my $get_LG = "get_final_gene.awk /research/zprodmore/gff3/drerio_vega.gff3 > ".
"<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega_chromosome.gff3";
system ("rm -f <!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega_chromosome.gff3");
system ($get_LG);

my $cmd = "cat load_drerio_vega_id.sql " .
    "unload_zfin_genes_gff.sql " .
    "unload_alias_scattered.sql " .
    "unload_xpat_gff.sql " .
    "unload_pheno_gff.sql " .
    "unload_antibody_gff.sql " .
#   " unload_mutant_gff.sql " .
#   " unload_zfin_transcript.sql ".
    "rollback.sql | $ENV{'INFORMIXDIR'}/bin/dbaccess  <!--|DB_NAME|-->";

system($cmd);

