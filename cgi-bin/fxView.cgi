#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";
require "Tableview.pl";
require "Figures.pl";

use CGI;

my $Query = new CGI();

print "Content-type: text/html\n\n";
#header();

if ($Query->param('scenario') == 2) {

  $scenario = "2";

  Tableview("neurog1");

} else {

  $scenario = "1";

  Tableview("epha4b");
}



#Tableview("gene");
print "<p>";

 print "<table width=100%  border=0 cellspacing=0 cellpadding=3>";
 print "<tr>";
 print "<td colspan=7><div align=right><a href=\"fxViewAll.cgi\">All Expression for this figure</a></div></td>";
 print "</tr>";
 print "<tr>";
 print "<td colspan=7><div align=right><a href=\"probe-history\">Comments/History</a></div></td>";
 print "</tr>";
 print "</table>";


footer();
