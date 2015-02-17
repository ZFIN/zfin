#!/private/bin/perl
# updatePublicationDate.pl
# This script first gets a list of publications with missing date, and then gets date information from PUBMED and do the update.


use DBI;
use LWP::Simple;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

#remove old report
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfPubsWithDateUpdated.txt");

### open a handle on the db
my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password)
  || die("Failed while connecting to <!--|DB_NAME|--> ");

$cur_get_pubs_with_no_date = $dbh->prepare('select accession_no, zdb_id from publication where pub_date is null or pub_date="" order by zdb_id;');
$cur_get_pubs_with_no_date->execute();
$cur_get_pubs_with_no_date->bind_columns(\$pub_acc_no,\$pub_zdbId);

%noDatePubAccessions = ();
$ctNoDatePubs = 0;

while ($cur_get_pubs_with_no_date->fetch()) {
  if ($pub_acc_no =~ /^\d+$/) {
    $noDatePubAccessions{$pub_zdbId} = $pub_acc_no;
    $ctNoDatePubs++;
  }
}

$cur_get_pubs_with_no_date->finish();

print "\nctNoDatePubs = $ctNoDatePubs\n\n";


$ctUpdated = 0;
%updatedPublications = ();

$cur_update_pub = $dbh->prepare_cached('update publication set pub_date = ? where accession_no = ?;');
$cur_insert_update = $dbh->prepare_cached('insert into updates (rec_id,field_name,new_value,when) select zdb_id,"pub_date","new date",current from publication where accession_no = ?;');

foreach $pubZDBid (sort keys %noDatePubAccessions) {
    $pubmedId = $noDatePubAccessions{$pubZDBid};
    $url = 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=' . $pubmedId . '&retmode=xml';
    $content = get $url;
    if (defined $content) {
      @fields1 = split(/<DateCreated>/, $content);
      $rightPart1 = $fields1[1];
      @fields2 = split(/<\/DateCreated>/, $rightPart1);
      $leftPart1 = $fields2[0];
      if ($leftPart1 =~ m/<Month>(\w+)<\/Month>/) {
            $monthCreated = $1;
            $monthCreated =~ s/^\s+//; 
            $monthCreated =~ s/\s+$//; 
            $monthCreated = ZFINPerlModules->month3LettersToNumber($monthCreated) if $monthCreated !~ m/\d/ ;
            $monthCreated = "0" . $monthCreated if $monthCreated =~ m/\d/ && $monthCreated !~ m/^0/ &&  $monthCreated < 10;            
      } else {
            $monthCreated = "-1";
      }

      @fields3 = split(/<PubDate>/, $content);
      $rightPart2 = $fields3[1];
      @fields4 = split(/<\/PubDate>/, $rightPart2);
      $leftPart2 = $fields4[0];

      if ($leftPart2 =~ m/<Year>(\d{4})<\/Year>/) {
            $year = $1;
      } else {
            $year = "-1";
      }
      
      if ($leftPart2 =~ m/<Month>(\w+)<\/Month>/) {
          $month = $1;
          $month =~ s/^\s+//; 
          $month =~ s/\s+$//;             
          $month = ZFINPerlModules->month3LettersToNumber($month) if $month !~ m/\d/ ;
          $month = "0" . $month if $month =~ m/\d/ && $month !~ m/^0/ && $month < 10;
      } else {
          $month = $monthCreated;
      }
      
      if ($leftPart2 =~ m/<Day>(\d+)<\/Day>/) {
          $day = $1;
          $day =~ s/^\s+//; 
          $day =~ s/\s+$//;             
          $day = "0" . $day if $day =~ m/\d/ && $day !~ m/^0/ && $day < 10;
      } else {
          $day = '01';
      }  

      if ($year != "-1" && $month != "-1") {
        $newDate = $month . "/" . $day . "/" . $year;
        $cur_update_pub->execute($newDate, $pubmedId);
        $cur_insert_update->execute($pubmedId);
        $updatedPublications{$pubZDBid} = $pubmedId;
        $ctUpdated++;               
      }
            
      undef $content;
      undef @fields1;
      undef @fields2;
      undef @fields3;
      undef @fields4;
      undef @rightPart1;
      undef $rightPart2;
      undef $leftPart1;
      undef $leftPart2;
      undef $monthCreated;
      undef $year;
      undef $month;
      undef $day;
      
    }  ## end of if defined $content
}

print "\nctUpdated = $ctUpdated\n\n";

$cur_update_pub->finish();
$cur_insert_update->finish();

$dbh->disconnect();

open (UPDATED, ">listOfPubsWithDateUpdated.txt") ||  die "Cannot open listOfPubsWithDateUpdated.txt : $!\n";

foreach $pubId (sort keys %noDatePubAccessions) {
  $pubmedid = $noDatePubAccessions{$pubId};
  if (exists($updatedPublications{$pubId})) {
       print UPDATED "$pubId\t$pubmedid\n";
  } else {
       print UPDATED "Not updated (still missing pub_date): $pubId\t$pubmedid\n";
  }
}

close(UPDATED);

if (-s $listOfPubsWithDateUpdated.txt) {
    # The file is not empty
}
else {
    system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfPubsWithDateUpdated.txt");
}
exit;




