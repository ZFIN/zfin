#!/private/bin/perl

# converts assembly_for_tom.tab into two files,
# one with the vega trimmed clones, and one with
# overlapping full length clones.


open(ASSEMBLY,$ARGV[1]) or die "can't open assembly file";
open(VEGACLONEGFF3,">".$ARGV[2]) or die "can't open vega clone output gff3 file";
open(FULLCLONEGFF3,">".$ARGV[3]) or die "can't open full clone output gff3 file";
open(ZDBIDS,"zdb_ids.txt") or die "can't open zdb_id file";
open(LENGTHS,"clone_lengths.txt") or die "can't open clone_lengths.txt";

my $source = $ARGV[0];

#a more detailed SO term would be tiling_path_clone possibly.
my $type = "clone";

my %zdbid;

while ($line = <ZDBIDS>) {
    chop $line;
    ($mrkr_abbrev, $mrkr_zdb_id) = split '\t',$line,3;

    $zdbid{$mrkr_abbrev} = $mrkr_zdb_id;
}

my %lengths;

while ($line = <LENGTHS>) {
    chop $line;
    ($mrkr_abbrev, $length) = split '\t',$line,2;

    $lengths{$mrkr_abbrev} = $length;
}


#I'm ignoring sequence regions on the assumption that they get loaded ahead of this

while ($line = <ASSEMBLY>) {
    chop $line;

    ($seqid,$start,$end,$name,$id,$sixth,$internal_start,$internal_end,$strand_int) = split '\t',$line,9;   

    #sanity check
    if ($lengths{$name} < $internal_end) {
        print $name . ": lengths don't match, assembly_for_tom/genbank: " . $internal_end . "/" . $lengths{$name} . "\n"; 
    }


    #end sanity check

    my $original_start = $start;
    my $original_end = $end;
    
    #handle the coordinate fixing differently for forward vs reverse stranges
    if ($strand_int == 1) {
      $start = $start - ($internal_start - 1);
      if (defined $lengths{$name}) {
         $end = $start + $lengths{$name};
      }
    } else {
	$start = $start - ($lengths{$name} - $internal_end);
        $end = $end + $internal_start;

    }

    $score = ".";

    if ($strand_int == -1) {
      $strand = "-";
    } else { 
      $strand = "+";
    }

    $phase = ".";

    $attributes = "ID=" . $id . ";Name=" . $name;

    if (defined $zdbid{$name}) {
        $attributes = $attributes . ";Alias=" . $zdbid{$name};
    }

    my $fullsource = $source . ".fulllength";


    print FULLCLONEGFF3 "$seqid\t$fullsource\t$type\t$start\t$end\t$score\t$strand\t$phase\t$attributes\n";
    print VEGACLONEGFF3 "$seqid\t$source\t$type\t$original_start\t$original_end\t$score\t$strand\t$phase\t$attributes\n";

}
