#! /private/bin/perl -w
#
# pub_check_and_addback_volpg.pl
#
# This script first runs a SQL to get all the active pubs at ZFIN with "Journal" and "Review" types which are missing vol or page numbers, and which do have 
# pubmed accession (id). Then the script pull the vol and page numbers from pubmed and update the publications.
# The script generate 2 reports and send it to Ceri: one for those pubs that are updated by this script and the other with pubs still missing vol or pgnumbers 
# could not be updated by this script.


use MIME::Lite;
use DBI;


#------------------ Send Checking Result ----------------
# No parameter
#

sub sendReport {
		
  $SUBJECT="Auto: publications that have been updated";
  $MAILTO="<!--|COUNT_PATO_OUT|-->";
  $TXTFILE="./report.txt";
 
  # Create a new multipart message:
  my $msg1 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg1 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg1->print(\*SENDMAIL);

  $SUBJECT="Auto: publications not updated";
  $MAILTO="<!--|COUNT_PATO_OUT|-->";
  $TXTFILE="./notupdated.txt";
 
  # Create a new multipart message:
  my $msg2 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg2 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg2->print(\*SENDMAIL);

  close(SENDMAIL);
}


#=======================================================
#
#   Main
#

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/pub_check/";

print "processing the publication checking and would add missing vol and page numbers ... \n";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) or die "Cannot connect to Informix database: $DBI::errstr\n";

$sql = 'select distinct zdb_id, accession_no, title 
          from publication 
         where status = "active" 
           and jtype in ("Journal", "Review") 
           and (pub_pages is null or pub_volume is null or pub_pages = "" or pub_volume = "") 
           and accession_no is not null 
           and accession_no not in ("None","none",""," ") 
           and title is not null';

$cur = $dbh->prepare($sql);
$cur ->execute();

$cur->bind_columns(\$pubZdbId,\$accession,\$pubTitle);

%pmids = ();
%titles = ();
      
while ($cur->fetch()) {
   $pmids{$pubZdbId} = $accession;
   $titles{$pubZdbId} = lc($pubTitle);
}

$cur->finish(); 

open (REPORT, ">report.txt") || die "Cannot open report.txt : $!\n";
open (NOTUPDATED, ">notupdated.txt") || die "Cannot open notupdated.txt : $!\n";

$sql = 'select zdb_id
          from publication 
         where status = "active" 
           and jtype in ("Journal", "Review") 
           and (pub_pages is null or pub_volume is null) 
           and (accession_no is null or accession_no in ("None","none",""," "))';

$cur = $dbh->prepare($sql);
$cur ->execute();

$cur->bind_columns(\$nopubmedidPubZdbId);

%nopubmedidPubZdbIds = ();
      
while ($cur->fetch()) {
   $nopubmedidPubZdbIds{$nopubmedidPubZdbId} = 1;
}

$cur->finish(); 

$ctNoPubmedId = 0;
foreach $key (sort keys %nopubmedidPubZdbIds) {
   $ctNoPubmedId++;
   print NOTUPDATED "\n\nThe following publication(s) are not processed because the pubmed IDs are missing.\n\n" if $ctNoPubmedId == 1;
   print NOTUPDATED "$key\n";
}

$cmdPart1 = "perl -MLWP::Simple -e ";
$cmdPart2 = '"getprint ';
$cmdPart3 = '" > pubXml 2> err';
$singleQuote = '\'';
$ctTotal = $updated = $notupdated = 0;
foreach $key (sort keys %pmids) {
  $ctTotal++;

  $url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=".$pmids{$key}."&retmode=xml";  
  
  $cmd = $cmdPart1 . $cmdPart2 . $singleQuote . $url . $singleQuote . $cmdPart3;  
  
  ###the cmd would be like this:  perl -MLWP::Simple -e "getprint 'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=21262879&retmode=xml'"
  
  system("$cmd");

  open(XMLFILE, "pubXml") || die("Could not open pubXml !");
  @lines=<XMLFILE>;
  close(XMLFILE);

  $xmlIssue = "none";
  foreach $line (@lines) {  
    $line =~ s/>\n+//g;          
    if ($line =~ m/<Volume>/) {      # volume
        undef (@fields);
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
        @xmlTitleWords = split(/\s+/, $xmlTitle);
        $titleStoredAtZfin = $titles{$key};
        
        $ctMatch = 0;
        foreach $w (@xmlTitleWords) {
          $w =~ s/^\W+//;
          $w =~ s/\W+$//;          
          $w = lc($w);
          $ctMatch++ if index($titleStoredAtZfin, $w) > 0;
        }
 
        @wordsInTitleStoredAtZfin = split(/\s+/, $titleStoredAtZfin);
        $titlePercentageSimilar = $ctMatch / scalar(@wordsInTitleStoredAtZfin) * 100;
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
        print NOTUPDATED "\n\nThe following publication(s) are not processed because the similarities between the paper titles are below 40%. Could be due to wrong pubmed ID?\n\n" if $notupdated == 1;
        print NOTUPDATED "$titlePercentageSimilar\t$key\t$pmids{$key}\nTitle stored in ZFIN: $titleStoredAtZfin\nTitle stored in pubmed: $xmlTitle\n\n";
  }  
}

$dbh->disconnect(); 
close (REPORT);
sendReport();

print "\nDone.\n\n\n";
  
exit;
