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

use MIME::Lite;

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
system("/bin/rm -f *.indexFile") and die "can not rm FlyBase indexFile" ;

print "\nRemoving old data files is done.\n";

# download 2 data files from MGI for updating human and mouse chromosome info 
system("/local/bin/wget -q ftp://ftp.informatics.jax.org/pub/reports/HMD_Human1.rpt -O MGI.data") and &emailError("cannot download data file 1 from MGI.");
system("/local/bin/wget -q ftp://ftp.informatics.jax.org/pub/reports/HMD_Rat1.rpt -O MGI2.data") and &emailError("cannot download data file 2 from MGI.");
print "\nDownloading from MGI is done\n";

# download data file from FlyBase for updating fly chromosome info  
system("/local/bin/wget -q ftp://flybase.net/genomes/Drosophila_melanogaster/current/fasta/ -O FlyBase.indexFile") and &emailError("cannot download index file for FlyBase data files.");
open (FLYBASE, "FlyBase.indexFile") || &emailError("Cannot open FlyBase.indexFile : $!\n");
@lines=<FLYBASE>;
close(FLYBASE);
foreach $line (@lines) {
  if ($line =~ m/>dmel-all-gene-r(.+)</) {
    $ftp = "ftp://flybase.net/genomes/Drosophila_melanogaster/current/fasta/dmel-all-gene-r" . $1;
    last;
  }
}
print "\n$ftp\n";
system("/local/bin/wget -q $ftp -O FlyBase.data.gz") and &emailError("cannot download data file from FlyBase.");
system("/local/bin/gunzip FlyBase.data.gz") and &emailError("cannot decompress data file of FlyBase.");
print "\nDownloading and decompressing FlyBase file is done.\n";

# download data file from SGD for updating fly chromosome info
system("/local/bin/wget -q ftp://ftp.yeastgenome.org/yeast/data_download/chromosomal_feature/SGD_features.tab -O SGD.data") and &emailError("cannot download data file from SGD.");
print "\nDownloading from SGD is done.\n";

# unload the current fly and yeast orthology data
system("$ENV{'INFORMIXDIR'}/bin/dbaccess $ENV{'DATABASE'} unloadOrthoData.sql") and &emailError("unable to unload fly and yeast orthology data from orthologue table.");
print "\nUnloading fly and yeast orthology data from ZFIN orthologue table is done.\n";

# excecute the Perl scripts to do parsing and prepare .unl files for updating orthologue table
system("/private/bin/perl parseMGIdata.pl") and &emailError("Can not parse MGI data file");
print "\nParsing MGI data file is done.\n";
system("/private/bin/perl parseFlyBaseData.pl") and &emailError("Can not parse FlyBase data file");
print "\nParsing FlyBase data file is done.\n";
system("/private/bin/perl parseSGDdata.pl") and &emailError("Can not parse SGD data file");
print "Parsing SGD data file is done.\n";

# update orthologue table with the chromosome info
system("$ENV{'INFORMIXDIR'}/bin/dbaccess $ENV{'DATABASE'} updateOrthologue.sql >out 2> errReport.txt") and &emailError("Failed to update orthologue table");
print "Updating the table orthologue with chromosome info is done\n";

&isEmptyFile ("duplicate_ortho_symbol.unl","No duplicate ortholog symbols",
	      "<!--|DB_OWNER|-->\@cs.uoregon.edu","Duplicate Ortholog Symbols");

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
    print MAIL "Subject: loadOrthoChromInfo.pl $_[0]\n";
    print MAIL "Error:\n";
    print MAIL "$_[0]";
    close MAIL;
    exit;
  }
sub isEmptyFile() { # much like the count lines in a file routine, except
    # this one determines whether or not to send an email.  Email is only
    # sent if the file is not empty.

    my $filename = $_[0];
    my $printMessage = $_[1];
    my $emailAddress = $_[2];
    my $emailHeader = $_[3];
    
    $count = 0;
    
# count the number of lines read from the file.

    open(FILE1, "< ./$filename") or die "can't open $filename";
    
    $count++ while <FILE1>;

# count now holds the number of lines read

    if ($count < 1) {
	print "$printMessage" ;
    }
    else {
	&sendLoadReport("$emailHeader","$emailAddress", 
			"./$filename") ;
    }
    close FILE1;
}

sub sendLoadReport ($) { # send email on error or completion
    
# . is concantenate
# $_[x] means to take from the array of values passed to the fxn, the 
# number indicated: $_[0] takes the first member.
    
    my $SUBJECT="Auto LoadGOTerms:".$_[0];
    my $MAILTO=$_[1];
    my $TXTFILE=$_[2];
    
    # Create a new multipart message:
    $msg1 = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';

    attach $msg1 
	Type     => 'text/plain',   
	Path     => "$TXTFILE";

    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg1->print(\*SENDMAIL);
    close (SENDMAIL);
    
}
