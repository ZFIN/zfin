#!/opt/zfin/bin/perl 

# quick check to what kinds of matches were made in okfile
# run like: perl okfile_gp_misses.pl | sort | uniq -c
# expect output like:
#        59 A:GB match
#       487 A:GP match
#       452 B:ELSE
#     11962 B:GP match
#     20869 B:REFSEQ match
#     10221 B:ZFIN_MATCH

use strict;
use warnings;
use DBI;
use POSIX;

sub main {
    $/ = "\/\/\n"; #custom record separator
    open INPUT, "okfile" or die "Cannot open okfile";
    foreach my $record (<INPUT>) {

        if ($record =~ /GP_NO_MATCH/) {
            if ($record =~ /GB match/) {
                print "A:GB match";
            } elsif ($record =~ /GP match/) {
                print "A:GP match";
            } elsif ($record =~ /REFSEQ match/) {
                print "A:REFSEQ match";
            } elsif ($record =~ /REFSEQ_NO_MATCH/) {
                print "A:REFSEQ_NO_MATCH";
            } elsif ($record =~ /DR   ZFIN/) {
                print "A:ZFIN_MATCH";
            } else {
                print "A:ELSE";
            }
        } else {
            if ($record =~ /GB match/) {
                #Should never match this
                print "B:GB match";
            } elsif ($record =~ /GP match/) {
                print "B:GP match";
            } elsif ($record =~ /REFSEQ match/) {
                print "B:REFSEQ match";
            } elsif ($record =~ /REFSEQ_NO_MATCH/) {
                print "B:REFSEQ_NO_MATCH";
            } elsif ($record =~ /DR   ZFIN/) {
                print "B:ZFIN_MATCH";
            } else {
                print "B:ELSE";
            }
        }
        print "\n";
    }
}

main();
