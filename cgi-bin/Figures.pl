#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";

sub Figures {
  if ($_[0] eq "gene") {
    $data_file="fig4.dat";
  } else {
    $data_file="images.dat";
  }

  open(DAT, $data_file) || die("Could not open file!");
  @figures_raw_data=<DAT>;
  close(DAT);

  $mycolor=white;

  if ($_[0] eq "gene") {
    #pub();

  }

  foreach $img (@figures_raw_data) {
    chop($img);
    ($image,$caption)=split(/\t/,$img);
    print "<table align=\"center\" width=\"90%\" bgcolor=\"#EEEEEE\">";
    print "<tr><td valign=\"top\" align=\"center\" bgcolor=\"#FFFFFF\">";
    print "<a href=\"$image\"><img src=\"$image\" width=\"150\" valign=\"top\"></a>";
    print "View larger version<br><br>";
    print "<a href=\"$image\" target=\"newwindow\"><small>[in a new window]</small></a></td>";
    print "<td valign=\"top\"><small>$caption</small></td>";
    print "</tr>";
    print "</table> <br>";
  }

}
1;
