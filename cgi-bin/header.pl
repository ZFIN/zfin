#!/usr/bin/perl
sub header {
 print <<ENDHTML ; 
<LINK rel=stylesheet type="text/css" href="/zfin_style.css">
   <BODY bgcolor=white leftmargin=0 topmargin=0 marginwidth=0 marginheight=0>
 <basefont size=2>
<table width=100% cellspacing=0 border=0 cellpadding=0 class=header>
  <tr>
     <td rowspan=2  bgcolor=#006666 width=80 align=left valign=bottom>
	     
	     <IMG src=\/images/zfinlogo.gif\ border=0>   
	   
     </td>
     <td>
	    <IMG src=\/images/zfintxt.gif\ border=0>
       </td>
	  <td align="center" valign="bottom" bgcolor="#FFFFFF">
	   <DIV class=\header\>
            <font color=\#006666\>Anatomy</font>&nbsp&nbsp
            <font color=\#006666\>Publications</font>&nbsp&nbsp
            <font color=\#006666\>People</font> &nbsp&nbsp
            <font color=\#006666\>Labs</font> &nbsp&nbsp
            <font color=\#006666\>Companies </font> &nbsp&nbsp
            <font color=\#006666\>Acc #</font>
	   </DIV>
          </td>
    </tr>
  <tr bgcolor=#006666>
    <td colspan=2 width=100%>
      <table width=100% border=0 height=20 cellspacing=0 cellpadding=0>
        <tr>
	  <td align=center>
	   <DIV class="header">
            Home
	   </DIV>
          </td>
	  <td align=center>
	   <DIV class=\header\>
            Mutants / Transgenics
	   </DIV>
          </td>
          <td align=center>
	   <DIV class=\header\>
            Wild-Types
	   </DIV>
          </td>
          <td align=center>
	   <DIV class=\header\>
            Genes / Markers / Clones
	   </DIV>
          </td>
          <td align=center>
	   <DIV class=\header\>
            <A HREF=fxQuery.cgi>Expression
	   </DIV>
          </td>
        
          <td  align=center>
	   <DIV class=\header\>
            Maps
	   </DIV>
          </td>
	 <!--  <td  align=center>
	   <DIV class=\header\>
            Acc #
	   </DIV>
          </td>
	 	<td  align=center>
	   <DIV class=\header\>
            Publications
	   </DIV>
          </td>
          <td  align=center>
	   <DIV class=\header\>
            People
	   </DIV>
          </td>
          <td  align=center>
	   <DIV class=\header\>
            Labs
	   </DIV>
          </td>
          <td  align=center>
	   <DIV class=\header\>
            Companies
	   </DIV>
          </td> -->
        </tr>
      </table>
    </td>
  </tr>
</table>
<table cellpadding=5 bgcolor=\"#FFFFFF\" width=100%><tr><td>
ENDHTML
}
1;
