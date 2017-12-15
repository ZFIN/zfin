#!/private/bin/perl
# mergeJournals.pl
# copy the input file from  /research/zarchive/load_files/Journal/
# and perform journal-merging actions

use DBI;
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

#remove old log file and alias list
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/journalMergeLog");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/aliasList");

system("/usr/bin/scp /research/zarchive/load_files/Journal/mergeJournalInput <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/");

open (LOG, ">journalMergeLog") || die "Cannot open journalMergeLog : $!\n";

if (!-e "mergeJournalInput") {
   print LOG "No input file. Exit.\n";
   exit;
}

open (JOURNALS, "mergeJournalInput") ||  die "Cannot open mergeJournalInput : $!\n";

open (ALIASLIST, ">aliasList") || die "Cannot open aliasList : $!\n";

# number of figures before merging the journals
$sql = 'select fig_zdb_id from figure;';

$numFiguresBefore = ZFINPerlModules->countData($sql);

# number of journals before merging the journals
$sql = 'select jrnl_zdb_id from journal;';     

$numJournalsBefore = ZFINPerlModules->countData($sql);

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password)
  or die "Cannot connect to Informix database: $DBI::errstr\n";

$ct = 0;
while(<JOURNALS>) {
  $ct++;
  next if $ct == 1;
  chomp;
  $line = $_;
  if ($line) {
    @fields = split(/\|/, $line);
    $toDelete = $fields[0];
    $toRetain = $fields[1];
    @synonyms = split(/,/, $fields[2]);
    if ($ct < 18) {
       print "$line\n";
       print "$toDelete\t$toRetain\t$synonyms[0]\t$synonyms[3]\n\n";
     }

     $cur = $dbh->prepare('update publication set pub_jrnl_zdb_id = ? where pub_jrnl_zdb_id = ?;');
     $cur->execute($toRetain, $toDelete);

     $cur = $dbh->prepare('delete from zdb_active_source where zactvs_zdb_id = ?;');                                                                                                                                                            
     $cur->execute($toDelete);

     $cur = $dbh->prepare('insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values (?, ?,  "journal merged");');
     $cur->execute("$toDelete", "$toRetain");

     foreach $synonym (@synonyms) {
       $synonym =~ s/^\s+//;
       $synonym =~ s/\s+$//;  
       print ALIASLIST "$toRetain|$synonym|\n";
     }

     undef @synonymes, @fields;
  }
}

$cur->finish();

$dbh->disconnect();

close JOURNALS;

close ALIASLIST;

# number of figures after merging the journals
$sql = 'select fig_zdb_id from figure;';     

$numFiguresAfter = ZFINPerlModules->countData($sql);    

# number of journals after merging the journals
$sql = 'select jrnl_zdb_id from journal;';

$numJournalsAfter = ZFINPerlModules->countData($sql);

print "\nNumber of figures before merging journals: $numFiguresBefore;\n";
print "Number of figures after merging journals: $numFiguresAfter;\n";
print "Number of journals before merging journals: $numJournalsBefore;\n";
print "Number of journals before merging journals: $numJournalsAfter;\n";

print LOG "\nNumber of figures before merging journals: $numFiguresBefore;\n";
print LOG "Number of figures after merging journals: $numFiguresAfter;\n";
print LOG "Number of journals before merging journals: $numJournalsBefore;\n";
print LOG "Number of journals after merging journals: $numJournalsAfter;\n";


close LOG;

print "\nct = $ct\n\n";

#remove the used input file
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/mergeJournalInput");

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> insertJournalAlias.sql") && die "inserting journal alias failed.";

exit;



