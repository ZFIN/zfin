#!/private/bin/perl

# FILE: preprocess_zfishbook.pl


use MIME::Lite;
use DBI;


#------------------ Send Checking Result ----------------
# No parameter
#

sub sendReport {
		
  my $SUBJECT="Auto: zfishbook sanity checking result";
  my $MAILTO="xshao\@cs.uoregon.edu";
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

  my $SUBJECT="Auto: zfishbook pre_load_input";
  my $MAILTO="xshao\@cs.uoregon.edu";
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


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/zfishbook/";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";


## for gene trap construct Ids
##%gtCnstructIds = ();

##$rp2 = "RP2";
##$rp14point5 = "R14.5";
##$gtCnstructIds[$rp2] = "ZDB-GTCONSTRCT-111117-2";
##$gtCnstructIds[$rp14point5] = "ZDB-GTCONSTRCT-100624-1";

##print "gtCnstructIds[$rp2]:  $gtCnstructIds[$rp2]\n";
##print "gtCnstructIds[$rp14point5]:  $gtCnstructIds[$rp14point5]\n";


open (ZFISHBOOKDATA, "zfishbookData.txt") || die "Cannot open zfishbookData.txt : $!\n";
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
  next if $ct < 3;
  if ($line) {

    chop($line);
    undef (@fields);
    @fields = split(/\|/, $line); 

    $prev = $fields[0];
    $prev =~ s/^\s+//; 
    $prev =~ s/\s+$//;
    if (exists $prviousNames{$prev}) {
       $numErr++;
       $numOfCrucialErrors++;
       print REPORT "\n$ct :: $prev ::  Redundant: $prev\n";
    }  else {
       $prviousNames{$prev} = 1;
    }
    
    $lineNum = substr($prev, 3);
    
    
###    $allele = $fields[1];
    
    $allele = "mn" . $lineNum . "Gt";
    
    $allele =~ s/^\s+//; 
    $allele =~ s/\s+$//;
    
    $allele =~ s/GT$/Gt/;
    
    if (exists $alleles{$allele}) {
       $numErr++;
       $numOfCrucialErrors++;
       print REPORT "\n$ct :: $prev :: Redundant: $allele\n";
    }  else {
       $alleles{$allele} = 1;
    }    

    if (exists $allelePrevs{$allele}) {
       $numErr++;
       $numOfCrucialErrors++;
         print REPORT "\n$ct :: $prev :: Redundant allele-prev : $allele $prev\n";
    }  else {
         $allelePrevs{$allele} = $prev;
    }    

    $vector = $fields[2];
    $vector =~ s/^\s+//; 
    $vector =~ s/\s+$//; 

    $cnstrtId = "";    
##    if (!$vector || $vector eq "" || !exists($gtCnstructIds[$vector])) {

    if (!$vector || $vector eq "" || ($vector ne "RP2" && $vector ne "R14.5" && $vector ne "R15")) {
        print REPORT "\n$ct :: $prev :: no vector or construct found\n";
        $numErr++;
        $numOfCrucialErrors++;
    } else {
        if ($vector eq "RP2") {
           $cnstrtId = "ZDB-GTCONSTRCT-111117-2"; 
        } elsif ($vector eq "R14.5") {
           $cnstrtId = "ZDB-GTCONSTRCT-100624-1";
        } else {
           $cnstrtId = "ZDB-GTCONSTRCT-100121-2";
        }
        
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
         $numErr++;
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
    if ($allele =~ m/mn/)  {
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
   ###   print "$ct|$prev|$lineNum|$allele\n\n";
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
        
   ####     print "$ct|$prev|$lineNum|$allele ---  featureId = $featureId   -- ZFINfeatureIdFromAlias = $ZFINfeatureIdFromAlias\n\n";
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
   ###   print "$ct|$prev|$lineNum|$allele\n\n";
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
        
 ####       print "$ct|$prev|$lineNum|$allele ---  featureId = $featureId   -- ZFINfeatureIdFromAliasWithoutGt = $ZFINfeatureIdFromAliasWithoutGt\n\n";
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
   ###   print "$ct|$prev|$lineNum|$allele\n\n";
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
    
    print ZFISHBOOK "$ct|$prev|$lineNum|$allele|$geneId|$featNamePart|$featureId|$cnstrtId|\n";


  }
}
undef @lines;

$dbh->disconnect(); 

print  "\nnumber of non-crucial error of pre_load checking:  $numErr\n";

print REPORT "\nnumber of non-crucial errors:  $numErr\n\n";
print REPORT "\nnumber of crucial errors:  $numOfCrucialErrors\n\n";

print REPORT "\nThe loading is not done due to crucial error(s).\n\n" if $numOfCrucialErrors > 0;

close (REPORT);

sendReport();

exit;


