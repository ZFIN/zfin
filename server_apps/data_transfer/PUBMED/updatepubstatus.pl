#!/private/bin/perl 


use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

#remove old files
system("/bin/rm -f updated");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/xml*");

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
  || die("Failed while connecting to <!--|DB_NAME|--> ");

$cur = $dbh->prepare('select accession_no, zdb_id from publication where status != "active" and accession_no is not null;');
$cur->execute();
my ($pub_acc_no, $pub_zdbId);
$cur->bind_columns(\$pub_acc_no,\$pub_zdbId);
%nonActivePubAccessions = ();
$ctInactivePubs = 0;
while ($cur->fetch()) {
  if ($pub_acc_no =~ /^\d+$/) {
    $nonActivePubAccessions{$pub_acc_no} = $pub_zdbId;
    $ctInactivePubs++;
  }
}

$cur->finish();

print "\nctInactivePubs = $ctInactivePubs\n\n";

## do nothing if no inactive pub
exit if $ctInactivePubs == 0;

my $cur_update_pub = $dbh->prepare_cached('update publication set status = "active" where accession_no = ?;');

my $cur_insert_update = $dbh->prepare_cached('insert into updates (rec_id,field_name,new_value,when) select zdb_id,"status","active",current from publication where accession_no = ?;');

open (PPUB, ">updated");
$ctUpdated = 0;
foreach $pubmed (keys %nonActivePubAccessions) {
    system("/local/bin/wget -q 'http://www.ncbi.nlm.nih.gov/pubmed?term=$pubmed&report=xml&format=text' -O xml$pubmed");

    open(PMED, "xml$pubmed") || die("Could not open xml$pubmed !");
    @lines=<PMED>;
    close(PMED);

    foreach $line (@lines) {
       $line =~ s/>\n+//g;
       if ($line =~ m/PublicationStatus/ && $line =~ m/ppublish/) {      # PublicationStatus
            $cur_update_pub->execute($pubmed);
            $cur_insert_update->execute($pubmed);
            $ctUpdated++;
            print PPUB "$nonActivePubAccessions{$pubmed}\t$pubmed\n";
       }
    }
}
close(PPUB);

$cur_update_pub->finish();
$cur_insert_update->finish();

$dbh->disconnect();

print "\nctUpdated = $ctUpdated\n\n";

$dbname = "<!--|DB_NAME|-->";

$subject = "Auto from $dbname: " . "$ctUpdated of $ctInactivePubs inactive pubs have been updated to active according to PUBMED";
ZFINPerlModules->sendMailWithAttachedReport("van_slyke\@zfin.org,xshao\@zfin.org","$subject","updated");

exit;



