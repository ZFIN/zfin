#!/usr/bin/env perl
# 
# Read in the FASTA file which normally named as ZGI.mmddyy. 
# Exclude non TC# entrance. Write output to standard output.
#

use strict;

if (@ARGV == 0 ) {
    print "Please provide TIGR fasta file as input.\n";
}

$/ = ">";
while(<>) {

    if ( /^TC/ ) {
	print ">";
	chop;
	print;
    }
}

