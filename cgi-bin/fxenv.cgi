#!/usr/bin/perl
require "header.pl";
require "footer.pl";



print "Content-type: text/html\n\n";
print "<html>\n\n";
header();


print <<ENDHTML;
<table cellpadding=5 width=100%>
	<tr>
	  <td><table width="100%" cellpadding="0" cellspacing="0">
        <tr>
          <td width="20%"></td>
          <td align="left" width="60%"><div align="center"><strong>Morpholinos</strong><br></td>
        </tr>
      </table>	    <br>
	    <table width="100%"  border="0" cellspacing="0" cellpadding="3">
        <tr>
          <td><strong>Gene</strong></td>
          <td><strong>Morpholino</strong></td>
          <td><strong>Sequence</strong></td>
          <td><strong>Details</strong></td>
          </tr>
        <tr bgcolor="#EEEEEE">
          <td><em><u>gene</u></em></td>
          <td> MO </td>
          <td>5'-ttg ttc ctt gag aac tca gcg cca t-3'</td>
          <td>MOs were microinjected into zebrafish embryos at the one- to two-cell stage in a volume of ~2 nl, at a final concentration of 0.3 mM in water</td>
          </tr>
      </table></td>
	</tr></table>

ENDHTML
footer();

