#!/local/bin/perl 

#
# loadsp.pl
#
use MIME::Lite;
#------------------- Download -----------

sub downloadGOtermFiles () {

   system("/local/bin/wget -q http://www.geneontology.org/ontology/function.ontology -O function.ontology");
   system("/local/bin/wget -q http://www.geneontology.org/ontology/process.ontology -O process.ontology");
   system("/local/bin/wget -q http://www.geneontology.org/ontology/component.ontology -O component.ontology");

 }
#   Main
#


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

#chdir "/research/zcentral/www_homes/hoover/server_apps/data_transfer/LoadGO/";

#remove old files
 
system("/bin/rm -f *.ontology");
system("/bin/rm -f *.unl");
system("/bin/rm -f *.txt");

&downloadGOtermFiles();

print "Download done\n";

system("parse_defs.r");

$count = 0;
$retry = 1;
# wait till parsing is finished
while( !( -e "godefs_parsed.unl")) {

  $count++;
  if ($count > 10)
  {
    if ($retry) 
    {
      $count = 0;
      $retry = 0;
      print "retry parse_defs.r\n";
      system("parse_defs.r");
    }
    else
    {
         &sendErrorReport("Failed to run parse_defs.r"); 
      exit;
    }
  }  
}

print "parsing obo file done\n";
print "parsing ontology.pl function.ontology process.ontology component.ontology\n";

system ("test.pl function.ontology process.ontology component.ontology");

$count = 0;
$retry = 1;

# wait till parsing is finished
while( !( -e "ontology.unl")) {

  $count++;
  if ($count > 10)
  {
    if ($retry) 
    {
      $count = 0;
      $retry = 0;
      print "retry ontology.pl\n";
      system("test.pl function.ontology process.ontology component.ontology");
    }
    else
    {
         &sendErrorReport("Failed to run ontology.pl"); 
      exit;
    }
  }  
}

#create output of new secondary and obsolete terms.

sub sendLoadReport ($) {

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
# ------------ Loading ---------------------
print "loading...\n";

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> loadgoterms.sql >out 2> report.txt");

#wait for the files to be created by incrementing a counter
#while we wait. first, reset the count variable.

$count = 0;

#while the new_obsolete_terms.unl file does not exist,

    while( !( -e "./new_obsolete_terms.unl")) {

#auto incriment the counter

  $count++;

#if the count gets too large, fail the load as we don't want any infinite
#counting

  if ($count > 100) {
      print "loadgo.pl failed at creating obsolete unload file" ;
      exit ;
  }
}

#wait for the files to be created by incrementing a counter
#while we wait. first, reset the count variable

$count = 0;

#while the newannotsecterms.unl file does not exist,

    while( !( -e "./newannotsecterms.unl")) {

#auto incriment the counter

  $count++;

#if the count gets too large, fail the load as we don't want any infinite
#counting

 if ($count > 100) {
      print "loadgo.pl failed at creating secondary term unload file" ;
      exit ;
  }
}

#open the files we create and count line by line until we read the EOF.
#if the count is 0, then we don't want to send a report to curators.
#else, send the file to the curators.  Do this for both the secondary
#term report and the obsolete term report. first, reset the count variable.

$count = 0;

#count the number of lines read from the file.

open(FILE1, "< ./newannotsecterms.unl") or die "can't open $file";

$count++ while <FILE1>;

#count now holds the number of lines read

  if ($count < 1) {
      print "No new secondary terms\n" ;
  }
  else {
    &sendLoadReport("Terms now secondary","doughowe\@uoregon.edu", 
		"./newannotsecterms.unl") ;
    &sendLoadReport("Terms now secondary","staylor\@cs.uoregon.edu", 
		"./newannotsecterms.unl") ;
  }

$count = 0;

#count the number of lines read from the file. 

open(FILE2, "< ./new_obsolete_terms.unl") or die "can't open $file";

$count++ while <FILE2>;

#count now holds the number of lines read

  if ($count < 1) {
      print "No new obsolete terms\n" ;
  }
  else {
    
      &sendLoadReport("Terms now obsolete","staylor\@cs.uoregon.edu",
		      "./new_obsolete_terms.unl");
       &sendLoadReport("Terms now obsolete","doughowe\@uoregon.edu",
		      "./new_obsolete_terms.unl");
  }

exit;
