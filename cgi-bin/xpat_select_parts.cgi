#!/private/bin/perl -T
{
 use CGI;
 use DBI;

 ### the hard coded env paths need a better idea
 $ENV{INFORMIXDIR}      = '<!--|INFORMIX_DIR|-->';
 $ENV{INFORMIXSERVER}   = '<!--|INFORMIX_SERVER|-->';
 $ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->';
 ### open a handle on the db
 my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
 || die "Failed while connecting to <!--|DB_NAME|--> "; #$DBI::errstr";

 my $form_elements = 0; 
 my $first_with_data_is_open = false;

 $CSS=<<ENDCSS;
A  {  text-decoration: none; 
      font-family: "Right Hand of Colin Bold",sans-serif;
      font-size: 10pt;
      font-weight: bold;
      line-height: 120%; }
EM {  font-family: "Right Hand of Colin Bold",sans-serif;
      font-size: 12pt;
      font-style: plain;
      font-weight: bold;
      line-height: 120%;}
B {   font-family: "Right Hand of Colin Bold",sans-serif;
      font-size: 10pt;
      font-style: plain;
      font-weight: bold;
      line-height: 100%;}

ENDCSS

 $JS=<<ENDJS;


 var tabs = new Array();

 function popup_url(url) {
    open(url,'Description','toolbar=yes,scrollbars=yes,resizable=yes');
 }

 function check(part) {

//   for(i = 0 ; i < document.partselect.elements.length ; i++ ) {
//    if (document.partselect.elements[i].name == part) {
//	 box = document.partselect.elements[i];
//	 break;
//       }
//   }

   box = document.partselect.elements[part];
   i = part;
   if (box.checked == true) { 
     self.opener.add_part(document.partselect.elements[part].name);
   } else {
     self.opener.drop_part(document.partselect.elements[part].name);
   }
   t = 99;
   parent_tab = tabs[i];
   i++;
   while ((t > parent_tab) && (i < document.partselect.elements.length)) {
     if (document.partselect.elements[i] == null) {
         break;
     }

     childbox = document.partselect.elements[i];
     t = tabs[i];
     if (t > parent_tab) {
        child_check(childbox, box.checked);
     }

     i++;
   }

 } 

 function child_check(box, bool) {

   box.checked = bool;

   if (box.checked == true) { 
     self.opener.add_part(box.name);
   } else {
     self.opener.drop_part(box.name);
   }
 }


 function check_selected() {
   i = 0;
   while (i < document.partselect.elements.length) {
     document.partselect.elements[i].checked=false;
     i++;
   }
   i = 0;
   while (i < self.opener.document.critform.structure_list.options.length) {
     part = self.opener.document.critform.structure_list.options[i].value;
     j = 0;
     while (j < document.partselect.elements.length) {
       box = document.partselect.elements[j];
       if (part == box.name) {
	 box.checked = true;
       }
       j++;
     }
     i++;
   }
 }

 function manual_add(part) {
  self.opener.add_part(part);
 }

ENDJS

 my $Q = new CGI();

 print $Q->header();

 print $Q->start_html(-title=>'Part & System Select', -bgcolor=>'#FFFFFF',-style=>$CSS);

 print "<SCRIPT LANGUAGE='javascript1.2'>";
 print $JS;
 print "</SCRIPT>";

# print "<form name=\"partselect\" method=get action=\"/cgi-bin/xpat_select_parts.cgi\">\n";
 print $Q->start_form(-name=>'partselect', -method=>'GET', -action=>'/cgi-bin/xpat_select_parts.cgi') . "\n";

 print $Q->hidden(-name=>'mode') . $Q->hidden(-name=>'submode',-default=>'');
 $form_elements++; $form_elements++;
 print $Q->hidden(-name=>'lastclicked');
 $form_elements++;
 print "<font size=3><b><center>Add Structure or System to Search...</b></font></center><small><br></small>";

# print "&nbsp;&nbsp;&nbsp;&nbsp;Add a keyword manually: &nbsp; " . $Q->textfield(-name=>'userentered',-value=>'') . "\n";  
# $form_elements++;
# print "&nbsp;&nbsp;" . $Q->button(-name=>'add',-value=>'Add to search',-onClick=>'manual_add(document.partselect.userentered.value); document.partselect.userentered.value=""') . "<br>&nbsp;&nbsp;&nbsp;&nbsp;or select from the heirarchy below:<br>\n";
# $form_elements++;

 table_top();

 if ($Q->param('mode') eq "parts") {
   parts();
# } elsif ($Q->param('mode') eq "systems") {
#   systems();
 } elsif ($Q->param('mode') eq "alpha") {
   $form_elements = alpha($form_elements);
 } else {
   instructions();
 }

 table_bottom();
 print "<br>" . $Q->button(-name=>'Close',-value=>'Close',-onClick=>'window.close();');
 print "</form>\n";
 print "<script>check_selected();</script>\n";
 if (defined $Q->param('lastclicked')) {
   print "<script>document.location='#" . $Q->param('lastclicked') . "';</script>\n";
 }

 print $Q->end_html();

 sub alpha() {
   my ($form_elements) = @_;
   my ($name, $id);
   my $cur = $dbh->prepare("select unique anatdisp_item_name, anatdisp_item_zdb_id, anatstgstat_anat_item_stg_count, lower(anatdisp_item_name) from anatomy_display, anatomy_stage_stats, stage where anatdisp_item_zdb_id = anatstgstat_anat_item_zdb_id and stg_zdb_id = anatstgstat_stg_zdb_id and stg_name = 'Any stage' order by 4;");
   $cur->execute;
   $cur->bind_col(1, \$name);
   $cur->bind_col(2, \$id);
   $cur->bind_col(3, \$count);

   while ($cur->fetch) {
    if ($count < 1 ) {  
      print "   <a href=\"javascript:alert('Currently there are no expression\n assays for this structure at this developmental stage.');\"><img src=\"/images/notbox.gif\" height=14 width=14 border=0></a>$name\n";
    } else {  
       print "   <input type=checkbox name=\"$name\" onClick=\"check($form_elements);\">$name\n";
       $form_elements++;
     }
   }

   print "</pre>\n";
   return $form_elements;
 }

 sub parts() {
     my $open = false;
     my $total = 0;
     my ($stg_name,$stg_zdb_id,$stg_hours_start,$stg_hours_end);    #temp vars
     my ($part_name, $seq_num, $tabs);
#     my $cur = $dbh->prepare("select get_stg_name_long_html(stg_zdb_id, 'popup_url'), stg_zdb_id, stg_hours_start, stg_hours_end from stage where stg_name not like 'Any%' order by stg_hours_start;");
     my $cur;
     my $cur2;

     $cur = $dbh->prepare("SELECT child.stg_name_long, \
                                     child.stg_zdb_id, child.stg_hours_start, child.stg_hours_end, child.stg_comments_relative_url \
                              FROM stage parent, stage child, stage_contains \
                              WHERE parent.stg_name = 'Any stage' \
                                    and parent.stg_zdb_id = stgcon_container_zdb_id \
                                    and child.stg_zdb_id  = stgcon_contained_zdb_id \
                              ORDER BY child.stg_hours_start; ");
     $cur->execute;
     while ($cur->fetch) {
       $total++;
     }

     $cur = $dbh->prepare("SELECT child.stg_name_long, \
                                     child.stg_zdb_id, child.stg_hours_start, child.stg_hours_end, child.stg_comments_relative_url \
                              FROM stage parent, stage child, stage_contains \
                              WHERE parent.stg_name = 'Any stage' \
                                    and parent.stg_zdb_id = stgcon_container_zdb_id \
                                    and child.stg_zdb_id  = stgcon_contained_zdb_id \
                              ORDER BY child.stg_hours_start; ");

     $cur->execute;
     $cur->bind_col(1, \$stg_name);
     $cur->bind_col(2, \$stg_zdb_id);
     $cur->bind_col(3, \$stg_hours_start);
     $cur->bind_col(4, \$seq_hours_end);
     $cur->bind_col(5, \$stg_url);

    print "\n\n";
#     print "</pre><center><small>(click the arrow to view contents of a stage, <br> click on the stage name for stage information, <br> and click on the checkbox to add a structure to the query)</small></center><pre>\n";

#   print "   <img src=\"/images/star.gif\" height=12> - stage data    <input type=checkbox> - structure data     <img src=\"/images/notbox.gif\" height=14 width=14> - no structure data\n\n";
     print "<table bgcolor=#EEEEEE align=center cellspacing=0 cellpadding=3> \
<tr><td><b>Legend: </b> \
<small>Gene expression data present...</small> <br>\
&nbsp;&nbsp;<small><img src=\"/images/star.gif\" height=12> - for this stage</small>\
&nbsp;&nbsp;<small><input type=checkbox> - for this structure</small> \
&nbsp;&nbsp;<small><img src=\"/images/notbox.gif\" height=14 width=14> - none</small>&nbsp;&nbsp;</td></tr></table>";
     $form_elements++;
     print "\n\n";
     print "  <EM>Expand a stage below to select structures:</EM>\n\n";

     my $i = 0;
     while ($cur->fetch) {

#       $cur2 = $dbh->prepare("select count(stgcon_contained_zdb_id)::integer from stage, stage_contains where stgcon_container_zdb_id = '$stg_zdb_id' group by stgcon_container_zdb_id;");
#       $cur2->execute;
#       $cur2->bind_col(1,\$stg_child_count);
#       $cur2->fetch;
#     print "child stages: " . $stg_child_count;


       if (!defined $Q->param('s'.$i)) {
	 if (($cur->rows == 1) || ($cur->rows == $total)) {       
	   $open = false;
	   print $Q->hidden(-name=>'s'.$i,-default=>'0');
	 } else {
	   $open = true;
	   print $Q->hidden(-name=>'s'.$i,-default=>"$stg_zdb_id");
	 }
       } else {
	 if ($Q->param('s'.$i) eq '0') {
	   print $Q->hidden(-name=>'s'.$i,-default=>'0');
	   $open = false;
	 } else {
	   print $Q->hidden(-name=>'s'.$i,-default=>"$stg_zdb_id");
	   $open = true;
	 }
       }

#     if (defined $Q->param('s'.$i)) {
#       if ($Q->param('s'.$i) == 0) {
#	 $open = false;
#       } else {
#	 $open = true;
#       }
#     }

       print "   "; print "<a name = \"$stg_zdb_id\">";

       if ($open eq true) {
	 print "<a href=\"javascript:document.partselect.mode.value='parts'; document.partselect.s$i.value=\'0\'; document.partselect.submit();\"><img src=\"/images/ARROWS/open.gif\" border=0 height=11>";
      } else { print "<a href=\"javascript:document.partselect.mode.value='parts'; document.partselect.s$i.value=\'$stg_zdb_id\'; document.partselect.lastclicked.value='$stg_zdb_id'; document.partselect.submit();\"><img src=\"/images/ARROWS/closed.gif\" border=0 height=11>"; }

      print "$stg_name" . "</a> [<a href=\"javascript: popup_url('$stg_url');\"><b>stage info</b></a>]\n";  $form_elements++; 

       if ($open eq true) {
#	 $form_elements = print_subtree($stg_zdb_id, $form_elements);
	 $form_elements = print_substages($stg_zdb_id, $i, $form_elements);
       }
       $i++;
     }
     print "";
     
     return $form_elements;

 }  

 sub systems() {

    print "<br>\n";
    print "&nbsp;&nbsp;<EM>Choose a system:</EM><br><br>\n";

 }

 sub instructions() {
   print <<END;
                <center>
<table width=500 cellpadding=5><tr><td bgcolor = #EEEEEE>


<b>Hints:</b>
<ul>
<li>Clicking on a check box will add the structure
    to the query in the main ZFIN window.
<li>Within the <b>Structures List</b> section, 
    clicking a checkbox will automatically add 
    all of it's children to the query.  Children, or any check box, can be removed from the query by unchecking their check box.
<li>Structures with check boxes have expression data currently in ZFIN, structures with grey boxes do not currently have any expression data.
<li>The <img src="/images/star.gif" height=12> icon denotes stages for which ZFIN currently has expression data.
<li>The query form searches the keyword field of expression assays.
<li>Selecting a structure within this popup window
    does not affect the developmental stage selection in the query form. 


</ul>


</td></tr></table>
                </center>
                <pre>














                </pre>

END
# '
 }

 sub print_substages() {
   my ($zdb_id, $s, $form_lements) = @_;
   my $cur;
   my $query;
   my $open = false;
#   $query = "select stg_name_long, \
#                       stg_zdb_id, stg_hours_start, stg_hours_end, stg_comments_relative_url \ 
#                from stage, stage_contains \
#                where stgcon_container_zdb_id = '$zdb_id' \
#                      and stgcon_contained_zdb_id = stg_zdb_id \
#                order by stg_hours_start;";

   $query = "select stg_name_long, stg_zdb_id, stg_hours_start, stg_hours_end, \
                    stg_comments_relative_url, sum(anatstgstat_total_count) \
             from stage, stage_contains, anatomy_stage_stats \
             where stgcon_container_zdb_id = '$zdb_id' \
                   and stgcon_contained_zdb_id = stg_zdb_id \
                   and stgcon_contained_zdb_id = anatstgstat_stg_zdb_id \
             group by stg_name_long, stg_zdb_id, stg_hours_start, stg_hours_end, stg_comments_relative_url \
             order by stg_hours_start;";


   $cur = $dbh->prepare($query);
   $cur->execute;
   $cur->bind_col(1, \$stg_name);
   $cur->bind_col(2, \$stg_zdb_id);
   $cur->bind_col(3, \$stg_hours_start);
   $cur->bind_col(4, \$seq_hours_end);
   $cur->bind_col(5, \$stg_url);
   $cur->bind_col(6, \$sum);

        my $i = 0;
    my $count_test = 0;
     while ($cur->fetch) {
       
#       print "$sum";

      if (!defined $Q->param('s'.$s.'s'.$i)) {
	if (($first_with_data_is_open eq false) && ($sum > 0)) {
	  print $Q->hidden(-name=>'s'.$s.'s'.$i,-default=>"$stg_zdb_id");
	  $open = true;
	} else {
	  print $Q->hidden(-name=>'s'.$s.'s'.$i,-default=>'0');
	  $open = false;
	}
       } else {
	 if ($Q->param('s'.$s.'s'.$i) eq '0') {
	   print $Q->hidden(-name=>'s'.$s.'s'.$i,-default=>'0');
	   $open = false;
	 } else {
	   print $Q->hidden(-name=>'s'.$s.'s'.$i,-default=>"$stg_zdb_id");
	   $open = true;
	 }
       }

       if ($open eq true) { $first_with_data_is_open = true; }
      
      if ($sum > 0) {
	$star = "<img src=\"/images/star.gif\" height=12>";
      } else {
	$star = "";
      }


       $count_test = 100;
       print "   ";
       print "   "; print "<a name = \"$stg_zdb_id\">";
       if ($open eq true) {
	 print "<a href=\"javascript:document.partselect.mode.value='parts'; document.partselect.s". $s . "s" . $i . ".value=\'0\'; document.partselect.submit();\"><img src=\"/images/ARROWS/open.gif\" border=0 height=11>";
      } else { print "<a href=\"javascript:document.partselect.mode.value='parts'; document.partselect.s". $s . "s" . $i . ".value=\'$stg_zdb_id\'; document.partselect.lastclicked.value='$stg_zdb_id'; document.partselect.submit();\"><img src=\"/images/ARROWS/closed.gif\" border=0 height=11>"; }

       print "$stg_name" . "</a> $star [<a href=\"javascript: popup_url('$stg_url');\"><b>stage info</b></a>]\n";  $form_elements++;
       if ($open eq true) {
	 $form_elements = print_subtree($stg_zdb_id, $form_elements);
       }
       $i++;
     }
   if ($count_test == 0) {
     $form_elements = print_subtree($zdb_id, $form_elements);
   }
     
     print "";
   
   return $form_elements;

 }

 sub print_subtree() {
  my ($zdbid, $form_elements) = @_;

   my ($name, $id, $seq, $tabs);
#   my $query = "select anatdisp_item_name, anatdisp_item_zdb_id, anatdisp_seq_num, anatdisp_indent, anatstgstat_total_count from anatomy_display, anatomy_stage_stats, stage where anatdisp_stg_zdb_id = '$zdbid' and anatstgstat_stg_zdb_id = stg_zdb_id and anatdisp_item_zdb_id = anatstgstat_anat_item_zdb_id and stg_name='Any stage' order by anatdisp_seq_num;";

  my $query = "select anatdisp_item_name, anatdisp_item_zdb_id, anatdisp_seq_num, \
       anatdisp_indent, anatstgstat_total_count \
from   anatomy_display, anatomy_stage_stats \
where  anatdisp_stg_zdb_id = '$zdbid' \
       and anatdisp_item_zdb_id = anatstgstat_anat_item_zdb_id \
       and anatstgstat_stg_zdb_id = anatdisp_stg_zdb_id
order by anatdisp_seq_num;";


#and anatdisp_stg_zdb_id = anatstgstat_stg_zdb_id
    #print $query;
  my $cur = $dbh->prepare($query);
   $cur->execute;
   $cur->bind_col(1, \$name);
   $cur->bind_col(2, \$id);
   $cur->bind_col(3, \$seq);
   $cur->bind_col(4, \$tabs);
   $cur->bind_col(5, \$count);
  my $i;
  while ($cur->fetch) {
    print "   "; 
    print "   "; # default indent
    $i = $tabs;
    while ($i > 0) { print "   "; $i--; }
    if ($count < 1 ) {  
      print "<a href=\"javascript:alert('Currently there are no expression\n assays for this structure at this developmental stage.');\"><img src=\"/images/notbox.gif\" height=14 width=14 border=0></a>$name\n";
    } else {  
      print "<input type=checkbox name=\"$name\" onClick=\"check($form_elements); check_selected();\" tabs=\"$tabs\">$name";
      print "<SCRIPT>";
      print "tabs[$form_elements] = $tabs;";
      print "</SCRIPT>\n";
      $form_elements++;
    }
  }
  return $form_elements;
 }

 sub table_top() {
   
   my $alpha_bg = "#DDDDDD";
   my $parts_bg = "#DDDDDD";
   my $systems_bg = "#DDDDDD";
   my $instructions_bg = "#DDDDDD";

    if ($Q->param('mode') eq "parts") {
      $parts_bg = "#88a6a6";
    } elsif ($Q->param('mode') eq "systems") {
      $systems_bg = "#88a6a6";
    } elsif ($Q->param('mode') eq "alpha") {
      $alpha_bg = "#88a6a6";
    } else {
      $instructions_bg = "#88a6a6";
    }
   print "<table border = 0 cellpadding=0 cellspacing=0 width=100%>\n";
   print "	<tr>\n";
   print "		<td colspan=1 bgcolor=$parts_bg><h2><center><a href=\"/cgi-bin/xpat_select_parts.cgi?mode=parts\">Structures<br>List</a></center></h2></td>\n";
   print "              <td colspan=1 bgcolor=#FFFFFF><pre></pre></td>";
   print "		<td colspan=1 bgcolor=$alpha_bg><h2><center><a href=\"/cgi-bin/xpat_select_parts.cgi?mode=alpha\">Alphabetical<br>List</a></center></h2></td>\n";
#   print "   		<td colspan=1 bgcolor=$systems_bg><h2><center><a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems\">Systems<br>List</a></center></h2></td>\n";
   print "              <td colspan=1 bgcolor=#FFFFFF><pre></pre></td>";
   print "		<td colspan=1 bgcolor=$instructions_bg><h2><center><a href=\"/cgi-bin/xpat_select_parts.cgi?mode=instructions\">Help</a></h2></td>\n";
   print "      </tr>\n";
   print "      <tr>\n";
   print "              <td colspan=5 bgcolor=#88a6a6>\n<pre>";

 }

 sub table_bottom() {
   print "             </pre>\n";
   print "	       </td>\n";
   print "	</tr>\n";
   print "</table>\n";
 }
}
