#!/usr/bin/perl
require "header.pl";
require "footer.pl";
require "pub.pl";
require "Figures.pl";
sub Tableview {
if ($_[0] eq "gene") {
$data_file="epha4b.dat";
}
else {
$data_file="results.dat";
}

open(DAT, $data_file) || die("Could not open file!");
@raw_data=<DAT>;
close(DAT);

$mycolor=white;
print "Content-type: text/html\n\n";
#print "<HTML><BODY>";
header();
if ($_[0] eq "gene") {
  pub();
}
my $genesym='';
print <<ENDHTML ;
ENDHTML
#Figures("gene");
print "<img src=/images/Fig-4.gif width=200 height=200></a> <br>";
print "<p>";
print "Fig. 4. Mutation of hdac1 does not perturb primary neural patterning but neurogenesis is severely impaired. In situ hybridisation for expression of CNS patterning markers and proneural genes in hdac1 hi1618 (A,C,E,G) sibling and (B,D,F,H) mutant embryos. (A,B) pax2a at 24 hpf marks the optic chiasm, midbrain-hindbrain boundary, otic vesicle and scattered spinal interneurones; (C,D) epha4 at 32 hpf marks the forebrain and hindbrain rhombomeres 1, 3 and 5; (E,F) ash1b and (G,H) ngn1 at 25 hpf mark neuronal precursors in brain and spinal cord. Expression of pax2a and epha4 in the embryonic CNS is unperturbed by the hdac1 hi1618 mutation, whereas expression of the proneural genes ash1b and ngn1 is substantially reduced in hdac1 hi1618 mutant embryos";
print "<p>";
 
foreach $expression (@raw_data)
{
 chop($expression);
 ($gene,$fish,$structure,$stage,$assay,$xpression,$figure,$condn)=split(/\t/,$expression);
if ($mycolor eq "white") {
     $mycolor=EEEEEE;
   }
 else {
   $mycolor=white;
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
}
else {
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
