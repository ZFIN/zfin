#!/usr/bin/env perl
#use strict ; 
#use warnings ; 
$line = $ARGV[0] ; 
@tokens = split( /\ \|/, $line); 
print substr(@tokens[0],1); 
