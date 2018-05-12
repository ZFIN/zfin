#! /private/bin/perl -w 

##
# orphanChecks.pl
#
# Check the consistence and correctness of data in zfin database
##

use Getopt::Long qw(:config bundling);
use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

#######################  Main ###########################################
#
# Define Usage
#

$usage = "Usage: orphanChecks.pl <dbname>  ";

$document = <<ENDDOC;

$usage

Command line parameters:

  dbname   Name of database to validate. This must be a ZFIN database.

ENDDOC

if (@ARGV < 1) {
  print $document and exit 1;
}

#
# Define GLOBALS
#

$globalDbName = $ARGV[0]; #"<!--|DB_NAME|-->";
$globalUsername = "";
$globalPassword = "";

#set environment variables

$dbh = DBI->connect ("DBI:Pg:dbname=$globalDbName;host=localhost", $globalUsername, $globalPassword) or die "Cannot connect to database: $DBI::errstr\n";

$dbaEmail = '<!--|VALIDATION_EMAIL_DBA|-->';
  
$now_string = localtime;

print "\nStart at $now_string \n\n";

system("/bin/rm -f orphansPrevious");
system("/bin/rm -f orphansFound");

open (PREVORPH, ">orphansPrevious") || die "Cannot open orphansPrevious : $!\n";
print PREVORPH "\nAction on the orphans identified previously:\n\n\n";

$sqlOrphanData = "select zorphand_zdb_id, zorphand_home_table, zorphand_home_zdb_id_column from zdb_orphan_data;";
$curOrphanData = $dbh->prepare($sqlOrphanData);
$curOrphanData->execute();
$curOrphanData->bind_columns(\$orphanId,\$orphanTable, \$orphanCol);

$ctPrev = 0;
while ($curOrphanData->fetch()) {
  $sqlCheck = "select $orphanCol from $orphanTable where $orphanCol = '$orphanId';";
  $curCheck = $dbh->prepare($sqlCheck);
  $curCheck->execute();
  $curCheck->bind_columns(\$orphanIdStillThere);
  $ctStillThere = 0;
  while ($curCheck->fetch()) {
    $ctStillThere++;
  }
  $curCheck->finish();
  
  if ($ctStillThere > 0) {
     $ctPrev++;
     $sqlDeleteOrphData = "delete from zdb_orphan_data where zorphand_zdb_id = '$orphanId';";
     print PREVORPH "$sqlDeleteOrphData\n";
     $curDeleteOrphData = $dbh->prepare($sqlDeleteOrphData);
     $curDeleteOrphData->execute();
     $curDeleteOrphData->finish();
  } else {
     $sqlCheckActiveData = "select zactvd_zdb_id from zdb_active_data where zactvd_zdb_id = '$orphanId';";
     $curCheckActiveData = $dbh->prepare($sqlCheckActiveData);
     $curCheckActiveData->execute();
     $curCheckActiveData->bind_columns(\$orphanIdStillActiveData);
     $ctStillActiveData = 0;
     while ($curCheckActiveData->fetch()) {
       $ctStillActiveData++;
     }
     $curCheckActiveData->finish();
     if ($ctStillActiveData > 0) {
         $ctPrev++;
         $sqlDeleteOrphData2 = "delete from zdb_orphan_data where zorphand_zdb_id = '$orphanId';";
         print PREVORPH "$sqlDeleteOrphData2\n";
         $curDeleteOrphData2 = $dbh->prepare($sqlDeleteOrphData2);
         $curDeleteOrphData2->execute();
         $curDeleteOrphData2->finish();     
     } else {     
         $sqlDeleteActiveData = "delete from zdb_active_data where zactvd_zdb_id = '$orphanId';";
         print PREVORPH "$sqlDeleteActiveData\n";
         $curDeleteActiveData = $dbh->prepare($sqlDeleteActiveData);
         $curDeleteActiveData->execute();
         $curDeleteActiveData->finish();  
     }
  }

}

$curOrphanData->finish();

$sqlOrphanDataS = "select zorphans_zdb_id, zorphans_home_table, zorphans_home_zdb_id_column from zdb_orphan_source;";
$sqlOrphanDataS = $dbh->prepare($sqlOrphanDataS);
$sqlOrphanDataS->execute();
$sqlOrphanDataS->bind_columns(\$orphanIdS,\$orphanTableS, \$orphanColS);

while ($sqlOrphanDataS->fetch()) {
  $sqlCheckS = "select $orphanColS from $orphanTableS where $orphanColS = '$orphanIdS';";
  $curCheckS = $dbh->prepare($sqlCheckS);
  $curCheckS->execute();
  $curCheckS->bind_columns(\$orphanIdStillThereS);
  $ctStillThere = 0;
  while ($curCheckS->fetch()) {
    $ctStillThere++;
  }
  $curCheckS->finish();
  
  if ($ctStillThere > 0) {
     $ctPrev++;
     $sqlDeleteOrphSrc = "delete from zdb_orphan_source where zorphans_zdb_id = '$orphanIdS';";
     print PREVORPH "$sqlDeleteOrphSrc\n";
     $curDeleteOrphSrc = $dbh->prepare($sqlDeleteOrphSrc);
     $curDeleteOrphSrc->execute();
     $curDeleteOrphSrc->finish();
  } else {
     $sqlCheckActiveSrc = "select zactvs_zdb_id from zdb_active_source where zactvs_zdb_id = '$orphanIdS';";
     $curCheckActiveSrc = $dbh->prepare($sqlCheckActiveSrc);
     $curCheckActiveSrc->execute();
     $curCheckActiveSrc->bind_columns(\$orphanIdStillActiveSrc);
     $ctStillActiveSrc = 0;
     while ($curCheckActiveSrc->fetch()) {
       $ctStillActiveSrc++;
     }
     $curCheckActiveSrc->finish();
     if ($ctStillActiveSrc > 0) {
         $ctPrev++;
         $sqlDeleteOrphSrc2 = "delete from zdb_orphan_source where zorphans_zdb_id = '$orphanIdS';";
         print PREVORPH "$sqlDeleteOrphSrc2\n";
         $curDeleteOrphSrc2 = $dbh->prepare($sqlDeleteOrphSrc2);
         $curDeleteOrphSrc2->execute();
         $curDeleteOrphSrc2->finish();     
     } else {     
         $sqlDeleteActiveSrc = "delete from zdb_active_source where zactvs_zdb_id = '$orphanIdS';";
         print PREVORPH "$sqlDeleteActiveSrc\n";
         $curDeleteActiveSrc = $dbh->prepare($sqlDeleteActiveSrc);
         $curDeleteActiveSrc->execute();
         $curDeleteActiveSrc->finish();  
     }
  }

}

$sqlOrphanDataS->finish();

close PREVORPH;

ZFINPerlModules->sendMailWithAttachedReport($dbaEmail,"about previous orphans","orphansPrevious") if $ctPrev > 0;


$sqlAllTypes = "select zobjtype_name, zobjtype_home_table, zobjtype_home_zdb_id_column from zdb_object_type;";
$curAllTypes = $dbh->prepare($sqlAllTypes);
$curAllTypes->execute();
$curAllTypes->bind_columns(\$typeName, \$tableName, \$colName);

%allTypesTables = ();
%allTypesCols = ();
while ($curAllTypes->fetch()) {
  $allTypesTables{$typeName} = $tableName;
  $allTypesCols{$typeName} = $colName;
}
$curAllTypes->finish();

$sqlAllActiveData = "select zactvd_zdb_id from zdb_active_data;";
$curAllActiveData = $dbh->prepare($sqlAllActiveData);
$curAllActiveData->execute();
$curAllActiveData->bind_columns(\$activeDataId);

%allIds = ();
while ($curAllActiveData->fetch()) {
  if ($activeDataId =~ m/^ZDB\-([A-Z]+)\-\d{6}\-\d+$/) {
      $allIds{$activeDataId} = $1;
  }
}
$curAllActiveData->finish();

$sqlAllActiveSrc = "select zactvs_zdb_id from zdb_active_source;";
$curAllActiveSrc = $dbh->prepare($sqlAllActiveSrc);
$curAllActiveSrc->execute();
$curAllActiveSrc->bind_columns(\$activeSrcId);

while ($curAllActiveSrc->fetch()) {
  if ($activeSrcId =~ m/^ZDB\-([A-Z]+)\-\d{6}\-\d+$/) {
      $allIds{$activeSrcId} = $1;
  }
}
$curAllActiveSrc->finish();

$ctDataOrph = 0;
$ctSourceOrph = 0;
open (ORPH, ">orphansFound") || die "Cannot open orphansFound : $!\n";
print ORPH "\nAction on the orphans found this time:\n\n";

foreach $id (keys %allIds) {  
  $value = $allIds{$id};
  
  if (exists($allTypesTables{$value}) && exists($allTypesCols{$value})) {
    $dataTable = $allTypesTables{$value};
    $dataCol = $allTypesCols{$value};
  
    $sqlCheckOrph = "select $dataCol from $dataTable where $dataCol = '$id';";
    
    $curCheckOrph = $dbh->prepare($sqlCheckOrph);
    $curCheckOrph->execute();
    $curCheckOrph->bind_columns(\$idStillThere);
    $ctStillThere = 0;
    while ($curCheckOrph->fetch()) {
      $ctStillThere++;
    }
    $curCheckOrph->finish();
    
    if ($ctStillThere == 0) {   
      $orphTable = 'zdb_orphan_source';
      $deleteTable = 'zdb_active_source';
      $deleteCol = 'zactvs_zdb_id';
      if ($value eq 'COMPANY' || $value eq 'JRNL' || $value eq 'LAB' || $value eq 'PERS' || $value eq 'PUB' || $value eq 'SALIAS') {
          $ctSourceOrph++;
      } else {
          $ctDataOrph++;
          $orphTable = 'zdb_orphan_data';
          $deleteTable = 'zdb_active_data';
          $deleteCol = 'zactvd_zdb_id';
      }
      $sqlInsertOrphTable = "insert into $orphTable values('$id', '$dataTable', '$dataCol');";
      print ORPH "$sqlInsertOrphTable\n";
      $curInsertOrphTable = $dbh->prepare($sqlInsertOrphTable);
      $curInsertOrphTable->execute();
      $curInsertOrphTable->finish(); 
      
      $sqlDelete = "delete from $deleteTable where $deleteCol = '$id';";
      print ORPH "$sqlDelete\n";
      $curDelete = $dbh->prepare($sqlDelete);
      $curDelete->execute();
      $curDelete->finish();      
    }
  }  
}

if ($ctDataOrph > 0) {
  $sqlUpdateHistoryOrphData = "update validate_check_history set vldcheck_count = $ctDataOrph where vldcheck_name = 'zdbActiveDataStillActive';";    
  $curUpdateHistoryOrphData = $dbh->prepare($sqlUpdateHistoryOrphData);
  $curUpdateHistoryOrphData->execute();
  $curUpdateHistoryOrphData->finish();
}

if ($ctSourceOrph > 0) {
  $sqlUpdateHistoryOrphSrc = "update validate_check_history set vldcheck_count = $ctSourceOrph where vldcheck_name = 'zdbActiveSourceStillActive';";    
  $curUpdateHistoryOrphSrc = $dbh->prepare($sqlUpdateHistoryOrphSrc);
  $curUpdateHistoryOrphSrc->execute();
  $curUpdateHistoryOrphSrc->finish();
}

close ORPH;

ZFINPerlModules->sendMailWithAttachedReport($dbaEmail,"new orphans found","orphansFound") if $ctDataOrph > 0 || $ctSourceOrph > 0;

$dbh->disconnect();

$now_string = localtime;

print "\nEnd at $now_string \n\n";

exit;



