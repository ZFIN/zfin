#!/private/bin/perl

# NCBIorthology.pl
# First, the script downloads the following NCBI data files:
# ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz
# ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Mus_musculus.gene_info.gz
# ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Invertebrates/Drosophila_melanogaster.gene_info.gz
# The script then parses data in the above files and store related data in a series of data structures and then compare data stored at ZFIN to decide what
# should be updated. Updating the orthology name would be done automatically by calling updateOrthologyNames.sql. Report of zebrafish names that may be updated 
# is generated and emailed to Ken, who will update the zebrafish names in place on the report. Then that report will be used by another script, updateZebrafishGeneNames.pl.

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

## set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";

system("/bin/rm -f logOthologyUpdateName");
system("/bin/rm -f orthNamesUpdatedReport");
system("/bin/rm -f updateGeneNamesReport");
system("/bin/rm -f inconsistentZebrafishGeneNamesReport");
system("/bin/rm -f ncbiIdsNotFoundReport");
system("/bin/rm -f orthNamesUpdateList.unl");
system("/bin/rm -f updateOrthologyNameSQLlog1");
system("/bin/rm -f updateOrthologyNameSQLlog2");


system("/bin/rm -f Homo_sapiens.gene_info");
system("/bin/rm -f Mus_musculus.gene_info");
system("/bin/rm -f Drosophila_melanogaster.gene_info");

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz");
&doSystemCommand("/local/bin/gunzip Homo_sapiens.gene_info.gz");

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Mus_musculus.gene_info.gz");
&doSystemCommand("/local/bin/gunzip Mus_musculus.gene_info.gz");

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Invertebrates/Drosophila_melanogaster.gene_info.gz");
&doSystemCommand("/local/bin/gunzip Drosophila_melanogaster.gene_info.gz");

&doSystemCommand("/bin/cat Mus_musculus.gene_info >> Homo_sapiens.gene_info");

&doSystemCommand("/bin/cat Drosophila_melanogaster.gene_info >> Homo_sapiens.gene_info");

open LOG, '>', "logOthologyUpdateName" or die "can not open logOthologyUpdateName: $! \n";

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


open (NCBI, "Homo_sapiens.gene_info") ||  die "Cannot open Homo_sapiens.gene_info : $!\n";

$ctLines = $ctHumanGenes = $ctMouseGenes = $ctFlyGenes = 0;

while (<NCBI>) {
  chomp;
  
  @fieldsNCBI = split("\t");

  $taxId = $fieldsNCBI[0];
    
  ## excluding species other than human, mouse, fly and the documentation lines
  next if $taxId ne "9606" and $taxId ne "10090" and $taxId ne "7227";
  
  $ctLines++;
  
  ## human gene info
  if ($taxId eq "9606") {
    $humanNCBIgeneID = $fieldsNCBI[1];
    $NCBIidsAndNamesHuman{$humanNCBIgeneID} = $fieldsNCBI[11] if ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[11]);
    $ctHumanGenes++;
  }
  
  ## mouse gene info
  if ($taxId eq "10090") {
    $mouseNCBIgeneID = $fieldsNCBI[1];
    $NCBIidsAndNamesMouse{$mouseNCBIgeneID} = $fieldsNCBI[11] if ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[11]);
    $ctMouseGenes++;
  }

  ## fly gene info
  if ($taxId eq "7227") {
    $flyNCBIgeneID = $fieldsNCBI[1];
    $NCBIidsAndNamesFly{$flyNCBIgeneID} = $fieldsNCBI[11] if ZFINPerlModules->stringStartsWithLetterOrNumber($fieldsNCBI[11]);
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

$sqlGetZFgeneNamesByHumanAndMouseOrth =
'select distinct organism,ortho_name,dblink_acc_num,c_gene_id,mrkr_abbrev,mrkr_name ' . 
  'from db_link,orthologue,marker ' . 
 'where dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-27","ZDB-FDBCONT-040412-28","ZDB-FDBCONT-040412-23") ' .
   'and zdb_id = dblink_linked_recid ' .
   'and c_gene_id = mrkr_zdb_id ' .
   'and mrkr_type = "GENE" ' .
   'order by mrkr_abbrev;';

$cur = $dbh->prepare($sqlGetZFgeneNamesByHumanAndMouseOrth);
$cur->execute();
$cur->bind_columns(\$organism,\$orthoName,\$ncbiID,\$zdbGeneId,\$zdbGeneAbbrev,\$zdbGeneName);

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
# value: human orthology name (stored in orthologue)

%namesHumanOrthZFIN = ();

# %namesHumanOrthNCBI - for human orthology names from NCBI
# key: ZF gene ZDB ID
# value: human orthology name from NCBI

%namesHumanOrthNCBI = ();

# %namesMouseOrthZFIN - for mouse orthology names stored at ZFIN
# key: ZF gene ZDB ID
# value: mouse orthology name (stored in orthologue)

%namesMouseOrthZFIN = ();

# %namesMouseOrthNCBI - for human orthology names from NCBI
# key: ZF gene ZDB ID
# value: human orthology name from NCBI

%namesMouseOrthNCBI = ();

# %namesFlyOrthZFIN - for fly orthology names stored at ZFIN
# key: ZF gene ZDB ID
# value: fly orthology name (stored in orthologue)

%namesFlyOrthZFIN = ();

# %namesFlyOrthNCBI - for fly orthology names from NCBI
# key: ZF gene ZDB ID
# value: fly orthology name from NCBI

%namesFlyOrthNCBI = ();

open (NOTFOUND, ">ncbiIdsNotFoundReport") || die "Cannot open ncbiIdsNotFoundReport : $!\n";

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
       if (exists($NCBIidsAndNamesHuman{$ncbiID})) {
           $namesHumanOrthNCBI{$zdbGeneId} = $NCBIidsAndNamesHuman{$ncbiID};
       } else {
           print NOTFOUND "$zdbGeneId\t$zdbGeneAbbrev\t$ncbiID\t$organism\n";
           $ctNotFound++;
       }
   } elsif ($organism eq "Mouse") {
       $namesMouseOrthZFIN{$zdbGeneId} = $orthoName;
       if (exists($NCBIidsAndNamesMouse{$ncbiID})) {
           $namesMouseOrthNCBI{$zdbGeneId} = $NCBIidsAndNamesMouse{$ncbiID};
       } else {
           print NOTFOUND "$zdbGeneId\t$zdbGeneAbbrev\t$ncbiID\t$organism\n";
           $ctNotFound++;
       }
   } elsif ($organism eq "Fruit fly") {
       $namesFlyOrthZFIN{$zdbGeneId} = $orthoName;
       if (exists($NCBIidsAndNamesFly{$ncbiID})) {
           $namesFlyOrthNCBI{$zdbGeneId} = $NCBIidsAndNamesFly{$ncbiID};
       } else {
           print NOTFOUND "$zdbGeneId\t$zdbGeneAbbrev\t$ncbiID\t$organism\n";
           $ctNotFound++;
       }
   } else {
       print LOG "\nError: $organism   $ncbiID   $zdbGeneId   $zdbGeneAbbrev    $zdbGeneName\n"; 
   }
print "\n$organism,\t$namesZFgene{$zdbGeneId},\t$zdbGeneId,\t$zdbGeneAbbrev,\t$zdbGeneName\n" if $ct < 5;
   
}

print "\n\nct = $ct\n\n";



$cur->finish(); 

$dbh->disconnect();


%zebrafishGeneNamesUpdated = ();
$ctAll3NamesIdentical = $ctIdenticalToHumanAndNoMouse = $ctIdenticalToMouseAndNoHuman = $ctNoneHumanOrMouse = 0;

$total = 0;
$ctDiffrentZFgeneNames = $ctUpdatedOrthNames = 0;

open (ORTHNAMEREPORT, ">orthNamesUpdatedReport") || die "Cannot open orthNamesUpdatedReport : $!\n";
open (ORTHNAMEUPDATE, ">orthNamesUpdateList.unl") || die "Cannot open orthNamesUpdateList.unl : $!\n";
open (ZFNAMEREPORT, ">inconsistentZebrafishGeneNamesReport") ||  die "Cannot open inconsistentZebrafishGeneNamesReport : $!\n";

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
   } else {
       next;
   }
     
   if (exists($namesMouseOrthNCBI{$zdbGeneId})) { 
       $mouseGeneName = $namesMouseOrthNCBI{$zdbGeneId} if exists($namesMouseOrthNCBI{$zdbGeneId});
       $mouseGeneNameNoComma = $mouseGeneName;
       $mouseGeneNameNoComma =~ s/,//g;
       $mouseGeneNameNoCommaLowerCase = lc($mouseGeneNameNoComma);
   } else {
       next;
   }
   
   if (exists($namesHumanOrthNCBI{$zdbGeneId}) && !($geneNameZFnoCommaLowerCase =~ m/\Q$humanGeneNameNoCommaLowerCase/ || $humanGeneNameNoCommaLowerCase =~ m/\Q$geneNameZFChoppedNoCommaLowerCase/)) {
       if (exists($namesMouseOrthNCBI{$zdbGeneId})) {
           if (!($geneNameZFnoCommaLowerCase =~ m/\Q$mouseGeneNameNoCommaLowerCase/ || $mouseGeneNameNoCommaLowerCase =~ m/\Q$geneNameZFChoppedNoCommaLowerCase/)) {
              $zebrafishGeneNamesUpdated{$zdbGeneId} = $humanGeneName;
              print ZFNAMEREPORT "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
              print ZFNAMEREPORT "gene name (z): $geneNameZF\n";
              print ZFNAMEREPORT "gene name (h): $humanGeneName\n";    
              print ZFNAMEREPORT "gene name (m): $mouseGeneName\n\n";
              $ctDiffrentZFgeneNames++;
           } else {
              # gene name not needed to be updated, since mouse gene name is the same, although different from human gene name
           }
           
       } else {
           $zebrafishGeneNamesUpdated{$zdbGeneId} = $humanGeneName;
           print ZFNAMEREPORT "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
           print ZFNAMEREPORT "gene name (z): $geneNameZF\n";
           print ZFNAMEREPORT "gene name (h): $humanGeneName\n";    
           print ZFNAMEREPORT "gene name (m): no mouse orthology; or the mouse NCBI gene Id not found at NCBI\n\n";
           $ctDiffrentZFgeneNames++;
       }
       
   } else {
       if (exists($namesHumanOrthNCBI{$zdbGeneId})) {
           if (exists($namesMouseOrthNCBI{$zdbGeneId})) {
               if (!($geneNameZFnoCommaLowerCase =~ m/\Q$mouseGeneNameNoCommaLowerCase/ || $mouseGeneNameNoCommaLowerCase =~ m/\Q$geneNameZFChoppedNoCommaLowerCase/)) {
                  # gene name not needed to be updated, since human gene name is the same, although different from mouse gene name
               } else {
                  $ctAll3NamesIdentical++;
               }
           
           } else {
               $ctIdenticalToHumanAndNoMouse++;
           }
       } else {
           if (exists($namesMouseOrthNCBI{$zdbGeneId})) {
              if (!($geneNameZFnoCommaLowerCase =~ m/\Q$mouseGeneNameNoCommaLowerCase/ || $mouseGeneNameNoCommaLowerCase =~ m/\Q$geneNameZFChoppedNoCommaLowerCase/)) {
                  $zebrafishGeneNamesUpdated{$zdbGeneId} = $mouseGeneName;
                  print ZFNAMEREPORT "$zdbGeneId     $symbolsZFgene{$zdbGeneId}\n";
                  print ZFNAMEREPORT "gene name (z): $geneNameZF\n";
                  print ZFNAMEREPORT "gene name (h): no human orthology; or the human NCBI gene Id not found at NCBI\n";   
                  print ZFNAMEREPORT "gene name (m): $mouseGeneName\n\n";
                  $ctDiffrentZFgeneNames++;
               } else {
                  $ctIdenticalToMouseAndNoHuman++;
               }           
           } else {
               $ctNoneHumanOrMouse++;
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

close(LOG);

$cmd = "$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> updateOrthologyNames.sql >updateOrthologyNameSQLlog1 2> updateOrthologyNameSQLlog2";
&doSystemCommand($cmd);

&doSystemCommand("/bin/cat updateOrthologyNameSQLlog2 >> updateOrthologyNameSQLlog1");

$subject = "Auto from $dbname: " . "NCBIorthology.pl :: updateOrthologyNameSQLlog";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","updateOrthologyNameSQLlog1");

$subject = "Auto from $dbname: " . "NCBIorthology.pl :: updateOrthologyNamePerlLog";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","logOthologyUpdateName");

$subject = "Auto from $dbname: " . "$ctUpdatedOrthNames orthologue names have been updated by the script";
ZFINPerlModules->sendMailWithAttachedReport("<!--|VALIDATION_EMAIL_GENE|-->","$subject","orthNamesUpdatedReport") if $ctUpdatedOrthNames > 0;

$subject = "Auto from $dbname: " . "$ctDiffrentZFgeneNames zebrafish gene names to be considered for updating";
ZFINPerlModules->sendMailWithAttachedReport("<!--|VALIDATION_EMAIL_GENE|-->","$subject","inconsistentZebrafishGeneNamesReport") if $ctDiffrentZFgeneNames > 0;

exit;


sub doSystemCommand {

  $systemCommand = $_[0];

  print LOG "$0: Executing [$systemCommand] \n";
    
  $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) { 
     $subjectLine = "Auto from $dbname: " . "updateZFgeneNames.pl :: failed at: $systemCommand . $! ";
     print LOG "\nFailed to execute system command, $systemCommand\nExit.\n\n";
     
     &reportErrAndExit($subjectLine);
  }
}


sub reportErrAndExit {
  $subjectError = $_[0];
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subjectError","logOthologyUpdateName");
  close LOG;
  exit;
}

























































