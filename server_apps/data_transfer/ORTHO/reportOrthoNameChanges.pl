#!/private/bin/perl

# reportOrthoNameChanges.pl
# The script then parses data in the above files and store related data in a series of data structures and then compare data stored at ZFIN to decide what
# should be updated. 

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

## set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";

sub reportOrthoNameChanges() {

    
    &doSystemCommand("scp /research/zarchive/load_files/Orthology/alreadyExamined <!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/")  if (!-e "alreadyExamined");
    
    open (ALREADY, "alreadyExamined") ||  die "Cannot open alreadyExamined : $!\n";

    @linesAlreadyDone = <ALREADY>;
    
    close(ALREADY);
    
    $ctAlready = 0;
    
# Use the following hash to store the gene records already examined
    
# %zdbGeneIdsAlreadyExamined
# key: ZDB gene ID
# value: gene symbol
    
    %zdbGeneIdsAlreadyExamined = ();

    foreach $line (@linesAlreadyDone) {
	
	if ($line =~ m/(ZDB\-GENE\-\d{6}\-\d+)\s+([a-zA-Z0-9_]+)/) {
	    $zdbGeneIdsAlreadyExamined{$1} = $2;
	    $ctAlready++; 
	} 
	
    }

# Use the following 3 hashes to store the human, mouse and fly gene names parsed from NCBI data files

# %NCBIidsAndNamesHuman
# key: human gene NCBI ID
# value: human gene name

%NCBIidsAndNamesHuman = (); 

# %NCBIidsAndNamesMouse
# key: mouse gene NCBI ID
# value: mouse gene name

%NCBIidsAndNamesMouse = (); 

# %NCBIidsAndNamesFly
# key: fly gene NCBI ID
# value: fly gene name

%NCBIidsAndNamesFly = (); 


open (NCBI, "ortholog_info") ||  die "Cannot open ortholog_info : $!\n";

$ctLines = $ctHumanGenes = $ctMouseGenes = $ctFlyGenes = 0;

while (<NCBI>) {
  chomp;
  
  @fieldsNCBI = split("\t");
  
  $taxId = $fieldsNCBI[0];
  
  ## excluding species other than human, mouse, fly and the documentation lines
  
  $ctLines++;
  
  ## human gene info
  if ($taxId eq "9606") {
     
    $humanNCBIgeneID = $fieldsNCBI[1];
    if (ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[11])) {
        $NCBIidsAndNamesHuman{$humanNCBIgeneID} = $fieldsNCBI[11];
    } else {
        if (ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[8])) {
            $NCBIidsAndNamesHuman{$humanNCBIgeneID} = $fieldsNCBI[8];
        } else {
            print "\nProblematic human ortholog data: $_\n\n";
        }
    }
    $ctHumanGenes++;
  }
  
  ## mouse gene info
  if ($taxId eq "10090") {
  
    $mouseNCBIgeneID = $fieldsNCBI[1];
    if (ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[11])) {
        $NCBIidsAndNamesMouse{$mouseNCBIgeneID} = $fieldsNCBI[11];
    } else {
        if (ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[8])) {
            $NCBIidsAndNamesMouse{$mouseNCBIgeneID} = $fieldsNCBI[8];
        } else {
            print "\nProblematic mouse ortholog data: $_\n\n";
        }
    }
    $ctMouseGenes++;
  }

  ## fly gene info
  if ($taxId eq "7227") {
   
    $flyNCBIgeneID = $fieldsNCBI[1];
    if (ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[11])) {
         $NCBIidsAndNamesFly{$flyNCBIgeneID} = $fieldsNCBI[11];
    } else {
        if (ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[8])) {
            $NCBIidsAndNamesFly{$mouseNCBIgeneID} = $flyNCBIgeneID[8];
        } else {
            print "\nProblematic fly ortholog data: $_\n\n";
        }
    }    
    $ctFlyGenes++;
  }  
      
}

print "\ntotal number of lines parsed: $ctLines\nnumber of human genes: $ctHumanGenes\nnumber of mouse genes: $ctMouseGenes  \nnumber of fly genes: $ctFlyGenes\n\n";

print LOG "\ntotal number of lines parsed: $ctLines\nnumber of human genes: $ctHumanGenes\nnumber of mouse genes: $ctMouseGenes  \nnumber of fly genes: $ctFlyGenes\n\n";

close(NCBI);

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";

### needs to change to conform to new schema.
$sqlGetZFgeneNamesByHumanAndMouseOrth =
'select distinct organism_common_name,ortho_other_species_name,ortho_other_species_symbol,oef_accession_number,ortho_zebrafish_gene_zdb_id,mrkr_abbrev,mrkr_name from ortholog, ortholog_external_reference ,marker, organism where oef_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-27","ZDB-FDBCONT-040412-28","ZDB-FDBCONT-040412-23") and ortho_zdb_id = oef_ortho_zdb_id and ortho_zebrafish_gene_zdb_id = mrkr_zdb_id and mrkr_type = "GENE" and organism_taxid = ortho_other_species_taxid order by mrkr_abbrev;';

$cur = $dbh->prepare($sqlGetZFgeneNamesByHumanAndMouseOrth);
$cur->execute();
$cur->bind_columns(\$organism,\$orthoName,\$orthoAbbrev,\$ncbiID,\$zdbGeneId,\$zdbGeneAbbrev,\$zdbGeneName);

# %namesZFgene - for zebrafish gene names
# key: ZF gene ZDB ID
# value: ZF gene name

%namesZFgene = ();

# %symbolsZFgene - for zebrafish gene symbols
# key: ZF gene ZDB ID
# value: ZF gene symbol

%symbolsZFgene = ();

# %choppedNames - for zebrafish gene names with last letter chopped
# in many cases, there are two(2) copies of zebrafish genes 
# key: ZF gene ZDB ID
# value: ZF gene name with last letter chopped

%choppedNames = ();

# %lastLetters - for last letters of zebrafish gene names 
# in many cases, there are two(2) copies of zebrafish genes 
# key: ZF gene ZDB ID
# value: last letter of ZF gene name

%lastLetters = ();

# %namesHumanOrthZFIN - for human orthology names stored at ZFIN
# key: ZF gene ZDB ID
# value: human orthology name (stored in ortholog)

%namesHumanOrthZFIN = ();


# %symbolsHumanOrthZFIN - for human orthology symbols stored at ZFIN
# key: ZF gene ZDB ID
# value: human orthology symbol (stored in ortholog)

%symbolsHumanOrthZFIN = ();

# %namesHumanOrthNCBI - for human orthology names from NCBI
# key: ZF gene ZDB ID
# value: human orthology name from NCBI

%namesHumanOrthNCBI = ();

# %namesMouseOrthZFIN - for mouse orthology names stored at ZFIN
# key: ZF gene ZDB ID
# value: mouse orthology name (stored in ortholog)

%namesMouseOrthZFIN = ();

# %symbolsMouseOrthZFIN - for mouse orthology symbols stored at ZFIN
# key: ZF gene ZDB ID
# value: mouse orthology symbol (stored in ortholog)

%symbolsMouseOrthZFIN = ();


# %namesMouseOrthNCBI - for human orthology names from NCBI
# key: ZF gene ZDB ID
# value: human orthology name from NCBI

%namesMouseOrthNCBI = ();


# %namesFlyOrthZFIN - for fly orthology names stored at ZFIN
# key: ZF gene ZDB ID
# value: fly orthology name (stored in ortholog)

%namesFlyOrthZFIN = ();

# %symbolsFlyOrthZFIN - for fly orthology symbols stored at ZFIN
# key: ZF gene ZDB ID
# value: fly orthology symbol (stored in ortholog)

%symbolsFlyOrthZFIN = ();

# %namesFlyOrthNCBI - for fly orthology names from NCBI
# key: ZF gene ZDB ID
# value: fly orthology name from NCBI

%namesFlyOrthNCBI = ();

open (NOTFOUND, ">orthoNcbiIdsNotFoundReport.txt") || die "Cannot open orthoNcbiIdsNotFoundReport.txt : $!\n";

$ctNotFound = 0;

$ct = 0;
while ($cur->fetch()) {
$ct++;
   $namesZFgene{$zdbGeneId} = $zdbGeneName;
   $symbolsZFgene{$zdbGeneId} = $zdbGeneAbbrev;
   
   $choppedName = $zdbGeneName;
   
   $lastLetter = chop($choppedName);
   
   $choppedNames{$zdbGeneId} = $choppedName;
   
   $lastLetters{$zdbGeneId} = $lastLetter;
   

   if ($organism eq "Human") {
       $namesHumanOrthZFIN{$zdbGeneId} = $orthoName;
       $symbolsHumanOrthZFIN{$zdbGeneId} = $orthoAbbrev;
       if (exists($NCBIidsAndNamesHuman{$ncbiID})) {
           $namesHumanOrthNCBI{$zdbGeneId} = $NCBIidsAndNamesHuman{$ncbiID};
       } else {
           print NOTFOUND "$zdbGeneId\t$zdbGeneAbbrev\t$ncbiID\t$organism\n";
           $ctNotFound++;
       }
   } elsif ($organism eq "Mouse") {
       $namesMouseOrthZFIN{$zdbGeneId} = $orthoName;
       $symbolsMouseOrthZFIN{$zdbGeneId} = $orthoAbbrev;
       if (exists($NCBIidsAndNamesMouse{$ncbiID})) {
           $namesMouseOrthNCBI{$zdbGeneId} = $NCBIidsAndNamesMouse{$ncbiID};
       } else {
           print NOTFOUND "$zdbGeneId\t$zdbGeneAbbrev\t$ncbiID\t$organism\n";
           $ctNotFound++;
       }
   } elsif ($organism eq "Fruit fly") {
       $namesFlyOrthZFIN{$zdbGeneId} = $orthoName;
       $symbolsFlyOrthZFIN{$zdbGeneId} = $orthoAbbrev;
       if (exists($NCBIidsAndNamesFly{$ncbiID})) {
           $namesFlyOrthNCBI{$zdbGeneId} = $NCBIidsAndNamesFly{$ncbiID};
       } else {
           print NOTFOUND "$zdbGeneId\t$zdbGeneAbbrev\t$ncbiID\t$organism\n";
           $ctNotFound++;
       }
   } else {
       print LOG "\nError: $organism   $ncbiID   $zdbGeneId   $zdbGeneAbbrev    $zdbGeneName\n"; 
   }
   
}

print "\n\nct = $ct\n\n";



$cur->finish(); 

$dbh->disconnect();


$ctAll3NamesIdentical = $ctIdenticalToHumanAndNoMouse = $ctIdenticalToMouseAndNoHuman = $ctNoneHumanOrMouse = 0;

$total = 0;
$ctDiffrentZFgeneNames = $ctUpdatedOrthNames = 0;

open (ORTHNAMEREPORT, ">orthoNamesUpdatedReport.txt") || die "Cannot open orthNamesUpdatedReport : $!\n";
open (ORTHNAMEUPDATE, ">orthoNamesUpdateList.txt") || die "Cannot open orthNamesUpdateList.unl : $!\n";
open (ZFNAMEREPORT, ">orthoInconsistentZebrafishGeneNamesReport.txt") ||  die "Cannot open inconsistentZebrafishGeneNamesReport : $!\n";
open (PREVIOUSLYREPORTED, ">>alreadyExamined") ||  die "Cannot open alreadyExamined : $!\n";

%zdbGeneIdsAlreadyReported = ();

# sort by the hash values, i.e. the gene symbols
foreach $zdbGeneId (sort {$symbolsZFgene{$a} cmp $symbolsZFgene{$b}} (keys %symbolsZFgene)) {
   $total++;
   $geneNameZF = $namesZFgene{$zdbGeneId};
   $geneNameZFnoComma = $geneNameZF;
   $geneNameZFnoComma =~ s/,//g;
   $geneNameZFnoCommaLowerCase = lc($geneNameZFnoComma);
   
   $geneNameZFChopped = $choppedNames{$zdbGeneId};
   $geneNameZFChoppedNoComma = $geneNameZFChopped;
   $geneNameZFChoppedNoComma =~ s/,//g;   
   $geneNameZFChoppedNoCommaLowerCase = lc($geneNameZFChoppedNoComma);
   
   $lastLetter = $lastLetters{$zdbGeneId};
      
   if (exists($namesHumanOrthNCBI{$zdbGeneId})) {
       $humanGeneName = $namesHumanOrthNCBI{$zdbGeneId};
       $humanGeneNameNoComma = $humanGeneName;
       $humanGeneNameNoComma =~ s/,//g;
       $humanGeneNameNoCommaLowerCase = lc($humanGeneNameNoComma);
       
       if ($geneNameZFnoCommaLowerCase !~ m/\Q$humanGeneNameNoCommaLowerCase/ && $humanGeneNameNoCommaLowerCase !~ m/\Q$geneNameZFChoppedNoCommaLowerCase/) {
              if (!exists($zdbGeneIdsAlreadyExamined{$zdbGeneId}) && !exists($zdbGeneIdsAlreadyReported{$zdbGeneId})) {              
                $zdbGeneIdsAlreadyReported{$zdbGeneId} = $symbolsZFgene{$zdbGeneId};
                $ctDiffrentZFgeneNames++;
                print ZFNAMEREPORT "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
                print PREVIOUSLYREPORTED "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
                print ZFNAMEREPORT "gene name (z): $geneNameZF\n";
                print PREVIOUSLYREPORTED "gene name (z): $geneNameZF\n";
                print ZFNAMEREPORT "gene name (h): $humanGeneName\n"; 
                print PREVIOUSLYREPORTED "gene name (h): $humanGeneName\n"; 
                print ZFNAMEREPORT "gene symbol (h): $symbolsHumanOrthZFIN{$zdbGeneId}\n";
                print PREVIOUSLYREPORTED "gene symbol (h): $symbolsHumanOrthZFIN{$zdbGeneId}\n";
                if (exists($namesMouseOrthNCBI{$zdbGeneId})) {
                    print ZFNAMEREPORT "gene name (m): $namesMouseOrthNCBI{$zdbGeneId}\n";
                    print PREVIOUSLYREPORTED "gene name (m): $namesMouseOrthNCBI{$zdbGeneId}\n";
                    print ZFNAMEREPORT "gene symbol (m): $symbolsMouseOrthZFIN{$zdbGeneId}\n\n";
                    print PREVIOUSLYREPORTED "gene symbol (m): $symbolsMouseOrthZFIN{$zdbGeneId}\n\n";
                } else {
                    print ZFNAMEREPORT "gene name (m):\n";
                    print PREVIOUSLYREPORTED "gene name (m):\n";
                    print ZFNAMEREPORT "gene symbol (m):\n\n";
                    print PREVIOUSLYREPORTED "gene symbol (m):\n\n";
                }
              }
       } 
   } elsif (exists($namesMouseOrthNCBI{$zdbGeneId})) { 
      if (!exists($zdbGeneIdsAlreadyExamined{$zdbGeneId}) && !exists($zdbGeneIdsAlreadyReported{$zdbGeneId})) { 
       $mouseGeneName = $namesMouseOrthNCBI{$zdbGeneId};
       $mouseGeneNameNoComma = $mouseGeneName;
       $mouseGeneNameNoComma =~ s/,//g;
       $mouseGeneNameNoCommaLowerCase = lc($mouseGeneNameNoComma);
       if ($geneNameZFnoCommaLowerCase !~ m/\Q$mouseGeneNameNoCommaLowerCase/ && $mouseGeneNameNoCommaLowerCase !~ m/\Q$geneNameZFChoppedNoCommaLowerCase/) {                             
                $zdbGeneIdsAlreadyReported{$zdbGeneId} = $symbolsZFgene{$zdbGeneId};
                $ctDiffrentZFgeneNames++;
                print ZFNAMEREPORT "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
                print PREVIOUSLYREPORTED "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
                print ZFNAMEREPORT "gene name (z): $geneNameZF\n";
                print PREVIOUSLYREPORTED "gene name (z): $geneNameZF\n";
                print ZFNAMEREPORT "gene name (h):\n"; 
                print PREVIOUSLYREPORTED "gene name (h):\n";
                print ZFNAMEREPORT "gene symbol (h):\n"; 
                print PREVIOUSLYREPORTED "gene symbol (h):\n"; 
                print ZFNAMEREPORT "gene name (m): $mouseGeneName\n";
                print PREVIOUSLYREPORTED "gene name (m): $mouseGeneName\n";                
                print ZFNAMEREPORT "gene symbol (m): $symbolsMouseOrthZFIN{$zdbGeneId}\n\n";
                print PREVIOUSLYREPORTED "gene symbol (m): $symbolsMouseOrthZFIN{$zdbGeneId}\n\n";           
       }      
     }
   } 
   
   if(exists($namesHumanOrthZFIN{$zdbGeneId}) && exists($namesHumanOrthNCBI{$zdbGeneId}) && $namesHumanOrthZFIN{$zdbGeneId} ne $namesHumanOrthNCBI{$zdbGeneId}) {
       $ctUpdatedOrthNames++;
       print ORTHNAMEREPORT "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
       print ORTHNAMEREPORT "human orthology name at ZFIN: $namesHumanOrthZFIN{$zdbGeneId}\n";
       print ORTHNAMEREPORT "human orthology name at NCBI: $namesHumanOrthNCBI{$zdbGeneId}\n\n";  
       print ORTHNAMEUPDATE "$zdbGeneId|Human|$namesHumanOrthZFIN{$zdbGeneId}|$namesHumanOrthNCBI{$zdbGeneId}|\n";
   }
   
   if(exists($namesMouseOrthZFIN{$zdbGeneId}) && exists($namesMouseOrthNCBI{$zdbGeneId}) && $namesMouseOrthZFIN{$zdbGeneId} ne $namesMouseOrthNCBI{$zdbGeneId}) {
       $ctUpdatedOrthNames++;
       print ORTHNAMEREPORT "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
       print ORTHNAMEREPORT "mouse orthology name at ZFIN: $namesMouseOrthZFIN{$zdbGeneId}\n";
       print ORTHNAMEREPORT "mouse orthology name at NCBI: $namesMouseOrthNCBI{$zdbGeneId}\n\n";
       print ORTHNAMEUPDATE "$zdbGeneId|Mouse|$namesMouseOrthZFIN{$zdbGeneId}|$namesMouseOrthNCBI{$zdbGeneId}|\n";
   }   
   
   if(exists($namesFlyOrthZFIN{$zdbGeneId}) && exists($namesFlyOrthNCBI{$zdbGeneId}) && $namesFlyOrthZFIN{$zdbGeneId} ne $namesFlyOrthNCBI{$zdbGeneId}) {
       $ctUpdatedOrthNames++;
       print ORTHNAMEREPORT "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
       print ORTHNAMEREPORT "fly orthology name at ZFIN: $namesFlyOrthZFIN{$zdbGeneId}\n";
       print ORTHNAMEREPORT "fly orthology name at NCBI: $namesFlyOrthNCBI{$zdbGeneId}\n\n";
       print ORTHNAMEUPDATE "$zdbGeneId|Fruit fly|$namesFlyOrthZFIN{$zdbGeneId}|$namesFlyOrthNCBI{$zdbGeneId}|\n";
   }      
}

print "\n\ntotal = $total  ctDiffrentZFgeneNames = $ctDiffrentZFgeneNames  ctUpdatedOrthNames = $ctUpdatedOrthNames\n\n";

print "\n\nctAll3NamesIdentical = $ctAll3NamesIdentical  ctIdenticalToHumanAndNoMouse = $ctIdenticalToHumanAndNoMouse    ctIdenticalToMouseAndNoHuman = $ctIdenticalToMouseAndNoHuman    ctNoneHumanOrMouse = $ctNoneHumanOrMouse   \n\n";

print LOG "\n\nctAll3NamesIdentical = $ctAll3NamesIdentical      ctIdenticalToHumanAndNoMouse = $ctIdenticalToHumanAndNoMouse    ctIdenticalToMouseAndNoHuman = $ctIdenticalToMouseAndNoHuman    ctNoneHumanOrMouse = $ctNoneHumanOrMouse   \n\n";

print "total number of Zebrafish genes with different names from their human and/or mouse orthology: $ctDiffrentZFgeneNames \n";

print LOG "total number of Zebrafish genes with different names from their human and/or mouse orthology: $ctDiffrentZFgeneNames \n";

print "total number of orthology names different from NCBI: $ctUpdatedOrthNames \n";

print LOG "total number of orthology names different from NCBI: $ctUpdatedOrthNames \n";

close(ZFNAMEREPORT);

close(ORTHNAMEREPORT);
close(ORTHNAMEUPDATE);
close(PREVIOUSLYREPORTED);

close(LOG);

##this is taken care of in the bulk name/chromosome/position/accessionnumber update.
##$cmd = "$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> updateOrthologyNames.sql >updateOrthologyNameSQLlog1 2> updateOrthologyNameSQLlog2";
##&doSystemCommand($cmd);
#&doSystemCommand("/bin/cat updateOrthologyNameSQLlog2 >> updateOrthologyNameSQLlog1");
#$subject = "Auto from $dbname: " . "NCBIorthology.pl :: updateOrthologyNameSQLlog";
#ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","updateOrthologyNameSQLlog1");


### These should all just be artifacts in jenkins job.  Commenting out for now.
#$subject = "Auto from $dbname: " . "update.pl :: updateOrthologyNamePerlLog";
#ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","logOrthologyUpdateName");

#$subject = "Auto from $dbname: " . "$ctUpdatedOrthNames ortholog names have been updated by the script";
#ZFINPerlModules->sendMailWithAttachedReport("<!--|VALIDATION_EMAIL_GENE|-->","$subject","orthNamesUpdatedReport") if $ctUpdatedOrthNames > 0;

#$subject = "Auto from $dbname: " . "$ctDiffrentZFgeneNames zebrafish gene names to be considered for updating";
#ZFINPerlModules->sendMailWithAttachedReport("<!--|VALIDATION_EMAIL_GENE|-->","$subject","inconsistentZebrafishGeneNamesReport") if $ctDiffrentZFgeneNames > 0;

#$subject = "Auto from $dbname: " . "$ctDiffrentZFgeneNames zebrafish gene names to be considered for renaming";
#ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","inconsistentZebrafishGeneNamesReport");

system("scp <!--|ROOT_PATH|-->/server_apps/data_transfer/ORTHO/alreadyExamined /research/zarchive/load_files/Orthology/");

}

sub doSystemCommand {

  $systemCommand = $_[0];

  print LOG "$0: Executing [$systemCommand] \n";
    
  $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) { 
     $subjectLine = "Auto from $dbname: " . "reportOrthoNameChanges.pl :: failed at: $systemCommand . $! ";
     print LOG "\nFailed to execute system command, $systemCommand\nExit.\n\n";
     
     &reportErrAndExit($subjectLine);
  }
}


sub reportErrAndExit {
  $subjectError = $_[0];
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subjectError","logOrthologyUpdateName");
  close LOG;
  exit;
}

























































