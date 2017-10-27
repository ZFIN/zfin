#!/private/bin/perl

# updateZebrafishGeneNames.pl
# It parses the input file, geneNamesToUpdate (got from Ken and uploaded and stored on archive), 
# to prepare the update list and then call updateZebrafishGeneNames.sql to do the updating of ZF gene names.
# 

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

## set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";

system("/bin/rm -f geneNamesToUpdate");
system("/bin/rm -f new_names");
system("/bin/rm -f problemNames");
system("/bin/rm -f updateZebrafishGeneNameSQLlog1");
system("/bin/rm -f updateZebrafishGeneNameSQLlog2");
system("/bin/rm -f namesToUpdate.unl");
system("/bin/rm -f geneNamesUpdatedReport");

system("scp /research/zarchive/load_files/Orthology/geneNamesToUpdate <!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/");

open (INPUTNAMES, "geneNamesToUpdate") ||  die "Cannot open geneNamesToUpdate : $!\n";

@lines = <INPUTNAMES>;

close(INPUTNAMES);

open (UPDATEDGENENAMES, ">new_names") ||  die "Cannot open new_names : $!\n";

$ct = $ctUpdated = 0;

foreach $line (@lines) {
  $ct++;     
  chop($line);
  if ($line =~ m/(ZDB\-GENE\-\d{6}\-\d+)\s+([a-zA-Z0-9_]+)/) {
      print UPDATEDGENENAMES "$1";
  } elsif ($line =~ m/gene name\s*\(z\):(.+)$/) {
      $geneName = $1; 
      $ctUpdated++;
      
      print UPDATEDGENENAMES "\t$geneName\n";   
  }
      
}

print "\ntotal number of lines: $ct\n\n";

print "\ntotal number of lines written to new_names: $ctUpdated\n\n";

close(UPDATEDGENENAMES);

$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

$sqlGetZFgeneNamesByHumanAndMouseOrth =
"select distinct ortho_zebrafish_gene_zdb_id,mrkr_abbrev,mrkr_name
   from ortholog,marker,ortholog_external_reference 
  where oef_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-27','ZDB-FDBCONT-040412-28','ZDB-FDBCONT-040412-23')
   and oef_ortho_zdb_id = ortho_zdb_id
   and ortho_zebrafish_gene_zdb_id = mrkr_zdb_id
   and mrkr_type = 'GENE'
   order by mrkr_abbrev;";

$cur = $dbh->prepare($sqlGetZFgeneNamesByHumanAndMouseOrth);
$cur->execute();
$cur->bind_columns(\$zdbGeneId,\$zdbGeneAbbrev,\$zdbGeneName);

# %geneNamesZFIN
# key: ZF gene name stored at ZFIN 
# value: ZF gene ZDB ID
%geneNamesZFIN = ();

# %geneIdsZFIN 
# key: ZF gene ZDB ID
# value: ZF gene name
%geneIdsZFIN = ();

# %geneSymbolsZFIN
# key: ZF gene name
# value: ZF gene symbol
%geneSymbolsZFIN = ();

while ($cur->fetch()) {
   $geneNamesZFIN{$zdbGeneName} = $zdbGeneId;
   $geneIdsZFIN{$zdbGeneId} = $zdbGeneName;
   $geneSymbolsZFIN{$zdbGeneName} = $zdbGeneAbbrev;
}

$cur->finish(); 

$dbh->disconnect();

open (UPDATEDGENENAMES, "new_names") ||  die "Cannot open new_names : $!\n";
@lines2 = <UPDATEDGENENAMES>;
close(UPDATEDGENENAMES);

## key: new name
## value: ZDB ID
%newGeneNames = ();

open (PROBLEM, ">problemNames") ||  die "Cannot open problemNames : $!\n";
$totalNewNames = $ctProblem = $ctValidNewGeneNames = 0;
foreach $line (@lines2) {
  $totalNewNames++; 
  chop($line);
    
  if ($line =~ m/^(ZDB\-GENE\-\d{6}\-\d+)\s+(.+)$/) {
      $geneZdbID = $1;
      $geneName = $2;
      
      if (ZFINPerlModules->stringStartsWithLetterOrNumber($geneZdbID) && ZFINPerlModules->stringStartsWithLetterOrNumber($geneName)) {
        $geneName =~ s/^\s+//;   #remove leading spaces
        $geneName =~ s/\s+$//; #remove trailing spaces 
        $geneNamesToUpdateTo{$geneZdbID} = $geneName;  
        if (exists($newGeneNames{$geneName})) {
            print PROBLEM "$line\n";
            print PROBLEM "Problem: gene name same as that of the following on the same update-name list\n";
            print PROBLEM "$newGeneNames{$geneName}\n\n";
            $ctProblem++;                
        } elsif (exists($geneNamesZFIN{$geneName})) {
            ### this is just the name is not changed and left in place; it is not a problem
            ##print PROBLEM "$line\n";
            ##print PROBLEM "Problem: new gene name same as that of the following\n";
            ##print PROBLEM "$geneNamesZFIN{$geneName}\n\n";
            ##$ctProblem++;          
        } elsif (!exists($geneIdsZFIN{$geneZdbID})) {
            print PROBLEM "$line\n";
            print PROBLEM "Problem: wrong gene zdb id\n\n";
            $ctProblem++;          
        } elsif ($geneName =~ m/\|/g) {
            print PROBLEM "$line\n";
            print PROBLEM "Problem: gene name could not contain special character |\n\n";
            $ctProblem++;          
        } else {
            $newGeneNames{$geneName} = $geneZdbID;
            $ctValidNewGeneNames++;
        }
      } else {
          print PROBLEM "$line\n";
          print PROBLEM "Problem: missing gene ZDB Id or new gene name\n\n";
          $ctProblem++;       
      }
  }
      
}

close(PROBLEM);

print "\ntotalNewNames = $totalNewNames \nctProblem = $ctProblem \nctValidNewGeneNames = $ctValidNewGeneNames\n\n";

open (UPDATELIST, ">namesToUpdate.unl") ||  die "Cannot open namesToUpdate.unl : $!\n";
foreach $newName (keys %newGeneNames) {
  $id = $newGeneNames{$newName};
  $existingName = $geneIdsZFIN{$id};
  $symbol = $geneSymbolsZFIN{$existingName};  
  print UPDATELIST "$id|$existingName|$newName|$symbol|\n";
}

close(UPDATELIST);

if ($ctProblem == 0) {
    $cmd = "psql -d <!--|DB_NAME|--> -a -f updateZebrafishGeneNames_PG.sql >updateZebrafishGeneNameSQLlog1";
    system($cmd);

    system("/bin/cat updateZebrafishGeneNameSQLlog2 >> updateZebrafishGeneNameSQLlog1");

    $subject = "Auto from $dbname: " . "updateZebrafishGeneNames.pl :: updateZebrafishGeneNameSQLlog";
    ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","updateZebrafishGeneNameSQLlog1");


    $subject = "Auto from $dbname: " . "List of $ctValidNewGeneNames gene names that have been updated based on inputfile by Ken according to NCBI orthology info";
    ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","geneNamesUpdatedReport");
} else {
    $subject = "Auto from $dbname: " . "List of $ctProblem problematic gene names";
    ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","problemNames");
}

exit;


