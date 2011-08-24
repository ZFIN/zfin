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
# $data_cache = "/research/zusers/tomc/data_transfer/

# note there is doc on creating 'drerio_ensembl.unl'
# in ZFIN_WWW/server_apps/data_transfer/Ensembl/ 

print "Fetching data files from $data_cache\n";

my @infiles = qw(
    drerio_ensembl.unl ensembl_contig.gff3
);
#ensembl_chromosome.gff3

foreach $target (@infiles){
    print "$data_cache/$target\n";
    system ("cp -p $data_cache/$target .");
}

#foreach $argnum (0 .. $#ARGV) {print "$argnum \t $ARGV[$argnum]\n";}

my $cmd = "cat load_drerio_ensembl.sql"; 
if ( $ARGV[0] && "commit" =~ $ARGV[0] ) {
	$cmd .= " commit.sql";
	print "\n\t\tLoading Ensembl GFF3\n";
}else{
    $cmd .= " rollback.sql" ;
    print "\n\t\tTest Loading Ensembl GFF3\n";
}

system( $cmd . " | $ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|-->");


