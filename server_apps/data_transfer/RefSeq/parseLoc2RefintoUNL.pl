#! /private/bin/perl 

# This is not a cgi, so no -wT

# Container for desired data.
@AoA = ();

# Open the file loc2ref. For details on the file look in the README.
#
# Pull out the Zebrafish (7955) accession numbers.

open (CUT, "cut -f 1,2,4,6 loc2ref |") or die "cut loc2ref failed";

while ($line = <CUT>) {
  chop $line;
  ($org_id,$ll_id,$nm_acc,$np_acc) = split ' ',$line,4;
  if ($ll_id =~ /[0-9]*/ && $org_id == 7955) {
    if($nm_acc =~ /(.*)\./){$nm_acc = $1;}
    if($np_acc =~ /(.*)\./){$np_acc = $1;}
    
    push @AoA, "$ll_id|$nm_acc|$np_acc|" ;
  }
}

close (CUT);

# Sort the data.
@AoA = sort(@AoA);


# Print to a load file. Ignore adjacent records that are duplicates. 

open (UNL, ">loc2ref.unl") or die "cannot open loc2ref.unl";
for $x ( 0 .. $#AoA )
{
  if ($AoA[$x] ne $AoA[$x-1])
  {
    print UNL $AoA[$x]."\n";
  }

}
close (UNL);

exit;
