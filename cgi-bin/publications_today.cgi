#!/local/bin/perl 

########################################################################
#                                                                      
# This script sends an email notification to curators@zfin.org listing
# the publication entered TODAY. 
#
# Publication date is determined with spl routine get_date_from_id().
# Email is sent using sendmail.
#
# November 2003
# Exclude temporary (status = "temporary") publications.
# Include publications whose status has changed from temporary to active.
#                                                                      
########################################################################

use DBI;
use MIME::Lite;


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$mailprog = '/usr/lib/sendmail -t -oi -oem';

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 


chdir "<!--|ROOT_PATH|-->/cgi-bin/";

openReport();

    $eight_digit_date = d8();

    $query = "select title, zdb_id, year(pub_date) as pyear, authors, source
              from publication
              where get_date_from_id(zdb_id,'YYYYMMDD') = '$eight_digit_date'
                and status = 'active'
              UNION
              select title, zdb_id, year(pub_date) as pyear, authors, source
              from publication, updates
              where zdb_id = rec_id
                and field_name = 'status'
                and new_value = 'active'
                and when::date = TODAY
              order by 2";

    open (REPORT, ">>report") or die "can not open report";

    print REPORT "ZFIN Publications added ".`date "+%a %b %e, %Y"`."\n";

    my $cur = $dbh->prepare($query);

    $cur->execute;
    my($title, $zdb, $pyear, $auth, $src);
    $cur->bind_columns(\$title,\$zdb,\$pyear,\$auth,\$src);

    $count = 0;
    while ($cur->fetch)
    {
      $auth = cleanTail($auth);
      $title = cleanTail($title);
      $src = cleanTail($src);
      print REPORT "[$zdb]\n";
      print REPORT "$auth ($pyear) $title. $src.\n\n";
      $count++;
    }
    print REPORT "\n";
    close(REPORT);

sendReport() if ($count != 0);


exit;


sub cleanTail () {
  my $var = $_[0];

  while ($var =~ /\000$/) {
    chop ($var);
  }

  return $var;
}


sub d8 {

  $date = `date '+%Y%m%d'`;
  
  chop($date);
  return $date;
}


sub writeReport()
  {
    open (REPORT, ">>report") or die "cannot open report";
    print REPORT "$_[0] \n\n";
    close (REPORT);
  }

sub openReport()
  {
    system("/bin/rm -f report");
    system("touch report");
  }

sub sendReport()
  {
    open(MAIL, "| $mailprog") || die "cannot open mailprog $mailprog, stopped";
    open(REPORT, "report") || die "cannot open report";

    print MAIL "To: curators\@zfin.org\n";
#    print MAIL "To: bsprunge\@cs.uoregon.edu\n";
    print MAIL "Subject: Publications Entered Today\n";
    while($line = <REPORT>)
    {
      print MAIL $line;
    }
    close (REPORT);
    close (MAIL);
    $dbh->disconnect();
  }



