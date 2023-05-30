#!/opt/zfin/bin/perl 

# move_prob_files.pl
# Just rename the prob files to add .txt

use strict;
use warnings;

sub main {
    my $files = [0..10];
    foreach my $file (@$files) {
        my $filename = "prob$file";
        my $dest = $filename . ".txt";
        print "renaming $filename to $dest\n";
        rename($filename, $dest);
    }
}

main();
