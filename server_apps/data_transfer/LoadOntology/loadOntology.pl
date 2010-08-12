#!/private/bin/perl 

use MIME::Lite;

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadOntology/";
chdir "$dir";


my $fileUrl = $ARGV[0];
my $fileName = $ARGV[1];

# see paths.txt for paths to the fileName and the fileName itself.
#-------------------SubRoutines-------------#

sub downloadOntologyTermFiles () { # download the obo file from passed in fileUrl


    system("/local/bin/curl -sL $fileUrl -o $fileName") and die "can not download $fileName";
}

sub sendLoadReport ($) { # send email on error or completion
    
# . is concantenate
# $_[x] means to take from the array of values passed to the fxn, the 
# number indicated: $_[0] takes the first member.
    
    my $SUBJECT="Auto OntologyTerms:".$_[0];
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

$usage = "\nUsage: loadOntology.pl urlToFile(has to be a webaddress) fileName \n";

if (@ARGV < 2) {
  print $usage and exit;
}

print "loadOntology.pl running in: $dir"."\n" ;

#remove old files

system("/bin/rm -f *.unl") and die "can not rm unl" ;
system("/bin/rm -f *.txt") and die "can not rm txt" ;
system("/bin/rm -f *.obo") and die "can not rm obo" ;
system("/bin/rm -f sec_unload_report") and die "can not remove sec_unload_report";
system("/bin/rm -f *.ontology") and die "can not rm ontology";

&downloadOntologyTermFiles();

print "Download done\n";

# parseObo.pl is a script that fetches the OBO format
# flat file and parses it.

system("parseObo.pl $fileName") and die "can not parse obo file" ;



$count = 0;
$retry = 1;

# wait till parsing is finished

while( !( -e "term_parsed.unl")) {
    
    $count++;
    if ($count > 10) {
	if ($retry) {
	    $count = 0;
	    $retry = 0;
	    print "retry parseObo.pl\n";
	    system("parseObo.pl $fileName");
	}
	else {
	    print ("Failed to run parseObo.pl"); 
	    exit;
	}
    }  
}

# open the parsed OBO file and make sure that there are more than 10 lines
# written.

open(DEFS_PARSED, "< ./term_parsed.unl") or die "can't open term_parsed.unl";

$count++ while <DEFS_PARSED>;

if ($count < 10) {

    &sendLoadReport("parseObo.pl failed","<!--|VALIDATION_EMAIL_DBA|-->", "./term_parsed.unl") ;

}
else {	 

    print "term_parsed is not null $count \n";

}

# reset the counter and close the file.
$count = 0 ;

close DEFS_PARSED;

print "parsing obo file done\n";

#------------Loading Database---------------------#

print "loading...\n";

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadTerms.sql >out 2> report.txt") and die "loadTerms.sql failed";

if ($fileName eq "quality.obo"){
    system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> fixPatoAnnotations.sql >out 2> report.txt") and die "fixPatoAnnotations.sql failed";
}

if ($fileName eq "sequence_ontology.obo"){
    system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> fixSoAnnotations.sql >out 2> report.txt") and die "fixSoAnnotations.sql failed";
}



# wait for the files to be created by incrementing a counter
# while we wait. first, reset the count variable.

&countFileLines ("term_obsolete.unl", "loadOntology.pl failed at creating obsolete unload file");

# open the files we create and count line by line until we read the EOF.
# if the count is 0, then we don't want to send a report to curators.
# else, send the file to the curators.  Do this for both the secondary
# term report and the obsolete term report. first, reset the count variable.

#&isEmptyFile ("newannotsecterms.unl","No new secondary terms\n","<!--|VALIDATION_EMAIL_DBA|-->\@cs.uoregon.edu","Terms now secondary");


&isEmptyFile ("report.txt","loadterms.sql failed to run\n","<!--|VALIDATION_EMAIL_DBA|-->","SQL term load results");

&isEmptyFile ("new_terms.unl","no new terms\n","<!--|VALIDATION_EMAIL_DBA|-->","new terms\n");

&isEmptyFile ("terms_missing_obo_id.txt","no terms missing obo ids\n","<!--|VALIDATION_EMAIL_DBA|-->","terms missing obo ids\n");

&isEmptyFile ("term_no_longer_secondary.txt","No terms once secondary are now primary\n","<!--|VALIDATION_EMAIL_DBA|-->","Error in the .obo file?  Terms once secondary are now primary.");

system ("/bin/chmod 654 <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "could not chmod data_Transfer files";

system ("/bin/chgrp fishadmin <!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/*") and die "could not chmod data_Transfer files";

exit;
