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
ENDCSS

 $JS=<<ENDJS;

 function check(part) {

   for(i = 0 ; i < document.partselect.elements.length ; i++ ) {
     if (document.partselect.elements[i].name == part) {
	 box = document.partselect.elements[i];
	 break;
       }
   }
   if (box.checked == true) { 
     top.content.criteria.add_part(part);
   } else {
     top.content.criteria.drop_part(part);
   }
 } 

 function check_selected() {
   i = 0;
   while (i < document.partselect.elements.length) {
     document.partselect.elements[i].checked=false;
     i++;
   }
   i = 0;
   while (i < top.content.criteria.document.critform.structure_list.options.length) {
     part = top.content.criteria.document.critform.structure_list.options[i].value;
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
  top.content.criteria.add_part(part);
 }

ENDJS

 my $Q = new CGI();

 print $Q->header();

 print $Q->start_html(-title=>'Part & System Select', -style=>$CSS, -script=>$JS, -onload=>'check_selected();');

# print "<form name=\"partselect\" method=get action=\"/cgi-bin/xpat_select_parts.cgi\">\n";
 print $Q->start_form(-name=>'partselect', -method=>'GET', -action=>'/cgi-bin/xpat_select_parts.cgi') . "\n";

 print $Q->hidden(-name=>'mode') . $Q->hidden(-name=>'submode',-default=>'');

 print "<font size=3><b><center>Add Structure or System to Search...</center></b></font><br>";

 print "&nbsp;&nbsp;&nbsp;&nbsp;Add a keyword manually: &nbsp; " . $Q->textfield(-name=>'userentered',-value=>'') . "\n";  
 print "&nbsp;&nbsp;" . $Q->button(-name=>'add',-value=>'Add to search',-onClick=>'manual_add(document.partselect.userentered.value); document.partselect.userentered.value=""') . "<br>&nbsp;&nbsp;&nbsp;&nbsp;or select from the heirarchy below:<br>\n";


 table_top();

 if ($Q->param('mode') eq "parts") {
   parts();
# } elsif ($Q->param('mode') eq "systems") {
#   systems();
 } elsif ($Q->param('mode') eq "alpha") {
   alpha();
 } else {
   instructions();
 }

 table_bottom();

 print "</form>\n">

 print $Q->end_html();

 sub alpha() {
   my ($name, $id);
   my $cur = $dbh->prepare("select unique anatdisp_item_name, anatdisp_item_zdb_id from anatomy_display order by anatdisp_item_name;");
   $cur->execute;
   $cur->bind_col(1, \$name);
   $cur->bind_col(2, \$id);

   while ($cur->fetch) {
     if  (($name eq "Brachet's cleft") || ($name eq "Kupffer's vesicle")) {  
       print "     BUG - part had apostrophy that I can't deal with\n";;
     } else {  
       print "     <input type=checkbox name=\"$name\" onClick=\"check('$name');\">$name\n";

     }
   }

   print "</pre>\n";
 }

 sub parts() {
     my ($stg_name,$stg_zdb_id,$stg_hours_start,$stg_hours_end);    #temp vars
     my ($part_name, $seq_num, $tabs);
     my $cur = $dbh->prepare("select stg_name_long, stg_zdb_id, stg_hours_start, stg_hours_end from stage where stg_name_long not like 'Any%' order by stg_hours_start;");
     $cur->execute;
     $cur->bind_col(1, \$stg_name);
     $cur->bind_col(2, \$stg_zdb_id);
     $cur->bind_col(3, \$stg_hours_start);
     $cur->bind_col(4, \$seq_hours_end);

     print "\n\n";
     print "  <EM>Choose a stage below to select parts:</EM>\n\n";

     my $i = 0;
     while ($cur->fetch) {
       print "      ";
       if ((defined $Q->param('s'.$i)) && ($Q->param('s'.$i) ne "0")) {
	 print "[<a href=\"javascript:document.partselect.mode.value='parts'; document.partselect.s$i.value=\'0\'; document.partselect.submit();\">close</a>]";
       } else { print "[<a href=\"javascript:document.partselect.mode.value='parts'; document.partselect.s$i.value=\'$stg_zdb_id\'; document.partselect.submit();\">open</a>]"; }

       print "$stg_name" . $Q->hidden(-name=>'s'.$i,-default=>'0') . "\n";
       if ((defined $Q->param('s'.$i)) && ($Q->param('s'.$i) ne "0")) {
	 print_subtree($stg_zdb_id);
       }
       $i++;
     }
     print "";

 }  

 sub systems() {
   if (!defined $Q->param('submode')) {

    print "<br>\n";
    print "&nbsp;&nbsp;<EM>Choose a system:</EM><br><br>\n";

    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Skeletal System</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Suspensoria</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Opercular-branciostegal series</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Hyoid Bars</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Body</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Musculature System</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Digestive System</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Excretory System</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Reproductive System</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Cardiovascular System</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Respitory System</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Gas Bladder</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Lymphatic</a><br>\n";
    print "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems&submode=stg_zdb_id\">Nervous System</a><br>\n";
    print "<br><br>\n";

  } else {
    print <<END;
<h2>&nbsp;Nervous System</h2>
<pre>
     <input type=checkbox name="Inner ear (vestibular end-organ)" onClick="check('Inner ear (vestibular end-organ)')">Inner ear (vestibular end-organ)
            <input type=checkbox name="Otolith organs" onClick="check('Otolith organs')">Otolith organs
                  <input type=checkbox name="Utricle" onClick="check('Utricle')">Utricle
                      <input type=checkbox name="Lapillus (otolith)" onClick="check('Lapillus (otolith)')">Lapillus (otolith)
                  <input type=checkbox name="Saccule" onClick="check('Saccule')">Saccule
                      <input type=checkbox name="Sagitta (otolith)" onClick="check('Sagitta (otolith)')">Sagitta (otolith)
                  <input type=checkbox name="Lagena" onClick="check('Lagena')">Lagena
                      <input type=checkbox name="Asteriscus (otolith)" onClick="check('Asteriscus (otolith)')">Asteriscus (otolith)
                  <input type=checkbox name="Macula Neglecta" onClick="check('Macula Neglecta')">Macula Neglecta
            <input type=checkbox name="Semicircular Canals" onClick="check('Semicircular Canals')">Semicircular Canals
                  <input type=checkbox name="Horizontal" onClick="check('Horizontal')">Horizontal
                  <input type=checkbox name="Rostral (anterior) Vertical" onClick="check('Rostral (anterior) Vertical')">Rostral (anterior) Vertical
                  <input type=checkbox name="Caudal (posterior) Vertical" onClick="check('Caudal (posterior) Vertical')">Caudal (posterior) Vertical
     <input type=checkbox name="Olfactory bulb" onClick="check('Olfactory bulb')">Olfactory bulb
            <input type=checkbox name="external cellular layer" onClick="check('external cellular layer')">external cellular layer
            <input type=checkbox name="glomerular layer" onClick="check('glomerular layer')">glomerular layer
            <input type=checkbox name="primary olfactory fiber layer" onClick="check('primary olfactory fiber layer')">primary olfactory fiber layer
                  <input type=checkbox name="internal cellular layer (with mitral cells)" onClick="check('internal cellular layer (with mitral cells)')">internal cellular layer (with mitral cells)
            <input type=checkbox name="area dorsalis telencephali" onClick="check('area dorsalis telencephali')">area dorsalis telencephali
            <input type=checkbox name="lateral olfactory tract" onClick="check('lateral olfactory tract')">lateral olfactory tract
            <input type=checkbox name="medial olfactory tract" onClick="check('lateral olfactory tract')">medial olfactory tract
            <input type=checkbox name="area ventralis telencephali" onClick="check('area ventralis telencephali')">area ventralis telencephali
            <input type=checkbox name="dorsal zone of V" onClick="check('dorsal zone of V')">dorsal zone of V
            <input type=checkbox name="central zone of V" onClick="check('central zone of V')">central zone of V
            <input type=checkbox name="lateral zone of V" onClick="check('lateral zone of V')">lateral zone of V
            <input type=checkbox name="ventral zone of V" onClick="check('ventral zone of V')">ventral zone of V
            <input type=checkbox name="central zone of D" onClick="check('central zone of D')">central zone of D
            <input type=checkbox name="lateral zone of D" onClick="check('lateral zone of D')">lateral zone of D
            <input type=checkbox name="medial zone of D" onClick="check('medial zone of D')">medial zone of D
            <input type=checkbox name="posterior zone of D" onClick="check('posterior zone of D')">posterior zone of D
            <input type=checkbox name="nucleus commissuralis of V" onClick="check('nucleus commissuralis of V')">nucleus commissuralis of V
            <input type=checkbox name="commissura anterior, pars dorsalis" onClick="check('commissura anterior, pars dorsalis')">commissura anterior, pars dorsalis
            <input type=checkbox name="dorsal zone of D" onClick="check('dorsal zone of D')">dorsal zone of D
            <input type=checkbox name="commissura anterior, pars ventralis" onClick="check('commissura anterior, pars ventralis')">commissura anterior, pars ventralis
            <input type=checkbox name="lateral forebrain bundle" onClick="check('lateral forebrain bundle')">lateral forebrain bundle
            nucleus entopeduncularis
            nucleus preopticusparvocellularispars anterior
            pars subcommissuralis of V
            pars postcommissuralis of V
            chiasma opticum
            optic tract
            medial forebrain bundle
            pars lateralis posterior of D
               nucleus preopticus magnocellularis pars magnocellularis
            nucleus preopticus parvocellularis pars posterior
            nucleus suprachiasmaticus
            commissura horizontalis
            commissura supraoptica
            epiphysis
            habenula
            nucleus intermedius thalami
               nucleus preopticus magnocellularis pars gigantocellularis
            nucleus pretectalis superficialis parvocellularis
            rostral nucleus of Saidel & Butler
            nucleus Suprachiasmaticus
            nucleus ventrolateralis thalami
            nucleus ventromedialis thalami
            ventral optic tract
            dorsomedial optic tract
            fasciculus retroflexus
            periventricular zone of ventral hypothalamus
            dorsal accessory optic nucleus
            nucleus pretectalis superficialis magnocellularis
            tectum opticum
            nucleus anterior thalami
            posterior pretectal nucleus
            accessory pretectal nucleus
            commmissura habenularis
            central pretectal nucleus
            ventral accessory optic nucleus
            nucleus tuberis anterior
            nucleus preglomerulosus pars anterior
            nucleus preglomerulosus pars lateralis
            posterior pretectal nucleus
            perventricular nucleus of posterior tuberculum
            commissura tecti
            nucleus centralis posterior thalami
            commissura posterior
            nucleus dorsalis posterior thalami
            dorsal periventricular hypothalamus
            nucleus lateralis hypothalami
            nucleus diffusus lobus inferior hypothalamus
            nucleus paracommissuralis
            nucleus preglomerulosus pars medialis
            nucleus pretectalis periventricularis
            recessus lateralis
            torus longitudinalis
            nucleus tuberis posterior
            torus lateralis
            tractus pretecto-mamillaris
            mesencephalic nucleus of trigeminal nerve
            nucleus posterior thalami
            paraventricular organ
            caudal periventricular hypothalamus
            tertiary gustatory nucleus
            torus semicircularis
            fasciculus longitudinalis medialis
            nucleus fasciculus longitudinalis medialis
            nucleus tegmentalis rostralis
            nucleus subglomerulosis
            lateral division of valvula cerebelli
            vascular laguna of area postrema
            dorsal tegmental nucleus
            nucleus Edinger-Westphal
            fasciculus longitundinalis lateralis
            granular layer of valvula cerebelli
            molecular layer of valvula cerebelli
            nucleus preglomerulosis pars caudalis
            tractus tecto-bulbaris
            valvula cerebelli lateral division
            commissura ansulata
            nervus oculomotorius
            nucleus centralis of lobus inferior hypothalami
            nucleus nervi oculomotorius
            medial division of valvula cerebelli
            brachium conjunctivium
            corpus mamillare
            nucleus interpeduncularis
            nucleus lateralis valvulae
            nervus trochlearis
            nucleus nervi trochlearis
            tractus mesencephalo-cerebellaris
            tractus tecto-bulbaris cruciatus
       tractus tecto-bulbaris rectus
            nucleus raphe
            corpus cerebelli
            decussatio trochulearis
            griseum centrale
            nucleus isthmi
            superior reticular formation
            commissura interbulbularis
            nucleus motorius nervi trigemini pars dorsalis
            commissura cerebelli
            commissura gustatoria
            granular layer of corpus cerebelli
            molecular layer of corpus cerebelli
            secondary gustatory nucleus
            nucleus motorius nervi trigemini pars ventralis
            nervus trigeminus
            anterior lateral line nerves
            eminentia granularis
            sensory trigeminal nucleus
            secondary gustatory tract
            nervus facialis
            dorsal motor root of V
            descending trigeminal root
            posterior cerebellar tract
            nucleus motorius nervi trigemini pars ventralis
            rostral root of abducens nerve
            anterior octaval nucleus
            granular layer of lobus caudalis cerebelli
            Mauthner cell
            medial octavolateralis nucleus
            medial reticular nucleus
            nervus octavus
            Mauthner axon
            nucleus nervus abducens
  
            crista cerebellaris
            descending octaval nucleus
            nucleus motorius nervus facialis
            posterior lateral line nerve
            sensory root or nervus facialis
            lobus caudalis cerebelli
            lobus facialis
            tractus bulbo-spinalis
            decussation of the medial octavolateralis nucleus
            oliva inferior
            nervus glossopharyngeus
            inferior reticular formation
            lobus glossopharyngealeus
            tractus vestibulo-spinalis
            nucleus motorius of nervi vagi
            lobus vagus
            caudal octavolateralis nucleus
            nervus vagus
            nucleus motoris of nervi vagi
            funiculus lateralis
            decussation of nucleus funiculi medialis
            nucleus funiculi medialis
            cornu dorsale
            cornu ventrale
            funiculus lateralis
            radix dorsalis
</pre>
END
  }

 }

 sub instructions() {
   print <<END;
                <br>
                <center>
                Click the 'Structures' or 'Systems' tabs above <br>
                to navigate the anatomical hierarchy, <br>
                click on a part's checkbox to add it to the query form above.
                </center>
                <pre>














                </pre>

END
# '
 }

 sub print_subtree() {
  my ($zdbid) = @_;

   my ($name, $id, $seq, $tabs);
  my $query = "select anatdisp_item_name, anatdisp_item_zdb_id, anatdisp_seq_num, anatdisp_indent from anatomy_display where anatdisp_stg_zdb_id = '$zdbid' order by anatdisp_seq_num;";
    #print $query;
  my $cur = $dbh->prepare($query);
   $cur->execute;
   $cur->bind_col(1, \$name);
   $cur->bind_col(2, \$id);
   $cur->bind_col(3, \$seq);
   $cur->bind_col(4, \$tabs);

  my $i;
  while ($cur->fetch) {
    print "    "; # default indent
    $i = $tabs;
    while ($i > 0) { print "    "; $i--; }
    if (($name eq "Brachet's cleft") || ($name eq "Kupffer's vesicle")) {  
      print "BUG - part had apostrophy that I can't deal with\n";;
    } else {  
      print "<input type=checkbox name=\"$name\" onClick=\"check('$name');\">$name\n";
    }
  }
 }



 sub print_fake_subtree() {
   my ($zdbid) = @_;
     print <<END;
<h2>&nbsp;segmentation 14 somite (16h)</h2>

<pre>
	<input type="checkbox" name="Embryo" onClick="check('Embryo');">Embryo 
		<input type="checkbox" name="ectoderm" onClick="check('ectoderm');">ectoderm
		<input type="checkbox" name="endoderm" onClick="check('endoderm');">endoderm
		<input type="checkbox" name="mesoderm" onClick="check('mesoderm');">mesoderm
			<input type="checkbox" name="axial chorda mesoderm" onClick="check('axial chorda mesoderm');">axial chorda mesoderm
			<input type="checkbox" name="paraxial segmental plate" onClick="check('paraxial segmental plate');">paraxial segmental plate 
				<input type="checkbox" name="somites 1-14" onClick="check('somites 1-14');">somites 1-14
			<input type="checkbox" name="myotomes with epaxial & hypaxial regions" onClick="check('myotomes with epaxial & hypaxial regions');">myotomes with epaxial & hypaxial regions
				<input type="checkbox" name="muscle pioneer cells" onClick="check('muscle pioneer cells');">muscle pioneer cells
			<input type="checkbox" name="notochord" onClick="check('notochord');">notochord
			<input type="checkbox" name="polster" onClick="check('polster');">polster
			<input type="checkbox" name="prechordal plate" onClick="check('prechordal plate');">prechordal plate
		<input type="checkbox" name="neural crest" onClick="check('neural crest');">neural crest 
			<input type="checkbox" name="cephalic" onClick="check('cephalic');">cephalic
			<input type="checkbox" name="trunk" onClick="check('trunk');">trunk
		<input type="checkbox" name="organ system" onClick="check('organ system');">organ system
			<input type="checkbox" name="nervous system" onClick="check('nervous system');">nervous system
				<input type="checkbox" name="central nervous system" onClick="check('central nervous system');">central nervous system
					<input type="checkbox" name="brain" onClick="check('brain');">brain
						<input type="checkbox" name="forebrain" onClick="check('forebrain');">forebrain
							<input type="checkbox" name="telencephalon (N1)" onClick="check('telencephalon (N1)');">telencephalon (N1)
							<input type="checkbox" name="diencephalon (N2)" onClick="check('diencephalon (N2)');">diencephalon (N2)
						<input type="checkbox" name="midbrain (N3)" onClick="check('midbrain (N3)');">midbrain (N3)
						<input type="checkbox" name="hindbrain" onClick="check('hindbrain');">hindbrain
							<input type="checkbox" name="rhombomeres r2-r6 (N4-8)" onClick="check('rhombomeres r2-r6 (N4-8)');">rhombomeres r2-r6 (N4-8)
					<input type="checkbox" name="neural rod (future spinal cord)" onClick="check('neural rod (future spinal cord)');">neural rod (future spinal cord) 
				<input type="checkbox" name="peripheral nervous system" onClick="check('peripheral nervous system');">peripheral nervous system
					<input type="checkbox" name="peripheral sensory axons" onClick="check('peripheral sensory axons');">peripheral sensory axons
					<input type="checkbox" name="trigeminal (V) placode" onClick="check('trigeminal (V) placode');">trigeminal (V) placode
			<input type="checkbox" name="sensory system" onClick="check('sensory system');">sensory system 
				<input type="checkbox" name="eye" onClick="check('eye');">eye
					<input type="checkbox" name="lens primordium ??" onClick="check('lens primordium ??');">lens primordium ??
					<input type="checkbox" name="optic primordium" onClick="check('optic primordium');">optic primordium 
				<input type="checkbox" name=">otic placode" onClick="check('>otic placode');">otic placode
			<input type="checkbox" name="urogenital system" onClick="check('urogenital system');">urogenital system 
				<input type="checkbox" name="pronephric duct" onClick="check('pronephric duct');">pronephric duct
		<input type="checkbox" name="tail bud" onClick="check('tail bud');">tail bud
			<input type="checkbox" name="Kupffers vesicle (future mesoderm, notochord)" onClick="check('Kupffers vesicle (future mesoderm, notochord)');">Kupffers vesicle (future mesoderm, notochord)
	<input type="checkbox" name="extraembryonic" onClick="check('extraembryonic');">extraembryonic
		<input type="checkbox" name="yolk" onClick="check('yolk');">yolk
			<input type="checkbox" name="ball" onClick="check('ball');">ball
			<input type="checkbox" name="extension" onClick="check('extension');">extension
		<input type="checkbox" name="EVL" onClick="check('EVL');">EVL
		<input type="checkbox" name="I-ESL, E-YSL" onClick="check('I-ESL, E-YSL');">I-ESL, E-YSL
</pre>
END


 }

 sub table_top() {
   
   my $alpha_bg = "#EEEEEE";
   my $parts_bg = "#EEEEEE";
   my $systems_bg = "#EEEEEE";
   my $instructions_bg = "#EEEEEE";

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
   print "		<td colspan=1 bgcolor=$alpha_bg><h2><center><a href=\"/cgi-bin/xpat_select_parts.cgi?mode=alpha\">Alphabetical<br>List</a></center></h2></td>\n";
   print "		<td colspan=1 bgcolor=$parts_bg><h2><center><a href=\"/cgi-bin/xpat_select_parts.cgi?mode=parts\">Structures<br>List</a></center></h2></td>\n";
#   print "   		<td colspan=1 bgcolor=$systems_bg><h2><center><a href=\"/cgi-bin/xpat_select_parts.cgi?mode=systems\">Systems<br>List</a></center></h2></td>\n";
   print "		<td colspan=1 bgcolor=$instructions_bg><h2><center><a href=\"/cgi-bin/xpat_select_parts.cgi?mode=instructions\">Instructions</a></h2></td>\n";
   print "              <td colspan=1 bgcolor=EEEEEE><pre>    </pre></td>\n";
   print "      </tr>\n";
   print "      <tr>\n";
   print "              <td colspan=4 bgcolor=#88a6a6>\n<pre>";

 }

 sub table_bottom() {
   print "             </pre>\n";
   print "	       </td>\n";
   print "	</tr>\n";
   print "</table>\n";
 }
}
