#!/usr/bin/perl
require "header.pl";
require "footer.pl";



print "Content-type: text/html\n\n";
print "<html>\n\n";
header();


print <<ENDHTML;
<TABLE width=100%>
   <TR bgcolor=#cccccc>
     <TD>
          <FONT SIZE=+1><b>Enviroments Details</b></FONT>

     </TD>
   </TR>
</TABLE>
<table border=0 width=100%>
  <tr>
    <td width=85%><font><b>
        
	  Gene Name:  <i>epha4b</i><br>
	  Gene Symbol: <a href="/cgi-bin_edison/webdriver?MIval=aa-markerview.apg&OID=ZDB-GENE-990415-8"><i>epha4b</i></a>

	
        </b></font>	
    </td>
  </tr>
</table>
<p>
<table border=0 width=100% bgcolor=#EEEEEE>
	  <tr>
	    <td><em>Standard Conditions</em></td>
          </tr>

	  <tr>
	    <td>The environment does not depart significantly from standard conditions for zebrafish husbandry detailed in the <a href="http://zfin.org/zf_info/zfbook/zfbk.html"> Zebrafish Book </a> : Temp 28.5 deg C light 14h, dark 10h.
          </tr>
        </table> 
        <p>

ENDHTML
footer();

