#!/private/bin/perl

# FILE: cleanUpSecondaryGeneAccessions.pl
#
# It downloads, unzips and parses the following file and call a SQL script to clean up the secondary Gene accessions stored at ZFIN.
# 
# ftp://ftp.ncbi.nih.gov/gene/DATA/gene_history.gz
# tax_id GeneID Discontinued_GeneID Discontinued_Symbol Discontinue_Date 
# 7955    559281  322153  wu:fb51f11      20121116

use MIME::Lite;
use DBI;


#------------------ Send report ----------------
# No parameter
#
sub sendReport($) {

  $SUBJECT="Auto from ".$_[0].": the list of genes that have more than one Gene accessions";
  $MAILTO="<!--|PATO_EMAIL_CURATOR|-->";
  $TXTFILE="./genesWithGeneAcc";
 
  # Create a new multipart message:
  my $msg4 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg4 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg4->print(\*SENDMAIL);
  
  close(SENDMAIL);
}


#------------------ Send log files and the delete list ----------------
# No parameter
#
sub sendLogs($) {

  $SUBJECT="Auto from ".$_[0].": deleteLog1";
  $MAILTO="xshao\@cs.uoregon.edu";
  $TXTFILE="./deleteLog1";

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

  my $SUBJECT="Auto from ".$_[0].": deleteLog2";
  $MAILTO="xshao\@cs.uoregon.edu";
  $TXTFILE="./deleteLog2";

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

  $SUBJECT="Auto from ".$_[0].": the delet list of db_link records with discontinued Gene accessions";
  $MAILTO="xshao\@cs.uoregon.edu";
  $TXTFILE="./deleteListDblinksDetails";
 
  # Create a new multipart message:
  my $msg3 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg3 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg3->print(\*SENDMAIL);

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

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/";

#remove old files
 
system("rm -f gene_history");
system("rm -f genesWithGeneAcc");
system("rm -f deleteListDblinks");
system("rm -f deleteListDblinksDetails");
system("rm -f deleteLog1");
system("rm -f deleteLog2");

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Failed while connecting to <!--|DB_NAME|-->\n";
    
$sqlDiscontinuedAcc = 'select count(*), dblink_linked_recid from db_link where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1" group by dblink_linked_recid having count(*) > 1;'; 
$cur = $dbh->prepare($sqlDiscontinuedAcc) or die "Prepare fails";
$cur->execute() or die "Could not execute $sqlDiscontinuedAcc";  
my ($numOccurence, $ZDBgeneId);
$cur->bind_columns(\$numOccurence,\$ZDBgeneId);

$ctGenesWithDiscontinuedAccs = 0;

%genesWithDiscontinuedAccs = ();
while ($cur->fetch()) {
   $genesWithDiscontinuedAccs{$ZDBgeneId} = $numOccurence;
   $ctGenesWithDiscontinuedAccs++;
}

$cur->finish(); 
    
$sql = 'select distinct dbl1.dblink_zdb_id, dbl1.dblink_acc_num from db_link dbl1 where dbl1.dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1" and exists (select "x" from db_link dbl2 where dbl2.dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1" and 
dbl2.dblink_linked_recid = dbl1.dblink_linked_recid and dbl2.dblink_acc_num <> dbl1.dblink_acc_num) order by dbl1.dblink_zdb_id;'; 
$cur = $dbh->prepare($sql) or die "Prepare fails";
$cur->execute() or die "Could not execute $sql";
my ($dblinkId, $geneAcc);
$cur->bind_columns(\$dblinkId,\$geneAcc);

$ctDblinkIdGeneAccPairs = 0;
%geneAccDblinkIds = ();
while ($cur->fetch()) {
   $geneAccDblinkIds{$geneAcc} = $dblinkId;
   $ctDblinkIdGeneAccPairs++;
}

$cur->finish(); 

$dir = "<!--|ROOT_PATH|-->";

@dirPieces = split(/www_homes/,$dir);

$dbname = $dirPieces[1];
$dbname =~ s/\///;

print "\nRunning cleanUpSecondaryGeneAccessions.pl on $dbname ...\n\n";

print "\n\n ctGenesWithDiscontinuedAccs = $ctGenesWithDiscontinuedAccs \t ctDblinkIdGeneAccPairs = $ctDblinkIdGeneAccPairs \n\n";

system("wget ftp://ftp.ncbi.nih.gov/gene/DATA/gene_history.gz");
system("gunzip gene_history.gz");

open (GENEHISTORY, "gene_history") || die "Cannot open gene_history : $!\n";
@lines = <GENEHISTORY>;
close(GENEHISTORY);

open (DELETELIST, ">deleteListDblinks") || die "Cannot open deleteListDblinks : $!\n";

$ct = $numDeletedDiscontinuedGeneAccs = 0;
foreach $line (@lines) {
   $ct++;
   next if $ct < 2;  ## first line is documentation
   chop($line);
   @fields = split(/\s+/, $line); 
   
   $taxId = $fields[0];
   $GeneAcc = $fields[1];
   $discontinuedGeneAcc = $fields[2];
   
   ### ignore it if the tax id is not zebrafish
   next if $taxId ne "7955";
   
   ## print the db_link zdb_id and its acc based on the discontinued acc on gene_history
   if (exists($geneAccDblinkIds{$discontinuedGeneAcc})) {
        print DELETELIST "$geneAccDblinkIds{$discontinuedGeneAcc}|$discontinuedGeneAcc|\n";  
        $numDeletedDiscontinuedGeneAccs++;
   }
   
} 

close(DELETELIST);

print "\n\nnumber of genes with discontinued Gene Accessions: $ctGenesWithDiscontinuedAccs$num (before) \n\n\n";

print "\n\nnumber of discontinued Gene Accessions deleted: $numDeletedDiscontinuedGeneAccs$num \n\n\n";


system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> deleteDiscountinuedGeneAccs.sql > deleteLog1 2> deleteLog2") if $numDeletedDiscontinuedGeneAccs > 0;


$cur = $dbh->prepare($sqlDiscontinuedAcc) or die "Prepare fails";
$cur->execute() or die "Could not execute $sqlDiscontinuedAcc";  
$cur->bind_columns(\$numOccurence,\$ZDBgeneId);


$ctGenesWithDiscontinuedAccs = 0;

%genesWithDiscontinuedAccsAfterCleanup = ();
while ($cur->fetch()) {
   $genesWithDiscontinuedAccsAfterCleanup{$ZDBgeneId} = $numOccurence;
   $ctGenesWithDiscontinuedAccs++;
}

open (REPORT, ">genesWithGeneAcc") || die "Cannot open genesWithGeneAcc : $!\n";
foreach $geneZDBid (sort keys %genesWithDiscontinuedAccs) {
   if (exists($genesWithDiscontinuedAccsAfterCleanup{$geneZDBid})) {
       print REPORT "$genesWithDiscontinuedAccs{$geneZDBid}\t$geneZDBid\t$genesWithDiscontinuedAccsAfterCleanup{$geneZDBid}\t*** not fully cleaned up yet ***\n";
   } else {
       print REPORT "$genesWithDiscontinuedAccs{$geneZDBid}\t$geneZDBid\t1\tcleaned up\n";
   }
}
close(REPORT);

$cur->finish(); 

$dbh->disconnect(); 


sendLogs("$dbname") if $numDeletedDiscontinuedGeneAccs > 0;

sendReport("$dbname") if $ctGenesWithDiscontinuedAccs > 0;

print "\n\nnumber of genes with discontinued Gene Accessions: $ctGenesWithDiscontinuedAccs$num (after) \n\nAll done.\n\n\n";

exit;

