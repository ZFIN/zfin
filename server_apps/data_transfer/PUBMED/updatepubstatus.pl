#!/private/bin/perl 


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
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfUpdatedPubs");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/log1");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/log2");

### open a handle on the db
my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
  || die("Failed while connecting to <!--|DB_NAME|--> ");

$cur = $dbh->prepare('select accession_no, zdb_id from publication where status != "active" and accession_no is not null;');
$cur->execute();
my ($pub_acc_no, $pub_zdbId);
$cur->bind_columns(\$pub_acc_no,\$pub_zdbId);
%nonActivePubAccessions = ();
$ctInactivePubs = 0;
while ($cur->fetch()) {
  if ($pub_acc_no =~ /^\d+$/) {
    $nonActivePubAccessions{$pub_zdbId} = $pub_acc_no;
    $ctInactivePubs++;
  }
}

$cur->finish();

$dbh->disconnect();

print "\nctInactivePubs = $ctInactivePubs\n\n";

## if no inactive pub with pubmed id
if ($ctInactivePubs == 0) {
  $subjectError = "Auto from " . $dbname . ": updatepubstatus.pl :: There is no non-active publications found with PUBMED accession.";
  ZFINPerlModules->sendMailWithAttachedReport("xshao\@zfin.org",$subjectError,"updatepubstatus.pl");
  exit;
}

open (PPUB, ">listOfUpdatedPubs");
$ctUpdated = 0;
foreach $pubZDBid (sort keys %nonActivePubAccessions) {
    $pubmedId = $nonActivePubAccessions{$pubZDBid};
    $url = 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=' . $pubmedId . '&retmode=xml';
    $content = get $url;
    unless (defined $content) {
       ZFINPerlModules->sendMailWithAttachedReport("xshao\@zfin.org","Auto from $dbname : updatepubstatus.pl :: There is error happening when getting the XML contents from $url.","updatepubstatus.pl");
       close(PPUB);
       exit;
    }
    
    if ($content =~ m/<PublicationStatus>(\w+)<\/PublicationStatus>/) {
      $status = $1;
      if ($status eq "ppublish") {
        print PPUB "$pubZDBid\t$pubmedId\n";
        $ctUpdated++;
      }
    }
    undef $content;
}
close(PPUB);

print "\nctUpdated = $ctUpdated\n\n";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/updatePublicationStatus.sql >log1 2> log2");

$subject = "Auto from $dbname : " . "updatepubstatus.pl :: " . "$ctUpdated of $ctInactivePubs non-active pubs (with pubmed id) have been updated to active according to PUBMED";

ZFINPerlModules->sendMailWithAttachedReport("van_slyke\@zfin.org,xshao\@zfin.org","$subject","listOfUpdatedPubs");


ZFINPerlModules->sendMailWithAttachedReport("xshao\@zfin.org","Auto from $dbname : updatepubstatus.pl :: log file","log2");

exit;



