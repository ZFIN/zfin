#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";
require "Figures.pl";

print "Content-type: text/html\n\n";
header();
pub("all");
Figures("all");

footer();
