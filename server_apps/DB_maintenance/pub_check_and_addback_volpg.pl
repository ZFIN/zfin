#! /private/bin/perl -w
#
# pub_check_and_addback_volpg.pl
#
# This script first runs a SQL to get all the active pubs at ZFIN with "Journal" and "Review" types which are missing vol or page numbers, and which do have 
# pubmed accession (id). Then the script pull the vol and page numbers from pubmed and update the publications.
# The script generate 2 reports and send it to Ceri: one for those pubs that are updated by this script and the other with pubs still missing vol or pgnumbers 
# could not be updated by this script.

use strict;
use MIME::Lite;
use DBI;


#=======================================================
#
#   Main
#

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

print "processing the publication checking and would add missing vol and page numbers ... \n";

print "remove and re-create Update-Publication-Volume-And-Pages_w directory";
system("/bin/rm -rf Update-Publication-Volume-And-Pages_w");
system("/bin/mkdir Update-Publication-Volume-And-Pages_w"); 


my $dir = "<!--|ROOT_PATH|-->";

my @dirPieces = split(/www_homes/,$dir);

my $databasename = $dirPieces[1];
$databasename =~ s/\///;

print "\n$databasename\n";

my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

### open a handle on the db
my $dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) or die "Cannot connect to Informix database: $DBI::errstr\n";

my $sql = 'select distinct zdb_id, accession_no, title 
          from publication 
         where status = "active" 
           and jtype in ("Journal", "Review") 
           and (pub_pages is null or pub_volume is null or pub_pages = "" or pub_volume = "") 
           and accession_no is not null 
           and accession_no not in ("None","none",""," ") 
           and title is not null';

my $cur = $dbh->prepare($sql);
$cur ->execute();

my $pubZdbId;
my $accession;
my $pubTitle;

$cur->bind_columns(\$pubZdbId,\$accession,\$pubTitle);

my %pmids = ();
my %titles = ();
      
while ($cur->fetch()) {
   $pmids{$pubZdbId} = $accession;
   $titles{$pubZdbId} = $pubTitle;
}

$cur->finish(); 

my $directory = "./Update-Publication-Volume-And-Pages_w/";
open (REPORT, ">", $directory . "updated-publications.txt") || die "Cannot open update-publications.txt : $!\n";
open (NOTUPDATED, ">", $directory . "not-updated-publications.txt") || die "Cannot open not-updated-publications.txt : $!\n";

$sql = 'select zdb_id
          from publication 
         where status = "active" 
           and jtype in ("Journal", "Review") 
           and (pub_pages is null or pub_volume is null) 
           and (accession_no is null or accession_no in ("None","none",""," "))';

$cur = $dbh->prepare($sql);
$cur ->execute();

my $nopubmedidPubZdbId;
$cur->bind_columns(\$nopubmedidPubZdbId);

my %nopubmedidPubZdbIds = ();
      
while ($cur->fetch()) {
   $nopubmedidPubZdbIds{$nopubmedidPubZdbId} = 1;
}

$cur->finish(); 

my $ctNoPubmedId = 0;
my $key;
foreach $key (sort keys %nopubmedidPubZdbIds) {
   $ctNoPubmedId++;
   print NOTUPDATED "\n\nThe following publication(s) missing volume and/or page numbers are not processed because the pubmed IDs are missing.\n\n" if $ctNoPubmedId == 1;
   print NOTUPDATED "$key\n";
}

my $cmdPart1 = "/private/bin/perl -MLWP::Simple -e ";
my $cmdPart2 = '"getprint ';
my $cmdPart3 = '" > publications.xml 2> err';
my $singleQuote = '\'';
my $ctTotal = 0;
my $updated = 0;
my $notupdated = 0;
my $url = "";
my $cmd = "";
my $xmlIssue = "none";
my $line = "";
my @lines = ();
my @fields = ();
my $rightPart = "";
my $xmlVol = "";
my $xmlPg = "";
my $xmlTitle = "";
my @xmlTitleWords = ();
my $titleStoredAtZfin = "";
my $ctMatch = 0;
my $w = "";
my @wordsInTitleStoredAtZfin = ();
my $titlePercentageSimilar = 0;
my $lcXmlTitle;

foreach $key (sort keys %pmids) {
  $ctTotal++;

  $url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=".$pmids{$key}."&retmode=xml";  
  
  $cmd = $cmdPart1 . $cmdPart2 . $singleQuote . $url . $singleQuote . $cmdPart3;  
  
  ###the cmd would be like this:  /private/bin/perl -MLWP::Simple -e "getprint 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=21262879&retmode=xml'"
  
  system("$cmd");

  open(XMLFILE,"publications.xml") || die("Could not open publications.ml !");
  @lines=<XMLFILE>;
  close(XMLFILE);

  $xmlIssue = "none";
  foreach $line (@lines) {  
    $line =~ s/>\n+//g;          
    if ($line =~ m/<Volume>/) {      # volume
        @fields = split(/>/, $line);
        $rightPart = $fields[1];
        undef (@fields);  
        @fields = split(/</, $rightPart);
        $xmlVol = $fields[0];
        $xmlVol =~ s/\s+$//;
    } elsif ($line =~ m/<Issue>/) {      # issue -- as part of the vol at ZFIN publication table
        undef (@fields);
        @fields = split(/>/, $line);
        $rightPart = $fields[1];
        undef (@fields);  
        @fields = split(/</, $rightPart);
        $xmlIssue = $fields[0];
        $xmlIssue =~ s/\s+$//;
    } elsif ($line =~ m/<MedlinePgn>/) {      # page number
        undef (@fields);
        @fields = split(/>/, $line);
        $rightPart = $fields[1];
        undef (@fields);  
        @fields = split(/</, $rightPart);
        $xmlPg = $fields[0];
        $xmlPg =~ s/\s+$//;
    } elsif ($line =~ m/<ArticleTitle>/) {      # title
        undef (@fields);
        @fields = split(/>/, $line);
        $rightPart = $fields[1];
        undef (@fields);  
        @fields = split(/</, $rightPart);
        $xmlTitle = $fields[0];
        undef @xmlTitleWords;
        @xmlTitleWords = split(/\s+/, $xmlTitle);
        
        $titleStoredAtZfin = $titles{$key};
        undef @wordsInTitleStoredAtZfin;
        @wordsInTitleStoredAtZfin = split(/\s+/, $titleStoredAtZfin);
        
        $ctMatch = 0;
        foreach $w (@wordsInTitleStoredAtZfin) {
          $w =~ s/^\W+//;
          $w =~ s/\W+$//;          
          $w = lc($w);
          $lcXmlTitle = lc($xmlTitle);
          $ctMatch++ if index($lcXmlTitle, $w) >= 0;
        }
        $titlePercentageSimilar = $ctMatch / scalar(@xmlTitleWords) * 100;
    } 
  }

  if ($titlePercentageSimilar > 40) {
  
        if ($xmlVol) {
          $xmlVol = $xmlVol . "(" . $xmlIssue . ")" if $xmlIssue ne "none";
          
          $sql = 'update publication set pub_volume = ? where accession_no = ?; ';  
          $cur = $dbh->prepare($sql);
          $cur ->execute($xmlVol,$pmids{$key});  
          $cur->finish(); 
        }

        if ($xmlPg) {
          $sql = 'update publication set pub_pages = ? where accession_no = ?; ';  
          $cur = $dbh->prepare($sql);
          $cur ->execute($xmlPg,$pmids{$key});  
          $cur->finish(); 
        }

        $updated++;
        print REPORT "\nThe following publications have been updated with the vol and page numbers pulled from pubmed\n\n" if $updated == 1;
        print REPORT "zdbId              \tpubmed Id\tvol  \tpage numbers\n" if $updated == 1;
        print REPORT "-------------------\t---------\t-----\t----------\n" if $updated == 1;
        print REPORT "$key\t$pmids{$key}\t$xmlVol\t$xmlPg           \n";
  }  else {  
        $notupdated++;
        print NOTUPDATED "\n\nThe following publication(s) missing volume and/or page numbers are not processed because the similarities between the paper titles are below 40%. Could be due to wrong pubmed ID?\n\n" if $notupdated == 1;
        print NOTUPDATED "$key\npubmid: $pmids{$key}\nTitle stored in ZFIN: $titleStoredAtZfin\nTitle stored in pubmed: $xmlTitle\n\n";
  }  
}

close (REPORT);
close (NOTUPDATED);

print "$updated pubs fixed with vol and/or page numbers\n\n\n";

############################################################################
# This part deals with bad pub_doi field
###########################################################################

print "processing the publication checking and would fix the bad pub_doi ... \n";


$sql = 'select distinct zdb_id, pub_doi 
          from publication 
         where pub_doi like "% %"';

$cur = $dbh->prepare($sql);
$cur ->execute();

my $pubDOI;

$cur->bind_columns(\$pubZdbId,\$pubDOI);

my %dois = ();
      
while ($cur->fetch()) {
   $dois{$pubZdbId} = $pubDOI;
}

$cur->finish();
my $correctDOI = "";

my $ctTotalBadDOIs = 0;
open (DOI, ">", $directory . "doi.txt") || die "Cannot open doi.txt : $!\n";
foreach $key (sort keys %dois) {
   $pubDOI = $dois{$key};
   $correctDOI = $pubDOI;
   $correctDOI =~ s/\s+//g;
   $sql = 'update publication set pub_doi = ? where zdb_id = ?; ';  
   $cur = $dbh->prepare($sql);
   $cur ->execute($correctDOI,$key);  
   $cur->finish(); 
   $ctTotalBadDOIs++;
   print DOI "\nThe following publications have been updated with the pub_doi:\n\n" if $ctTotalBadDOIs == 1;
   print DOI "zdbId              \told pub_doi                 \tnew pub_doi                 \n" if $ctTotalBadDOIs == 1;
   print DOI "-------------------\t----------------------------\t----------------------------\n" if $ctTotalBadDOIs == 1;
   print DOI "$key\t$pubDOI\t$correctDOI      \n";   
}


$dbh->disconnect(); 
close (DOI);

print "$ctTotalBadDOIs bad dois found and fixed\n";

system("mv publications.xml Update-Publication-Volume-And-Pages_w/.");
system("mv err Update-Publication-Volume-And-Pages_w/.");

exit;
