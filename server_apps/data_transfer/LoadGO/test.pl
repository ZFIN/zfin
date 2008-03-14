#!/private/bin/perl

#
#  ontology.pl
#
#  This script asks all three ontology file as input and parse them all. 
#  The primary GO id and GO term, GO ontology go to file "ontology.unl".
#  The secondary GOs are stored with the primary GO id, GO term, ontology
#  into another file "ontsecgoid.unl". When loading to ZFIN, only the 
#  primary GO id is stored. We keep the secondary ones since they might
#  still be refered to.  


if(@ARGV < 3)  {
  print "\nPlease enter the name of all three GO term ontology files: \n   % ontology.pl function.ontology process.ontology component.ontology\n\n" and exit 1;
}

my %ontology;
$ontology{"function"} = "Molecular Function";
$ontology{"process"} = "Biological Process";
$ontology{"component"} = "Cellular Component";

open ONTO, ">ontology.unl" or die "Cannot open the gotermonto file:$!\n";

open ONTALIAS, ">ontsecgoid.unl" 
  or die "Cannot open the file for go id alias: $!\n";

while (<>) {
  
  my @argfile = split(/\./, $ARGV);
  my $ontolgname = $ontology{ $argfile[0] };

#  %nicotinic acetylcholine-activated cation-selective channel ; GO:0004889, GO:0016904 ; TC:1.A.9.1.- ; synonym:nicotinic acetylcholine receptor % cation channel ; GO:0005261 % excitatory extracellular ligand-gated ion channel ; GO:0005231
  if ( /^\s*</ || /^\s*%/ ) {
   
    @line = split(/% |< |<|%/, $_);
    $space = shift @line;
    while ($goterm = shift @line) {
      @term_id = split(/; /, $goterm);
      $term = $term_id[0];
      $term =~ s/\s+$//;      # get rid of trailing whitespace
      ($found) = $text =~/^%. + synonym:\s*(.+)\s%/; 
      print $1;
      chop($term_id[1]);         #get rid of extra space
      @goids = split(/,/, $term_id[1]);
      $prmgo = shift @goids;
      @prmgo = split (/:/, $prmgo);
      print ONTO "$prmgo[1]|$term|$ontolgname|\n"; 
      foreach $goid (@goids){
	@id = split(/:/,$goid);
	print ONTALIAS "$id[1]|$prmgo[1]|$term|$ontolgname|\n"; 
      }  
    }
  }
  @line = (); @term_id = (); @goids = (); @prmgo = (); @id = ();
  $space = ""; $term = ""; $id = ""; $prmgo = "";
}
  
close ONTO;
