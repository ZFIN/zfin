#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create GFF3 files for public download.
# and internal gbrowse use
#
# We query ZFIN for:
# Assembly clone lengths
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

#  data_chache for production is
my $data_cache = "/research/zprodmore/gff3";

# opportunity to change data_cache for testing enviroment i.e.:
# $data_cache = "/research/zusers/tomc/data_transfer/VEGA/Assembly/2010-Oct";

print "Fetching data files from $data_cache\n";

my @infiles = qw(
    drerio_vega_id.unl vega_chromosome.gff3 zfin_morpholino.gff3
    assembly_for_tom.tab clone_acc_status.unl
);
foreach $target (@infiles){
    print "$data_cache/$target\n";
    system ("cp -p $data_cache/$target .");
}
print "generating tracks\n";
my $cmd = "cat begin_work.sql " .
    "unload_assembly_clone_gff.sql " .
    "load_drerio_vega_id.sql " .
    "unload_zfin_genes_gff.sql " .
    "unload_alias_scattered.sql " .
    "unload_xpat_gff.sql " .
    "unload_pheno_gff.sql " .
    "unload_antibody_gff.sql " .
    "unload_zfin_morpholino.sql " . # should not need as much updating.
    "unload_vega_chromosome_gff.sql " .
#   "unload_mutant_gff.sql " .
#   "unload_zfin_transcript.sql ".
    "rollback.sql | $ENV{'INFORMIXDIR'}/bin/dbaccess  <!--|DB_NAME|-->";

system($cmd);
