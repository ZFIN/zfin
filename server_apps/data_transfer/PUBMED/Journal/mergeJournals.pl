#!/opt/zfin/bin/perl
# mergeJournals.pl
# copy the input file from  /research/zarchive/load_files/Journal/
# and perform journal-merging actions

use DBI;
use FindBin;
use lib "$FindBin::Bin/../../../";
use ZFINPerlModules qw(assertEnvironment);
assertEnvironment('PGHOST', 'DB_NAME', 'ROOT_PATH');

use ZFINPerlModules;
use Try::Tiny;

#set environment variables
$rootPath = $ENV{'ROOT_PATH'};
$dbhost = $ENV{'PGHOST'};
$dbname = $ENV{'DB_NAME'};
$username = "";
$password = "";

#remove old log file and alias list
system("/bin/rm -f $rootPath/server_apps/data_transfer/PUBMED/Journal/journalMergeLog");
system("/bin/rm -f $rootPath/server_apps/data_transfer/PUBMED/Journal/aliasList");

system("/usr/bin/scp /research/zarchive/load_files/Journal/mergeJournalInput $rootPath/server_apps/data_transfer/PUBMED/Journal/");

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
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
    or die "Cannot connect to database: $DBI::errstr\n";


$cur_alias = $dbh->prepare("select salias_source_zdb_id, salias_alias from source_alias where salias_source_zdb_id like 'ZDB-JRNL-%';");
$cur_alias->execute();
$cur_alias->bind_columns(\$id, \$alias);

$ctJournalAlias = 0;
%journalAlias = ();
while ($cur_alias->fetch()) {
   $constarint = $id . $alias;
   $journalAlias{$constarint} = 1;
   $ctJournalAlias++;
}

print "total number of existing journal alias: $ctJournalAlias\n";

$cur_alias->finish();


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

     $cur = $dbh->prepare('update publication set pub_jrnl_zdb_id = ? where pub_jrnl_zdb_id = ?;');
     $cur->execute($toRetain, $toDelete);

     $cur = $dbh->prepare('delete from zdb_active_source where zactvs_zdb_id = ?;');                                                                                                                                                            
     $cur->execute($toDelete);

     $cur = $dbh->prepare("insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values (?, ?,  'journal merged');");
     $cur->execute("$toDelete", "$toRetain");
     $cur->finish();

     foreach $synonym (@synonyms) {
       $synonym =~ s/^\s+//;
       $synonym =~ s/\s+$//;  
       $unique = $toRetain . $synonym;
       if (!exists($journalAlias{$unique})) {
           print ALIASLIST "$toRetain|$synonym\n";
           $journalAlias{$unique} = 1;
       }
     }

     undef @synonymes, @fields;
  }
}

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
print "Number of journals after merging journals: $numJournalsAfter;\n";

print LOG "\nNumber of figures before merging journals: $numFiguresBefore;\n";
print LOG "Number of figures after merging journals: $numFiguresAfter;\n";
print LOG "Number of journals before merging journals: $numJournalsBefore;\n";
print LOG "Number of journals after merging journals: $numJournalsAfter;\n";


close LOG;

print "\nct = $ct\n\n";

#remove the used input file
system("/bin/rm -f $rootPath/server_apps/data_transfer/PUBMED/Journal/mergeJournalInput");

try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $dbname -a -f insertJournalAlias.sql");
} catch {
  warn "Failed to execute insertJournalAlias.sql - $_";
  exit -1;
};

exit;

