#!/private/bin/perl

# FILE: addNewClonesJSmith.pl
# one time use script
# add new clones associated with Jeff Smith SNPs
# 

### set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"DATABASE"}="<!--|DB_NAME|-->";

open (INP1, "newClonesJSmith.txt") || die "Cannot open newClonesJSmith.txt : $!\n";
@lines=<INP1>;
close(INP1);

open (OUTPUT1,  ">newClonesJSmith.unl") || die "Can't open: newClonesJSmith.unl $!\n";
open (OUTPUT2,  ">notIntoZFINClonesJSmith.txt") || die "Can't open: notIntoZFINClonesJSmith.txt $!\n";

$ct1 = $ct2 = $ctNew = 0;
foreach $line (@lines) {
  $line =~ s/>\n+//g; 
  if ($line) {
    if ($line =~ m/^\d+:\s+(\w+)$/) {
      $ct1++;
      $acc = $1; 
    }

    if ($line =~ m/linkage group (\d+)(,|\s+)/) {
      $lg = $1; 
    } else {
      $lg = unknown;
    }
    
    if ($line =~ m/clone (\w+\-\w+)(,|\s+)/) {
      $ct2++;
      $clone = $1; 
      
      $partOfCloneName = substr($clone,0,5);
      $intoZFIN = 1;
      
      if ($partOfCloneName eq 'BUSM1') {
          $vector = 'pCYPAC-6';
          $probeID = 'ZDB-PROBELIB-020423-2';
          $type = 'PAC';
      } elsif ($partOfCloneName eq 'CH211') {
        $vector = 'pTARBAC2.1';
        $probeID = 'ZDB-PROBELIB-020423-3';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'CH73-') {
        $vector = 'pTARBAC2.1';
        $probeID = 'ZDB-PROBELIB-050214-1';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'DKEY-') {
        $vector = 'pIndigoBAC-536';
        $probeID = 'ZDB-PROBELIB-020423-4';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'DKEYP') {
        $vector = 'pIndigoBAC-536';
        $probeID = 'ZDB-PROBELIB-020423-5';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'RP71-') {
        $vector = 'pTARBAC2';
        $probeID = 'ZDB-PROBELIB-020423-1';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'XX-BA') {
        $vector = 'pBeloBAC11';
        $probeID = 'ZDB-PROBELIB-040512-1';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'XX-PA') {
        $vector = 'pCYPAC-6';
        $probeID = 'ZDB-PROBELIB-040512-1';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'XX-DZ') {
        $vector = 'pCYPAC-6';
        $probeID = 'ZDB-PROBELIB-040512-1';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'XX-BY') {
        $vector = 'pBeloBAC11';
        $probeID = 'ZDB-PROBELIB-040512-1';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'XX-ZF') {
        $vector = 'pFOS-1';
        $probeID = 'none';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'ZFOS-') {
        $vector = 'pFOS-1';
        $probeID = 'none';
        $type = 'BAC';
      } elsif ($partOfCloneName eq 'CH1073') {
        $vector = 'pCC1FOS-CHA_PmII';
        $probeID = 'ZDB-PROBELIB-070723-1';
        $type = 'FOSMID';
      } else {
        $intoZFIN = 0;
      } 
      
      if ($intoZFIN == 0) {
        print OUTPUT2 "$clone\t$acc\n";
      } else {
        print OUTPUT1 "$clone|$acc|$vector|$probeID|$type|$lg\n";
        $ctNew++;
      }
    }   
  }
}
undef @lines;

close(OUTPUT1);
close(OUTPUT2);
print "ct1: $ct1\tct2: $ct2\tctNew: $ctNew\n";

if ($ctNew > 0) {
  system( "$ENV{'INFORMIXDIR'}/bin/dbaccess -a $ENV{'DATABASE'} loadClonesJSmith.sql" );
}
  
exit;

