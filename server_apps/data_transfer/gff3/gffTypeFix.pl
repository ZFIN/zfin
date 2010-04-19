#!/private/bin/perl

open(CHROMOSOMES,$ARGV[1]) or die "can't open chromosome length file";
open(BADGFF3,$ARGV[2]) or die "can't open gff3 file";
open(GOODGFF3,">".$ARGV[3]) or die "can't open output gff3 file";
open(ZDBIDS,"zdb_ids.txt") or die "can't open zdb_id file";
open(CHRTEST,">chrtest.txt");

my $actual_source = $ARGV[0];


my %zdbid;

while ($line = <ZDBIDS>) {
    chop $line;
    ($mrkr_abbrev, $mrkr_zdb_id) = split '\t',$line,3;

    $zdbid{$mrkr_abbrev} = $mrkr_zdb_id;
}

my %sequence_region;

while ($line = <CHROMOSOMES>) {
    chop $line;
    ($chr, $length) = split '\t',$line,2;
    $sequence_region{$chr} = "##sequence-region\t$chr\t1\t$length\n";
    print CHRTEST $sequence_region{$chr};
}


my $vegatype;
my $last_seqid;
my $name;

while ($line = <BADGFF3>) {
    chop $line;
    
    #hopefully this means only process lines that don't start with a #
    if ($line =~ m/^\#/) {
        if ($line =~ m/^\#\#PUT A GFF3 HEADER HERE/) {
          print GOODGFF3 "##gff-version 3\n";
        } else {
          print GOODGFF3 $line . "\n";        
        }
    } else {
        ($seqid,$source,$type,$start,$end,$score,$strand,$phase,$attributes) = split '\t',$line,9;

        #if the last seqid is different from the current seqid, print out a sequence region line
        #here
	if ($seqid != $last_seqid) {
            print GOODGFF3 $sequence_region{$seqid};
        }
  
        #fix the type section
#        $type =~ s/five_prime_utr/five_prime_UTR/g;

        #fix the attributes section
#        $attributes =~ s/ NAME/Name/g;
#        $attributes =~ s/ PARENT/Parent/g;
        $attributes =~ s/; /;/g;   #remove space afer semicolons

        $attributes =~ m/.*;Name=([^;]*);.*/;
        $name = $1;

        if (defined $zdbid{$name})  {
            $attributes = $attributes . "Alias=" . $zdbid{$name};
        }

        # the source field is always KNOWN_protein_coding or NOVEL_protein_coding, and
        # it's referring to the gene.. so when we overwrite with the actual source, we
        # won't keep it.
        $source = $actual_source;
   

	print GOODGFF3 "$seqid\t$source\t$type\t$start\t$end\t$score\t$strand\t$phase\t$attributes\n";

        $last_seqid = $seqid;

    }
}

close (GOODGFF3);
close (BADGFF3);

exit;
