#! /local/bin/perl


open (CUT, "cut -f 1,2,6 loc2ref |") or die "cut loc2ref failed";
open (UNL, ">loc2ref.unl") or die "cannot open loc2ref.unl";

while ($line = <CUT>) {
  chop $line;
  ($ll_id,$ll_acc,$org_id) = split ' ',$line,3;
  if ($ll_id =~ /[0-9]*/ && $org_id == 7955) {
    while($ll_acc =~ /\./){chop $ll_acc;}
    print UNL "$ll_id|$ll_acc|\n";
  }
}

close (UNL);
close (CUT);


exit;
