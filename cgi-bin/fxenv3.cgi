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
        
	  Gene Name:  <i>neurog1</i><br>
	  Gene Symbol: <u>neurogenin1 </u>

	
        </b></font>	
    </td>
  </tr>
</table>
<p>
<table border=0 width=100% bgcolor=#EEEEEE>
	  <tr>
	    <td><em>Other Conditions : sound</em></td>
          </tr>

	  <tr>
	    <td>Best of the Bee-Gees, continuous play, 100 db
          </tr>
        </table> 
        <p>

ENDHTML
footer();

