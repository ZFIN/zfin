#!/private/bin/perl

#
# sptogo.pl
# 
# This script parse the SWISS-PROT keyword to GO translation table 
# to unload file "sp_mrkrgoterm.unl"

my (@line, @spkw, @spkw_id, @term_id, @term, @id);
my $spkwName;


if (@ARGV == 0) {
  print "Please enter the Swiss-Prot_keywords_to_GO_term file as command line argument.\n";
  exit;
}

open KWGO, ">sp_mrkrgoterm.unl" or die "Cannot open the sp_mrkrgoterm file: $!";
while(<>) {
 #SP_KW:KW-0117 Actin capping > GO:barbed-end actin filament capping ; GO:0051016
 if(/^SP/) {
    chomp;
    @line = split(/ > /, $_);
    $spIDandName = $line[0];
    @spkw = split(/ /, $spIDandName);
    @spkw_id = split(/:/, $spkw[0]);
    $spID = $spkw_id[1];

    @term_id  = split(/ ; /, $line[1]);
    @term = split(/GO:/, $term_id[0]);
    @id = split(/:/, $term_id[1]);
    
    #output: KW-0117| Actin capping|barbed-end actin filament capping|0051016|
    ## the following WRONG OUTPUT (2nd field) is produced by souce code which is commented out
    ##  KW-0117|Actin|barbed-end actin filament capping|0051016|
    ##print KWGO "$spkw_id[1]|$spkw[1]|$term[1]|$id[1]|\n";
    
    @spkwNamePieces = split(/$spID/, $spIDandName);
    $spkwName = $spkwNamePieces[1];
    $spkwName =~ s/^\s+//; #remove leading spaces
    $spkwName =~ s/\s+$//; #remove trailing spaces
    print KWGO "$spID|$spkwName|$term[1]|$id[1]|\n";
  }
 @line=(); @ip=(); @term_id=(); @term=(); @id=();   
 $spkwName = "";
}

