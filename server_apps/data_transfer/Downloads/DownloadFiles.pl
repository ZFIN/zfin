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
#	zfin id , zebrafish symbol, human symbol, OMIM id, LocusLink id
#   zebrafish - mouse
#	zfin id , zebrafish symbol, mouse symbol, MGI id, LocusLink id
#   zebrafish - fly
#	zfin id,  zebrafish symbol, fly symbol,  Flybase id
#   zebrafish - yeast
#	zfin id,  zebrafish symbol, yeast symbol,  SGD id
#
# Gene Onotology-
#	A copy of the file we send to GO.
#
# Gene Expression
#	gene zfin id , gene symbol, expression type, expression pattern zfin id
#
# Mapping data
#	zfin id, symbol, panel symbol, LG, loc, metric
#
# Sequence data - separate files for GenBank, RefSeq, LocusLink, Unigene, 
# SWISS-PROT, Interpro
#	zfin id, symbol, accession number
#	
# Alleles
#	zfin id, allele, locus, corresponding zfin gene id, gene symbol



# define GLOBALS

# set environment variables

$ENV{"DBDATE"}="Y4MD-";

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";

$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";

$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";

$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> DownloadFiles.sql");


