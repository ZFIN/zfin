#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";
require "Figures.pl";

sub Tableview {
  if ($_[0] eq "epha4b") {
    $data_file="epha4b.dat";
  } elsif ($_[0] eq "neurog1") {
    $data_file="neurog1.dat";
  } else {
    $data_file="results.dat";
  }

  if ($_[1] eq "2") {
    $scenario = "2";
  } else {
    $scenario = "1";
  }

  open(DAT, $data_file) || die("Could not open file!");
  @tableview_raw_data=<DAT>;
  close(DAT);

  $mycolor=white;

  if ($_[0] eq "epha4b") {
    pub();
  }
  if ($_[0] eq "neurog1") {
    pub();
  }
  my $genesym='';
  print <<ENDHTML ;
ENDHTML

Figures("gene");

  foreach $expression (@tableview_raw_data) {
    chop($expression);
    ($gene,$fish,$structure,$stage,$assay,$xpression,$figure,$condn)=split(/\t/,$expression);
    if ($mycolor eq "white") {
      $mycolor=EEEEEE;
    } else {
      $mycolor=white;
    }

    if ($structure eq "spinal cord" && $scenario eq "2") {
       $structure = "<u style=\"background-color:#ffff99;\">" . $structure . "</font>";
    }

    if ($gene eq $genesym) {
      print "<tr>";
      print " <tr bgcolor=$mycolor><td><u>$fish</u></td>";
      print "<td><u>$structure</u></td>";
      print "<td>$stage</td>";
      print "<td>$assay</td>";
      print "<td>$xpression</td>";
      print "<td>$figure</td>";
      print "<td>$condn</td>";
    } else {
      if ($genesym ne '') {
	print "</table>";
        print "<p>";
      }
      print "<strong>Gene:<em><u>$gene</em></u></strong><p>";
      print "<table width=100%  border=0 cellspacing=0 cellpadding=3>";
      print "<tr>";
      print "<td><strong>Fishes</strong></td>";
      print "<td><strong>Structures</strong></td>";
      print "<td><strong>Stage</strong></td>";
      print "<td><strong>Assay</strong></td>";
      print "<td><strong>Expr</strong></td>";
      print "<td><strong>Fig</strong></td>";
      print "<td><strong>Conditions</strong></td>";
      print "</tr>";
      print "<tr bgcolor=$mycolor><td><u>$fish</u></td>";
      print "<td><u>$structure</u></td>";
      print "<td>$stage</td>";
      print "<td>$assay</td>";
      print "<td>$xpression</td>";
      print "<td>$figure</td>";
      print "<td>$condn</td>";

      $genesym=$gene;
    }
    #print "</BODY></HTML>";
  }

}
1;
