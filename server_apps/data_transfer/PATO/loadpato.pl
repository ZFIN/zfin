#!/private/bin/perl 

# FILE: loadpato.pl
# PREFIX: lpato_
# DESCRIPTION: script that controls parsing and sql scripts for 
# loading and updating status of pato_terms in the pato_Term table at ZFIN.
# calls test.pl, parse_defs.r, and loadpatoterms.sql.  Emails are generated
# under conditions of error or obsolete or secondary term additions to 
# the pato_term table
# INPUT VARS: none
# OUTPUT VARS: 
# OUTPUT: unload files from the database, emails to PATO-team


use MIME::Lite;

#set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/PATO/";
chdir "$dir";
print "$dir"."\n" ;

#-------------------SubRoutines-------------#

sub downloadPATOtermFiles () { # download the obo file from PATO

    system("/local/bin/curl -s http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/quality.obo -o quality.obo") and die "can not download quality.obo";

    print "download done.\n" ;

    if ( -e "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/quality_old.obo") {

	system("/bin/rm <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/quality_old.obo") and die "can not rm quality_old.obo" ;

	print "rm'd quality_old.obo\n" ;
    }

    if ( -e "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/quality.obo") {
	
	system("/bin/mv <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/quality.obo <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/quality_old.obo") and die "can not mv quality_old.obo" ;
    
	print "mv'd quality.obo to quality_old.obo\n" ;
    }

}


sub sendLoadReport ($) { # send email on error or completion
    
# . is concantenate
# $_[x] means to take from the array of values passed to the fxn, the 
# number indicated: $_[0] takes the first member.
    
    my $SUBJECT="Auto PATOTerms:".$_[0];
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

system("/bin/rm -f *.unl") and die "can not rm unl" ;
system("/bin/rm -f *.txt") and die "can not rm txt" ;
system("/bin/rm -f *.obo") and die "can not rm obo" ;


&downloadPATOtermFiles();

print "Download done\n";

# parsePATOobo.pl is a script that fetches the OBO format PATO
# flat file and parses it.

system("parsePATOobo.pl quality.obo") and die "can not parse obo file" ;

$count = 0;
$retry = 1;

# wait till parsing is finished

while( !( -e "patoterm_parsed.unl")) {
    
    $count++;
    if ($count > 10) {
	if ($retry) {
	    $count = 0;
	    $retry = 0;
	    print "retry parsePATOobo.pl\n";
	    system("parsePATOobo.pl quality.obo");
	}
	else {
	    print ("Failed to run parsePATOobo.pl"); 
	    exit;
	}
    }  
}

# open the parsed OBO file and make sure that there are more than 10 lines
# written.

open(PATODEFS_PARSED, "< ./patoterm_parsed.unl") or die "can't open patoterm_parsed.unl";

$count++ while <PATODEFS_PARSED>;

if ($count < 10) {

    &sendLoadReport("parsePATOobo.pl failed","staylor\@cs.uoregon.edu", "./patoterm_parsed.unl") ;

}
else {	 

    print "patoterm_parsed is not null $count \n";

}

# reset the counter and close the file.
$count = 0 ;

close PATODEFS_PARSED;

print "parsing obo file done\n";

#------------Loading Database---------------------#

print "loading...\n";

# loadpatoterms.sql is a sql routine that checks for obsolete, secondary, and
# new pato terms from the 3 flat files and the OBO file.  It updates flags
# in ZFIN to reflect secondary or obsolete terms, and adds new terms to the
# pato_term table.  It also produces unload files with terms
# annotated to secondary or obsolete terms for curators to fix.
#
# Added on 7/13/2005: an addition to the loadpatoterms.sql script to check for
# obsolete or secondary PATO terms in the with field

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadpatoterms.sql >out 2> report.txt") and die "loadpatoterms.sql failed";

# wait for the files to be created by incrementing a counter
# while we wait. first, reset the count variable.

&countFileLines ("patoterm_obsolete.unl", "loadpato.pl failed at creating obsolete unload file");

# open the files we create and count line by line until we read the EOF.
# if the count is 0, then we don't want to send a report to curators.
# else, send the file to the curators.  Do this for both the secondary
# term report and the obsolete term report. first, reset the count variable.

#&isEmptyFile ("newannotsecterms.unl","No new secondary terms\n","staylor\@cs.uoregon.edu","Terms now secondary");


&isEmptyFile ("report.txt","loadpatoterms.sql failed to run\n","staylor\@cs.uoregon.edu","Pato SQL term load results");

&isEmptyFile ("updated_pato_terms.unl","no updated pato terms\n","staylor\@cs.uoregon.edu","Pato terms updated\n");

&isEmptyFile ("new_terms.unl","no new terms\n","staylor\@cs.uoregon.edu","new PATO terms\n");

&isEmptyFile ("terms_becoming_obsolete.unl","no new obsolete terms\n","staylor\@cs.uoregon.edu","new obsolete terms with annotations\n");

&isEmptyFile ("sec_unload_report","no annotations to secondary terms\n","staylor\@cs.uoregon.edu","new secondary terms report\n");

&isEmptyFile ("terms_missing_obo_id.txt","no terms missing obo ids\n","staylor\@cs.uoregon.edu","terms missing obo ids\n");

system ("/bin/chmod 654 <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "could not chmod data_Transfer files";

system ("/bin/chgrp fishadmin <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "could not chmod data_Transfer files";

exit;
