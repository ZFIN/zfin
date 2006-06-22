#!/private/bin/perl

# FILE: loadSNPdata.pl

# DESCRIPTION: a control script to load SNP data downloaded and parsed and
# validated into ZFIN.
# EFFECT:      SNP data inserted into ZFIN marker table, marker_relation table, etc.
# Caution:     in addition to snp.unl, the validated result file snp.unl must be existent

# Scripts called from this script in order:
# Name           Purpose
# --------------------------------------------------------------------------------------- 
# snp.pl         download SNP data files from NCBI and initially parse them
# sortSNPs.pl    fully parse the SNP data and generate datafile for curator's validation
# loadSNPs.sql   load SNP data into ZFIN, if they are not there

use MIME::Lite;

$mailprog = '/usr/lib/sendmail -t -oi -oem';
$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/SNP/";
chdir "$dir";
print "$dir\n";

# set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"DATABASE"}="<!--|DB_NAME|-->";

system("/local/bin/perl snp.pl") and &emailError("Unable to download or sth wrong with initial parsing.");
system("/local/bin/perl sortSNPs.pl") and &emailError("sth wrong with initial parsing.");
system("$ENV{'INFORMIXDIR'}/bin/dbaccess $ENV{'DATABASE'} loadSNPs.sql >out 2> errReport.txt") and &emailError("Failed to load SNP data");
print "SNP data loaded\n";

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
    print MAIL "Subject: loadSNPdata.pl $_[0]\n";
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
    
    my $SUBJECT="Auto LoadSNPs:".$_[0];
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