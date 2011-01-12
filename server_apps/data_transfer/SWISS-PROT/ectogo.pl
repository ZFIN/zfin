#!/private/bin/perl

#
# ectogo.pl
#
# This script parse the EC to GO translation table 
# to unload file "ec_mrkrgoterm.unl"

my (@line, @ec, @term_id, @term, @id);

if (@ARGV == 0) {
  print "Please enter the EC to GO file as command line argument.\n";
  exit;
}

open ECGO, ">ec_mrkrgoterm.unl" or die "Cannot open the ec_mrkrgoterm file: $!";
while(<>) {
#EC:1.1.1.1 > GO:alcohol dehydrogenase activity ; GO:0004022
#InterPro:IPR000175 Sodium:neurotransmitter symporter family > GO:neurotransmitter:sodium symporter ; GO:0005328
 
 if(/^EC/) {
    chomp;
    @line = split(/ > /, $_);
    @ec = split(/[: ]/, $line[0]);
    @term_id = split(/ ; /, $line[1]);
    @term = split(/GO:/, $term_id[0]);
    @id = split(/:/, $term_id[1]);
    
    ## FB case: 6392 -- not to map GO:0005515 
    print ECGO "$ec[1]|$term[1]|$id[1]|\n" if ($term_id[1] ne "GO:0005515");
    
  }
 @line=(); @ec=(); @term_id=(); @term=(); @id=();
}


    





