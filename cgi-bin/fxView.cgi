#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";
require "Tableview.pl";
require "Figures.pl";
#print "Content-type: text/html\n\n";
#header();
#Figures("gene");
Tableview("gene");
#Figures("gene");
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
