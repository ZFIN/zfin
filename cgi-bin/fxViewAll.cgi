#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";
require "Tableview.pl";

use CGI;

my $Query = new CGI();

print "Content-type: text/html\n\n";
header();

if ($Query->param('scenario') eq "2") {
  $scenario = "2";
} else {
  $scenario = "1";
}


Tableview("all", $scenario);
print "</table> <p>";

footer();
