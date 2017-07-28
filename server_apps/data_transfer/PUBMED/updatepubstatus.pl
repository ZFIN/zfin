#!/private/bin/perl
# updatepubstatus.pl
# This script first get a list of non-active publications with pubmed ids


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

print "$dbname\n\n";

#remove old report and log files
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfUpdatedPubs.txt");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/log1");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/log2");

### open a handle on the db
my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password)
  || die("Failed while connecting to <!--|DB_NAME|--> ");

$cur_get_nonactive_pubs = $dbh->prepare('select accession_no, zdb_id from publication where (status is null or status != "active") and accession_no is not null;');
$cur_get_nonactive_pubs->execute();
$cur_get_nonactive_pubs->bind_columns(\$pub_acc_no,\$pub_zdbId);

%nonActivePubAccessions = ();
$ctInactivePubs = 0;

while ($cur_get_nonactive_pubs->fetch()) {
  if ($pub_acc_no =~ /^\d+$/) {
    $nonActivePubAccessions{$pub_zdbId} = $pub_acc_no;
    $ctInactivePubs++;
  }
}

$cur_get_nonactive_pubs->finish();

print "\nctInactivePubs = $ctInactivePubs\n\n";

## if no inactive pub with pubmed id
if ($ctInactivePubs == 0) {
  #$subjectError = "Auto from " . $dbname . ": updatepubstatus.pl :: There is no non-active publications found with PUBMED accession.";
  #ZFINPerlModules->sendMailWithAttachedReport("xshao\@zfin.org",$subjectError,"updatepubstatus.pl");
    if (-s "<!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfActivatedPubs.txt"){
    }
    else {
	system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfActivatedPubs.txt");
    }
  exit;
}

$ctUpdated = 0;
%updatedPublications = ();

$cur_update_pub = $dbh->prepare_cached('update publication set status = "active" where accession_no = ?;');
$cur_insert_update = $dbh->prepare_cached('insert into updates (rec_id,field_name,new_value,upd_when) select zdb_id,"status","active",current from publication where accession_no = ?;');
$cur_insert_tracking = $dbh->prepare_cached('insert into pub_tracking_history (pth_pub_zdb_id, pth_status_id,  pth_status_set_by) select zdb_id, (select pts_pk_id from pub_tracking_status where pts_status= "NEW"), "ZDB-PERS-030612-1" from publication where accession_no = ?;');

foreach $pubZDBid (sort keys %nonActivePubAccessions) {
    $pubmedId = $nonActivePubAccessions{$pubZDBid};
    $url = 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=' . $pubmedId . '&retmode=xml';
    $content = get $url;
    if (defined $content) {
      if ($content =~ m/<PublicationStatus>(\w+)<\/PublicationStatus>/) {
        $status = $1;
        if ($status eq "ppublish" || $status eq "epublish") {
          $cur_update_pub->execute($pubmedId);
          $cur_insert_update->execute($pubmedId);
          $cur_insert_tracking->execute($pubmedId);
          $updatedPublications{$pubZDBid} = $pubmedId;
          $ctUpdated++;
        }
      }
      undef $content;
    }
}

print "\nctUpdated = $ctUpdated\n\n";

$cur_update_pub->finish();
$cur_insert_update->finish();

$dbh->disconnect();

open (ACTIVATED, ">listOfActivatedPubs.txt") ||  die "Cannot open listOfActivatedPubs.txt : $!\n";

foreach $updatedPubId (sort keys %updatedPublications) {
  $pubmedid = $updatedPublications{$updatedPubId};
  print ACTIVATED "$updatedPubId\t$pubmedid\n";
}

close(ACTIVATED);

if (-s "<!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfActivatedPubs.txt"){
    }
    else {
	system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfActivatedPubs.txt");
    }


#$subject = "Auto from $dbname : " . "updatepubstatus.pl :: " . "$ctUpdated of $ctInactivePubs non-active publications have been activated according to PUBMED";

#ZFINPerlModules->sendMailWithAttachedReport("van_slyke\@zfin.org,xshao\@zfin.org","$subject","listOfActivatedPubs.txt");

exit;



