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
          <td align="middle" width="60%"><div align="center"><strong>Expression Pattern Search Results </strong><br>
        ( <strong>1 </strong>matching record(s) found.) </div></td>
         
        </tr>
      </table>	    <br>
	    <table width="100%"  border="0" cellspacing="0" cellpadding="3">
        <tr>
          <td><strong>Gene Symbol-name</strong></td>
          <td><strong>Expression</strong></td>
          <td><strong>Publication(s)</strong></td>
          </tr>
        <tr bgcolor="#EEEEEE">
          <td><em>epha4b</em></td>
          <td>Published Figures <a href="fxResultsSummary.cgi"> (12 images)</a> </td>
          <td>(1)</td>
          </tr>
      </table></td>
	</tr></table>

ENDHTML
footer();

