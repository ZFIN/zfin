#!/local/bin/perl

# Parse the table format blast result 
#
# Usage :
#       blast2unl.pl [-d gb/sp] file
#
#  -d   database used in the blast. 
#         gb (default) = Genbank 
#         sp = SwissProt/TrEMBL
# file  blast table format output
#


use LWP::Simple;
use Getopt::Std;
my ($blastdb, $filename, $suffix, $input, @row);

getopts('d:');

if ($opt_d){
    $blastdb = $opt_d;
}else {
    $blastdb = "gb";
}

$input = $ARGV[0];

($filename, $suffix) = split(/\./,$input);

open IN, "<$input" or die "Cannot open file to read";
open OUT, ">$filename.unl" or die "Cannot open file to write";

while (<IN>) {
    @row = split;
    @query = split (/\|/, shift(@row));
    $qacc = $query[4];

   
    @subject = split (/\|/, shift(@row));
    if ($blastdb eq "gb") {
	$sacc = $subject[4];
    }else {
	$sacc = $subject[1];
    }

    print OUT "$qacc|$sacc|".join("|",@row);
    print OUT "|\n";
    @row = ();
}
close IN;
close OUT;

    

    
