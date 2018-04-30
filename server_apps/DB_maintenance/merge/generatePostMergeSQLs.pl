#!/private/bin/perl

## generatePostMergeSQLs.pl
## generatePostMergeSQLs.pl ZDB_ID1 ZDB_ID2
## only run this on PostGreSQL

## This program is used to generate the rest of SQLs to merge two ZFIN zdb_active_data records

use strict;
use DBI;

## check commandline parameters
die "Usage: generatePostMergeSQLs.pl ZDBID1 ZDBID2\n" if @ARGV != 2;

my $recordToBeDeleted = $ARGV[0]; 
my $recordToBeMergedInto = $ARGV[1];

my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

### open a handle on the db
my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "\n\nCannot connect to PostgreSQL database: $DBI::errstr\n\n";

my $type1;
if ($recordToBeDeleted =~ m/^ZDB\-([A-Z]+)\-\d{6}\-\d+$/) {
    $type1 = $1;
} else {
    die "\n\nNot a valid ZDB ID for the record to be deleted\n\n";
}

my $type2;
if ($recordToBeMergedInto =~ m/^ZDB\-([A-Z]+)\-\d{6}\-\d+$/) {
    $type2 = $1;
} else {
    die "\n\nNot a valid ZDB ID for the record to be merged into\n\n";
}

if ($type1 ne $type2) {
  die "\n\nCannot merge the different type of ZFIN records\n\n";
}

## global hash variable storing the SQLs
my %mergeSQLs = ();

die "\n\nUse generateSQLsForMergingRecords.pl \n\n" if ($type1 eq 'COMPANY' || $type1 eq 'JRNL' || $type1 eq 'LAB' || $type1 eq 'PERS' || $type1 eq 'PUB' || $type1 eq 'SALIAS');

## validate the two ZDB IDs
validateZDBID($recordToBeDeleted, 'zdb_active_data', 'zactvd_zdb_id');
validateZDBID($recordToBeMergedInto, 'zdb_active_data', 'zactvd_zdb_id');

## deal with possible violation of unique constraint in record_attribution table 
## when updating some tables, inserting into record_attribution table will be triggered
cleanupRecordAttributionTable($recordToBeMergedInto, $recordToBeDeleted);          

## INF-3371
if ($type1 eq 'ALT') {
  my $mutagenRecordToBeMergedInto;
  my $mutagenRecordToBeDeleted;
  my $sqlGetUnspecifiedFeatureAssay = "select featassay_mutagen from feature_assay where featassay_feature_zdb_id = '$recordToBeMergedInto' and featassay_mutagen = 'not specified';";
  my $curGetUnspecifiedFeatureAssay = $dbh->prepare_cached($sqlGetUnspecifiedFeatureAssay);
  $curGetUnspecifiedFeatureAssay->execute();
  $curGetUnspecifiedFeatureAssay->bind_columns(\$mutagenRecordToBeMergedInto);
  my $ctUnspecifiedMutagen = 0;
  while ($curGetUnspecifiedFeatureAssay->fetch()) { $ctUnspecifiedMutagen++; }
  $curGetUnspecifiedFeatureAssay->finish();

  my $sqlGetSpecifiedFeatureAssay = "select featassay_mutagen from feature_assay where featassay_feature_zdb_id = '$recordToBeDeleted' and featassay_mutagen != 'not specified';";
  my $curGetSpecifiedFeatureAssay = $dbh->prepare_cached($sqlGetSpecifiedFeatureAssay);
  $curGetSpecifiedFeatureAssay->execute();
  $curGetSpecifiedFeatureAssay->bind_columns(\$mutagenRecordToBeDeleted);
  my $ctSpecifiedMutagen = 0;
  while ($curGetSpecifiedFeatureAssay->fetch()) { $ctSpecifiedMutagen++; }
  $curGetSpecifiedFeatureAssay->finish();
  if ($ctSpecifiedMutagen > 0 && $ctUnspecifiedMutagen > 0) {
    my $sqlUpdateFeatureAssay = "update feature_assay set featassay_feature_zdb_id = '$recordToBeMergedInto' where featassay_feature_zdb_id = '$recordToBeDeleted';";
    $mergeSQLs{$sqlUpdateFeatureAssay} = 9;
  }
}

my $markerAbbrevToBeDeleted;
my $markerNameToBeDeleted;
my $markerAbbrevMergedInto;
my $markerNameMergedInto;

if ($type1 eq 'GENE' || $type1 eq 'MRPHLNO' || $type1 eq 'CRISPR' || $type1 eq 'TALEN') {
  my $sqlGetMarkerInfoForToBeDeleted = "select mrkr_abbrev, mrkr_name from marker where mrkr_zdb_id = '$recordToBeDeleted';";
  my $curGetMarkerInfoForToBeDeleted = $dbh->prepare_cached($sqlGetMarkerInfoForToBeDeleted);
  $curGetMarkerInfoForToBeDeleted->execute();            
  $curGetMarkerInfoForToBeDeleted->bind_columns(\$markerAbbrevToBeDeleted, \$markerNameToBeDeleted);
  while ($curGetMarkerInfoForToBeDeleted->fetch()) {}
  $curGetMarkerInfoForToBeDeleted->finish();

  my $sqlGetMarkerInfoForMergedInto = "select mrkr_abbrev, mrkr_name from marker where mrkr_zdb_id = '$recordToBeMergedInto';";
  my $curGetMarkerInfoForMergedInto = $dbh->prepare_cached($sqlGetMarkerInfoForMergedInto);
  $curGetMarkerInfoForMergedInto->execute();            
  $curGetMarkerInfoForMergedInto->bind_columns(\$markerAbbrevMergedInto, \$markerNameMergedInto);
  while ($curGetMarkerInfoForMergedInto->fetch()) {}
  $curGetMarkerInfoForMergedInto->finish();
  
  my $sqlTmpId = "select get_id('DALIAS') as dalias_id, get_id('NOMEN') as nomen_id from single into temp tmp_ids;";
  $mergeSQLs{$sqlTmpId} = 8;
  my $insertZdbActiveData = "insert into zdb_active_data select dalias_id from tmp_ids;";
  $mergeSQLs{$insertZdbActiveData} = 7;
  my $sqlInsertDataAlias = "insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                            select dalias_id, '$recordToBeMergedInto', '$markerAbbrevToBeDeleted', '1'
                              from tmp_ids;";
  $mergeSQLs{$sqlInsertDataAlias} = 6; 
  my $insertZdbActiveDataNomen = "insert into zdb_active_data select nomen_id from tmp_ids;";
  $mergeSQLs{$insertZdbActiveDataNomen} = 5;
  my $sqlInsertMrkrHistory = "insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date, 
                                                          mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, mhist_comments,mhist_dalias_zdb_id)
                              select nomen_id, '$recordToBeMergedInto', 'merged', 'same marker', CURRENT, 
                                    '$markerNameMergedInto', '$markerAbbrevMergedInto', 'none', dalias_id
                                from tmp_ids;";
  $mergeSQLs{$sqlInsertMrkrHistory} = 4;   
}

$dbh->disconnect();

my $sqlUpdateRecordAttribution = "update record_attribution set recattrib_data_zdb_id = '$recordToBeMergedInto' where recattrib_data_zdb_id = '$recordToBeDeleted';";
$mergeSQLs{$sqlUpdateRecordAttribution} = 9;

my $sqlDeleteReplacedData = "delete from zdb_replaced_data where zrepld_old_zdb_id = '$recordToBeDeleted';";
$mergeSQLs{$sqlDeleteReplacedData} = 3;

my $sqlUpdateReplacedData = "update zdb_replaced_data set zrepld_new_zdb_id = '$recordToBeMergedInto' where zrepld_new_zdb_id = '$recordToBeDeleted';";
$mergeSQLs{$sqlUpdateReplacedData} = 2;

my $sqlDelete = "delete from zdb_active_data where zactvd_zdb_id = '$recordToBeDeleted';";
$mergeSQLs{$sqlDelete} = 1;

my $sqlInsertReplacedData = "insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('$recordToBeDeleted', '$recordToBeMergedInto');";
$mergeSQLs{$sqlInsertReplacedData} = 0;


## print out all the generated SQLs, sort by the values first (depth, reversed; i.e. the deepest first), then by the keys (delete before update)
open (SQLFILE, ">post-merge.sql") || die "Cannot open post-merge.sql : $!\n"; 
my @sorted = sort { $mergeSQLs{$b} <=> $mergeSQLs{$a} || $a cmp $b } keys %mergeSQLs;
print SQLFILE "begin work;\n\n";
for (@sorted) {
    print SQLFILE "$_\n\n";
}
print SQLFILE "\nrollback work;\n\n\n";

close SQLFILE;

exit;

## in this function, delete from record_attribution SQL(s) are generated and stored in the hash, whenever violation of some unique constrait is found 
sub cleanupRecordAttributionTable {
  my $mergedInto = shift;
  my $toDelete = shift;
  my $sqlCheckRecordAttributionForMergedInto = "select recattrib_pk_id, recattrib_source_zdb_id, recattrib_source_type from record_attribution where recattrib_data_zdb_id = '$mergedInto';";
  my ($pkRecordAttribution, $source, $type);
  my $curCheckRecordAttributionForMergedInto = $dbh->prepare_cached($sqlCheckRecordAttributionForMergedInto);
  $curCheckRecordAttributionForMergedInto->execute();
  my %recordAttributionForMergedInto = ();
  my $ctRecordAttributionForMergedInto = 0;

  $curCheckRecordAttributionForMergedInto->bind_columns(\$pkRecordAttribution, \$source, \$type);                
                                
  while ($curCheckRecordAttributionForMergedInto->fetch()) {  
    $ctRecordAttributionForMergedInto++;
    $recordAttributionForMergedInto{$source.$type} = $pkRecordAttribution;
  }

  $curCheckRecordAttributionForMergedInto->finish();
              
  ## No need for 'delete from record_attribution' SQL if there is no record there for the record to be merged into 
  if ($ctRecordAttributionForMergedInto > 0) {                                                               
    my $sqlCheckRecordAttributionForDeleted = "select recattrib_pk_id, recattrib_source_zdb_id, recattrib_source_type from record_attribution where recattrib_data_zdb_id = '$toDelete';";             
                        
    my $curCheckRecordAttributionForDeleted = $dbh->prepare_cached($sqlCheckRecordAttributionForDeleted);
    $curCheckRecordAttributionForDeleted->execute();
    my %recordAttributionForDEleted = ();                

    $curCheckRecordAttributionForDeleted->bind_columns(\$pkRecordAttribution, \$source, \$type);                 
                
    while ($curCheckRecordAttributionForDeleted->fetch()) {  
      # violation found! Need to construct and store the delete SQL:
      if (exists($recordAttributionForMergedInto{$source.$type})) {
        my $deleteFromRecordAttributionSQL = "delete from record_attribution where recattrib_pk_id = '$pkRecordAttribution';";
        $mergeSQLs{$deleteFromRecordAttributionSQL} = 999;                          
      }
    }
            
    $curCheckRecordAttributionForDeleted->finish();
                
  } # end of if ($ctRecordAttributionForMergedInto > 0)
} # end of cleanupRecordAttributionTable function

## validate if ZFIN has the record
sub validateZDBID {
  my $zdbID = shift;
  my $zfinTable = shift;
  my $column = shift;
  my $sqlValidateZDBID = "select * from $zfinTable where $column = '$zdbID';";
  my $curValidateZDBID = $dbh->prepare_cached($sqlValidateZDBID);
  $curValidateZDBID->execute();
  
  my $ctRecords = 0;
  while ($curValidateZDBID->fetch()) {  
    $ctRecords++;
  }

  $curValidateZDBID->finish();
              
  die "\n\n$zdbID is not found at ZFIN\n\n" if $ctRecords == 0;
  
} # end of validateZDBID function




