#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";

sub Figures {
if ($_[0] eq "gene") {
$data_file="fig4.dat";
}
else {
$data_file="images.dat";
}

open(DAT, $data_file) || die("Could not open file!");
@raw_data=<DAT>;
close(DAT);

$mycolor=white;
print "Content-type: text/html\n\n";
#print "<HTML><BODY>";
header();
if ($_[0] eq "gene") {
#pub();

}
print "<table>";
foreach $img (@raw_data)
{
 chop($img);
 ($image,$caption)=split(/\t/,$img);
 print "<tr><td><img src=$image width =200 height = 200></td>";
 print "<td>$caption</td>";
# print "$caption";
#        print "<hr width=80%>";
  print "<tr><tr>";
  print "<p>";
}
print "</table>";
}
1;
