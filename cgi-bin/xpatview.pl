#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";
require "Figures.pl";

sub xpatview {
    $data_file="pubs.dat";


  open(DAT, $data_file) || die("Could not open file!");
  @xpatview_raw_data=<DAT>;
  close(DAT);

  $mycolor=white;


      print "<strong>Expression of :<em><u>neurogenin1</em></u></strong><p>";
      print "<strong>Gene:<em><u>neurog1</em></u></strong><p>";
      print "<table width=100%  border=0 cellspacing=0 cellpadding=3>";
      print "<tr>";
      print "<td><strong>Publication</strong></td>";
      print "<td><strong>Fish</strong></td>";
      print "<td><strong>Assay</strong></td>";
      print "<td><strong>Expression Summary</strong></td>";
      print "</tr>";

  foreach $xpatpub (@xpatview_raw_data) {
    chop($xpatpub);
    ($pub,$fish,$assay,$details)=split(/\t/,$xpatpub);
    if ($mycolor eq "white") {
      $mycolor=EEEEEE;
    } else {
      $mycolor=white;
    }


      print "<tr bgcolor=$mycolor><td><u>$pub</u></td>";
      print "<td><u>$fish</u></td>";
      print "<td>$assay</td>";
      print "<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>$details</u></td>";

    }

}
1;
