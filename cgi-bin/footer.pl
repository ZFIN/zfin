#!/usr/bin/perl

sub footer {
print <<ENDHTML ;
</td></tr></table>
<table width=100% class=\header\ cellpadding=0 cellspacing=0>
  <tr>
    <td class=footer>
      <table valign=center width=100%>
        <tr>
          <td align=center class=footer>
	   <DIV class=\header\>
            Home
	   </DIV>
          </td>
          <td align=center class=footer>
	   <DIV class=\header\>
	     Email ZFIN
	   </DIV>
          </td>
          <td align=center class=footer>
	   <DIV class=\header\>
            About ZFIN
	   </DIV>
          </td>
          <td align=center class=footer>
	   <DIV class=\header\>
            Helpful Hints
	   </DIV>
          </td>
          <td align=center class=footer>
	   <DIV class=\header\>
            Citing ZFIN
	   </DIV>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td align=center class=ack>
      <DIV class=ack>
      Copyright University of Oregon, 1994-2003, Eugene , Oregon.
      </DIV>
    </td>
  </tr>
  <tr>
    <td align=center class=ack>
      <DIV class=ack>
      <font size=-2>ZFIN logo design by Kari Pape, </font> <font size =-2>University of Oregon</font>
      </DIV>
    </td>
  </tr>
</table>
</body>
</html>
ENDHTML
}
1;
