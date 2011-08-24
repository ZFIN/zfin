#!/private/bin/perl -w
#-----------------------------------------------------------------------
# can be run via:
#	gmake load_vega 
# 	gmake load_vega_commit
#
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

# note there is doc on creating 'drerio_vega_id.unl'
# in /research/zusers/tomc/data_transfer/VEGA/Assembly/ -- TODO check where

print "Fetching data files from $data_cache\n";

my @infiles = qw(
    drerio_vega_id.unl  vega_chromosome.gff3
    assembly_for_tom.tab  clone_acc_status.unl  
);

foreach $target (@infiles){
    print "$data_cache/$target\n";
    system ("cp -p $data_cache/$target .");
}

#foreach $argnum (0 .. $#ARGV) {print "$argnum \t $ARGV[$argnum]\n";}
# may be worth checking the assembly & morpholino data files exist 
# before adding them to the command.

my $cmd = "cat load_drerio_vega_id.sql unload_assembly_clone_gff.sql unload_zfin_morpholino.sql " ;
if ( "commit" =~ $ARGV[0] ) {
	$cmd .= " commit.sql";
	print "\n\t\tLoading Vega GFF3\n";
} else {
    $cmd .= " rollback.sql" ;
    print "\n\t\tTest Loading Vega GFF3\n";
}

system( $cmd . " | $ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|-->");
