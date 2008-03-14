#! /private/bin/perl


open (CUT, "cut -f 1,2 loc2UG |") or die "cut loc2UG failed";
open (UNL, ">loc2UG.unl") or die 'cannot open loc2UG.unl';

while ($line = <CUT>) {

  chop $line;
  ($ll_id,$uni_gene) = split ' ',$line,2;

  if ( $uni_gene =~ /^Dr/ ) {
    $uni_num = substr($uni_gene,3);   #remove 'Dr.'
    print UNL "$ll_id|$uni_num|\n";
  }
}

close (UNL);
close (CUT);


exit;
