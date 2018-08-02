#!/private/bin/perl

# FILE: preprocess_zfishbook.pl


use MIME::Lite;
use DBI;


#------------------ Send Checking Result ----------------
#
#

sub sendReport($) {
		
  my $SUBJECT="Auto from ".$_[0]." : zfishbook sanity checking result";
  my $MAILTO="xshao\@zfin.org";
  my $TXTFILE="./report";
 
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

  my $SUBJECT="Auto from ".$_[0]." : zfishbook pre_load_input";
  my $MAILTO="xshao\@zfin.org";
  my $TXTFILE="./pre_load_input.txt";
 
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

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/zfishbook/";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to postgres database: $DBI::errstr\n";


## for gene trap construct Ids
%gtCnstructIds = ();

$gtCnstructIds{"RP2"} = "ZDB-GTCONSTRCT-111117-2";
$gtCnstructIds{"RP2.1"} = "ZDB-GTCONSTRCT-111117-2";
$gtCnstructIds{"RP8"} = "ZDB-GTCONSTRCT-121023-1";
$gtCnstructIds{"RP8.1"} = "ZDB-GTCONSTRCT-121023-1";
$gtCnstructIds{"RP8 pr. 18"} = "ZDB-GTCONSTRCT-121023-1";
$gtCnstructIds{"R14.5"} = "ZDB-GTCONSTRCT-100624-1";
$gtCnstructIds{"RP15"} = "ZDB-GTCONSTRCT-100121-2";
$gtCnstructIds{"RP1"} = "ZDB-GTCONSTRCT-130315-3";
$gtCnstructIds{"RP4"} = "ZDB-GTCONSTRCT-130315-2";
$gtCnstructIds{"RP7"} = "ZDB-GTCONSTRCT-130315-1";


open (ZFISHBOOKDATA, "/research/zarchive/load_files/Zfishbook/Nov_2016/zfishbookData.txt") || die "Cannot open zfishbookData.txt : $!\n";
@lines=<ZFISHBOOKDATA>;
close(ZFISHBOOKDATA);
%prviousNames = ();
%alleles = ();
%allelePrevs = (); 
$ct = 0;
$numErr = 0;
$numOfCrucialErrors = 0;
open (REPORT, ">report") || die "Cannot open report : $!\n";
open (ZFISHBOOK, ">pre_load_input.txt") || die "Cannot open pre_load_input.txt : $!\n";


foreach $line (@lines) {
  $ct++;
  next if $ct < 2;
  if ($line) {

    chop($line);
    undef (@fields);
    @fields = split(/\|/, $line); 

    $prev = $fields[0];
    $prev =~ s/^\s+//; 
    $prev =~ s/\s+$//;
    if (exists $prviousNames{$prev}) {
       $numOfCrucialErrors++;
       print REPORT "\n$ct :: $prev ::  Redundant: $prev\n";
    }  else {
       $prviousNames{$prev} = 1;
    }
    
    $lineNum = substr($prev, 3);
    
    $alleleOnZfishbookFile = $fields[1];
    if ($alleleOnZfishbookFile =~ m/xu/) {
        $lab = "ZDB-LAB-040114-1";
        $allele = "xu" . $lineNum . "Gt";
        $prefix = "xu";
    } else {
        $lab = "ZDB-LAB-970908-70";
        $allele = "mn" . $lineNum . "Gt";
        $prefix = "mn";
    }
    
    $allele =~ s/^\s+//; 
    $allele =~ s/\s+$//;
    
    $allele =~ s/GT$/Gt/;
    
    if (exists $alleles{$allele}) {
       $numOfCrucialErrors++;
       print REPORT "\n$ct :: $prev :: Redundant: $allele\n";
    }  else {
       $alleles{$allele} = 1;
    }    

    if (exists $allelePrevs{$allele}) {
       $numOfCrucialErrors++;
         print REPORT "\n$ct :: $prev :: Redundant allele-prev : $allele $prev\n";
    }  else {
         $allelePrevs{$allele} = $prev;
    }    

    $vector = $fields[2];
    $vector =~ s/^\s+//; 
    $vector =~ s/\s+$//; 

    $cnstrtId = "";    

    if (!$vector || !exists($gtCnstructIds{$vector})) {
        print REPORT "\n$ct :: $prev :: no vector or construct found\n";
        $numOfCrucialErrors++;
    } else {
        $cnstrtId = $gtCnstructIds{$vector};
    }
         
    $geneId = $fields[3];
    $geneId =~ s/^\s+//; 
    $geneId =~ s/\s+$//; 

    $geneAbbr = $fields[4];
    $geneAbbr =~ s/^\s+//; 
    $geneAbbr =~ s/\s+$//; 

    ### check if the zdbIds of the genes are valid or not    
    ### check if the gene zdbIds and gene abbrevs match or not        
    if ($geneId =~ m/ZDB-GENE-/)  {
      $cur = $dbh->prepare('select mrkr_abbrev from marker where mrkr_zdb_id = ?;');
      $cur->execute($geneId);
      my ($ZFINgeneAbbrev);
      $cur->bind_columns(\$ZFINgeneAbbrev);
      while ($cur->fetch()) {
         $ZDBgeneIDgeneAbbrevs{$geneId} = $ZFINgeneAbbrev;
      }
   
      if ($cur->rows == 0) {
         $numOfCrucialErrors++;
         print REPORT "\n$ct :: $prev :: $geneId is not a valid ZDB Id\n"; 
      }

      if ($ZFINgeneAbbrev ne $geneAbbr) {
         $numErr++;
         print REPORT "\n$ct :: $prev :: $geneId should have \"$ZFINgeneAbbrev\" rather than \"$geneAbbr\" as gene abbrev\n"; 
      }
      
      $cur->finish(); 
    } 
     

    $geneName = $fields[5];
    $geneName =~ s/^\s+//; 
    $geneName =~ s/\s+$//;   
    
    
    ### check if the gene zdbIds and gene name match or not  
    ### this kind error is not crucial
    if ($geneId =~ m/ZDB-GENE-/)  {
      $cur = $dbh->prepare('select mrkr_name from marker where mrkr_zdb_id = ?;');
      $cur->execute($geneId);
      my ($ZFINgeneName);
      $cur->bind_columns(\$ZFINgeneName);
      while ($cur->fetch()) {
         $ZDBgeneIDgeneNames{$geneId} = $ZFINgeneName;
      }
   
      if ($ZFINgeneName ne $geneName) {
         $numErr++;
         print REPORT "\n$ct :: $prev :: $geneId should have \"$ZFINgeneName\" rather than \"$geneName\" as gene name\n"; 
      }
      
      $cur->finish(); 
    }

    $alleleWithoutGt = $allele;
    $alleleWithoutGt =~ s/Gt$//;

    $featNamePart =  $allele;   
    if (!$geneId) {
      $featNamePart =~ s/Gt$//;
    }

    ### look up ZFIN feature zdbIds        
    if ($allele =~ m/mn/ || $allele =~ m/xu/)  {
      $cur = $dbh->prepare('select feature_zdb_id from feature where feature_name = ?;');
      $cur->execute($allele);
      my ($ZFINfeatureId);
      $cur->bind_columns(\$ZFINfeatureId);
      while ($cur->fetch()) {
         $ZDBfeatureIds{$allele} = $ZFINfeatureId;
      }
   
      if ($cur->rows == 0) {
         $featureId = "";
      } else {
         $featureId = $ZFINfeatureId;
      }
      
      $cur->finish(); 
      
      ### look up data_alias table
      if ($featureId eq "") {
        $cur = $dbh->prepare('select dalias_data_zdb_id from data_alias where dalias_data_zdb_id like "ZDB-ALT-%" and dalias_alias = ?;');
        $cur->execute($allele);
        my ($ZFINfeatureIdFromAlias);
        $cur->bind_columns(\$ZFINfeatureIdFromAlias);
        while ($cur->fetch()) {
           $ZDBfeatureIds{$allele} = $ZFINfeatureIdFromAlias;
        }
   
        if ($cur->rows == 0) {
           $featureId = "";
        } else {
           $featureId = $ZFINfeatureIdFromAlias;
        }
      
        $cur->finish();
      }
        

      ### look up feature table with mnXXXX without trailing 'Gt'
      if ($featureId eq "") {
        $cur = $dbh->prepare('select feature_zdb_id from feature where feature_name = ?;');
        $cur->execute($alleleWithoutGt);
        my ($ZFINfeatureIdNoGt);
        $cur->bind_columns(\$ZFINfeatureIdNoGt);
        while ($cur->fetch()) {
           $ZDBfeatureIds{$allele} = $ZFINfeatureIdNoGt;
        }
   
        if ($cur->rows == 0) {
           $featureId = "";
        } else {
           $featureId = $ZFINfeatureIdNoGt;
        }
      
        $cur->finish(); 
      }
    

      ### look up data_alias table with mnXXXX without trailing 'Gt'
      if ($featureId eq "") {
        $cur = $dbh->prepare('select dalias_data_zdb_id from data_alias where dalias_data_zdb_id like "ZDB-ALT-%" and dalias_alias = ?;');
        $cur->execute($alleleWithoutGt);
        my ($ZFINfeatureIdFromAliasWithoutGt);
        $cur->bind_columns(\$ZFINfeatureIdFromAliasWithoutGt);
        while ($cur->fetch()) {
           $ZDBfeatureIds{$allele} = $ZFINfeatureIdFromAliasWithoutGt;
        }
   
        if ($cur->rows == 0) {
           $featureId = "";
        } else {
           $featureId = $ZFINfeatureIdFromAliasWithoutGt;
        }
        $cur->finish();
      }

      ### look up feature table with GBTXXX
      if ($featureId eq "") {
        $cur = $dbh->prepare('select feature_zdb_id from feature where feature_name = ?;');
        $cur->execute($prev);
        my ($ZFINfeatureIdGBT);
        $cur->bind_columns(\$ZFINfeatureIdGBT);
        while ($cur->fetch()) {
           $ZDBfeatureIds{$allele} = $ZFINfeatureIdGBT;
        }
   
        if ($cur->rows == 0) {
           $featureId = "";
        } else {
           $featureId = $ZFINfeatureIdGBT;
        }
      
        $cur->finish(); 
      }



      ### look up data_alias table with with GBTXXX
      if ($featureId eq "") {
        $cur = $dbh->prepare('select dalias_data_zdb_id from data_alias where dalias_data_zdb_id like "ZDB-ALT-%" and dalias_alias = ?;');
        $cur->execute($prev);
        my ($ZFINfeatureIdFromAliasGBT);
        $cur->bind_columns(\$ZFINfeatureIdFromAliasGBT);
        while ($cur->fetch()) {
           $ZDBfeatureIds{$allele} = $ZFINfeatureIdFromAliasGBT;
        }
   
        if ($cur->rows == 0) {
           $featureId = "";
        } else {
           $featureId = $ZFINfeatureIdFromAliasGBT;
        }
      
        $cur->finish();  
      }

    } 
    
    print ZFISHBOOK "$ct|$prev|$lineNum|$allele|$geneId|$featNamePart|$featureId|$cnstrtId|$lab|$prefix\n";


  }
}
undef @lines;

$dbh->disconnect(); 

print "\nnumber of crucial errors:  $numOfCrucialErrors\n\n";
print  "\nnumber of non-crucial error of pre_load checking:  $numErr\n";

print REPORT "\nnumber of non-crucial errors:  $numErr\n\n";
print REPORT "\nnumber of crucial errors:  $numOfCrucialErrors\n\n";

print REPORT "\nThe loading is not done due to crucial error(s).\n\n" if $numOfCrucialErrors > 0;

close (ZFISHBOOK);
close (REPORT);

sendReport("$dbname");

exit;


