#! /local/bin/perl


open (CUT, "cut -f 1,2 loc2ref |") or die "cut loc2ref failed";
open (UNL, ">refseq_acc.unl") or die "cannot open refseq_acc.unl";

while ($line = <CUT>) {
  chop $line;
  ($ll_id,$ll_acc) = split ' ',$line,2;
  if ($ll_id =~ /[0-9]*/ ) {
    print UNL "$ll_id|$ll_acc|\n";
  }
}

close (UNL);
close (CUT);


exit;
