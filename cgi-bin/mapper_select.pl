#!/private/bin/perl -wT
{
 use CGI;

 my $Q = new CGI();



 sub mapper_select() {

   my $JSCRIPT=<<ENDJS;


function call_mapplet() {

<!-- make sure at least one panel is specified -->

if ((document.mapplet.MGH.checked == 0)&&(document.mapplet.GAT.checked == 0)&&(document.mapplet.MOP.checked ==0)&&(document.mapplet.LN54.checked == 0)&&(document.mapplet.T51.checked == 0)&&(document.mapplet.HS.checked == 0)) {

	 alert ('Please check at least one panel');
  	 return false;
	}

<!-- if loc_panel is selected make sure panel variable is checked -->
if (document.mapplet.loc_panel.selectedIndex !=0) {
	if (document.mapplet.loc_panel.selectedIndex == 1) document.mapplet.LN54.checked=true;
	else if (document.mapplet.loc_panel.selectedIndex ==2) document.mapplet.T51.checked = true;
	else if (document.mapplet.loc_panel.selectedIndex ==3) document.mapplet.HS.checked = true;
	else if (document.mapplet.loc_panel.selectedIndex ==4) document.mapplet.MGH.checked = true;
	else if (document.mapplet.loc_panel.selectedIndex ==5) document.mapplet.MOP.checked = true;
	else if (document.mapplet.loc_panel.selectedIndex ==6) document.mapplet.GAT.checked = true;
}

<!-- if marker and only marker is specified submit -->

if (document.mapplet.marker.value !== ''){
   if ((document.mapplet.loc.value== '')&&(document.mapplet.loc_panel.selectedIndex==0)&&(document.mapplet.loc_lg.selectedIndex==0)) {
	document.mapplet.submit();
    return true;
	}
  else  {
	alert(" Please specify either marker OR specify location, LG, and panel. ");
	return false;
	}
}


if ((document.mapplet.loc != null) && (document.mapplet.loc.value.search(/[^0-9\.]/) != -1 )) {
	alert('Locations must be specified as numeric values without units.');
	return false;
}


<!-- if marker is not specified and at least LG is - submit -->

if (document.mapplet.marker.value == '') {
	if (document.mapplet.loc_lg.selectedIndex !==0) {
		if ((document.mapplet.loc_panel.selectedIndex != 0) && (document.mapplet.loc.value != '')) {
			document.mapplet.submit();
		} else if ((document.mapplet.loc_panel.selectedIndex == 0) && (document.mapplet.loc.value == '')) {
			alert(' Please specify a location and panel for the selected linkage group '); return false;

		} else if ((document.mapplet.loc_panel.selectedIndex != 0) && (document.mapplet.loc.value == '')){
			alert(' A panel but no location was specified.\\n' + ' Please include a location for this panel.');return false;
		} else if ((document.mapplet.loc_panel.selectedIndex == 0) && (document.mapplet.loc.value != '')){
			alert(' A location but no panel was specified.\\n' + ' Please select a panel for this location. ');return false;
		}
	}
 else {
	alert(' Please specify either marker OR specify location, LG, and panel. ');
	return false;
	}
}

}

function call_zmapplet() {

   document.mapplet.action= '/cgi-bin/view_zmapplet.cgi';
    if (  ((document.mapplet.loc_lg.selectedIndex !==0) || (document.mapplet.loc.value != '') )  && (document.mapplet.loc_panel.selectedIndex == 0)){
    document.mapplet.loc_panel.selectedIndex = 4;
    }
    call_mapplet();

}

function call_reset() {

document.mapplet.marker.value = "";
document.mapplet.loc.value = "";
document.mapplet.loc_lg.selectedIndex = 0;
document.mapplet.loc_panel.selectedIndex = 0;
document.mapplet.MGH.checked = true;
document.mapplet.GAT.checked = true;
document.mapplet.HS.checked = true;
document.mapplet.MOP.checked = true;
document.mapplet.T51.checked = true;
document.mapplet.LN54.checked = true;

}



function start_help(anchor) {
   top.zfinhelp=open("/cgi-bin/webdriver?MIval=aa-helpframes.html&calling_page=mapperselecthelp.html&anchor="+anchor,"helpwindow","scrollbars=yes,toolbar=no,directories=no,menubar=no,status=no,resizable=yes,width=400,height=300");
}

var MGH=0;
var GAT=0;
var HS=0;
var MOP=0;
var T51=0;
var LN54=0;

ENDJS

  print "\n<SCRIPT>\n $JSCRIPT \n</SCRIPT>\n";

  print "\n<SCRIPT>\n window.name='mapper' \n</SCRIPT>\n";
  print "\n";

  print "<TABLE width=700 border=0><TR><TD>\n";

  print $Q->start_form(-name=>'mapplet', -action=>'/cgi-bin/view_mapplet.cgi',-method=>'post',-onSubmit=>'return call_mapplet();');
   print "\n";

   print "<input type=hidden name='view_map' value=1>";
   print "\n";

   print "<TABLE WIDTH=100%><TR><TD bgcolor=\"<!--|SIDEBAR_COLOR|-->\">\n";
   print "<FONT SIZE=4><b>View a map region, choose either a marker or location</b></font>";
   print "\n";
   print "</TD></TR></TABLE>";


   print "<TABLE width=100% border=0 cellspacing=0 cellpadding=4>";
   print "\n";

  print "<TR><TD><font size=2><A HREF=\"javascript:start_help('marker')\"><b>Marker Symbol:</font> </b></A>";
  print "\n";

   print $Q->textfield(-name=>'marker',-size=>'12',-onChange=>'document.mapplet.loc.value = \'\'; document.mapplet.loc_lg.selectedIndex = 0; document.mapplet.loc_panel.selectedIndex = 0;');
   print "\n";

   print "</TD><TD><b> <font size=2>OR</font> &nbsp;&nbsp;&nbsp; </b></TD>";
   print "\n";

#convert this: <?MIBLOCK COND="$(XST,$marker)"> <SCRIPT><?MIVAR> document.mapplet.marker.value = "$marker"<?/MIVAR> </SCRIPT> <?/MIBLOCK>  //to perl.. ouch
#converted to:
   if ($Q->param('marker')) {
     print "<SCRIPT>document.mapplet.marker.value = '" . $Q->param('marker') . "'; </SCRIPT>";
   }
   print "<TD colspan=2><font size=2><A HREF=\"javascript:start_help('position')\"><b>Position:</font> </b></A>";
   print "\n";

   print $Q->textfield(-name=>'loc',-size=>'5',-onChange=>'document.mapplet.marker.value=\'\';');
   print "\n";

   print "<font size=2><A HREF=\"javascript:start_help('lg')\"><b> LG:</font></B></A>";
   print "\n";

   print $Q->popup_menu(-name=>'loc_lg',-onChange=>'document.mapplet.marker.value=\'\';',-values=>[qw/Any 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25/], -labels=>('Any'=>'0'),-default=>'Any');


   print "<br>";
   print "\n";

   print "<font size=2><A HREF=\"javascript:start_help('panel')\"><B>Panel:</B></A></font>";
   print "\n";

   print $Q->popup_menu(-name=>'loc_panel',-onChange=>'document.mapplet.marker.value=\'\';',-values=>['???','LN54','T51','HS','MGH','MOP','GAT'],-labels=>('???'=>'0'),-default=>'???');


  print "</TD></TR></TABLE>\n \
         <hr size=1 width = 60%>\n \
         <TABLE width=100% border=0 cellspacing=0 cellpadding=0>\n \
         <TR> <TD align=center><font size=2> \n \
         <A HREF=\"javascript:start_help('search')\"><B>Panels to view:</B></A>\n";

 print $Q->checkbox(-name=>'LN54',-value=>'1',-label=>'LN54',-checked=>'checked');
 print $Q->checkbox(-name=>'T51',-value=>'1',-label=>'T51',-checked=>'checked');
 print $Q->checkbox(-name=>'HS',-value=>'1',-label=>'HS',-checked=>'checked');
 print $Q->checkbox(-name=>'MGH',-value=>'1',-label=>'MGH',-checked=>'checked');
 print $Q->checkbox(-name=>'MOP',-value=>'1',-label=>'MOP',-checked=>'checked');
 print $Q->checkbox(-name=>'GAT',-value=>'1',-label=>'GAT',-checked=>'checked');

 print "<A HREF=\"/cgi-bin/webdriver?MIval=aa-refcrosslist.apg\" style=\"margin-left:4em;\"><B>Mapping panels summary</B></A>";
 print "\n</font></TD></TR></TABLE>\n";

 print $Q->hidden(-name=>'refresh_map',-value=>'1');
 print "\n";


 print "<TABLE WIDTH=100%><TR><TD colspan=2 align=right bgcolor=\"<!--|SIDEBAR_COLOR|-->\">\n";
 print $Q->button(-name=>'view_map',-value=>'VIEW INDIVIDUAL MAPS',-onclick=>'call_mapplet();');
 print "&nbsp;&nbsp;\n";
 print $Q->button(-name=>'view_zmap',-value=>'VIEW MERGED MAP',-onclick=>'call_zmapplet();');
 print "&nbsp;&nbsp;&nbsp;&nbsp;\n";

 print $Q->button(-name=>'reset',-value=>'Reset',-onclick=>'call_reset();');
 print "\n";

 print "</TD></TR>";
 print "<TR> <TD align=left>NOTE: Large maps may take a few moments to load.  </TD>  \
<TD align=right> * indicates markers that map to different linkage groups on different panels.  </TD> \
</TR></TABLE>";
 print "</form>";
 print "</TD></TR></TABLE>";

}

}

