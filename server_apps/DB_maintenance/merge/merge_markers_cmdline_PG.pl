#!/private/bin/perl

use strict;
use DBI;

## merge_markers_cmdline_PG.pl Marker_ZDB_ID1 Marker_ZDB_ID2

## check commandline parameters
die "Usage: merge_markers_cmdline_PG.pl Marker_ZDB_ID1 Marker_ZDB_ID2\n" if @ARGV != 2;

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
    die "Not a valid ZDB ID for the record to be deleted\n";
}

my $type2;
if ($recordToBeMergedInto =~ m/^ZDB\-([A-Z]+)\-\d{6}\-\d+$/) {
    $type2 = $1;
} else {
    die "Not a valid ZDB ID for the record to be merged into\n";
}

if ($type1 ne $type2) {
  die "\n\nCannot merge the different type of markers.\n\n";
}

if ($type1 ne 'GENE' && $type1 ne 'GENEP' && $type1 ne 'MRPHLNO' && $type1 ne 'CRISPR' && $type1 ne 'TALEN') {
  die "\n\nThis program is for merging genes, pseudo-genes and STRs only.\n\n";
}

my $tableName;
my $primaryKeyColumn;

## global hash variable storing the SQLs
my %mergeSQLs = ();

$tableName = 'zdb_active_data';
$primaryKeyColumn = 'zactvd_zdb_id';

## validate the two ZDB IDs
validateZDBID($recordToBeDeleted, $tableName, $primaryKeyColumn);
validateZDBID($recordToBeMergedInto, $tableName, $primaryKeyColumn);

my %processed = ();
$processed{$tableName.$primaryKeyColumn} = 0;
$processed{'updatesrec_id'} = 0;
$processed{'zdb_replaced_datazrepld_old_zdb_id'} = 0;
$processed{'zdb_replaced_datazrepld_new_zdb_id'} = 0;
$processed{'record_attributionrecattrib_data_zdb_id'} = 0;
$processed{'record_attributionrecattrib_source_zdb_id'} = 0;

## deal with possible violation of unique constraint in record_attribution table 
## when updating some tables, inserting into record_attribution table will be triggered
cleanupRecordAttributionTable($recordToBeMergedInto, $recordToBeDeleted, 999);          

## call the recursive function to construct the merge-related SQLs
recursivelyGetSQLs($recordToBeDeleted, $recordToBeMergedInto, $tableName, $primaryKeyColumn, 0);
 
## sort by the values first (depth, reversed; i.e. the deepest level first),
## then by the keys (delete SQL before update SQL)
my @sorted = sort { $mergeSQLs{$b} <=> $mergeSQLs{$a} || $a cmp $b } keys %mergeSQLs;

## execute the SQLs according to the above order
my($mergeSQL,$curMerge);
for (@sorted) {
   $mergeSQL = $_;
   $curMerge = $dbh->prepare($mergeSQL);
   $curMerge->execute();
   $curMerge->finish();
}

## deal with possible violation of unique constraint in record_attribution table, again
## this is because when updating some tables, inserting into record_attribution table are triggered
cleanupRecordAttributionTable($recordToBeMergedInto, $recordToBeDeleted, 0);

## print out the SQLs for debugging purpose
my @sorted2 = sort { $mergeSQLs{$b} <=> $mergeSQLs{$a} || $a cmp $b } keys %mergeSQLs;
open (SQLFILE, ">merge.sql") || die "Cannot open merge.sql : $!\n"; 
for (@sorted2) {
    print SQLFILE "$_\n\n";
}

## for debugging purpose again, print out all the depth values and the SQLs, 
open (DEPTHANDSQL, ">depthAndSQLs.txt") || die "Cannot open depthAndSQLs.txt : $!\n"; 
for (@sorted2) {
    print DEPTHANDSQL "$mergeSQLs{$_}:\n$_\n\n";
}

close DEPTHANDSQL;

my $sqlDeleteFromRecordAttribution;
my $curDeleteFromRecordAttribution;

## update record_attribution table
my $k;
my $v;
foreach $k (keys %mergeSQLs) {
  $v = $mergeSQLs{$k};
  if ($v eq '0') {
    $sqlDeleteFromRecordAttribution = $k;
    $curDeleteFromRecordAttribution = $dbh->prepare($sqlDeleteFromRecordAttribution);
    $curDeleteFromRecordAttribution->execute();
    $curDeleteFromRecordAttribution->finish();
  }
}

my $sqlUpdateRecordAttribution = "update record_attribution set recattrib_data_zdb_id = '$recordToBeMergedInto' where recattrib_data_zdb_id = '$recordToBeDeleted';";
print SQLFILE "$sqlUpdateRecordAttribution\n\n";
my $curUpdateRecordAttribution = $dbh->prepare($sqlUpdateRecordAttribution);
$curUpdateRecordAttribution->execute();
$curUpdateRecordAttribution->finish();

my $markerAbbrevToBeDeleted;
my $markerNameToBeDeleted;
my $markerAbbrevMergedInto;
my $markerNameMergedInto;
my $daliasID;
my $nomenID;

## get marker abbreviations and names for the marker to be deleted; also, get the new dalias ID
my $sqlGetMarkerInfoForToBeDeleted = "select mrkr_abbrev, mrkr_name, get_id('DALIAS') as daliasid from marker where mrkr_zdb_id = '$recordToBeDeleted';";
my $curGetMarkerInfoForToBeDeleted = $dbh->prepare_cached($sqlGetMarkerInfoForToBeDeleted);
$curGetMarkerInfoForToBeDeleted->execute();            
$curGetMarkerInfoForToBeDeleted->bind_columns(\$markerAbbrevToBeDeleted, \$markerNameToBeDeleted, \$daliasID);
while ($curGetMarkerInfoForToBeDeleted->fetch()) {}
$curGetMarkerInfoForToBeDeleted->finish();

## get marker abbreviations and names for the marker to be merged into; also, get the new nomen ID
my $sqlGetMarkerInfoForMergedInto = "select mrkr_abbrev, mrkr_name, get_id('NOMEN') as nomenid from marker where mrkr_zdb_id = '$recordToBeMergedInto';";
my $curGetMarkerInfoForMergedInto = $dbh->prepare_cached($sqlGetMarkerInfoForMergedInto);
$curGetMarkerInfoForMergedInto->execute();            
$curGetMarkerInfoForMergedInto->bind_columns(\$markerAbbrevMergedInto, \$markerNameMergedInto, \$nomenID);
while ($curGetMarkerInfoForMergedInto->fetch()) {}
$curGetMarkerInfoForMergedInto->finish();

## add new data_alias record
my $sqlInsertZdbActiveData = "insert into zdb_active_data values('$daliasID');";
print SQLFILE "$sqlInsertZdbActiveData\n\n";
my $curInsertZdbActiveData = $dbh->prepare_cached($sqlInsertZdbActiveData);
$curInsertZdbActiveData->execute();
$curInsertZdbActiveData->finish();

my $sqlInsertDataAlias = "insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                          values ('$daliasID', '$recordToBeMergedInto', '$markerAbbrevToBeDeleted', '1');";
print SQLFILE "$sqlInsertDataAlias\n\n";
my $curAddNewDataAlias = $dbh->prepare($sqlInsertDataAlias);
$curAddNewDataAlias->execute();
$curAddNewDataAlias->finish();

## add new marker_history record
my $sqlInsertZdbActiveData2 = "insert into zdb_active_data values('$nomenID');";
print SQLFILE "$sqlInsertZdbActiveData2\n\n";
my $curInsertZdbActiveData2 = $dbh->prepare_cached($sqlInsertZdbActiveData2);
$curInsertZdbActiveData2->execute();
$curInsertZdbActiveData2->finish();

my $sqlInsertMrkrHistory = "insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date, 
                                                        mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, mhist_comments,mhist_dalias_zdb_id)
                            values ('$nomenID', '$recordToBeMergedInto', 'merged', 'same marker', now(), 
                                    '$markerNameMergedInto', '$markerAbbrevMergedInto', 'none', '$daliasID');";
print SQLFILE "$sqlInsertMrkrHistory\n\n";
my $curAddNewMrkrHistory = $dbh->prepare($sqlInsertMrkrHistory);
$curAddNewMrkrHistory->execute();
$curAddNewMrkrHistory->finish();

## delete from zdb_replaced_data table
my $sqlDeleteReplacedData = "delete from zdb_replaced_data where zrepld_old_zdb_id = '$recordToBeDeleted';";
print SQLFILE "$sqlDeleteReplacedData\n\n";
my $curDeleteReplacedData = $dbh->prepare($sqlDeleteReplacedData);
$curDeleteReplacedData->execute();
$curDeleteReplacedData->finish();

## update zdb_replaced_data table
my $sqlUpdateReplacedData = "update zdb_replaced_data set zrepld_new_zdb_id = '$recordToBeMergedInto' where zrepld_new_zdb_id = '$recordToBeDeleted';";
print SQLFILE "$sqlUpdateReplacedData\n\n";
my $curUpdateReplacedData = $dbh->prepare($sqlUpdateReplacedData);
$curUpdateReplacedData->execute();
$curUpdateReplacedData->finish();

## delete from zdb_active_data table
my $sqlDelete = "delete from zdb_active_data where zactvd_zdb_id = '$recordToBeDeleted';";
print SQLFILE "$sqlDelete\n\n";
my $curDelete = $dbh->prepare($sqlDelete);
$curDelete->execute();
$curDelete->finish();

## add new zdb_replaced_data record
my $sqlInsertReplacedData = "insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('$recordToBeDeleted', '$recordToBeMergedInto');";
print SQLFILE "$sqlInsertReplacedData\n\n";
my $curInsertReplacedData = $dbh->prepare($sqlInsertReplacedData);
$curInsertReplacedData->execute();
$curInsertReplacedData->finish();

$dbh->disconnect();

close SQLFILE;

exit;

## recursive function which takes the following 5 parameters:
## 1) value of the primary key column of the record to be deleted 
## 2) value of the primary key column of the record to be merged into
## 3) table name
## 4) primary key column name
## 5) depth
## all merge action related SQLs are generated and stored in a hash data structure to be 
## sorted and executed later on
 
sub recursivelyGetSQLs {
  my $toBeDeleted = shift;
  my $toBeMergedInto = shift;
  my $parentTable = shift;
  my $foreignKey = shift;
  my $depth = shift;
  $depth++;

  my $sql = "select distinct c.table_name, k1.column_name, k2.table_name, k2.column_name
                     from information_schema.constraint_table_usage c,
                          information_schema.key_column_usage k1, information_schema.table_constraints tc1,
                          information_schema.key_column_usage k2, information_schema.table_constraints tc2
                    where k1.column_name = '$foreignKey'
                      and k1.table_name = '$parentTable'
                      and tc1.table_name = k1.table_name
                      and tc1.constraint_name = k1.constraint_name
                      and c.table_name = k1.table_name
                      and c.constraint_name = k2.constraint_name
                      and c.table_name != k2.table_name
                      and tc2.table_name = k2.table_name
                      and tc2.constraint_name = k2.constraint_name
                      and tc2.constraint_type = 'FOREIGN KEY'
                 order by 1, 3, 4;";

  my $cur = $dbh->prepare_cached($sql);
  $cur->execute();
  my ($parentTableName, $colName, $childTableName, $foreignKeyColumn);
  $cur->bind_columns(\$parentTableName, \$colName, \$childTableName, \$foreignKeyColumn) ;
  
  while ($cur->fetch()) {  # the while loop to go thru all the child table one level deeper
    ## only process those that have not been processed before
    if (!exists($processed{$childTableName})) {             
      my $sqlMakeSense = "select * from $childTableName where $foreignKeyColumn = '$toBeDeleted';";
      my $curMakeSense = $dbh->prepare_cached($sqlMakeSense);
      $curMakeSense->execute();
      my $numberOfResults = 0;
      while ($curMakeSense->fetch()) {
        $numberOfResults++;
      }
      $curMakeSense->finish();

      ## do nothing if there is no records in the child table or if it is the record_attribution table (having another script to do something with this table)
      if ($numberOfResults > 0 && $childTableName ne 'record_attribution') {  
        ## find out the columns consisting of unique constrait
        my $sqlGetUniqueKeyColumn = "select kc.column_name from information_schema.key_column_usage kc,
                                                                information_schema.table_constraints tc
                                      where kc.table_name = '$childTableName'
                                        and tc.table_name = kc.table_name
                                        and tc.constraint_name = kc.constraint_name
                                        and tc.constraint_type = 'UNIQUE'";
        my $curGetUniqueKeyColumn = $dbh->prepare_cached($sqlGetUniqueKeyColumn);
        $curGetUniqueKeyColumn->execute();
            
        my $uniqueKeyCol;
        $curGetUniqueKeyColumn->bind_columns(\$uniqueKeyCol);
                
        my %uniqueKeyColumns = ();
        my $foreignKeyColumnInUniqueConstraint = 0;
        my $numberOfUniqueKeyCols = 0;
        while ($curGetUniqueKeyColumn->fetch()) {           
          $uniqueKeyColumns{$uniqueKeyCol} = $depth if ($uniqueKeyCol ne $foreignKeyColumn);
          $foreignKeyColumnInUniqueConstraint = 1 if ($uniqueKeyCol eq $foreignKeyColumn);
          $numberOfUniqueKeyCols++;                 
        }
        
        $curGetUniqueKeyColumn->finish();
  
        my $sqlGetPrimaryKeyColumn = "select kc.column_name from information_schema.key_column_usage kc,
                                                                 information_schema.table_constraints tc
                                       where kc.table_name = '$childTableName'
                                         and tc.table_name = kc.table_name
                                         and tc.constraint_name = kc.constraint_name
                                         and tc.constraint_type = 'PRIMARY KEY'";
        my $curGetPrimaryKeyColumn = $dbh->prepare_cached($sqlGetPrimaryKeyColumn);
        $curGetPrimaryKeyColumn->execute();
            
        my $primaryKeyCol;
        $curGetPrimaryKeyColumn->bind_columns(\$primaryKeyCol);
                
        my %primaryKeyColumns = ();
        my $primaryKeyOfChildTable;
        my $soloPK;
        my $foreignKeyColumnInPrimaryKey = 0;
        my $numberOfPrimaryKeys = 0;
        while ($curGetPrimaryKeyColumn->fetch()) {  
          if ($primaryKeyCol ne $foreignKeyColumn) {
              $primaryKeyColumns{$primaryKeyCol} = $depth;
              $primaryKeyOfChildTable = $primaryKeyCol;
          } else {
              $foreignKeyColumnInPrimaryKey = 1;
              $soloPK = $primaryKeyCol;
          }
          $numberOfPrimaryKeys++;                 
        }
        $curGetPrimaryKeyColumn->finish();  
  
        my $deleteSQL = "";

        ## One senario: when the related foreign key column is part of the unique constraint which consists of more than one columns.
        ## In this senario, the primary key is not a composite key
        ## And, in this senario, the recursive function is called to go one level deeper    
        if ($foreignKeyColumnInUniqueConstraint > 0 && $numberOfUniqueKeyCols > 1 && $numberOfPrimaryKeys == 1) {
                    
          my $selectList = join (", ", keys %uniqueKeyColumns);
              
          my $sqlCheckValuesForMergedInto = "select $primaryKeyOfChildTable, " . $selectList . " from $childTableName where $foreignKeyColumn = '$toBeMergedInto';";             
                                   
          my $curCheckValuesForMergedInto = $dbh->prepare_cached($sqlCheckValuesForMergedInto);
          $curCheckValuesForMergedInto->execute();
          my %valuePairsForMergedInto = ();
          my $primaryKeyColValueForMergedInto;
          my $ctValuePairsMergedInto = 0;
          if ($numberOfUniqueKeyCols == 2) {
              my $uniqueValueForMergedInto;
              $curCheckValuesForMergedInto->bind_columns(\$primaryKeyColValueForMergedInto, \$uniqueValueForMergedInto);                
                                
              while ($curCheckValuesForMergedInto->fetch()) {  
                $ctValuePairsMergedInto++;
                $valuePairsForMergedInto{$uniqueValueForMergedInto} = $primaryKeyColValueForMergedInto;
              }              
                                                
          } elsif ($numberOfUniqueKeyCols == 3) {
              my ($uniqueValueForMergedInto1, $uniqueValueForMergedInto2);
              $curCheckValuesForMergedInto->bind_columns(\$primaryKeyColValueForMergedInto, \$uniqueValueForMergedInto1, \$uniqueValueForMergedInto2);                
                                
              while ($curCheckValuesForMergedInto->fetch()) {  
                $ctValuePairsMergedInto++;
                $valuePairsForMergedInto{$uniqueValueForMergedInto1.$uniqueValueForMergedInto2} = $primaryKeyColValueForMergedInto;
              }

          } elsif ($numberOfUniqueKeyCols == 4) {
              my ($uniqueValueForMergedInto1, $uniqueValueForMergedInto2, $uniqueValueForMergedInto3);
              $curCheckValuesForMergedInto->bind_columns(\$primaryKeyColValueForMergedInto, \$uniqueValueForMergedInto1, \$uniqueValueForMergedInto2, \$uniqueValueForMergedInto3);                
                                
              while ($curCheckValuesForMergedInto->fetch()) {  
                $ctValuePairsMergedInto++;
                $valuePairsForMergedInto{$uniqueValueForMergedInto1.$uniqueValueForMergedInto2.$uniqueValueForMergedInto3} = $primaryKeyColValueForMergedInto;
              }
          }
          
          $curCheckValuesForMergedInto->finish();
              
          ## No need for delete SQL or go to deeper level if there is nothing to check against for unique constraint violation 
          if ($ctValuePairsMergedInto > 0) {
                                                               
            my $sqlCheckValuesForDeleted = "select $primaryKeyOfChildTable, " . $selectList . " from $childTableName where $foreignKeyColumn = '$toBeDeleted';";             
                                    
            my $curCheckValuesForDeleted = $dbh->prepare_cached($sqlCheckValuesForDeleted);
            $curCheckValuesForDeleted->execute();
            my %valuePairsForDeleted = ();
            my $primaryKeyColValueForDeleted;
            my $primaryKeyColValueForMergedIntoAsPara;
                
            if ($numberOfUniqueKeyCols == 2) {
              my $uniqueValueForDeleted;
              $curCheckValuesForDeleted->bind_columns(\$primaryKeyColValueForDeleted, \$uniqueValueForDeleted);                
                
              while ($curCheckValuesForDeleted->fetch()) {  
                # violation found! Need to construct and store the delete SQL:
                if (exists($valuePairsForMergedInto{$uniqueValueForDeleted})) {
                  $deleteSQL = "delete from $childTableName where $primaryKeyOfChildTable = '$primaryKeyColValueForDeleted';";
                  $mergeSQLs{$deleteSQL} = $depth;
                        
                  $primaryKeyColValueForMergedIntoAsPara = $valuePairsForMergedInto{$uniqueValueForDeleted};
                  ## call the recursive function to go one level deeper
                  recursivelyGetSQLs($primaryKeyColValueForDeleted, $primaryKeyColValueForMergedIntoAsPara, $childTableName, $primaryKeyOfChildTable, $depth);                          
                        
                }
              }
            } elsif ($numberOfUniqueKeyCols == 3) {
                my ($uniqueValueForDeleted1, $uniqueValueForDeleted2);
                $curCheckValuesForDeleted->bind_columns(\$primaryKeyColValueForDeleted, \$uniqueValueForDeleted1, \$uniqueValueForDeleted2);                
                
                while ($curCheckValuesForDeleted->fetch()) {  
                  # violation found! Need to construct and store the delete SQL:
                  if (exists($valuePairsForMergedInto{$uniqueValueForDeleted1.$uniqueValueForDeleted2})) {
                    $deleteSQL = "delete from $childTableName where $primaryKeyOfChildTable = '$primaryKeyColValueForDeleted';";
                    $mergeSQLs{$deleteSQL} = $depth;
                        
                    $primaryKeyColValueForMergedIntoAsPara = $valuePairsForMergedInto{$uniqueValueForDeleted1.$uniqueValueForDeleted2};
                    ## call the recursive function to go one level deeper
                    recursivelyGetSQLs($primaryKeyColValueForDeleted, $primaryKeyColValueForMergedIntoAsPara, $childTableName, $primaryKeyOfChildTable, $depth);                                          
                  }
                }
            } elsif ($numberOfUniqueKeyCols == 4) {
                my ($uniqueValueForDeleted1, $uniqueValueForDeleted2, $uniqueValueForDeleted3);
                $curCheckValuesForDeleted->bind_columns(\$primaryKeyColValueForDeleted, \$uniqueValueForDeleted1, \$uniqueValueForDeleted2, \$uniqueValueForDeleted3);                
                
                while ($curCheckValuesForDeleted->fetch()) {  
                  # violation found! Need to construct and store the delete SQL:
                  if (exists($valuePairsForMergedInto{$uniqueValueForDeleted1.$uniqueValueForDeleted2.$uniqueValueForDeleted3})) {
                    $deleteSQL = "delete from $childTableName where $primaryKeyOfChildTable = '$primaryKeyColValueForDeleted';";
                    $mergeSQLs{$deleteSQL} = $depth;
                        
                    $primaryKeyColValueForMergedIntoAsPara = $valuePairsForMergedInto{$uniqueValueForDeleted1.$uniqueValueForDeleted2.$uniqueValueForDeleted3};
                    ## call the recursive function to go one level deeper
                    recursivelyGetSQLs($primaryKeyColValueForDeleted, $primaryKeyColValueForMergedIntoAsPara, $childTableName, $primaryKeyOfChildTable, $depth);                           
                  }
                }
            }
            
            $curCheckValuesForDeleted->finish();

                
          } # end of if ($ctValuePairsMergedInto > 0)
            

        } # end of if ($foreignKeyColumnInUniqueConstraint > 0 && $numberOfUniqueKeyCols > 1 && $foreignKeyColumnInPrimaryKey > 0 && $numberOfPrimaryKeys == 1)
        
        ## Another senario: when the related foreign key column is part of the primary key which consists of more than one columns 
        ## In this senario, the delete SQL should be constructed and stored
        if ($foreignKeyColumnInPrimaryKey == 1 && $numberOfPrimaryKeys > 1) {
                      
          my $selectListP = join (", ", keys %primaryKeyColumns);
              
          my $sqlCheckValuesForMergedIntoP = "select " . $selectListP . " from $childTableName where $foreignKeyColumn = '$toBeMergedInto';";             
                        
          my $curCheckValuesForMergedIntoP = $dbh->prepare_cached($sqlCheckValuesForMergedIntoP);
          $curCheckValuesForMergedIntoP->execute();
          my %valuePairsForMergedIntoP = ();
          my $ctValuePairsMergedIntoP = 0;
          if ($numberOfPrimaryKeys == 2) {
            my $uniqueValueForMergedIntoP;
            $curCheckValuesForMergedIntoP->bind_columns(\$uniqueValueForMergedIntoP);                
                                
            while ($curCheckValuesForMergedIntoP->fetch()) {  
              $ctValuePairsMergedIntoP++;
              $valuePairsForMergedIntoP{$uniqueValueForMergedIntoP} = $depth;
            }
                
          } elsif ($numberOfPrimaryKeys == 3) {
              my ($uniqueValueForMergedIntoP1, $uniqueValueForMergedIntoP2);
              $curCheckValuesForMergedIntoP->bind_columns(\$uniqueValueForMergedIntoP1, \$uniqueValueForMergedIntoP2);                
                                
              while ($curCheckValuesForMergedIntoP->fetch()) {  
                $ctValuePairsMergedIntoP++;
                $valuePairsForMergedIntoP{$uniqueValueForMergedIntoP1.$uniqueValueForMergedIntoP2} = $depth;
              }
          } elsif ($numberOfPrimaryKeys == 4) {
              my ($uniqueValueForMergedIntoP1, $uniqueValueForMergedIntoP2, $uniqueValueForMergedIntoP3);
              $curCheckValuesForMergedIntoP->bind_columns(\$uniqueValueForMergedIntoP1, \$uniqueValueForMergedIntoP2, \$uniqueValueForMergedIntoP3);                
                                
              while ($curCheckValuesForMergedIntoP->fetch()) {  
                $ctValuePairsMergedIntoP++;
                $valuePairsForMergedIntoP{$uniqueValueForMergedIntoP1.$uniqueValueForMergedIntoP2.$uniqueValueForMergedIntoP3} = $depth;
              }
          }
          
          $curCheckValuesForMergedIntoP->finish();
              
          ## No need for delete SQL if there is nothing to check againt for primary key unique constraint violation 
          if ($ctValuePairsMergedIntoP > 0) {
                                                               
            my $sqlCheckValuesForDeletedP = "select " . $selectListP . " from $childTableName where $foreignKeyColumn = '$toBeDeleted';";             
                                    
            my $curCheckValuesForDeletedP = $dbh->prepare_cached($sqlCheckValuesForDeletedP);
            $curCheckValuesForDeletedP->execute();
            my %valuePairsForDeletedP = ();
            my $primaryKeyColValueForDeletedP;
            if ($numberOfPrimaryKeys == 2) {
              my $uniqueValueForDeletedP;
              $curCheckValuesForDeletedP->bind_columns(\$uniqueValueForDeletedP);                
                
              while ($curCheckValuesForDeletedP->fetch()) {  
                # violation found! Need to construct and store the delete SQL:
                if (exists($valuePairsForMergedIntoP{$uniqueValueForDeletedP})) {
                  $deleteSQL = "delete from $childTableName where $foreignKeyColumn = '$toBeDeleted' and ";
                  foreach my $primaryKeyComponent (keys %primaryKeyColumns) {
                    $deleteSQL = $deleteSQL . $primaryKeyComponent . " = '$uniqueValueForDeletedP';";
                  }
                        
                  $mergeSQLs{$deleteSQL} = $depth;                       
                }
              }
            } elsif ($numberOfPrimaryKeys == 3) {
                my ($uniqueValueForDeletedP1, $uniqueValueForDeletedP2);
                $curCheckValuesForDeletedP->bind_columns(\$uniqueValueForDeletedP1, \$uniqueValueForDeletedP2);                
                
                while ($curCheckValuesForDeletedP->fetch()) {  
                  # violation found! Need to construct and store the delete SQL:
                  if (exists($valuePairsForMergedIntoP{$uniqueValueForDeletedP1.$uniqueValueForDeletedP2})) {
                    $deleteSQL = "delete from $childTableName where $foreignKeyColumn = '$toBeDeleted' and ";
                    my $ct = 0;
                    foreach my $primaryKeyComponent (keys %primaryKeyColumns) {
                      $ct++;                         
                      if ($ct == 1) {
                        $deleteSQL = $deleteSQL . $primaryKeyComponent . " = '$uniqueValueForDeletedP1' and ";
                      } else {
                          $deleteSQL = $deleteSQL . $primaryKeyComponent . " = '$uniqueValueForDeletedP2';" 
                      }
                    }
                        
                    $mergeSQLs{$deleteSQL} = $depth;                                              
                  }
                }
            } elsif ($numberOfPrimaryKeys == 4) {
                my ($uniqueValueForDeletedP1, $uniqueValueForDeletedP2, $uniqueValueForDeletedP3);
                $curCheckValuesForDeletedP->bind_columns(\$uniqueValueForDeletedP1, \$uniqueValueForDeletedP2, \$uniqueValueForDeletedP3);                
                
                while ($curCheckValuesForDeletedP->fetch()) {  
                  # violation found! Need to construct and store the delete SQL:
                  if (exists($valuePairsForMergedIntoP{$uniqueValueForDeletedP1.$uniqueValueForDeletedP2.$uniqueValueForDeletedP3})) {
                    $deleteSQL = "delete from $childTableName where $foreignKeyColumn = '$toBeDeleted' and ";
                    my $ct = 0;
                    foreach my $primaryKeyComponent (keys %primaryKeyColumns) {
                      $ct++;
                      if ($ct == 1) {
                          $deleteSQL = $deleteSQL . $primaryKeyComponent . " = '$uniqueValueForDeletedP1' and ";
                      } elsif ($ct == 2) {
                          $deleteSQL = $deleteSQL . $primaryKeyComponent . " = '$uniqueValueForDeletedP2' and ";
                      } else {
                          $deleteSQL = $deleteSQL . $primaryKeyComponent . " = '$uniqueValueForDeletedP3';" 
                      }
                    }
                        
                    $mergeSQLs{$deleteSQL} = $depth;                                              
                  }
                }
            }
            
            $curCheckValuesForDeletedP->finish();
                
          } # end of if ($ctValuePairsMergedIntoP > 0)     


        } # end of if ($foreignKeyColumnInPrimaryKey == 1 && $numberOfPrimaryKeys > 1)
         
        ## In the senario of FK is the solo PK, call the recursive function to go one level deeper without constructing the update SQL
        if ($foreignKeyColumnInPrimaryKey == 1 && $numberOfPrimaryKeys == 1) {                        
            recursivelyGetSQLs($toBeDeleted, $toBeMergedInto, $childTableName, $soloPK, $depth);             
            
        } else {
            ## construct and store the update SQLs first, if flagged 
            my $updateSQL = "update $childTableName 
                                set $foreignKeyColumn = '$toBeMergedInto'
                              where $foreignKeyColumn = '$toBeDeleted';";
            $mergeSQLs{$updateSQL} = $depth;           
        } 
            
      } ## end of if ($numberOfResults > 0 && $childTableName ne 'record_attribution') 

      ## mark the child table as processed
      $processed{$childTableName.$foreignKeyColumn} = $depth;
          
    } ## end of if (!exists($processed{$childTableName}))


        
  } ## end of the while loop to go thru all the child table one level deeper
      
  $cur->finish();       


} # end of recursive function

## in this function, delete from record_attribution SQL(s) are generated and stored in the hash, whenever violation of some unique constrait is found 
sub cleanupRecordAttributionTable {
  my $mergedInto = shift;
  my $toDelete = shift;
  my $order = shift;
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
        $mergeSQLs{$deleteFromRecordAttributionSQL} = $order;                         
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



