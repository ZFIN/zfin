#!/opt/zfin/bin/perl 

# rearrange_prob_files.pl
# Put higher priority records at the top of the file
# 1.  records that already exist in our DB
# 2.  records with a ZFIN gene ID

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
    my $files = [1..10];
    foreach my $file (@$files) {
        my $filename = "prob$file";
        print "$filename\n";
        rearrange_problem_files($filename);
    }
}

sub rearrange_problem_files {
    my $filename = shift;
    my $record_count = 1;
    open INPUT, $filename or die "Cannot open $filename";
    open (OUTPUT, ">$filename.rclean") ||  die "Cannot open $filename.rclean : $!\n";

    my @high_priority = (); #records that already exist in our DB and have a ZFIN gene ID
    my @medium_high_priority = (); #records that already exist in our DB without a ZFIN gene ID
    my @medium_priority = (); #records with a ZFIN gene ID
    my @low_priority = (); #records without a ZFIN gene ID

    # read the first five lines (header) into high priority queue
    $/ = "\n";
    for (my $i = 0; $i < 5; $i++) {
        my $line = <INPUT>;
        push @high_priority, $line;
    }
    push @high_priority, "#--------------------------------------------\n";

    $/ = "\/\/\n"; #custom record separator
    foreach my $record (<INPUT>) {
        if ($record eq "#\n") {
            next;
        }
        my $ac_line;
        my @ac = ();
        my $ac_count = 0;
        my $already_exists_pubs = 0;
        my $has_zfin_gene = 0;

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
                $already_exists_pubs = check_ac_exists_in_database(\@ac);
            }
        }
        if (!$ac_count) {
            #print "$filename: No AC lines found in record $record_count\n";
        }
        $record_count++;

        $has_zfin_gene = check_for_zfin_gene($record);
        if ($has_zfin_gene || $already_exists_pubs) {
            my $header = "     # ";
            if ($has_zfin_gene) {
                $header .= "HAS_ZDB_ID ($has_zfin_gene) ";
            }
            if ($already_exists_pubs) {
                $header .= "ALREADY_EXISTS ($already_exists_pubs) ";
            }
            my @lines = split /\n/, $record;
            my $line1 = shift(@lines);
            $line1 =~ s/^\s+//;
            $line1 .= $header;
            unshift(@lines, $line1);
            $record = join("\n", @lines) . "\n";
        }

        if ($already_exists_pubs && $has_zfin_gene) {
            push @high_priority, $record;
        } elsif ($already_exists_pubs) {
            push @medium_high_priority, $record;
        } elsif ($has_zfin_gene) {
            push @medium_priority, $record;
        } else {
            push @low_priority, $record;
        }
    }

    foreach my $record (@high_priority) {
        print OUTPUT $record;
    }
    foreach my $record (@medium_high_priority) {
        print OUTPUT $record;
    }
    foreach my $record (@medium_priority) {
        print OUTPUT $record;
    }
    foreach my $record (@low_priority) {
        print OUTPUT $record;
    }

    close INPUT;
    close OUTPUT;

    #rename original file
    rename $filename, "$filename.rorig";
    #rename clean file to original name
    rename "$filename.rclean", $filename;
    #delete original file
    unlink "$filename.rorig";
}

#check if any of the AC lines in the record already exist in the database
#return 1 if any of the AC lines already exist (associated with a manually curated entry -- ZDB-PUB-220705-2)
sub check_ac_exists_in_database {

    my @ac = @{$_[0]};

    foreach my $ac (@ac) {
        my $cache_hit_ref = $global_cache{$ac};
        if (!$cache_hit_ref) {
            next;
        }
        my @cache_hit = @$cache_hit_ref;
        my $pub_ids = $cache_hit[2];
        return $pub_ids;
    }
    return 0;
}

sub check_for_zfin_gene {
    my $record = $_[0];
    if ($record =~ /DR   ZFIN; (ZDB-.*)$/m) {
        return $1;
    }
    return 0;
}

main();
