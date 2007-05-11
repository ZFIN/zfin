#!/private/bin/perl 

# FILE: goreport.pl

# DESCRIPTION: FB 1608/1609. Reports attributions for a gene and also reports
# status of curation.
# calls goreport.sql

# INPUT VARS: none
# OUTPUT VARS: 



use MIME::Lite;

#set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|ROOT_PATH|-->/server_apps/Reports/GO/";
chdir "$dir";
print "$dir"."\n" ;


#-------------------SubRoutines-------------#

sub sendResults {
    
    
    my $SUBJECT="Auto:golist.txt file";
    my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";
    my $ATTFILE = "golist.txt.gz";
 
    # Create a new multipart message:
    $msg1 = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';

    attach $msg1 
        Type     => 'application/octet-stream',
        Encoding => 'base64',
        Path     => "$ATTFILE",
        Filename => "$ATTFILE";

    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg1->print(\*SENDMAIL);
    close (SENDMAIL);
    
}



#-----------------------MAIN--------------------#


system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> goreport.sql") and die "goreport.sql did not complete successfully";

system ("/bin/rm -rf golist.txt.gz");
system ("/bin/gzip golist.txt > golist.txt.gz");

sendResults();
exit;
