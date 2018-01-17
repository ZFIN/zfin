#!/private/bin/perl -w
#
# reportPubsForGeneAndFeature_PG.pl
#
# This script executes the SQLs to get all the publications at ZFIN for genes and features.

use strict;
use MIME::Lite;
use DBI;


#------------------ Send Checking Result ----------------
# No parameter
#

sub sendReportGenePubs($) {
		
  my $SUBJECT="Auto from " . $_[0] . " : publications of genes";
  my $MAILTO="<!--|VALIDATION_EMAIL_MUTANT|-->";
  my $TXTFILE="./pubListForGene";
 
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

  close(SENDMAIL);
}

sub sendReportFeaturePubs($) {

  my $SUBJECT="Auto from " . $_[0] . " : publications of features";
  my $MAILTO="<!--|VALIDATION_EMAIL_MUTANT|-->";
  my $TXTFILE="./pubListForFeature";
 
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


## remove old reports
system("rm -f pubListForGene");
system("rm -f pubListForFeature");

print "getting the publications for genes and features ... \n";

my $dir = "<!--|ROOT_PATH|-->";

my @dirPieces = split(/www_homes/,$dir);

my $databasename = $dirPieces[1];
$databasename =~ s/\///;

print "$databasename\n\n";

my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

my $sqlPubGene = "
           select recattrib_data_zdb_id, recattrib_source_zdb_id 
             from record_attribution 
            where recattrib_data_zdb_id like 'ZDB-GENE-%' 
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union 
           select mrel_mrkr_2_zdb_id, recattrib_source_zdb_id 
             from record_attribution, marker_relationship 
            where recattrib_data_zdb_id = mrel_zdb_id
              and mrel_mrkr_2_zdb_id like 'ZDB-GENE-%'
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select mrel_mrkr_1_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, marker_relationship 
	    where recattrib_data_zdb_id = mrel_zdb_id
              and mrel_mrkr_1_zdb_id like 'ZDB-GENE-%'
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select dalias_data_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, data_alias 
	    where recattrib_data_zdb_id = dalias_zdb_id
              and dalias_data_zdb_id like 'ZDB-GENE-%' 
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select dblink_linked_recid, recattrib_source_zdb_id 
	     from record_attribution, db_link 
	    where recattrib_data_zdb_id = dblink_zdb_id
              and dblink_linked_recid like 'ZDB-GENE-%' 
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select ortho_zebrafish_gene_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, ortholog 
	    where recattrib_data_zdb_id = ortho_zdb_id
              and oevdisp_gene_zdb_id like 'ZDB-GENE-%'  
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select mrkrgoev_mrkr_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, marker_go_term_evidence 
	    where recattrib_data_zdb_id = mrkrgoev_zdb_id
              and mrkrgoev_mrkr_zdb_id like 'ZDB-GENE-%'   
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select fmrel_mrkr_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, feature_marker_relationship 
	    where recattrib_data_zdb_id = fmrel_zdb_id
              and fmrel_mrkr_zdb_id like 'ZDB-GENE-%'  
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select fmrel_mrkr_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, feature_marker_relationship, genotype_feature 
	    where recattrib_data_zdb_id = genofeat_zdb_id
	      and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
              and fmrel_mrkr_zdb_id like 'ZDB-GENE-%'  
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           ;"
              
              

my $cur = $dbh->prepare($sqlPubGene);
$cur ->execute();

my $geneId;
my $pubId;

$cur->bind_columns(\$geneId,\$pubId);


my %pubsForGenes = ();

my $geneAndPub;      
while ($cur->fetch()) {
   $geneAndPub = $geneId . "\t" . $pubId;
   $pubsForGenes{$geneAndPub} = 1;
}

$cur->finish(); 

open (REPORT1, ">pubListForGene") || die "Cannot open pubListForGene : $!\n";

my $ctPubsForGene = 0;
### sort by the hash value (geneId) then by hash key (pubId)
foreach my $k (sort keys %pubsForGenes) {
    $ctPubsForGene++;
    print REPORT1 "$k\n";
}

close (REPORT1);

my $sqlPubFeature = "
           select recattrib_data_zdb_id, recattrib_source_zdb_id 
             from record_attribution 
            where recattrib_data_zdb_id like 'ZDB-ALT-%' 
            and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select dalias_data_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, data_alias 
	    where recattrib_data_zdb_id = dalias_zdb_id
              and dalias_data_zdb_id like 'ZDB-ALT-%'    
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select dblink_linked_recid, recattrib_source_zdb_id 
	     from record_attribution, db_link 
	    where recattrib_data_zdb_id = dblink_zdb_id
              and dblink_linked_recid like 'ZDB-ALT-%'    
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select fmrel_ftr_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, feature_marker_relationship 
	    where recattrib_data_zdb_id = fmrel_zdb_id
              and fmrel_ftr_zdb_id like 'ZDB-ALT-%'       
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           union
           select genofeat_feature_zdb_id, recattrib_source_zdb_id 
	     from record_attribution, genotype_feature 
	    where recattrib_data_zdb_id = genofeat_zdb_id
              and genofeat_feature_zdb_id like 'ZDB-ALT-%'  
              and recattrib_source_zdb_id like 'ZDB-PUB-%'
           ;"


$cur = $dbh->prepare($sqlPubFeature);
$cur ->execute();

my $featureId;

$cur->bind_columns(\$featureId,\$pubId);

###   key:    pubId
###   value:  featureId
my %pubsForFeatures = ();

my $featureAndPub;       
while ($cur->fetch()) {
   $featureAndPub = $featureId . "\t" . $pubId;
   $pubsForFeatures{$featureAndPub} = 1;
}

$cur->finish(); 

open (REPORT2, ">pubListForFeature") || die "Cannot open pubListForFeature : $!\n";

my $ctPubsForFeature = 0;
### sort by the hash value (featureId) then by hash key (pubId)
foreach my $key (sort { ($pubsForFeatures{$a} cmp $pubsForFeatures{$b}) || ($a cmp $b) } keys %pubsForFeatures) {
    $ctPubsForFeature++;
    print REPORT2 "$key\n";
}

close (REPORT2);

$cur->finish(); 

$dbh->disconnect(); 

sendReportGenePubs("$databasename");
sendReportFeaturePubs("$databasename");

print "\n\nctPubsForGene = $ctPubsForGene  \nctPubsForFeature = $ctPubsForFeature\n\nDone.\n";

  
exit;

