#!/private/bin/perl

###################################################################################
#  File: parseSGDdata.pl
#  This script parses the SGD data file to extract yeast chromosome info and
#  then append the parsed result to "chromInfo.unl" for updating orthologue table
###################################################################################

open (INP, "SGD.data") || die "Can't open SGD.data : $!\n";
@lines=<INP>;

open (INP2, "yeastOrtho.unl") || die "Can't open yeastOrtho.unl : $!\n";
@lines2=<INP2>;

%yeastOrthos = ();
foreach $line (@lines2) {
  @fields = split(/\|/, $line);
  $y = $fields[0]; 
  $o = $fields[1];
  if($y && $o && $y eq "Yeast") {
    $yeastOrthos{$o} = 1;
  }
}   

# open the file for appending the output text
open (RESULT,  ">>chromInfo.unl") || die "Can't open: chromInfo.unl $!\n";

$ct = 0; @markers = @chrs = ();
foreach $line (@lines) {
  $ct++;
  @fields = split(/\t/, $line);
  $m = $fields[4]; 
  $ch = $fields[6];
  @words = split(/\s+/, $ch);
  $chNum = $words[1];
  if($m && $chNum && exists($yeastOrthos{$m}) && $chNum ne "chromosome") {
    print RESULT "$m|$chNum||Yeast";
    print RESULT "\n";
  }
}
close INP;
close INP2;

close RESULT;

exit;
