#!/private/bin/perl


open(CHROMOSOMES,$ARGV[0]) or die "can't open chromosome length file";

my $urlbase = "http://vega.sanger.ac.uk/Danio_rerio/exportview?type1=bp&anchor1=1&type2=bp&format=fasta&action=export&_format=Text&output=txt";
my $urlend = "&submit=Continue+>>";

my %sequence_region;
while ($line = <CHROMOSOMES>) {
    chop $line;
    ($chr, $length) = split '\t',$line,2;

    my $url = $urlbase . "&seq_region_name=" . $chr;
    $url = $url . "&anchor2=" . $length;
    $url = $url . $urlend;
     
    system("/local/bin/curl -s \"" . $url . "\" -o Chr" . $chr . ".fa" . "\n"); 
#    $sequence_region{$chr} = "##sequence-region\t$chr\t1\t$length\n";
#    print CHRTEST $sequence_region{$chr};
}


exit;
