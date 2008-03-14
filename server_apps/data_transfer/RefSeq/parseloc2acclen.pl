#! /private/bin/perl


open (FILE_nM, "zebrafish.gbff") or die "open z.gbff failed";
open (FILE_nP, "zebrafish.gnp") or die "open z.gnp failed";
open (UNL, ">loc2acclen.unl") or die "cannot open loc2acclen.unl";

while ($line = <FILE_nM>) {
  if ($line =~ /^LOCUS\W*(NM_[0-9]*)\W*([0-9]*)/)
  {
    print UNL "$1|$2|\n";
  }
}

while ($line = <FILE_nP>) {
  if ($line =~ /^LOCUS\W*(NP_[0-9]*)\W*([0-9]*)/)
  {    
    print UNL "$1|$2|\n";
  }
}

close (UNL);
close (FILE_nM);
close (FILE_nP);


exit;
