#! /local/bin/perl


open (CUT, "cut -f 1,2,5,6 loc2acc |") or die "cut loc2acc failed";
open (UNL, ">loc2acc.unl") or die "cannot open loc2acc.unl";

while ($line = <CUT>) {
  chop $line;
  ($ll_id,$ll_genbank,$ll_genpept,$org) = split ' ',$line,4;
  if ($ll_id =~ /[0-9]*/ && $org == 7955) {
    while($ll_genbank =~ /\./){chop $ll_genbank;}
    while($ll_genpept =~ /\./){chop $ll_genpept;}
    print UNL "$ll_id|$ll_genbank|$ll_genpept|\n";
  }
}

close (UNL);
close (CUT);


exit;
