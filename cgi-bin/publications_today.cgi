#!/private/bin/perl 

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


chdir "<!--|ROOT_PATH|-->/<!--|CGI_BIN_DIR_NAME|-->/";

openReport();

    $eight_digit_date = d8();

    $query = "select title, zdb_id, year(pub_date) as pyear, authors, jrnl_abbrev, pub_volume, pub_pages
              from publication, journal
              where get_date_from_id(zdb_id,'YYYYMMDD') = '$eight_digit_date'
                and status = 'active'
                and jrnl_zdb_id = pub_jrnl_zdb_id
              UNION
              select title, zdb_id, year(pub_date) as pyear, authors, jrnl_abbrev, pub_volume, pub_pages
              from publication, updates, journal
              where zdb_id = rec_id
                and jrnl_zdb_id = pub_jrnl_zdb_id
                and field_name = 'status'
                and new_value = 'active'
                and when::date = TODAY
              order by 2";

    open (REPORT, ">>report") or die "can not open report";

    print REPORT "ZFIN Publications added ".`date "+%a %b %e, %Y"`."\n";

    my $cur = $dbh->prepare($query);

    $cur->execute;
    my($title, $zdb, $pyear, $auth, $jrnl_abbrev, $vol, $pages);
    $cur->bind_columns(\$title,\$zdb,\$pyear,\$auth,\$jrnl_abbrev,\$vol,\$pages);

    $count = 0;
    while ($cur->fetch)
    {
      $auth = cleanTail($auth);
      $title = cleanTail($title);
      $jrnl_abbrev = cleanTail($jrnl_abbrev);
      $vol = cleanTail($vol);
      $pages = cleanTail($pages);
      print REPORT "[$zdb]\n";
      print REPORT "$auth ($pyear) $title. $jrnl_abbrev. $vol:$pages\n\n";
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

    print MAIL "To: curators\@zfin.org,mvalle\@uoneuro.uoregon.edu,zfinadmn\@zfin.org\n";
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



