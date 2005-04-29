#! /local/bin/perl


open (CUT, "cut -f 1,2,3,4,5,6 gene_info |") or die "cut LL.out_dr failed";
open (UNLDR, ">ll_id.unl") or die "cannot open ll_id.unl";
open (UNLHS, ">ll_hs_id.unl") or die "cannot open ll_hs_id.unl";
open (UNLMM, ">ll_mm_id.unl") or die "cannot open ll_mm_id.unl";

while ($line = <CUT>) {
  chop $line;
  ($org,$ll_id,$abbrev,$org_id,$alias1,$alias2) = split ' ',$line,6;
  

  if ($org eq "7955" && $org_id =~ "^ZDB") {
    print UNLDR "$ll_id|$org_id|\n";
  }  
  elsif ($org eq "9606" && $org_id ) {
    print UNLHS "$ll_id|$abbrev|$org_id|\n";
  }
  elsif ($org eq "10090" && $org_id ) {
    if ($abbrev =~ /^MGI:/) 
    { 
      $alias1 =~ s/\|/,/g;
      $abbrev = $alias1;
    }    
    print UNLMM "$ll_id|$abbrev|$org_id|\n";
  }
}

close (UNLMM);
close (UNLHS);
close (UNLDR);
close (CUT);


exit;
