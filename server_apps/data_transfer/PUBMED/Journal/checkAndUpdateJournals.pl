#!/private/bin/perl
# checkJournals.pl
# This script check the existing journals stored at ZFIN against the all journal file (ftp://ftp.ncbi.nih.gov/pubmed/J_Medline.txt) 
# from NCBI.


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
#system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/listOfUpdatedPubs.txt");
#system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/log1");
#system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/PUBMED/log2");


system("/local/bin/wget ftp://ftp.ncbi.nih.gov/pubmed/J_Medline.txt");

open (ALLJOURNALS, "J_Medline.txt") ||  die "Cannot open J_Medline.txt : $!\n";

###@allJournals = <ALLJOURNALS>;

##close ALLJOURNALS;

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


system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> checkJournals.sql >checkJournals.log 2> checkJournalsSQLError.log") && die "checking journals failed.";

exit;



