#! /local/bin/perl


open (CUT, "cut -f 1,2,6 loc2acc |") or die "cut loc2acc failed";
open (UNL, ">loc2acc.unl") or die "cannot open loc2acc.unl";

while ($line = <CUT>) {
  chop $line;
  ($ll_id,$ll_acc,$org) = split ' ',$line,3;
  if ($ll_id =~ /[0-9]*/ && $org == 7955) {
    while($ll_acc =~ /\./){chop $ll_acc;}
    print UNL "$ll_id|$ll_acc|\n";
  }
}

close (UNL);
close (CUT);


exit;
