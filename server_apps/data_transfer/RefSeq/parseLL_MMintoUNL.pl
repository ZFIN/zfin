#! /private/bin/perl


open (CUT, "cut -f 1,9 LL.out_mm |") or die "cut LL.out_mm failed";
open (UNL, ">ll_mm_id.unl") or die "cannot open ll_mm_id.unl";

while ($line = <CUT>) {
  chop $line;
  ($ll_id,$mgi_id) = split /\t/,$line,2;
  if ($mgi_id =~ /MGI:([0-9]*)/ ) {
    $mgi_num = $1;
    print UNL "$ll_id|$mgi_num|\n";
  }
}

close (UNL);
close (CUT);


exit;
