#! /private/bin/perl -w 


##
# elsevier_report.pl
# script runs once a week to generate a list of elsevier pubs
# for Dave F. 
##

use DBI;
use MIME::Lite;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$mailprog = '/usr/lib/sendmail -t -oi -oem';

# subroutines to send email reports to curators.

sub openReport()
  {
    system("/bin/rm -f reportElsevier");
    system("/bin/touch reportElsevier");
  }

sub sendReport()
  {
    open(MAIL, "| $mailprog") 
	|| die "cannot open mailprog $mailprog, stopped";
 
    open(REPORT, "reportElsevier") 
	|| die "cannot open reportElsevier";

    print MAIL "To:dfashena\@uoneuro.uoregon.edu,xshao\@cs.uoregon.edu\n";

    print MAIL "Subject: AutoGen: Elsevier publication report\n";

    while($line = <REPORT>)
    {
	print MAIL $line;
    }
    close (REPORT);

    close (MAIL);
  }


## -------  MAIN -------- ##

# open a handle on the db

my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
                       {AutoCommit => 1,RaiseError => 1}
                      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 


# move into the appropriate directory

chdir "<!--|ROOT_PATH|-->/server_apps/Report/";

# set the mail program

$mailprog = '/usr/lib/sendmail -t -oi -oem';

# generate the Elsevier publication report for Dave F.

openReport();

open (REPORT, "> reportElsevier") or die "can not open report" ;

$elsevier_query = "select distinct zdb_id,jrnl_name,entry_time from record_attribution, publication, journal
                    where recattrib_source_zdb_id = zdb_id
                      and pub_jrnl_zdb_id = jrnl_zdb_id
                      and jrnl_publisher in ('Elsevier','Cell Press') 
                      and jrnl_is_nice = 't' 
                 order by jrnl_name, entry_time desc;";


# execute the query

my $elsevier_cur = $dbh->prepare($elsevier_query);

$elsevier_cur->execute;

my($pub_zdb_id,$journal,$time);

$elsevier_cur->bind_columns(\$pub_zdb_id,\$journal,\$time);


my $grandTotal = 0;
my $total = 0;

my $prev_journal = "";

print "\nStarted SQL for the elsevier report ....\n\n";

while ($elsevier_cur->fetch) {
   print REPORT "$journal\n-------------------------------\n" if $grandTotal == 0;   
   $grandTotal++;
   $prev_journal = $journal if $grandTotal == 1;
   if ($prev_journal eq $journal) {
       $total++;       
   } else {
       print REPORT "Total with $prev_journal: $total\n";
       print REPORT "\n\n$journal\n-------------------------------\n";
       $total = 1;
   }
   
   print REPORT "$pub_zdb_id\n";
   $prev_journal = $journal;
}
print REPORT "Total with $prev_journal: $total\n";

print REPORT "\n\nGrand total number of Elsevier publications with at least one record in ZFIN record_attribution table: $grandTotal\n\n";

close(REPORT);

sendReport();

print "\nDone\n\n";

# close the connection to the database.

$dbh->disconnect();



exit;
