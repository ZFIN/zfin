#! /local/bin/perl


open (CUT, "cut -f 1,2,5,6 loc2ref |") or die "cut loc2ref failed";
open (UNL, ">loc2ref.unl") or die "cannot open loc2ref.unl";

while ($line = <CUT>) {
  chop $line;
  ($ll_id,$nm_acc,$np_acc,$org_id) = split ' ',$line,4;
  if ($ll_id =~ /[0-9]*/ && $org_id == 7955) {
    if($nm_acc =~ /(.*)\./){$nm_acc = $1;}
    if($np_acc =~ /(.*)\./){$np_acc = $1;}
    print UNL "$ll_id|$nm_acc||$np_acc||\n";
  }
}

close (UNL);
close (CUT);


exit;
