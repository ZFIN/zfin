#!/private/bin/perl 
{
 use CGI;
 

 $yellow = "#FFFFDD";
 $pink = "#FFEEEE"; 

 $query = new CGI();
 print $query->header();
 
 $JSCRIPT=<<ENDJS;

function do_reload() {
  document.forms[0].action="/cgi-bin_B/map-options.pl";
  document.forms[0].target="";
  document.forms[0].submit();
}

function edit(panel) {
  document.options.edit_panel=panel; 
  document.options.target=""; 
  document.options.action="/cgi-bin_B/map-options.pl"; 
  document.options.submit();
}

function call_mapplet() {
	 <!-- make sure the checkbox to turn the panel on is selected -->
	 i = 0;
	 while ((i < document.options.elements.length) && (document.options.elements[i].type!="checkbox")) {
	       i++;
	 }
	 allclear = true;
	 if (document.options.elements[i].checked == true) {  <!-- the panel is turned on -->

		 if (document.options.elements[i+1].value=="Marker") { <!-- by marker -->
		    if (document.options.elements[i+2].value == "")  { <!-- this could happen if they fill in a between without specifying the marker -->
		       alert("To search by marker, please specify a marker name.");
		       allclear = false;
		    }
		    if (  ((document.options.elements[i+3].value != "") && (document.options.elements[i+4].value == "")) ||   ((document.options.elements[i+4].value != "") && (document.options.elements[i+3].value == "")) ) {
		       alert("Please specify both high and low values.");
		       allclear = false;
		    }
		   if ( (document.options.elements[i+3].value.search(/[^0-9\.]/) != -1 ) || ( document.options.elements[i+4].value.search(/[^0-9\.]/) != -1 )) {
		      alert('Locations must be specified as numeric values without units.');
		      allclear = false;
		   }
		   
		 } else {  <!-- by location -->
		   if (document.options.elements[i+5].selectedIndex == 0) {
		      alert("Please specify a linkage group.");
		      allclear = false;
		   }
		   if (  ((document.options.elements[i+8].value != "") && (document.options.elements[i+9].value == "")) ||   ((document.options.elements[i+9].value != "") && (document.options.elements[i+8].value == "")) ) {
		       alert("Please specify both high and low values.");
		       allclear = false;
		    } else if ((document.options.elements[i+7].value == "") && (document.options.elements[i+8].value == "") && (document.options.elements[i+9].value == "")) {
		       alert("Please specify a location on the selected linkage group.");
		       allclear = false;
		    }

		    
			
		   if ( ( document.options.elements[i+7].value.search(/[^0-9\.]/) != -1 ) || (document.options.elements[i+8].value.search(/[^0-9\.]/) != -1 ) || ( document.options.elements[i+9].value.search(/[^0-9\.]/) != -1 )) {
		      alert('Locations must be specified as numeric values without units.');
		      allclear = false;
		   }
		   
		 }

	 }

	 if (allclear == true) {
	    document.options.submit();
	 }
}

ENDJS

 $CSS=<<ENDCSS;
P   {  text-indent: 1em; }
ENDCSS
 


 print $query->start_html(-title=>'Map Options',-BGCOLOR=>'#FFFFFF',-script=>$JSCRIPT,-style=>$CSS);
 
 
 
 print $query->startform(-method=>'get',-action=>'/cgi-bin_B/view_mapplet.cgi',-name=>'options',-target=>'pbrowser');

# $query->hidden(-name=>'edit_panel');
 if ($query->param('edit_panel') ne "") {
   $edit = $query->param('edit_panel');
 }
 
 for ($query->param) {

     if (($edit ne "") && !($_ =~ /$edit/)) { #if we're editing a panel, we don't want to print hiddens for it
       $buf = $buf . $query->hidden($_ ) ."\n";
     } elsif ($edit eq "") {
       $buf = $buf . $query->hidden($_ ) ."\n";
     }

 }
 print $buf;

 $i = 1;
 while ($query->param(panel .$i) ne "") {
   $panel = $query->param('panel'.$i);
   if ($query->param($panel) eq "") {
     print $query->hidden(-name=>$panel,-value=>'0');
   }
   $i++;
 }

 print "\n<center>";
# print '<P align=right>' . $query->submit(-name=>'refresh_map',-value=>'View Map') . '</P>';
 
 if ($query->param('edit_panel') ne "") {
   
  for ($query->param('edit_panel')) {
   $panel =  $_;
#   print $query->hidden(-name=>'panel'.$i);
#   $panel = $query->param('panel'.$i);  

   print "<table name=" . $panel . "_table cellspacing=0 cellpadding=1 width = 100% border = 0 bgcolor=#FFFFFF> <tr><td bgcolor=#DDDDDD>\n";
   
   print $query->hidden(-name=>$panel.'_units');
   $units = $query->param($panel.'_units');
   
   print $query->checkbox(-name=>$panel,-value=>'1',-label=>'',-onClick=>'')  . 'Show ' . $query->b($panel) . ' panel';

#   if ($query->param($panel) ne '1') {
#     $notes = "Click checkbox to edit parameters for " . $panel; 
#   } else {
     $notes = "Define map region by either marker or location";
#   }

   print "</td><td bgcolor=#DDDDDD> <P> <i><font color=red>" .  $notes  .  "</font></i></P></td><tr><td>";

   
 #  if ($query->param($panel) eq '1') {

     #both of the radio buttons are created at the same time, and I get an array back, so I can place them individually

#       @OR = $query->radio_group(-name=>$panel.'_or',-value=>['Marker','Location'],-nolabels=>1,-default=>'Location'); 
     print $query->hidden(-name=>$panel.'_or',-default=>'Location');

#     print $OR[0] . "Marker: ";
     print "Marker: ";

     $query->hidden(-name=>'marker');
     $marker = $query->param('marker');
     
     $onchange = "document.options." . $panel . ".checked=true;";
#     $marker_onchange= $onchange . "document.options." . $panel . "_or[0].checked=true;";
   $jsform = "document.options." . $panel;
   $marker_onchange= $onchange . $jsform . "_or.value=\'Marker\'; " . $jsform . "_lg.selectedIndex = 0; " . $jsform . "_near_loc.value=\'\'; " . $jsform . "_lg_lo.value=\'\'; " . $jsform . "_lg_hi.value=\'\'; ";

     print $query->textfield(-name=>$panel.'_m',-size=>'10',-default=>$marker,-onChange=>$marker_onchange); #selected_marker
     print "&nbsp;<font size=-1 color=red>and</font>";
     print "</td><td>";
     
     print 'within ' . $query->textfield(-name=>$panel.'_m_lo',-size=>'5',-onChange=>$marker_onchange) . ' ' . $units . ' above and ' . $query->textfield(-name=>$panel.'_m_hi',-size=>'5',-onChange=>$marker_onchange) . ' ' . $units . ' below marker';
     
     print "</td> </tr>"; 
#     print "<tr><td colspan=2 bgcolor=#CCCCCC><P><font color=red size=-1>Query by either Marker OR Location</font></td></tr>";
     print " <tr><td bgcolor=#EEEEEE>";
     
#     print $OR[1] . 'Location: ';
   print '<font size=-1 color=red>or</font><br> Location: ';

     $loc_onchange = $onchange . $jsform . "_or.value=\'Location\'; " . $jsform . "_m.value=\'\'; " . $jsform . "_m_lo.value=\'\'; " . $jsform . "_m_hi.value=\'\'; ";
     
     $lg = ' LG ' . $query->popup_menu(-name=>$panel.'_lg',-values=>[qw/?? 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25/], -default=>'?',-onChange=>$loc_onchange);
   
     print $lg;
     print "&nbsp;<font size=-1 color=red>and</font>";

     print "<p align=top><br></p></td><td bgcolor=#EEEEEE>";
     
     
     
#     @LG_OR = $query->radio_group(-name=>$panel.'_lg_or', -value=>['near', 'units'],-nolabels=>1, -default=>'units');
      print $query->hidden(-name=>$panel.'_lg_or',-default=>'units');

     $NLlg = ' LG ' . $query->popup_menu(-name=>$panel.'_lg',-values=>[qw/?? 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25/], -default=>'??',-onChange=>$loc_onchange);
     $NL_onchange = $loc_onchange . $jsform . "_lg_or.value=\'near\'; " . $jsform . "_lg_hi.value=\'\'; " . $jsform . "_lg_lo.value=\'\'; ";
     print $LG_OR[0] . 'near location: ' . $query->textfield(-name=>$panel.'_near_loc',-size=>'5',-onChange=>$NL_onchange) . ' ' . $units . '<br>'; 

     $BL_onchange = $loc_onchange . $jsform . "_lg_or.value=\'units\'; " . $jsform . "_near_loc.value=\'\'; ";
     $BLlg = ' LG ' . $query->popup_menu(-name=>$panel.'_lg',-values=>[qw/?? 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25/], -default=>'??',-onChange=>$BL_onchange);
     print $LG_OR[1] . ' &nbsp;&nbsp;&nbsp;&nbsp;  &nbsp;&nbsp;&nbsp;&nbsp;  <font size=-1 color=red>or</font><br> between locations: ' . $query->textfield(-name=>$panel.'_lg_lo',-size=>'5',-onChange=>$BL_onchange) . ' ' . $units . ' and ' . $query->textfield(-name=>$panel.'_lg_hi',-size=>'5',-onChange=>$BL_onchange) . ' ' . $units . '<br>';

#     $BM_onchange = $loc_onchange . "document.options." . $panel . "_lg_or[1].checked=true;";
#     print $LG_OR[1] . 'between markers: ' . $query->textfield(-name=>$panel.'_lg_m_lo',-size=>'10' ,-onChange=>$BM_onchange) . ' and ' . $query->textfield(-name=>$panel.'_lg_m_hi',-size=>'10',-onChange=>$BM_onchange) . '<br>';

     print "</td></tr></table>
<hr width=60% size=1>
<table cellspacing=0 cellpadding=1 width = 100% border = 0 bgcolor=#FFFFFF> 
<tr><td> Show Marker types: </td><td>";
     
     print $query->checkbox(-name=>$panel.'_gene',-checked=>'checked',-value=>'1',-label=>'Gene',-onClick=>$onchange) . ' ';
     print $query->checkbox(-name=>$panel.'_fish',-checked=>'checked',-value=>'1',-label=>'Mutant',-onClick=>$onchange) . ' '; 
     print $query->checkbox(-name=>$panel.'_est',-checked=>'checked',-value=>'1',-label=>'EST',-onClick=>$onchange) . ' ';
#     print $query->checkbox(-name=>$panel.'_anon',-checked=>'checked',-value=>'1',-label=>'Anonymous',-onClick=>$onchange) . ' ';
     print $query->hidden(-name=>$panel.'_anon',-value=>'1');

     print $query->button(-name=>'refresh_map',-value=>'Submit Changes', -onClick=>'call_mapplet();') . ' ';
     print $query->button(-name=>'cancel', -value=>'Cancel', -onClick=>'parent.pbrowser.document.selectform.submit();');
#"self.location='http://zfin.org/cgi-bin_B/webdriver?MIval=aa-mapperselect.apg';") . ' ';

   #}
   print "</td></tr></table><br>";
      #print $query->submit(-name=>'refresh_map',-value=>'View Map');
    }
 
  } else {
#   print $query->hidden(-name=>"edit_panel", -value=>"");
    print "<table border = 0><tr>";
    for ($i = 1 ; $query->param('panel'.$i) ne "" ; $i++) {
	 $panel = $query->param('panel'.$i); 
	 print "<td>";
         #print $query->checkbox(-name=>$panel,-value=>'1',-label=>'',-onClick=>'') . $panel . ' '; 
	 if ($query->param($panel) eq '1') {
	   print $query->button(-name=>$panel . '_vis',-value=>'Hide ' . $panel, -onClick=>"document.options." . $panel . ".value = 0; document.options.submit();") . "\n";
	 } else {
   	   print $query->button(-name=>$panel . '_vis',-value=>'Show ' . $panel, -onClick=>"document.options.edit_panel.value='" . $panel . "'; document.options." . $panel . ".value = 1; document.options.submit();") . "\n";
	 }
	 print "<br>";
	 print $query->button(-name=>$panel . '_opts', -value=>'Adjust ' . $panel, -onClick=>"document.options.edit_panel.value='" . $panel . "'; document.options.target=''; document.options.action='/cgi-bin_B/map-options.pl'; document.options.submit();   ");
	 print "</td>";
       }
    print "</tr></table>";
    print "<br><br></center>" . $query->button(-name=>'back',-value=>'Return to search form.',-onClick=>'parent.pbrowser.document.selectform.submit();');
  }

 print '<P align=right>' . '</P>';
 
 $query->delete(-name=>'.cgifields');
# print $query->button(-name=>'test',value=>'test',-onClick=>'call_mapplet();'); 
 print '</form>';
# print $query->endform();

 print $query->end_html;


}
