#!/local/bin/perl 

# FILE: loadgo.pl
# PREFIX: lgo_ (none added as of Feb. 2005)

# DESCRIPTION: script that controls parsing and sql scripts for 
# loading and updating status of go_terms in the go_Term table at ZFIN.
# calls test.pl, parse_defs.r, and loadgoterms.sql.  Emails are generated
# under conditions of error or obsolete or secondary term additions to 
# the go_term table

# INPUT VARS: none
# OUTPUT VARS: 

# OUTPUT: unload files from the database, emails to GO-team


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

print "loading...\n";

# loadgoterms.sql is a sql routine that checks for obsolete, secondary, and
# new go terms from the 3 flat files and the OBO file.  It updates flags
# in ZFIN to reflect secondary or obsolete terms, and adds new terms to the
# go_term table.  It also produces unload files with terms
# annotated to secondary or obsolete terms for curators to fix.
#
# Added on 7/13/2005: an addition to the loadgoterms.sql script to check for
# obsolete or secondary GO terms in the with field

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> goreport.sql") and die "loadgoterms.sql did not complete successfully";

system ("rm -rf golist.txt.gz");
system ("gzip golist.txt > golist.txt.gz");

sendResults();
exit;
