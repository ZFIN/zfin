#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "xpatview.pl";
use CGI;



print "Content-type: text/html\n\n";
print "<html>\n\n";
header();

xpatview();

footer();

