#!/usr/bin/perl
sub pub {
if ($_[0] ne "all") {
 print <<ENDHTML ; 
<table width=100%>
                <tr>
          <td align=left colspan=2> <strong> Cunliffe, V.T. (2004) <br>
            </strong>
Histone deacetylase 1 is required to repress Notch target gene expression during zebrafish neurogenesis and t
o maintain the production of motoneurones in response to hedgehog signalling
             Development 131(12):2983-2995. ( <a href="fxFigures.cgi">All Figures for this publication</a> ) </td>
            </tr>
                <tr>
          <td align=left colspan=2> <br>
          <b><u>ABSTRACT AND ADDITIONAL INFORMATION</u></b>
          
            </tr>
                <tr>
          <td align=left colspan=2> <strong><br>
          Figures returned by search: </strong><a href="fxViewAll.cgi">  All expression for this figure</a></td>
          
            </tr>
        </table>
ENDHTML
}
else {

 print <<ENDHTML ; 
<table width=100%>
                <tr>
          <td align=left colspan=2> <strong> Cunliffe, V.T. (2004) <br>
            </strong>
Histone deacetylase 1 is required to repress Notch target gene expression during zebrafish neurogenesis and t
o maintain the production of motoneurones in response to hedgehog signalling
             Development 131(12):2983-2995.
            </tr>
                <tr>
          <td align=left colspan=2>
          
          
            </tr>
        </table>
<p>
<b><u>ABSTRACT AND ADDITIONAL INFORMATION</u></b>

<p><p>
ENDHTML
}
}
1;
