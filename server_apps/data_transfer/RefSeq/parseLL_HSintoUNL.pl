#! /private/bin/perl


open (CUT, "cut -f 1,4,9 LL.out_hs |") or die "cut LL.out_hs failed";
open (UNL, ">ll_hs_id.unl") or die "cannot open ll_hs_id.unl";

while ($line = <CUT>) {
  chop $line;
  ($ll_id,$omim,$gdb_id) = split /\t/,$line,3;
  if ($gdb_id =~ /GDB:([0-9]*)/ ) {
    $gdb_num = $1;
    print UNL "$ll_id|$omim|$gdb_num|\n";
  }
}

close (UNL);
close (CUT);


exit;
