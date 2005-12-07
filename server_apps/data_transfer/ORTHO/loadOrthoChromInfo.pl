#!/private/bin/perl

# FILE: loadOrthoChromInfo.pl

# DESCRIPTION: a control script to update chromosome info for ZFIN orthologue table.
# It first downloads the data files containing chromosome info of mouse, 
# human, fruit fly, and yeast. Then it executes the Perl scripts of parsing 
# the downloaded data files to extract chromosome info. Finally, it excecutes  
# the sql scripts of updating the chromosome info for ZFIN orthologue table.
# If something goes wrong in the process of downloading, parsing or loading data.
# an email will be sent to the database owner.

# INPUT VARS: none

# OUTPUT: Text files used for updating ZFIN orthologue table.
#         ZFIN orthologue table must have been updated after this script is excecuted.

# Scripts called from this script in order:
# Name                  Purpose
# ----------------------------------
# unloadOrthoData.sql   unload ZFIN exissting fly and yeast orthology data
# parseMGIdata.pl       parse orthology data file of MGI
# parseFlyBaseData.pl   parse orthology data file of FlyBase
# parseSGDdata.pl       parse orthology data file of SGD
# updatOrthologue.sql   update ZFIN orthologue table with chromosome info

# set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"DATABASE"}="<!--|DB_NAME|-->";

$mailprog = '/usr/lib/sendmail -t -oi -oem';
$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/";
chdir "$dir";
print "$dir\n";

# remove old files 
system("/bin/rm -f *.data") and die "can not rm old data file";
system("/bin/rm -f *.unl") and die "can not rm unl" ;
system("/bin/rm -f *.txt") and die "can not rm txt" ;
print "\nRemoving old data files is done.\n";

# download data file from MGI for updating human and mouse chromosome info 
system("/local/bin/wget -q ftp://ftp.informatics.jax.org/pub/reports/HMD_Human1.rpt -O MGI.data") and &emailError("Cannot download data file from MGI.");
print "\nDownloading from MGI is done\n";

# download data file from FlyBase for updating fly chromosome info 
system("/local/bin/wget -q ftp://flybase.net/genomes/Drosophila_melanogaster/current/fasta/dmel-all-gene-r4.2.1.fasta -O FlyBase.data") and &emailError("Cannot download data file from FlyBase.");
print "\nDownloading from FlyBase is done.\n";

# download data file from SGD for updating fly chromosome info
system("/local/bin/wget -q ftp://ftp.yeastgenome.org/yeast/data_download/chromosomal_feature/SGD_features.tab -O SGD.data") and &emailError("Cannot download data file from SGD.");
print "\nDownloading from SGD is done.\n";

# unload the current fly and yeast orthology data
system("$ENV{'INFORMIXDIR'}/bin/dbaccess $ENV{'DATABASE'} unloadOrthoData.sql") and &emailError("Unable to unload fly and yeast orthology data from orthologue table.");
print "\nUnloading fly and yeast orthology data from ZFIN orthologue table is done.\n";

# excecute the Perl scripts to do parsing and prepare .unl files for updating orthologue table
system("/local/bin/perl parseMGIdata.pl") and &emailError("Can not parse MGI data file");
print "\nParsing MGI data file is done.\n";
system("/local/bin/perl parseFlyBaseData.pl") and &emailError("Can not parse FlyBase data file");
print "\nParsing FlyBase data file is done.\n";
system("/local/bin/perl parseSGDdata.pl") and &emailError("Can not parse SGD data file");
print "Parsing SGD data file is done.\n";

# update orthologue table with the chromosome info
system("$ENV{'INFORMIXDIR'}/bin/dbaccess $ENV{'DATABASE'} updateOrthologue.sql >out 2> errReport.txt") and &emailError("Failed to update orthologue table");
print "Updating the table orthologue with chromosome info is done\n";

exit;

#=========================================
# emailError
#
# INPUT:
#    string ::  error message
# OUTPUT:
#    none
# EFFECT:
#    error message is sent to db owner.
#

sub emailError($)
  {
    open(MAIL, "| $mailprog") || die "Cannot open mailprog $mailprog";
    print MAIL "To: <!--|DB_OWNER|-->\@cs.uoregon.edu\n";
    print MAIL "Subject: loadOrthoChromInfo.pl error\n";
    print MAIL "Error:\n";
    print MAIL "$_[0]";
    close MAIL;
    exit;
  }