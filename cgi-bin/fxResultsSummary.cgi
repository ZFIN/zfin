#!/usr/bin/perl
require "header.pl";
require "footer.pl";
use CGI;

my $Query = new CGI();

if ($Query->param('scenario') == 2) {

  $scenario = "2";
  $match = 2;

} else {

  $scenario = "1";
  $match=1;

}


print "Content-type: text/html\n\n";
print "<html>\n\n";
header();

if ($Query->param('scenario') eq "2") {

  $scenario = "2";
  $gene = "<u>neurog1</u>";
  $structures = "<u>spinal cord</u>";

} else {

  $scenario = "1";
  $gene = "<u>epha4b</u>";
  $structures = "<u>forebrain</u>, <u>rhombomere1</u>, <u>rhombomere3</u>, <u>rhombomere 5</u>";

}



print <<ENDHTML;
<table cellpadding=5 width=100%>
	<tr>
	  <td><table width="100%" cellpadding="0" cellspacing="0">
        <tr>
          <td width="20%"></td>
          <td align="middle" width="60%"><div align="center"><strong>Expression Pattern Search Results </strong><br>
        ( <strong>$match </strong>matching record(s) found.) </div></td>
         
        </tr>
      </table>	    <br>
	    <table width="100%"  border="0" cellspacing="0" cellpadding="3">
        <tr>
          <td><strong>Gene Symbol-name</strong></td>
          <td><strong>Fish</strong></td>
          <td><strong>Structures</strong></td>
          <td><strong>Publication(s)</strong></td>
          <td><strong>Date</strong></td>
          <td><strong>Results</strong></td>
          </tr>
        <tr bgcolor="#EEEEEE">
          <td><em>$gene</em></td>
          <td><u>WT</u>, <u>hdac1<sup> hi1618</sup></u> </td>
          <td>$structures</td>
          <td><a href="http://edison.zfin.org/cgi-bin_edison/webdriver?MIval=aa-pubvie
w2.apg&OID=ZDB-PUB-040601-1">Cunliffe, V.T.</a> </td>
          <td>(2004)</td>
         <td><a href="fxView.cgi?scenario=$scenario">(1 Figure)</a> </td>
          </tr>
        <tr bgcolor="white">
          <td><em>$gene</em></td>
          <td> </td>
          <td></td>
          <td><a href="http://edison.zfin.org/cgi-bin_edison/webdriver?MIval=aa-pubvie
w2.apg&OID=ZDB-PUB-010810-1">Thisse <em>et al</em></a> </td>
          <td>(2001)</td>
         <td><a href="http://edison.zfin.org/cgi-bin_edison/webdriver?MIval=aa-xpatview.apg&OID=ZDB-XPAT-020809-13">(1 Figure)</a> </td>
          </tr>
      </table></td>
	</tr></table>

ENDHTML
footer();

