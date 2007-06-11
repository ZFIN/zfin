#! /local/bin/perl


open (CUT, "cut -f 1,2,3,4,5,6 gene_info |") or die "cut LL.out_dr failed";
open (UNLDR, ">ll_id.unl") or die "cannot open ll_id.unl";
open (UNLHS, ">ll_hs_id.unl") or die "cannot open ll_hs_id.unl";
open (UNLMM, ">ll_mm_id.unl") or die "cannot open ll_mm_id.unl";

while ($line = <CUT>) {
  chop $line;
  ($org,$ll_id,$abbrev,$org_id,$alias1,$dbRef,$tail) = split ' ',$line,7;
  

  if ($org eq "7955" && $dbRef =~ "^ZFIN:(ZDB-GENE-[0-9]*-[0-9]*)") {
    print UNLDR "$ll_id|$1|\n";
  }  
  elsif ($org eq "9606" && $dbRef ) {
    print UNLHS "$ll_id|$abbrev|$org_id|\n";
  }
  elsif ($org eq "10090" && $dbRef ) {
    if ($dbRef =~ /^MGI:/) 
    { 
      $alias1 =~ s/\|/,/g;
      $abbrev = "$abbrev,$alias1";
    }    
    print UNLMM "$ll_id|$abbrev|$org_id|\n";
  }
}

close (UNLMM);
close (UNLHS);
close (UNLDR);
close (CUT);


exit;
