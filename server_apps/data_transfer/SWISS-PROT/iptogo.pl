#!/private/bin/perl

#
# iptogo.pl
#
# This script parse the InterPro to GO translation table 
# to unload file "ip_mrkrgoterm.unl"

my (@line, @ip, @term_id, @term, @id);

if (@ARGV == 0) {
  print "Please enter the InterPro_to_GO_term file as command line argument.\n";
  exit;
}

open IPGO, ">ip_mrkrgoterm.unl" or die "Cannot open the ip_mrkrgoterm file: $!";
while(<>) {

#InterPro:IPR000175 Sodium:neurotransmitter symporter family > GO:neurotransmitter:sodium symporter ; GO:0005328
 
 if(/^InterPro/) {
    chomp;
    @line = split(/ > /, $_);
    @ip = split(/[: ]/, $line[0]);
    @term_id = split(/ ; /, $line[1]);
    @term = split(/GO:/, $term_id[0]);
    @id = split(/:/, $term_id[1]);

    ## FB case: 6392 -- not to map GO:0005515
    print IPGO "$ip[1]|$term[1]|$id[1]|\n" if ($term_id[1] ne "GO:0005515");
    
  }
 @line=(); @ip=(); @term_id=(); @term=(); @id=();
}


    





