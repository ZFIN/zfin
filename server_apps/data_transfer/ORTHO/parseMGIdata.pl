#!/private/bin/perl

########################################################################################
#  File: parseMGIdata.pl
#  This script parses the MGI data file to extract human and mouse chromosome info and
#  then output the parsed result to "chromInfo.unl" for updating orthologue table
########################################################################################

open (INP,  "MGI.data") || die "Can't open MGI.data : $!\n";
@lines=<INP>;
close INP;

# the following is for marking the header text to be excluded from the MGI data file
$ct = 0;
$ct1 = $ct2 = 50;
foreach $line (@lines) {
  $ct++;
  if ($ct < $ct1) {
    $ct1 = $ct if ($line =~ m/-----/g);
  }
  if ($ct > $ct2) {
    $ct2 = $ct if ($line =~ m/rows affected/g);
  }
}

$ct = 0; %outStrs = ();
foreach $line (@lines) {
  $ct++;
  if ($ct > $ct1 && $ct < $ct2 - 1) {
    $flagPorQ = $flagSemicolon = $flagAndOr = 0;
    @fields = split(/\s+/, $line);
    $humanChrInfo = $fields[0];
    $len = length($humanChrInfo);
    if ($len <= 2) {
      $humanChr = $humanChrInfo;
      $humanLoc = "UN";
    } else {
      $humanChr = $humanLoc = "";
      @chars = split(//, $humanChrInfo);
      for $char (@chars) {
        $flagSemicolon = 1 if ($char eq ';');
        $flagPorQ = 1 if ($char eq 'p' || $char eq 'q' || $char eq 'c');
        if ($flagPorQ == 1) {
          $humanLoc = $humanLoc . $char;
        } else {
          $humanChr = $humanChr . $char;
        }
      }
    }
    
    $flagAndOr = 1 if ($fields[1] =~ m/and/i || $fields[1] =~ m/or/i);
    
    if ($flagAndOr == 1) {
      $humanLoc = $humanLoc . " " . $fields[1] . " " . $fields[2];
      $humanSym = $fields[4];
      $msChr = $fields[6];
      $cm = $fields[7];
      $msSym = $fields[9];
    } elsif ($flagSemicolon == 1) {
      $humanLoc = $humanLoc . " " . $fields[1];
      $humanSym = $fields[3];
      $msChr = $fields[5];
      $cm = $fields[6];
      $msSym = $fields[8];
    } else {
      $offset1 = 0;
      $humanEntrezID = $fields[1];
      $offset1 = 1 if ($humanEntrezID =~ /\D/g);
      $humanSym = $fields[2 - $offset1];
      $msChr = $fields[4 - $offset1];
      $cm = $fields[5 - $offset1];
      $offset2 = 0;
      $msEntrezID = $fields[6 - $offset1];
      $offset2 = 1 if ($msEntrezID =~ /\D/g);
      $msSym = $fields[7 - $offset1 - $offset2];
    }
    $humanLoc =~ s/\|/;/g;
    $comb1 = $humanSym . "Human";
    if (!exists($outStrs{$comb1})) {
      $humanStr = $humanSym . "|" . $humanChr . "|" . $humanLoc . "|Human|";
      $outStrs{$comb1} = $humanStr;
    } else {
      $outStrs{$comb1} = "bad";
    }
    $comb2 = $msSym . "Mouse";
    if (!exists($outStrs{$comb2})) {    
      $mouseStr = "$msSym" . "|" . $msChr . "|" . $cm;
      $mouseStr .= " cM" if ($cm =~ /\d+\.\d\d/);
      $mouseStr .= "|Mouse|";
      $outStrs{$comb2} =  $mouseStr;
    } else {
      $outStrs{$comb2} = "bad";
    }
  }
}

open (RESULT,  ">chromInfo.unl") || die "Can't open: chromInfo.unl $!\n";
foreach $k (sort keys %outStrs) {
  if ($outStrs{$k} ne "bad") {
    print RESULT $outStrs{$k};
    print RESULT "\n";
  }
}

close RESULT;

exit;
