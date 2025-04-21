#!/usr/bin/env perl
#
# The script reads GenBank daily update flat file (compressed), filters out to only include records for
# Danio rerio (zebrafish), Mus musculus (mouse), and Homo sapiens (human). It replaces the original file.

use Getopt::Std;
use POSIX;

# Get command line options
getopts('h');

my $usage = <<EOF;

  Usage:  filterFlatFile.pl file

    Input file : Gb daily update file nc*.flat.gz
    Output file: The same file, but limited to only relevant organisms
EOF

if (@ARGV == 0 || $opt_h) {
    print $usage;
    exit;
}

my ($organism);

while (my $gbfile = shift @ARGV) {

    if ($gbfile !~ /\.flat\.gz$/) {
        print "Error: File must have extension '.flat.gz'. \n";
        exit;
    }

    my $currentTime = strftime("%Y-%m-%d %H:%M:%S", localtime(time()));
    print "Processing $gbfile, filesize: " . format_number(-s $gbfile) . " bytes at $currentTime\n";

    # Open the compressed input file via a pipe
    open(FLATFILEINPUT, "gunzip -c $gbfile |") or die "Cannot open the file to read: $!.";

    my @file = split(/\./, $gbfile);
    my $prefix = shift(@file);
    my $filteredFileName = $prefix . ".filtered.flat.gz";

    my $totalRecordCount = 0;
    my $totalFilteredRecordCount = 0;
    my $totalRecordCoundWithLocus = 0;
    my $danioCount = 0;
    my $musCount = 0;
    my $homoCount = 0;

    # Open the output file and pipe to gzip for compression
    open(FLATFILEOUTPUT, "| gzip --fast -n > $filteredFileName") or die "Cannot open the file to write: $!.";

    $/ = "//\n";  # Set record separator to GenBank format
    my $progress = 0;
    while (<FLATFILEINPUT>) {
        $totalRecordCount++;

        $progress++;
        if ($progress % 100 == 0) {
            print ".";
            flush STDOUT;
        }

        next unless /LOCUS\s+(\w+)\s+(\d+)\sbp\s+(\w+)\s+\w+\s+(\w+).+/;

        $totalRecordCoundWithLocus++;

        /ORGANISM\s+([\w\[\]\'].+)\n/ or die "ORGANISM unmatched at record $progress : \n\n$_ \n";
        $organism = $1;
        if ($organism eq 'Danio rerio' || $organism eq 'Mus musculus' || $organism eq 'Homo sapiens') {
            $totalFilteredRecordCount++;
            print FLATFILEOUTPUT $_;
        }
        if ($organism eq 'Danio rerio') {
            $danioCount++;
        }
        if ($organism eq 'Mus musculus') {
            $musCount++;
        }
        if ($organism eq 'Homo sapiens') {
            $homoCount++;
        }

    }

    close FLATFILEINPUT;
    close FLATFILEOUTPUT;

    print("\nAfter filtering $totalRecordCount records ($totalRecordCoundWithLocus with LOCUS line), there are $totalFilteredRecordCount records remaining for relevant organisms (danio rerio [$danioCount], mus musculus [$musCount], homo sapiens [$homoCount]).\n");

    print("\nRenaming original file to $gbfile.original\n");
    rename $gbfile, "$gbfile.original" or die "Cannot rename file: $!";

    print("New file with compressed filtered contents: $filteredFileName\n");

    print("Renaming new file to replace original $filteredFileName -> $gbfile\n");
    rename $filteredFileName, $gbfile or die "Cannot rename file";

    print("Replaced original file with filtered contents. New file size: " . format_number(-s $gbfile) . " bytes\n");

    print("Deleting original file: $gbfile.original\n");
    unlink($gbfile . ".original");

    $currentTime = strftime("%Y-%m-%d %H:%M:%S", localtime(time()));
    print("Finished at " . $currentTime . "\n");
}

sub format_number {
    my $number = shift;
    my $string = "" . $number;
    my $reversedString = reverse $string;
    my $buffer = "";
    my $count = 0;
    for my $char (split //, $reversedString) {
        $buffer .= "," if ($count % 3 == 0 && $count != 0);
        $buffer .= $char;
        $count++;
    }
    return reverse $buffer;
}