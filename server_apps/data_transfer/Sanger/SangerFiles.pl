#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create data files for Kirstin (& Carol) to download.
# specification request 
# From: Kerstin Jekosch <kj2@sanger.ac.uk>
# Date: Mon, 21 Jul 2003 12:10:32 +0100	
##What I would need is
##1. sequence that was used for testing expression (accession number)
##2. your ZFIN id (ZDB-XPAT?) for the expression so that I can link
##3. the name of the lab that generated that so that I can link (not 
##'really' necessary as you do that already)
##number 2. being the essential part of it. The file I can download from 
##your page doesn't give me the accession (os let's say I can't find it 
##:o). What kind of identifier eg. is ibd5023?
##example:
##ZDB-GENE-000607-35      id:ibd5023      RNA in situ     ZDB-XPAT-020919-362     
##ZDB-GENE-000607-36      id:ibd5059      RNA in situ     ZDB-XPAT-020919-448     
##ZDB-GENE-000607-37      id:ibd5050      RNA in situ     ZDB-XPAT-020919-422     
##Thanks in advance Kerstin
#
# Sanger Expression
# EST_sequence_link_accession, gene zfin id , gene symbol, expression type, expression pattern zfin id, lab_name
#
# define GLOBALS

# set environment variables

$ENV{"DBDATE"}="Y4MD-";

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";

$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";

$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";

$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Sanger";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> SangerFiles.sql");


