#!/opt/zfin/bin/perl 

# clean_prob_files.pl
# Reduce clutter in problem files by removing entries that already
# exist in the database with attribution to ZDB-PUB-220705-2.
# Those have already been manually checked and are not problems.

use strict;
use warnings;
use DBI;
use POSIX;
use FindBin;

#import from package in this directory:
use lib "$FindBin::Bin/";
use ProblemFileUtil;
my %global_cache = %{ProblemFileUtil->getAllExistingUniprotRecordsByAccession()};

sub main {
    my $files = [0..10];
    foreach my $file (@$files) {
        my $filename = "prob$file";
        rearrange_problem_files($filename);
    }
}

sub rearrange_problem_files {
    my $filename = shift;
    my $record_count = 1;
    $/ = "\/\/\n"; #custom record separator
    open INPUT, $filename or die "Cannot open $filename";
    open (OUTPUT, ">$filename.clean") ||  die "Cannot open $filename.clean : $!\n";

    foreach my $record (<INPUT>) {
        my $ac_line;
        my @ac = ();
        my $ac_count = 0;
        my $ignore_record = 0;

        #split lines by newline
        my @lines = split /\n/, $record;

        #check each line
        foreach my $line (@lines) {
            #check for ID line
            if ($line =~ /^AC\s+(.*)/) {
                $ac_line = $1;
                @ac = split /;\s+/, $ac_line;

                #remove trailing semicolon
                foreach (@ac) {
                    $_ =~ s/;$//;
                }

                $ac_count = @ac;
                #check each AC line
                $ignore_record = check_ac_exists_with_manual_curation(\@ac);
            }
        }
        if (!$ac_count) {
            #print "$filename: No AC lines found in record $record_count\n";
        }
        $record_count++;
        if (!$ignore_record) {
            print OUTPUT $record;
        }
    }
    close INPUT;
    close OUTPUT;

    #rename original file
    rename $filename, "$filename.orig";
    #rename clean file to original name
    rename "$filename.clean", $filename;
    #delete original file
    unlink "$filename.orig";
}

#check if any of the AC lines in the record already exist in the database
#return 1 if any of the AC lines already exist (associated with a manually curated entry -- ZDB-PUB-220705-2)
sub check_ac_exists_with_manual_curation {

    my @ac = @{$_[0]};

    # print "  $filename: '@ac' \n";
    foreach my $ac (@ac) {
        my $cache_hit_ref = $global_cache{$ac};
        if (!$cache_hit_ref) {
            next;
        }
        my @cache_hit = @$cache_hit_ref;
        my $pub_ids = $cache_hit[2];

        if ($pub_ids =~ /ZDB-PUB-220705-2/) {
            # print "  $filename,$record_count,$ac,ALREADY EXISTS\n";
            return 1;
        }
    }
    return 0;
}

main();
