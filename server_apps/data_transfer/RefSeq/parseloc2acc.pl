#! /private/bin/perl


@AoA = ();


# Open the file loc2acc. For details on the file look in the README.
#
# Pull out the Zebrafish (7955) accession numbers.
# Remove revision numbers (acc.#)

open (CUT, "cut -f 1,2,4,6 loc2acc |") or die "cut loc2acc failed";

while ($line = <CUT>) {
  chop $line;
  ($org,$ll_id,$ll_genbank,$ll_genpept) = split ' ',$line,4;
  if ($ll_id =~ /[0-9]*/ && $org == 7955) {
    while($ll_genbank =~ /(.*)\./){$ll_genbank = $1;}
    while($ll_genpept =~ /(.*)\./){$ll_genpept = $1;} 
    if ($ll_genbank eq ""){$ll_genbank = "-";}
    if ($ll_genpept eq ""){$ll_genpept = "-";}
    
    push @AoA, "$ll_id|$ll_genbank|$ll_genpept|" ;
  }
}

close (CUT);

# Sort the data.
@AoA = sort(@AoA);


# Print to a load file. Ignore adjacent records that are duplicates. 

open (UNL, ">loc2acc.unl") or die "cannot open loc2acc.unl";

for $x ( 0 .. $#AoA)
{
  if ( $AoA[$x] ne $AoA[$x-1] )
  {
    print UNL $AoA[$x]."\n";
  }

}
close (UNL);


exit;
