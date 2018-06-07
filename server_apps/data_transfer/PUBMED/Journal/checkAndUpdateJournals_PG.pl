#!/private/bin/perl
# checkAndUpdateJournals.pl
# This script check the existing journals stored at ZFIN against the all journal file (ftp://ftp.ncbi.nih.gov/pubmed/J_Medline.txt) 
# from NCBI, reporting problems, and update the journal data accordingly.

#set environment variables

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

print "$dbname\n\n";

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
      print NCBIJOURNALS "$journalLine|\n";
  } else {
      next;
  }
}

close ALLJOURNALS;

close NCBIJOURNALS;


system("psql -d <!--|DB_NAME|--> -a -f  checkAndUpdateJournals.sql") && die "Checking and updating journals failed.";

open (WRONGISSN, "><!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/wrongIssnPrintByMedAbbr.txt") ||  die "Cannot open wrongIssnPrintByMedAbbr.txt : $!\n";

print WRONGISSN "journal zdbID|journal abbrev|issn print currently stored|issn print at nlm|journal name|nlm ID|\n";

close WRONGISSN;

system("/bin/cat <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/wrongIssnPrintByMedAbbrWithNoHeader.txt >> <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/Journal/wrongIssnPrintByMedAbbr.txt");

exit;



