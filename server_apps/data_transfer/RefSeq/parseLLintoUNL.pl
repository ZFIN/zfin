#! /private/bin/perl


open (CUT, "cut -f 1,2,3,4,5,6 gene_info |") or die "cut LL.out_dr failed";
open (UNLDR, ">ll_id.unl") or die "cannot open ll_id.unl";

while ($line = <CUT>) {
  chop $line;
  ($org,$ll_id,$abbrev,$org_id,$alias1,$dbRef,$tail) = split ' ',$line,7;
  

  if ($org eq "7955" && $dbRef =~ "^ZFIN:(ZDB-GENE-[0-9]*-[0-9]*)") {
    print UNLDR "$ll_id|$1|\n";
  }
   
}

close (UNLDR);
close (CUT);


exit;
