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

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadGO/";
chdir "$dir";
print "$dir" ;


#-------------------SubRoutines-------------#

sub downloadGOtermFiles () { # download the 3 flat files from GO

    system("/local/bin/wget -q http://www.geneontology.org/ontology/function.ontology -O function.ontology") and die "can not download function";
    system("/local/bin/wget -q http://www.geneontology.org/ontology/process.ontology -O process.ontology") and die "can not download process";
    system("/local/bin/wget -q http://www.geneontology.org/ontology/component.ontology -O component.ontology") and die "can not download component";

}

#create output of new secondary and obsolete terms.

sub sendLoadReport ($) { # send email on error or completion
    
#. is concantenate
#$_[x] means to take from the array of values passed to the fxn, the 
#number indicated: $_[0] takes the first member.
    
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

sub countFileLines () { # count the number of lines in a file. Takes the
    # filename and the error message that gets printed if the file
    # can not be found as parameters. The ./ gets added to the filename
    # by the routine.

    my $filename = $_[0];
    my $errorMessage = $_[1];

    $count = 0;

# while the newannotsecterms.unl file does not exist,

    while( !( -e "./$filename")) {
	
# auto incriment the counter

	$count++;

# if the count gets too large, fail the load as we don't want any infinite
# counting

	if ($count > 100) {
	    print "$errorMessage" ;
	    exit ;
	}
    }   
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


#-----------------------MAIN--------------------#

#remove old files
 
system("/bin/rm -f *.ontology") and die "can not rm ontology";
system("/bin/rm -f *.unl") and die "can not rm unl" ;
system("/bin/rm -f *.txt") and die "can not rm txt" ;

&downloadGOtermFiles();

print "Download done\n";

# parse_defs.r is a rebol script that fetch the OBO format GO
# flat file and parses it.  We do this because eventually
# we'll need to convert our parsing scripts for the flat files to 
# parsing scripts for the OBO file, and because the obsolete terms are
# easier to pull out of the OBO file.

system("parse_defs.r") and die "can not parse_defs" ;

$count = 0;
$retry = 1;

# wait till parsing is finished

while( !( -e "godefs_parsed.unl")) {
    
    $count++;
    if ($count > 10) {
	if ($retry) {
	    $count = 0;
	    $retry = 0;
	    print "retry parse_defs.r\n";
	    system("parse_defs.r");
	}
	else {
	    print ("Failed to run parse_defs.r"); 
	    exit;
	}
    }  
}

# open the parsed OBO file and make sure that there are more than 10 lines
# written.  The OBO file should contain > 18K records as of Feb.2005.  Less
# than 10 lines indicates a problem with the parser.

open(GODEFS_PARSED, "< ./godefs_parsed.unl") or die "can't open godefs_parsed";

$count++ while <GODEFS_PARSED>;

if ($count < 10) {

    &sendLoadReport("parse_defs.r failed","<!--|GO_EMAIL_CURATOR|-->", "./godefs_parsed.unl") ;

}
else {	 

    print "godefs_parsed is not null $count \n";

}

# reset the counter and close the file.
$count = 0 ;

close GODEFS_PARSED;

print "parsing obo file done\n";

print "parsing ontology.pl function.ontology process.ontology component.ontology\n";

# test.pl is a perl script that does the actual parsing of the 3 ontology
# flat files

system ("test.pl function.ontology process.ontology component.ontology") and die "can not test.pl";

$count = 0;
$retry = 1;

# wait till parsing is finished
while( !( -e "ontology.unl")) {
    
    $count++;
    if ($count > 10) {
	if ($retry) {
	    $count = 0;
	    $retry = 0;
	    print "retry ontology.pl\n";
	    system("test.pl function.ontology process.ontology component.ontology") and die "can not test.pl second time";
	}
	else {
	    print("Failed to run ontology.pl"); 
	    exit;
	}
    }  
}


#------------Loading Database---------------------#

print "loading...\n";

# loadgoterms.sql is a sql routine that checks for obsolete, secondary, and
# new go terms from the 3 flat files and the OBO file.  It updates flags
# in ZFIN to reflect secondary or obsolete terms, and adds new terms to the
# go_term table.  It also produces unload files with terms
# annotated to secondary or obsolete terms for curators to fix.
#
# Added on 7/13/2005: an addition to the loadgoterms.sql script to check for
# obsolete or secondary GO terms in the with field

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadgoterms.sql >out 2> report.txt");

# wait for the files to be created by incrementing a counter
# while we wait. first, reset the count variable.

&countFileLines ("new_obsolete_terms.unl", "loadgo.pl failed at creating obsolete unload file");

&countFileLines ("newannotsecterms.unl","loadgo.pl failed at creating secondary term unload file");

# open the files we create and count line by line until we read the EOF.
# if the count is 0, then we don't want to send a report to curators.
# else, send the file to the curators.  Do this for both the secondary
# term report and the obsolete term report. first, reset the count variable.

&isEmptyFile ("newannotsecterms.unl","No new secondary terms\n",
	      "<!--|GO_EMAIL_CURATOR|-->","Terms now secondary");


&isEmptyFile ("new_obsolete_terms.unl","No new obsolete terms\n",
	      "<!--|GO_EMAIL_CURATOR|-->","Terms now obsolete");

&isEmptyFile ("obso_sec_with.unl","No obsolete or secondary terms in with field\n",
	      "<!--|GO_EMAIL_CURATOR|-->","Inferred from terms (With) terms now obsolete/secondary");

exit;

