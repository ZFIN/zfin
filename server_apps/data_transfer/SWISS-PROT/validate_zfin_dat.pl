#!/opt/zfin/bin/perl 

# validate_zfin_dat.pl
# quick check that in the zfin.dat file, each record has all EMBL DR lines before any RefSeq DR lines

use strict;
use warnings;
use DBI;
use POSIX;

sub main {
    $/ = "\/\/\n"; #custom record separator
    open INPUT, "zfin.dat" or die "Cannot open zfin.dat";
    foreach my $record (<INPUT>) {
        my $embl = 0;
        my $refseq = 0;
        my $id = "";

        #split lines by newline
        my @lines = split /\n/, $record;

        #check each line
        foreach my $line (@lines) {
            my $line_is_embl = 0;
            my $line_is_refseq = 0;

            #check for ID line
            if ($line =~ /^ID\s+(.*)/) {
                $id = $1;
            }
            if ($line =~ /^DR\s+EMBL/) {
                $line_is_embl = 1;
                $embl = 1;
            }
            if ($line =~ /^DR\s+RefSeq/) {
                $line_is_refseq = 1;
                $refseq = 1;
            }
            #check that embl is before refseq
            if ($line_is_embl == 1 && $refseq == 1) {
                print "ERROR: EMBL DR line is after RefSeq DR line in record $id: \n";
                exit 1;
            }
        }
    }
}

main();
