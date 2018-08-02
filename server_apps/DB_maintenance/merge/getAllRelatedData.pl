#!/private/bin/perl

## getAllRelatedData.pl
## getAllRelatedData.pl ZDB_ID

## This program is used to generate all related data for a given ZFIN record

use strict;
use DBI;

## check commandline parameters
die "Usage: getAllRelatedData.pl ZDBID\n" if @ARGV != 1;

my $record = $ARGV[0]; 

my $dbname = "<!--|DB_NAME|-->";
my $username = "";
my $password = "";

### open a handle on the db
my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "\n\nCannot connect to PostgreSQL database: $DBI::errstr\n\n";

my $type;
if ($record =~ m/^ZDB\-([A-Z]+)\-\d{6}\-\d+$/) {
    $type = $1;
} else {
    die "Not a valid ZDB ID\n";
}

my $tableName;
my $primaryKeyColumn;

## if not zdb_active_data records
if ($type eq 'COMPANY' || $type eq 'JRNL' || $type eq 'LAB' || $type eq 'PERS' || $type eq 'PUB' || $type eq 'SALIAS') {
   $tableName = 'zdb_active_source';
   $primaryKeyColumn = 'zactvs_zdb_id';
} else {
   $tableName = 'zdb_active_data';
   $primaryKeyColumn = 'zactvd_zdb_id';
}

## validate the ZDB ID
validateZDBID($record, $tableName, $primaryKeyColumn);

my %processed = ();
$processed{$tableName.$primaryKeyColumn} = 0;

open (REPORT, ">allRelatedData.txt") || die "Cannot open allRelatedData.txt : $!\n";

## call the recursive function to report all related data
recursivelyGetAllRelatedData($record, $tableName, $primaryKeyColumn, 0);

$dbh->disconnect();

close REPORT;

exit;

## recursive function which takes the following 4 parameters:
## 1) value of the primary key column of the record
## 2) table name
## 3) primary key column name
## 4) depth
## all merge action related SQLs are generated and stored in a hash data structure to be 
## sorted and executed later on
 
sub recursivelyGetAllRelatedData {
  my $recordID = shift;
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
    if (!exists($processed{$childTableName.$foreignKeyColumn})) {       
      my $sqlGetData = "select * from $childTableName where $foreignKeyColumn = '$recordID';";
      my $curGetData = $dbh->prepare_cached($sqlGetData);
      $curGetData->execute();
      my $numberOfResults = 0;
      my @row = ();
      while (@row = $curGetData->fetchrow_array()) {
        $numberOfResults++;
        print REPORT "\nDepth:$depth\tParent Table:$parentTableName\t$colName\tChild Table:$childTableName\t$foreignKeyColumn\nData:\n @row \n";
      }
      $curGetData->finish();

      ## do nothing if there is no records in the child table
      if ($numberOfResults > 0) {  
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
  
        ## One senario: when the related foreign key column is part of the unique constraint which consists of more than one columns.
        ## In this senario, the primary key is not a composite key
        ## And, in this senario, the recursive function is called to go one level deeper    
        if ($foreignKeyColumnInUniqueConstraint > 0 && $numberOfUniqueKeyCols > 1 && $numberOfPrimaryKeys == 1) {
                    
          my $selectList = join (", ", keys %uniqueKeyColumns);
              
          my $sqlCheckValues = "select $primaryKeyOfChildTable, " . $selectList . " from $childTableName where $foreignKeyColumn = '$recordID';";             
            
          my $curCheckValues = $dbh->prepare_cached($sqlCheckValues);
          $curCheckValues->execute();
          my %valuePairs = ();
          my $primaryKeyColValue;
          my $ctValuePairs = 0;
          if ($numberOfUniqueKeyCols == 2) {
              my $uniqueValue;
              $curCheckValues->bind_columns(\$primaryKeyColValue, \$uniqueValue);                
                                
              while ($curCheckValues->fetch()) {  
                $ctValuePairs++;
                $valuePairs{$uniqueValue} = $primaryKeyColValue;
              }              
                                                
          } elsif ($numberOfUniqueKeyCols == 3) {
              my ($uniqueValue1, $uniqueValue2);
              $curCheckValues->bind_columns(\$primaryKeyColValue, \$uniqueValue1, \$uniqueValue2);                
                                
              while ($curCheckValues->fetch()) {  
                $ctValuePairs++;
                $valuePairs{$uniqueValue1.$uniqueValue2} = $primaryKeyColValue;
              }

          } elsif ($numberOfUniqueKeyCols == 4) {
              my ($uniqueValue1, $uniqueValue2, $uniqueValue3);
              $curCheckValues->bind_columns(\$primaryKeyColValue, \$uniqueValue1, \$uniqueValue2, \$uniqueValue3);                
                                
              while ($curCheckValues->fetch()) {  
                $ctValuePairs++;
                $valuePairs{$uniqueValue1.$uniqueValue2.$uniqueValue3} = $primaryKeyColValue;
              }
          }
          
          $curCheckValues->finish();
              
          ## No need for delete SQL or go to deeper level if there is nothing to check against for unique constraint
          if ($ctValuePairs > 0) {

            ## call the recursive function to go one level deeper
            recursivelyGetAllRelatedData($primaryKeyColValue, $childTableName, $primaryKeyOfChildTable, $depth);
                                                                              
          } 
            

        } # end of if ($foreignKeyColumnInUniqueConstraint > 0 && $numberOfUniqueKeyCols > 1 && $foreignKeyColumnInPrimaryKey > 0 && $numberOfPrimaryKeys == 1)
        
         
        ## In the senario of FK is the solo PK, call the recursive function to go one level deeper without constructing the update SQL
        if ($foreignKeyColumnInPrimaryKey == 1 && $numberOfPrimaryKeys == 1) {                        
            recursivelyGetAllRelatedData($recordID, $childTableName, $soloPK, $depth);             
            
        } 
            
      } ## end of if ($numberOfResults > 0) 

      ## mark the child table as processed
      $processed{$childTableName.$foreignKeyColumn} = $depth;
          
    } ## end of if (!exists($processed{$childTableName.$foreignKeyColumn}))


        
  } ## end of the while loop to go thru all the child table one level deeper
      
  $cur->finish();       


} # end of recursive function


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

