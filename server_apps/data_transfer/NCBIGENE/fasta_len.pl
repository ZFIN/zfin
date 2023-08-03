#!/usr/bin/perl
use strict;
use warnings;

# Write a program that takes a multi-FASTA file of DNA sequences and prints the
# ID and length of each sequence in the file. The file name should be
# specified on the command line.

# Example file contents:
# >ABC12345.1 TSA: Danio rerio CG2_NonNorm_contig_22545 transcribed RNA sequence
# CTCAACACAGATGAATGATACAGTTTTGGAAGAACCACGCACCGCCACTTCGCTTTAATCACGTCCAATT
# TATCCGACACTTCCAGAGAAGTGCAAATGGGCAGACAATAGACGCTCAGTGGATGAAGGCGACCTGACAG
# TGCAGACGAGCGCAAGTC
#
# >DEFRG4241.2 TSA: Danio rerio FDR_LOC100700518.1.1 transcribed RNA sequence
# GCTCTTTGATTCTGATAAAGGAGGACATCGATGCTTTTAAAGGTTGCAGTGGTGGCTGTCTCTTTAAGAA
# GCCCGTCCAGCTGTCATTGGA
#
# >TAGWE011070.1 TSA: Danio rerio CG2_NonNorm_contig_11079 transcribed RNA sequence
# TTTTTTTTTGACTGTAAAGTTGTTTATTTTTCAAGAGAAAACTAGTTAAAATGTCACACTTAAAAAGCAG
# TAACTGCAAAAGACATAATTCTTTTACAAATGAACAGAAATTATTGCTGCAAGTCTTCATTTACTTAGCC
# TCTATTGTAACGTTGTACACTATAAAACTTTAAAAGTAATGGCATCAAAATAAATGTTCAATTCAAATTA
# GTCTGGATTAAATCAAAATCACATGATTGCCAAAAATCACAAACCAAAAACACACAGGTAATGTCTAATC
# ACGTGTCTAAATGA
#
# ...


# Get filename from command line arguments
my $input_file = $ARGV[0];

open my $fh, '<', $input_file or die "Could not open file '$input_file' $!";

my $current_id = '';
my $sequence = '';

while (my $line = <$fh>) {
    chomp $line;

    # If the line starts with '>', it's a new sequence ID
    if ($line =~ /^>(\w+)/) {
        my $new_id = $1;

        # If we have a sequence ID, print out the result for the previous sequence
        if ($current_id) {
            my $length = length $sequence;
            print "$current_id|$length|\n";
        }

        $current_id = $new_id;
        $sequence = '';
    } else {
        # Append this line to the sequence
        $sequence .= $line;
    }
}

# Don't forget the last sequence
if ($current_id) {
    my $length = length $sequence;
    print "$current_id|$length|\n";
}

close $fh;
