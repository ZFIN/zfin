#! /local/bin/perl


open (CUT, "cut -f 1,9 LL.out_dr |") or die "cut LL.out_dr failed";
open (UNL, ">ll_id.unl") or die "cannot open ll_id.unl";

while ($line = <CUT>) {
  chop $line;
  ($ll_id,$zdb_id) = split ' ',$line,2;
  if ($zdb_id ne "") {
    print UNL "$ll_id|$zdb_id|\n";
  }
}

close (UNL);
close (CUT);


exit;
