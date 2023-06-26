#!/opt/zfin/bin/perl 

# validate_prob_files.pl
# quick check on prob files to see if the records already exist in our DB with other references

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
    open CSVOUT, ">validate_prob_files.csv" or die "Cannot open validate_prob_files.csv";
    #print header
    print CSVOUT "file,record,accession,exists already,pub_count,gene_count,pub_ids,gene_ids,dblink_ids,pub_names,gene_abbrevs,accession,dblink_infos,fdb_db_name\n";

    my $files = [0..10];
    foreach my $file (@$files) {
        my $filename = "prob$file";
        validate_problem_files($filename);
    }

    close CSVOUT;
}

sub validate_problem_files {
    my $filename = shift;
    my $record_count = 1;
    $/ = "\/\/\n"; #custom record separator
    open INPUT, $filename or die "Cannot open $filename";
    foreach my $record (<INPUT>) {
        my $ac_line;
        my @ac = ();
        my $ac_count = 0;

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
                check_acs(\@ac, $filename, $record_count);
            }
        }
        if (!$ac_count) {
            #print "$filename: No AC lines found in record $record_count\n";
        }
        $record_count++;
    }
}

sub check_acs {

    my @ac = @{$_[0]};
    my $filename = $_[1];
    my $record_count = $_[2];

    foreach my $ac (@ac) {
        my $cache_hit_ref = $global_cache{$ac};
        if (!$cache_hit_ref) {
            print CSVOUT "$filename,$record_count,$ac,NOT EXISTS\n";
            next;
        }
        my @cache_hit = @$cache_hit_ref;
        #join array into string
        my $line = join ',', @cache_hit;
        print CSVOUT "$filename,$record_count,$ac,EXISTS,$line\n";
    }
}

main();
