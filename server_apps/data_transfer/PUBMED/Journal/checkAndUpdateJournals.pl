#!/opt/zfin/bin/perl
# checkAndUpdateJournals.pl
# This script check the existing journals stored at ZFIN against the all journal file (ftp://ftp.ncbi.nih.gov/pubmed/J_Medline.txt) 
# from NCBI, reporting problems, and update the journal data accordingly.

use DBI;
use Try::Tiny;

#set environment variables

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

print "$dbname\n\n";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

#remove old report and log files
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/*.txt");

system("/local/bin/wget ftp://ftp.ncbi.nih.gov/pubmed/J_Medline.txt");

open (ALLJOURNALS, "J_Medline.txt") ||  die "Cannot open J_Medline.txt : $!\n";

open (NCBIJOURNALS, ">journalsFromNCBI.txt") || die "Cannot open journalsFromNCBI.txt : $!\n";
$ct = 0;
%titles = ();
while(<ALLJOURNALS>) {
  chomp;
  $journalLine = $_;
  $ct++;
  if ($journalLine =~ m/^JournalTitle/) {
      $journalLine =~ s/JournalTitle://;
      $journalLine =~ s/^\s+//;
      print NCBIJOURNALS "$journalLine|";
  } elsif ($journalLine =~ m/MedAbbr/) {
      $journalLine =~ s/^MedAbbr://;
      $journalLine =~ s/^\s+//;
      print NCBIJOURNALS "$journalLine|";
  } elsif ($journalLine =~ m/^ISSN\s+\(Print\)/) {
      @fields = split(/:/,$journalLine);
      $issnPrint = $fields[1];
      $issnPrint =~ s/^\s+//;
      print NCBIJOURNALS "$issnPrint|";
      undef @fields;
  } elsif ($journalLine =~ m/^ISSN\s+\(Online\)/) {
      @fields = split(/:/,$journalLine);
      $issnOnline = $fields[1];
      $issnOnline =~ s/^\s+//;
      print NCBIJOURNALS "$issnOnline|";
      undef @fields;
  } elsif ($journalLine =~ m/^IsoAbbr/) {
      $journalLine =~ s/IsoAbbr://;
      $journalLine =~ s/^\s+//;
      print NCBIJOURNALS "$journalLine|";
  } elsif ($journalLine =~ m/^NlmId/) {
      $journalLine =~ s/NlmId://;
      $journalLine =~ s/^\s+//;
      print NCBIJOURNALS "$journalLine\n";
  } else {
      next;
  }
}

close ALLJOURNALS;

close NCBIJOURNALS;

try {
  system("psql -d <!--|DB_NAME|--> -a -f  checkAndUpdateJournals.sql");
} catch {
  warn "Failed to execute checkAndUpdateJournals.sql - $_";
  exit -1;
};

open (WRONGISSN, "><!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/wrongIssnPrintByMedAbbr.txt") ||  die "Cannot open wrongIssnPrintByMedAbbr.txt : $!\n";

print WRONGISSN "journal zdbID|journal abbrev|issn print currently stored|issn print at nlm|journal name|nlm ID|\n";

close WRONGISSN;

system("/bin/cat <!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/wrongIssnPrintByMedAbbrWithNoHeader.txt >> <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/wrongIssnPrintByMedAbbr.txt");


$cur_nlmids = $dbh->prepare("select jrnl_nlmid, jrnl_zdb_id, jrnl_abbrev from journal where jrnl_nlmid is not null order by jrnl_zdb_id;");
$cur_nlmids->execute();
$cur_nlmids->bind_columns(\$nmlid, \$jid, \$abbrev);

open (DUPLJOURNAL, ">dup_journal_with_same_nmlid.txt") || die "Cannot open dup_journal_with_same_nmlid.txt : $!\n";
print DUPLJOURNAL "From|To|synonyms, delimited|other ISSNs I found\n";
$ctJsWithNmlID = $ctDupl = 0;
%nmlIDs = ();
%featureTypes = ();
while ($cur_nlmids->fetch()) {
   if(!exists($nmlIDs{$nmlid})) {
       $nmlIDs{$nmlid} = $jid;
   } else {
       $keptJid = $nmlIDs{$nmlid};
       print DUPLJOURNAL "$jid|$keptJid|$abbrev||\n";
       $ctDupl++;
   }
   $ctJsWithNmlID++;
}

close DUPLJOURNAL;

print "total number of journals with nlm id: $ctJsWithNmlID\n";

print "total number of duplicated journals with the same nlm id: $ctDupl\n";

$cur_nlmids->finish();

$dbh->disconnect();

exit;

