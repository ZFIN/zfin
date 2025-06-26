#!/private/bin/perl
#
# Read in VegaProtein Zebrafish cdna.fa file, switch the item order in defline,
# write output to standard output.
#
# defline:
# 

use strict;

if (@ARGV == 0 ) {
    print "Please provide VegaProtein fasta file as input.\n";
    exit;
}

while (<>) {
    
    if (/^>(.+)$/) {
	my @defline = split(/ +/,$1);
	# new order:
	my $geneId = $defline[3];
	my $tscriptId = $defline[4];
	my $protId = $defline[0];
	$geneId =~ s/gene//g;
	$geneId =~ s/\://g;
	$tscriptId =~ s/transcript\://g;
	print ">tpe|$protId|$tscriptId|$geneId $defline[5] $defline[6] $defline[1] $defline[2]\n";
    }
    else {
	print;
    }
}


exit;
