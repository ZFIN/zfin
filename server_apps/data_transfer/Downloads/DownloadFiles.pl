#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create data files for public download.
#
# We extract several different kinds of information:
# 
# All genetic markers (includes genes, ests, sslps, etc.)
#	zfin id, name, symbol, type
#	
# Synonyms  (for any item in all genetic markers file) There may be multiple lines 	
#   per zfin id
#	zfin id, synonym 
#
# Orthology - separate files for: 
#   zebrafish - human
#	zfin id , zebrafish symbol, human symbol, OMIM id, Entrez Gene id
#   zebrafish - mouse
#	zfin id , zebrafish symbol, mouse symbol, MGI id, Entrez Gene id
#   zebrafish - fly
#	zfin id,  zebrafish symbol, fly symbol,  Flybase id
#   zebrafish - yeast
#	zfin id,  zebrafish symbol, yeast symbol,  SGD id
#
# Gene Onotology-
#	A copy of the file we send to GO.
#
# Gene Expression
#	gene zfin id , gene symbol, probe zfin id, probe name, expression type,
#       expression pattern zfin id, pub zfin id, genotype zfin id, 
#       experiment zfin id#
# Mapping data
#	zfin id, symbol, panel symbol, LG, loc, metric
#
# Sequence data - separate files for GenBank, RefSeq, EntrezGene, Unigene, 
# SWISS-PROT, Interpro
#	zfin id, symbol, accession number
#	
# Genotypes
#	zfin id, allele/construct, type, gene symblol, corresponding zfin gene id
#
# Morpholino
#       zfin id of gene, gene symbol, zfin id of MO, MO symbol, public note



# define GLOBALS

# set environment variables

$ENV{"DBDATE"}="Y4MD-";

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";

$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";

$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";

$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> DownloadFiles.sql");
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> patoNumbers.sql");

system("./generateStagedAnatomy.pl");

# remove HTML tags from the public note column of the download file of Morpholino data
$fileWithHTMLtags = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos2.txt';
open (INP, $fileWithHTMLtags) || die "Can't open $fileWithHTMLtags : $!\n";
@lines=<INP>;
$fileWithNoHTMLtags = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos.txt';
open (RESULT,  ">$fileWithNoHTMLtags") || die "Can't open: $fileWithNoHTMLtags $!\n";
foreach $line (@lines) {
  $line =~ s/<[^<>]+>//g;
  print RESULT "$line";
}
close INP;
close RESULT;
