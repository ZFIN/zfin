#!/private/bin/perl -wT
#
 {   ### mod it
  #use strict;

  $print_note = 0; #to print comments, set to 1

  use CGI  qw / :standard/;
  use CGI::Carp 'fatalsToBrowser';
  use DBI;
  use HTTP::Request::Common qw/POST/;
  use LWP::UserAgent;
  use URI::Escape qw/uri_escape/;

  require('./mapper_select.pl');

  $CGI::POST_MAX=1024 * 100;    # max 100K posts
  $CGI::DISABLE_UPLOADS = 1;    # no uploads
  my $Q = new CGI();

  #use POSIX 'strftime';		# just for the debugging dump
  #open(IN, ">> mapplet_beta.log") || die "input dump failed";
  #print IN  strftime('%r %A %B %d %Y', localtime) ."\t";
  ### capture where the call was made from
  #$_ = $Q->referer(); /MIval=aa-/; print IN $'."\t";
  #foreach $name ($Q->param()){print IN "$name=".$Q->param($name)."|"};
  #print IN "\n\n";
  #close IN;


  ### the hard coded env paths need a better idea
  $ENV{INFORMIXDIR}      = '<!--|INFORMIX_DIR|-->';
  $ENV{INFORMIXSERVER}   = '<!--|INFORMIX_SERVER|-->';
  $ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->';
  ### open a handle on the db
  my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
  || die "Failed while connecting to <!--|DB_NAME|--> "; #$DBI::errstr";


  my @allpanels=();                      ### the panels the db knows about
  my @panels=();                         ### panel this map actualy attempts to use
  my @allpanels_order=();                ### an enumeration of all panels
  my @allpanels_metric=();               ### cM or cR  for each panel
  my @allpanels_id=();                   ### zdb_id for each panel
  my $panel="";                          ### current panel
  my ($sql,$junk,$tmp,$tmp_id);		             ### throw away vars
  my $cur = $dbh->prepare('select abbrev, disp_order, metric, zdb_id from panels where disp_order > 0 order by disp_order asc');
  $cur->execute;
  $cur->bind_col(1, \$panel);
  $cur->bind_col(2, \$junk);
  $cur->bind_col(3, \$tmp);
  $cur->bind_col(4, \$tmp_id);
  while ($cur->fetch){
    push (@allpanels ,$panel );
    push (@allpanels_order ,$junk );
    push (@allpanels_metric ,$tmp );
    push (@allpanels_id ,$tmp_id );
  }

  ### the known marker types
  my $types = 'SSLP';
  my $anon_type = "RAPD\',\'RFLP\',\'BAC_END\',\'PAC_END\',\'STS\',\'SNP";
  my $gene_type = "GENE\',\'GENEP";
  my $est_type  = "EST\',\'CDNA";
  my $bac_type  = "BAC\',\'PAC";
  my $fish_type  = "GENO\',\'MUTANT";

  ###
  ### WARNING and ERROR CODES to be passed to the Options page
  ###

  my $no_data    = 1;

  ### number of mappings above and below $marker to gather (aproximatly)
  ### running out of linkage group or running into a bin will affect the number of mappings returned;
  my $g_zoom = 30;
  my $zoom  = 30;		### the target number of markers for any one backbone
  my %zooms;                    ### hash of zoom values
  my $lg = -73;			### the linkgage group containing $marker
  my $lgs;                      ### hash of linkage groups
  my $loc = -1;			### the location of $marker
  my $lo;			### name above $loc on map
  my $hi;			### name below $loc on map
  my $m_lo;			### offset above $loc on map
  my $m_hi;			### offset below $loc on map
  my $lg_lo;			### $loc - $m_lo;
  my $lg_hi;			### $loc + $m_hi;

  my @row;			### a row returned from an sql query
  my $g_OID ='';		### the zdb_id of marker we found -- for applet, and options to ID unique name
  my $lines ;			### all the rows returned from a paticular sql query with fields terminated with '|'
  my $g_data ='';		### all the $lines returned from all the sql queries --- param to the applet
  my $g_printdata ='';		### all the $lines returned from all the sql queries --- param to the applet
  my $print_type = 0;            ### a code for types to print --1 gene, 2 est, 4 anon, 8 fish (0 == 15)
  my $g_height = 1;		### the maximum number of rows returned by any query --- anticipated heigth of the applet
  my $g_width =  0;		### the number of $panels $marker is found on * bbw ---  anticipated width of the applet
  my $g_zdbid;		    # global ID
  my $g_lg;			    # global LG
  my $zdbid = '';		### the zdbid comming in from a search marker page.

  my $or;			### is used to flag marker or lg options,
  my $lg_or;			### used to choose between near and between when location is choosen
  my $rowref;			### a refference to an array of rows;-- use  %$rowref   $%$rowref[0]

  my $g_error = 0;		### global error

  my $edit_panel;		### if given, the panel that is being changed

  #values Which might be used to repopulate the select map form
  my $sm_m = '';
  my $sm_lg = '';
  my $sm_panel = '';
  my $sm_loc = '-73';
  my $sm_refresh = '-73';

  ### incase we want the mapplet to open somewhere particular some day
  my $frame = '_top';

  ### holds the maximum number of markers that contain the given name on any panel.
  ### changing~
  my $unique = 0;

  ### the page that will be output, either mapplet or error message or
  ### option help generation, ...
  my $g_opt= ''; # options form (global)

  ### used to pass scafolding notes to the web page
  my $note = "Begin Notes(I)<p>\n";

  ###
  ### Emit a Blank Page if called with no paramerers
  ###
  if( ! $Q->param ) {
    print  $Q->header .
      $Q->start_html("This Page Is Intentionaly Left Blank... go figure").
	  'This Page Is Intentionaly Left Blank' .
	  $Q->end_html."\n"; exit;
  }

  ################################################################################
  ################################################################################
  ### Begin Frame_Set Exists (which is the same as no frames at all)
  ###
  ### isolate panels of interest, (undefined is not false)
  ### and a panel to be edited (if one is available)
  ###
  if( defined $Q->param('edit_panel') and  $Q->param('edit_panel')) {
    $edit_panel = $Q->param('edit_panel');
  }else{ $edit_panel = '';}

  #$note = $note . "All Panels \n<p>";
  for $panel (@allpanels) {
    $note .= "$panel  \n <p>";
    ### if loc_panel handles the crossview request for an entire LG for one panel,
    ### prior to frameless this was hanlded by setting the panel to 1 in qstring
    ### when building a new frameset
    if(defined $Q->param("loc_panel") && $panel eq $Q->param("loc_panel"))  {
      $Q->param($panel,1);
    }
    if( (defined $Q->param($panel))
	&& ($Q->param($panel) == 1)
	&& ($panel ne $edit_panel )) {
      push(@panels, $panel);$Q->param($panel,1);
      #$note .= "\tpush $panel <br>";
    }
    elsif($panel ne $edit_panel ) {
      #$note .= "\tset $panel = 0 <br>";
      $Q->param($panel,0);
    }
  }
  #if no panels are implicated -- select them all
  if(length(@panels) < 1 && (!$edit_panel) ) {
    #$note = $note ." short sheeted! \n <P>";
    for $panel (@allpanels) {
      push(@panels, $panel);
      $Q->param($panel,1);
    }
  }
  #$note = $note . "panel-string -is $panels_string \n <P>";
  #$note = $note . "Using |@panels| and |$edit_panel| <P>\n";

  ###  Coming from a "external"  unisource  page,

  ###  markerlister -- view_map, marker, {panel(s)}
  ###  genelister   -- view_map, marker, {panel(s)}
  ###  markerview   -- view_map, marker, OID,
  ###  crossview    -- view_map,(panel),lg

  ###  mapperselect -- view_map,
  ###                            markername
  ###                        OR
  ###                            panel, lg (depreciated)
  ###                        OR
  ###                            panel, lg, location


  if( (defined $Q->param("view_map")) && ($g_error == 0) ) { # view_map is defined and no error reported

    ### types is never defined by an external page so use them all.
    $types = "SSLP\',\'RAPD\',\'RFLP\',\'STS\',\'SNP\',\'GENE\',\'GENEP\',\'BAC\',\'PAC'\,\'BAC_END\',\'PAC_END'\,\'EST\',\'CDNA\',\'GENO\',\'MUTANT";
    #$types =  $types . ",\'" . $anon_type  . ",\'" . $gene_type  . ",\'" . $est_type . ",\'" . $bac_type . ",\'" . $fish_type . "\'" ;
    #$note = $note . "\n" . $types .  "\n"; print note;

    if( ( !(defined $Q->param("OID")) ) || ($Q->param("OID") =~ '') ) { # parse it as  ZDB-type-date-nunber  ?
      ### coming from some single source search
      ### (mapperselect name or near or lg ...)
      ### check sutibility of given marker name if given
      ### if not unique shunt off to "search results"
      ### they can return to their specific marker from there
      ### if there is no such marker just say so

      if( (defined $Q->param("marker")) && ($Q->param("marker")) ) {
	$marker = lc($Q->param("marker"));
    undef $rowref;
	$rowref = check_uniq($marker);
	$unique = (defined $$rowref[0][0])? @$rowref: 0;

    $note = $note . "\trow ref length " . $unique . "\n";
	###
	### yow!  too many answers
	###
	if( $unique > 1) {	#defined @$rowref[1] ){ # # not unique shunt off to search result page
	 ### $note = $note . $unique . " ->Too Many Choices  <p>\n";
	  my $bot = LWP::UserAgent->new();
	  my $req = POST 'http://<!--|DOMAIN_NAME|-->/webdriver',
	  [   compare=> 'contains',
	      marker_type=> 'all',
	      lg=> 0,
	      refcross=> 'NULL',
	      plinks=> 'on',
	      action=> 'SEARCH',
              paged_by=> 'mapper',
              map_type=> 'individual',
	      MIval=> 'aa-newmrkrselect.apg',
              query_results=> 'exist',
	      input_name=> "$marker",
	      zfin_login=> $Q->cookie('zfin_login')
	  ];
	  print "Content-Type: text/html; charset=ISO-8859-1\r\n\r\n";
	  my $res = $bot->request($req);

      # check the outcome
      if ($res->is_success) {print $res->content . "\n";}
      else { print "Error: " . $res->status_line . "\n";}
	  exit 1;
	}
	###
	### huh? got nothing, advance to re-try do not pass map
	###
	elsif($unique < 1) { #! defined $rowref ){ #
	  print $Q->header(). "\n".
		 $Q->start_html(-TITLE => "ZFIN View ZMAP", -bgcolor=> 'white')."\n".
		 "<script type=\"text/javascript\" src=\"/header.js\"></script>" ."\n";
      mapper_select(Q);
      print
	     "<p><p><p><p>No mapping data is available for ".
		 "\"<font color=red><i><b>$marker</b></i></font>\"\n<p><p><p>".
		 "<script type=\"text/javascript\"' src=\"/footer.js\"></script>";
	  exit 1;
	}
	###
	### ding! add the lg and zdbid for the unique marker found (or approximated with "contains")
	###
	else {
      $unique = 1;
	  $zdbid  = $$rowref[0][0];
	  $lg     = $$rowref[0][1];
	  #$note = $note . $marker. " -> " .$zdbid . " on LG ". $lg . " Found as unique enough"."\n";
	  $Q->param("OID", $zdbid);
	  $Q->param("lg", $lg);
	}
	### selectmap gets re-populated with $marker
	$sm_m =  $marker;
	$sm_refresh = 1;
      }
      ### end from a single marker textstring
      ###
      ### Begin from a single location source.
      ### Panel, LG and maybe [near location]
      ###
      elsif( defined $Q->param("loc") &&  $Q->param("loc")!= ''  )  {
	$sm_loc =  $Q->param("loc");
	if (defined $Q->param("loc_panel")) {
	  $sm_panel = $Q->param("loc_panel");

	}
	$sm_lg = $g_lg =  $Q->param("loc_lg");
	($zdbid, $marker, $loc) = get_closest( $sm_panel, $sm_lg, $sm_loc );

	if(defined $zdbid) { $Q->param("OID",$zdbid);}
    #$sm_m = get_OIDs_abbrev($zdbid); # ???
    if ($sm_refresh != 2) { $sm_refresh = 1;}
	if(defined $marker) { $Q->param('marker=',$marker);}
	$Q->param("lg", $Q->param("loc_lg"));
    #selectmap gets re-populated with a  map location
    $sm_refresh = 2;
      }
      $g_zdbid =  $Q->param("OID");
      #$sm_refresh = 1;
    }#~ marker not unique
    if (defined $Q->param("loc_lg")){
        $note =  $note ."@panels and ". $Q->param("loc_lg")." <p>\n";
    }else{
        $note =  $note ."@panels and LG not defined <p>\n";
    }
    ### We now know that there are not multiple interperations of $marker
    ### because there is a OID otherwise
    ### they would have been shunted off to either redo or search results page.
    ###

    if( (defined $Q->param("OID")) && ($Q->param("OID")) )  {
      $g_OID = $zdbid  =  $Q->param("OID");
      $sm_m = get_OIDs_abbrev($zdbid);
      if ($sm_refresh != 2) { $sm_refresh = 1;}
      if( defined $Q->param("lg") && $Q->param("lg") ) {$lg =  $Q->param("lg");}
      else {			### need a linkage group
	#get OID lg
	$lg = get_OIDs_lg($zdbid) ;
	$note = $note . "found $marker to be unique on LG $lg as $zdbid  \n";
      }
      $sm_lg = $lg;
            #$note = $note . "Arrived from somewhere that resolves to marker $zdbid on $lg <p>\n";
            #$note = $note . "Will look for $types nearby<p>\n";

      ### find closest markers to zdbid on given panels
      ### expects globals  $types and  @panels to exist

      $g_data = uni_query($lg, $zdbid );
    }

    ###
    ### or they need the whole LG on selected panels...
    ###

    elsif( (defined $Q->param("loc_lg")) && ($Q->param("loc_lg")) ) {
      # asking for a whole linkage group
      $sm_lg = $lg = $Q->param("loc_lg");
      $Q->param('lg',$lg);
      #$g_width = $g_height = 0;
      #$note = $note . "LG QUERY <p>\n";
      foreach $panel (@panels) {
	$Q->param($panel.'_ztotal',1);
    $note = $note . "being asked for all of $lg on $panel <p>\n";
	$g_data =  $g_data . lg_query ( $panel ,$lg );
	$zooms{$panel} = $zoom; #zoom hash
	$lgs{$panel} = $lg; #lg hash
	$note = $note ." ztotal for lg $lg on $panel  is ". $Q->param($panel.'_ztotal') . "<p>\n";
      }
    }
    if(!$g_data) {		### all empty backbones
      $note = $note . "\tNo Markers found on array @panels  <p>\n";
      if ($g_error < 1){$g_error = 0;}
    }
    if ($sm_refresh != 2) { $sm_refresh = 1;}
  }
  ### end coming from a "view_map" source with no errors
  ###############################################################################
  ###
  ###
  ### comming from OPTIONS page zoom/edit/show  or a 'multi source' page
  ###
  ###
  elsif($g_error == 0)  {# comming from options page
    $note =  $note. " Multi Source Query with |$edit_panel| selected <p>\n";

    ## all the panels that are along for the ride are being re-calculated
    ### REFRESH
    foreach $panel (@panels) {
      $types='SSLP'; $print_type = 0;
      if($Q->param($panel.'_gene') && $Q->param($panel.'_gene')==1){$types = "$types\',\'$gene_type"; $print_type += 1;}
      if($Q->param($panel.'_est')  && $Q->param($panel.'_est')==1) {$types = "$types\',\'$est_type"; $print_type += 2;}
      if($Q->param($panel.'_anon') && $Q->param($panel.'_anon')==1){$types = "$types\',\'$anon_type"; $print_type += 4;}
      if($Q->param($panel.'_fish') && $Q->param($panel.'_fish')==1){$types = "$types\',\'$fish_type"; $print_type += 8;}
      if($Q->param($panel.'_bac')  && $Q->param($panel.'_bac')==1)    {$types = "$types\',\'$bac_type"; $print_type += 16; }
      if($print_type == 0){ $types = "SSLP\',\'RAPD\',\'RFLP\',\'STS\',\'GENE\',\'GENEP\',\'BAC\',\'PAC\',\'BAC_END\',\'PAC_END\',\'EST\',\'CDNA\'\'GENO\',\'MUTANT"; $print_type += 31;}

      $note = $note . " will be finding  $types  markers on this refresh panel<p>\n";
      ### hidden panel, lg, lo,hi & zoom re-emited as hidden vars
      $note = $note . "Refreshing $panel LG ".$Q->param($panel.'_lg')." between ".$Q->param($panel.'_lg_lo')." & ".$Q->param($panel.'_lg_hi')."<p>\n";

      ### hack to protect bad refreshes that should not be getting here but are
      if( defined $Q->param($panel.'_lg') &&
	  defined $Q->param($panel.'_lg_lo')&&
	  defined $Q->param($panel.'_lg_hi') ) {

	$lg_lo = $Q->param($panel.'_lg_lo');
	$lg_hi = $Q->param($panel.'_lg_hi');
	$lg =    $Q->param($panel.'_lg');

	$zoom = get_between( $panel, $lg, $types, $lg_lo, $lg_hi );
	$zooms{$panel} = $zoom; #zoom hash
	$lgs{$panel} = $lg; #lg hash

	if ($zoom > 0) {
	  #$curbetween->finish;
	  $g_height = ($g_height < $zoom)? $zoom : $g_height;
	  ### add room in applet for another backbone
	  $g_width += 320;	#bbw
	  $g_opt =  $g_opt .
	    ### -- do display this panel
	    "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"1\">\n".
	      "<INPUT TYPE=\"hidden\" NAME=\"".$panel."_ztotal\" VALUE=".$Q->param($panel.'_ztotal') .">\n".
		$Q->hidden($panel . '_lg',  $lg) . "\n".
			  $Q->hidden($panel . '_lg_lo', $lg_lo) . "\n".
			    $Q->hidden($panel . '_lg_hi', $lg_hi) . "\n".
			      $Q->hidden($panel.'_zoom',$zoom ) . "\n".
				$Q->hidden($panel.'_from_BETWEEN', 1) . "\n";
	}else{			### empty backbone
	  ### close the option block for this panel?
	  #$error &= $no_data;
	  $g_opt =  $g_opt . "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"0\">\n";
	  $Q->param($panel, 0);	# turn off check box for this panel in mapperselect form
	}

	### find and forward any other hidden vars for this particular panel
	#pass_hidden_panel($panel);
	my @vars =
	  ('_OID','_m','_or','_near_loc','_m_lo','_m_hi','_gene','_est','_anon','_fish','_ztotal');
	my $var;
	foreach $var(@vars){
	  if($Q->param($panel.$var)){
	    $g_opt = $g_opt .  $Q->hidden($panel. $var,  $Q->param($panel. $var)). "\n";
	  }
	}
       	#$g_opt = $g_opt . $Q->hidden($panel."_from_REFRESH")."\n"; # bread crumbs
      } else {$Q->param($panel. 0);} ### should nor get here
    }
    ### there is at most one panel left and that is the one being altered
    ### could be "SEEK" in which case we punt for now (if OID on lg: return n-closest, else return lg)
    ### could be "ZOOM" in which case the zoom we grab the n' closest to OID
    ### could be "EDIT" in which case the path of the old options page is followed
    ### could be a HIDE in which case we are done.
    $panel = $edit_panel;
    if ($panel) {
      $types='SSLP'; $print_type = 0;
      if($Q->param($panel.'_gene') && $Q->param($panel.'_gene')==1){$types = "$types\',\'$gene_type"; $print_type += 1;}
      if($Q->param($panel.'_est') && $Q->param($panel.'_est')==1)  {$types = "$types\',\'$est_type"; $print_type += 2;}
      if($Q->param($panel.'_anon') && $Q->param($panel.'_anon')==1){$types = "$types\',\'$anon_type"; $print_type += 4; }
      if($Q->param($panel.'_fish') && $Q->param($panel.'_fish')==1){$types = "$types\',\'$fish_type"; $print_type += 8; }
      if($Q->param($panel.'_bac')  && $Q->param($panel.'_bac')==1)    {$types = "$types\',\'$bac_type"; $print_type += 16; }
      if($print_type == 0){ $types = "SSLP\',\'RAPD\',\'RFLP\',\'STS\',\'GENE\',\'GENEP\',\'EST\',\'CDNA\'\'BAC\',\'PAC\',\'BAC_END\',\'PAC_END\',\'GENO\',\'MUTANT"; $print_type += 31;}

      $note = $note . " will be finding  $types  markers on the edit panel\n";
      $Q->param($panel, 1);

      if( ! defined $Q->param($panel.'_zoom') && ! defined $Q->param($panel.'_or') ) {
	### SEEK(un-hide)
	$note = $note . " <br>SEEK! on |".$panel ."|<p>\n";

	$lines = '';
	if( $Q->param('OID')){
	  $note = $note . "check for |".$Q->param('OID')."| on LG |". $Q->param('lg') . "|\n";
	  ### expects global $types to exist
	  $lines = uni_query( $Q->param('lg'), $Q->param('OID'), $panel ); ###
	}
	if(! $lines){
	  $Q->param($panel.'_ztotal',1);
	  $note = $note . "Get all of LG |". $Q->param('lg') . "|\n";
	  $g_data =  $g_data . lg_query( $panel, $Q->param('lg') );
	  $zooms{$panel} = $zoom; #zoom hash
	  $lgs{$panel} = $lg; #lg hash
	}else {
	  $g_data = $g_data . $lines;
	  $note = $note ."got some near ".$Q->param('OID')."<p>\n";
	}
	$sm_refresh = 2;
	### print?
	$g_printdata =
	  $g_printdata .
	    $panel .'|'.
	      $Q->param($panel.'_lg').'|'.
		$Q->param('OID').'|'.
		  $Q->param($panel.'_lg_lo').'|'.
		    $Q->param($panel.'_lg_hi').'|'.
		      $print_type .'|'.
		      "\n";
      }#~seek
      elsif( defined $Q->param($panel.'_zoom') && $Q->param($panel.'_zoom') < 0) {### ZOOM
		$note = $note . " ZOOM! \n";
	$zoom =  abs($Q->param($panel.'_zoom'));
	$zoom = ($zoom > $Q->param($panel.'_ztotal')) ? $Q->param($panel.'_ztotal'): $zoom;
	$Q->param($panel.'_zoom', $zoom );
		$note = $note . " ZOOMING towards  |$zoom| markers on |$panel|<p>\n";
	$lg_lo = $Q->param($panel.'_lg_lo');
	$lg_hi = $Q->param($panel.'_lg_hi');
	$lg = $Q->param($panel.'_lg');

	if( ! defined $Q->param($panel.'_OID')) {
	  ($zdbid, $marker, $loc) = get_closest(
						$panel,
						$Q->param($panel.'_lg'),
						$lg_lo + ($lg_hi - $lg_lo) / 2.0
					       );
	  $Q->param($panel.'_OID', $zdbid);
	  	  $note = $note . "PICKING |$zdbid| (|$marker|) on |$panel| at |$loc| to zoom about<p>";
	}else {
        $loc = defined $Q->param($panel.'_loc')? $Q->param($panel.'_loc'): '';
    }
	$zdbid = $Q->param($panel.'_OID');
		$note = $note . " Zoom is  centered on $zdbid at |$loc|<p>\n";
		$note = $note ." ZOOMPARAMS:: |$zdbid|, |$lg|, |$panel|, |$zoom|, |$loc| pre-lo $lg_lo 7 pre-hi $lg_hi <p>\n";
	($lg_lo, $lg_hi) = get_zooms ($zdbid, $lg, $panel, $zoom, $loc );
	$note = $note . " Zoom is  centered on  $zdbid at |$loc|<br>\n";
	$note = $note ."ZOOMRETURNS:: |$lg_lo|, |$lg_hi| <p>\n";
 	$Q->delete($panel.'_lg_lo','');
	$Q->delete($panel.'_lg_hi','');
	$zoom = get_between($panel, $lg, $types, $lg_lo, $lg_hi);
	$zooms{$panel} = $zoom; #zoom hash
	$lgs{$panel} = $lg; #lg hash
	if ($zoom >= 0){
	  $g_height = ($g_height < $zoom)? $zoom : $g_height;
	  ### add room in applet for another backbone
	  $g_width +=320;	#bbw
	  $g_opt =  $g_opt .
	    ### -- do display this panel
	    "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"1\">\n".
	      "<INPUT TYPE=\"hidden\" NAME=\"".$panel."_ztotal\" VALUE=".$Q->param($panel.'_ztotal') .">\n".
		$Q->hidden($panel . '_lg',  $lg) . "\n".
		  $Q->hidden($panel . '_lg_lo', $lg_lo) . "\n".
		    $Q->hidden($panel . '_lg_hi', $lg_hi) . "\n".
		       $Q->hidden($panel . '_gene',   $Q->param($panel.'_gene')) . "\n".
		    $Q->hidden($panel . '_est',   $Q->param($panel.'_est')) . "\n".
		      $Q->hidden($panel . '_anon',   $Q->param($panel.'_anon')) . "\n".
			$Q->hidden($panel . '_fish',   $Q->param($panel.'_fish')) . "\n".
            $Q->hidden($panel . '_bac',   $Q->param($panel.'_bac')) . "\n".
		      $Q->hidden($panel.'_zoom', $zoom ) . "\n";
	} else{			### empty backbone
	  ### close the option block for this panel?
	  $error &= $no_data;
	  $Q->param($panel, 0);	# turn off check box for this panel in mapperselect form
	}
      }# ~ zoom #####################
      else {### EDIT
	# there is a potential change in the number of markers on the backbone, heck they can change lgs
	# so reset ztotal
        $lg      = $Q->param($panel."_lg");
	if ( $lg =~/\?\?/) { undef $lg;}
	else {$ztotal =  get_total($panel, $lg, $types);}

	$Q->param($panel.'_ztotal', $ztotal );

		$note = $note . " EDIT! \n";
	$or_flag = $Q->param($panel."_or");
	$lg_lo   = $Q->param($panel."_lg_lo");
	$lg_hi   = $Q->param($panel."_lg_hi");
	$loc     = $Q->param($panel."_near_loc");
        if (!$lg) { $lg = $Q->param('loc_lg');}

	######################################################################################
	if($or_flag =~ /Location/) {
	  	  $note = $note . "\tLOCATION <p>\n";
	  $lg_or_flag =  $Q->param($panel. '_lg_or');

	  if (
	      (($lg_or_flag =~ /units/) && ((! defined $lg_lo) || (! defined  $lg_hi)) )
	      ||
	      (($lg_or_flag =~ /near/ ) && (! defined  $loc))
	     ){#must be asking for the whole LG
	    $Q->param($panel.'_ztotal',1);
	    $note = $note . "Get all of LG |". $Q->param('lg') . "|\n";
	    $g_data =  $g_data . lg_query( $panel, $lg ); #(types)
	    $zooms{$panel} = $zoom; #zoom hash
	    $lgs{$panel} = $lg; #lg hash
	  }
	  elsif($lg_or_flag =~ /units/) {# lg flag is units
	    	    $note = $note . "\t\t\tOPTION FORM BETWEEN <p>\n";
	    $zoom = get_between($panel, $lg, $types, $lg_lo, $lg_hi);
	    $zooms{$panel} = $zoom; #zoom hash
	    $lgs{$panel} = $lg; #lg hash
	    if ($zoom > 0){
	      $g_height = ($g_height < $zoom)? $zoom : $g_height;
	      ### add room in applet for another backbone
	      $g_width +=320;	#bbw
	      $Q->param($panel, 1);
	      $g_opt =  $g_opt .
		### -- do display this panel
		"<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"1\">\n".
		  "<INPUT TYPE=\"hidden\" NAME=\"".$panel."_ztotal\" VALUE=".$ztotal .">\n".
		    $Q->hidden($panel . '_lg',  $lg) . "\n".
		      $Q->hidden($panel . '_lg_lo', $lg_lo) . "\n".
			$Q->hidden($panel . '_lg_hi', $lg_hi) . "\n".
			   $Q->hidden($panel . '_gene',   $Q->param($panel.'_gene')) . "\n".
		    $Q->hidden($panel . '_est',   $Q->param($panel.'_est')) . "\n".
		      $Q->hidden($panel . '_anon',   $Q->param($panel.'_anon')) . "\n".
			$Q->hidden($panel . '_fish',   $Q->param($panel.'_fish')) . "\n".
            $Q->hidden($panel . '_bac',   $Q->param($panel.'_bac')) . "\n".
			  $Q->hidden($panel.'_zoom', $zoom ) . "\n".
			    $Q->hidden($panel.'_from_BETWEEN', 1) . "\n";
             $sm_refresh = 1;
	    }else {		### empty backbone
	      ### close the option block for this panel?
	      $error &= $no_data;
	      $g_opt =  $g_opt . "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"0\">\n"; #$Q->hidden($panel, 0). "\n";
	      $Q->param($panel, 0); # turn off check box for this panel in mapperselect form

	    }
	  }# ~ lg_flag is units
	  ### the call to get_between should:
	  ###   be adding rows to g_data
	  ###   adjusting lo & hi to what it finds
	  ###  adding bbw to applet width
	  ###  $or_flag stays Location

	  ##########################################
	  elsif($lg_or_flag =~ /near/ ) { #  just turn the 'near query' into a 'marker query'
	    $note = $note . "\t\tNEAR $loc on <p>\n";
	    ($zdbid,$marker,$loc) = get_closest($panel,$lg,$loc);
	    $Q->param($panel."_OID", $zdbid);
	    $Q->param($panel."_m", $marker );
	    $Q->param($panel."_loc", $loc);
	    $Q->param($panel.'_m_lo','');
	    $Q->param($panel.'_m_hi','');
	    $or_flag = 'Marker';
	  }
	}# end location set
	###################################################################################
	if( $or_flag  && ($or_flag =~ /Marker/ )) {
	  	  $note = $note . " MARKER on $panel  <p>\n";
	  ### need to have (unique)marker _m_lo & _m_hi (distances)
	  ### find if marker exists uniquely on this panel
	  $marker = $Q->param($panel."_m");
	  $m_lo =   $Q->param($panel."_m_lo");
	  $m_hi =   $Q->param($panel."_m_hi");

	  if(! $zdbid ) {
	  $note = $note . "checking $marker, $panel , $lg for uniqueness \n";
        undef $rowref;
	    $rowref  = check_uniq($marker, $panel );
	    #$unique  = (defined $$rowref[0][0])? @$rowref: 0;
	    if(defined $rowref && (! defined @$rowref[1]) ){ #
          $unique = 1;
	      $zdbid = $$rowref[0][0];
	      $lg =    $$rowref[0][1];
	      #$loc =   $$rowref[0][2];
	      $ztotal =  get_total($panel, $lg, $types);
	      #$Q->param( $panel.'_lg_near', $loc);
	      $Q->param($panel."_lg", $lg);
	    }
	  }else{$unique = 1;}
	  if($unique == 1) {
	    if( (! $m_lo ) || (! $m_hi )) { #reverts to a standard fetch
	      #***********************************************************
	      $lo = $hi = $m_hi = $m_lo = '';
	      $lg_hi = $lg_lo = $error = '0';

	      ### expects $types & $zoom as globals
	      $lines = uni_query($lg, $zdbid,$panel);
	      	$note = $note . "\tLooking on $panel \n";
	      if( $lines ){	### if we have some map to display; =~ tr/\0// /
		### add room in applet for another backbone
		$g_data = $g_data . $lines;
				$note = $note . "\tFound markers on $panel  <p>\n";
		$g_opt =  $g_opt .
		  ### name of the marker on this $panel
		  $Q->hidden($panel . '_m', $marker) . "\n".
		    $Q->hidden($panel. '_OID', $zdbid) . "\n";
	      }else {		### empty backbone
		### close the option block for this panel
				$note = $note . "\tNo markers found on $panel  <p>\n";
		$error  &=  $no_data;
	      }
	    }			###############################
	    else {		# has a specified range
	      $loc = $Q->param($panel.'_lg_near');
	      $lg_lo = $loc - $m_lo;
	      $lg_hi = $loc + $m_hi;
	      $zoom = get_between($panel, $lg, $types, $lg_lo, $lg_hi);
	      $zooms{$panel} = $zoom; #zoom hash
	      $lgs{$panel} = $lg; #lg hash
	      if ($zoom > 0){
		$g_height = ($g_height < $zoom)? $zoom : $g_height;
		### add room in applet for another backbone
		$g_width +=320;	#bbw
		### make sure we have the correct total markers
		$ztotal = get_total($panel, $lg, $types);
        $Q->param($panel.'_ztotal', $ztotal);

		$g_opt =  $g_opt .
		  ### -- do display this panel
		  "<INPUT TYPE=\"hidden\" NAME=\"".$panel."_ztotal\" VALUE=".$Q->param($panel.'_ztotal') .">\n".
		    $Q->hidden($panel . '_lg',  $lg) . "\n".
		      $Q->hidden($panel . '_lg_lo', $lg_lo) . "\n".
			$Q->hidden($panel . '_lg_hi', $lg_hi) . "\n".
			   $Q->hidden($panel . '_gene',   $Q->param($panel.'_gene')) . "\n".
		    $Q->hidden($panel . '_est',   $Q->param($panel.'_est')) . "\n".
		      $Q->hidden($panel . '_anon',   $Q->param($panel.'_anon')) . "\n".
			$Q->hidden($panel . '_fish',   $Q->param($panel.'_fish')) . "\n".
            $Q->hidden($panel . '_bac',   $Q->param($panel.'_bac')) . "\n".
			  $Q->hidden($panel.'_zoom', $zoom ) . ";\n"
	      }else{# zoom == 0			### empty backbone
		### close the option block for this panel?
		$error &= $no_data;
		$Q->param($panel, 0); # turn off check box for this panel in mapperselect form
	      }
	    }
	  }
	}
	##################################
	$sm_refresh = 2;
      }				### end else edit
    }
    else {			### just hid a panel
      $sm_refresh = 2
    }
    ### if no mappings were found on this subset of panels indicate it
    if (! $g_data  && $g_error < 1){$g_error &= 1;}

  }				# end from options
  ################################################################################
  ################################################################################
  ################################################################################
  ### build up the "Mapplet" page
  $note = $note . " <p> END NOTE <P>\n";
  print $Q->header . "\n";
  print $Q->start_html(-TITLE => "ZFIN View Map", -bgcolor=> 'white')."\n";
  print "<script type=\"text/javascript\" src=\"/header.js\"></script>";

  ### based on error codes emit the dynamic part of the page
  if ($g_error == 0 && $g_data) {
    $g_height *= 13;
    $g_height += 95;
    if( $g_height < 210){$g_height = 210;}

    mapper_select(Q);

    ###
    ### -- wrap the pair of buttons in a table to keep them on one line.
    ###
    print  "<table border=0><tr>";
    ### emit a print button
    ### emit an options button
    print "<td> ".$Q->start_form (
				  -method=>'POST',
				  -action=>'/cgi-bin/map-options.pl',
				  -encoding=>'application/x-www-form-urlencoded',
				  -name=>'optform'
				 )."\n".
				   ### emit the set of constant values the option page  needs
				   pass_hidden()."\n".
				     ### emit the dynamic option page values collected from the various db queries
				     $g_opt."\n".
				       $Q->submit(-name=>"Hide / Show / Adjust a Panel",-align=>'right')."\n".

					$Q->end_form."\n</td>";
    print '<td><img src=/client_apps/Map/mapkey.gif>'."\n";

    my $POprint ='';
    for $panel (@allpanels) { $POprint = $POprint . $panel . "|"; }### kevin's code

    print "</td><td>" . $Q->start_form (
				  -method=>'GET',
				  -action=>'/cgi-bin/print_map.cgi',
				  -encoding=>'application/x-www-form-urlencoded',
				  -name=>'print_map',
				  -target=>'print'
				 ). "\n".
				   $Q->submit(-name=>"New Window with Printer Friendly  Map") . "\n".
				     $Q->hidden("height",$g_height)."\n".
				       $Q->hidden("width" ,$g_width)."\n".
  				         $Q->hidden("panel_order",$POprint)."\n".
					   $Q->hidden("data",$g_printdata )."\n".
					     $Q->end_form."\n </td>\n";


    print "</tr></table>";

    ### ~KS
    ### print zoom buttons and percentages
    ###


    my $ztot = 0;
    my $z = 0;
    my $newz = 0;
    my $percent = 0;
    my $order_increment=0;

    print "<form name='foo' action='null' method='GET'>";
    #print $Q->start_form (-method=>'GET',-action=>'null',-name=>'zoom');
    #print $Q->submit();
    print "\n\n<table width=$g_width border=0 cellpadding=0 cellspacing=0><tr>";

    for $panel (@allpanels) {
      if ((defined $Q->param($panel.'_ztotal')) && ($Q->param($panel) == '1')) {
	$ztot = $Q->param($panel.'_ztotal');
	$z = $zooms{$panel};
	$lg = $lgs{$panel};

	#zoom out
	$newz = $z + 20;
	if ($newz > $ztot) { $newz = $ztot; }
	print "\n<td><input type=button value=\"Zoom Out\"".
    " onClick=\"document.optform.edit_panel.value='" . $panel ."';".
    " document.optform." . $panel . "_zoom.value = '-".$newz ."';".
    " document.optform.refresh_map.value = 1;".
    " document.optform.action='view_mapplet.cgi';".
    " document.optform.target='';".
    " document.optform.submit();\">\n\n";

	#draw percentage

	$percent = int $z * 100 / $ztot;
	if ($percent < 1)
	  { $percent = 1; }
	if ($percent > 100)
	  { $percent = 100; }
	print "&nbsp;" . $percent . "% &nbsp;";

	#zoom in
	if ($z > 25) { $newz = $z - 20; } else { $newz = $z - 10; }
	if ($newz < 5) {$newz = 5;}
	print "<input type=button value=\"Zoom In\" ".
    " onClick=\"document.optform.edit_panel.value='" . $panel . "';".
    " document.optform." . $panel . "_zoom.value = '-" . $newz . "';".
    " document.optform.refresh_map.value = 1;".
    " document.optform.action='view_mapplet.cgi';".
    " document.optform.target='';".
    " document.optform.submit();\">";

	print "<br><font size=-1><b>&nbsp;&nbsp;".
    "<a href=\"/cgi-bin/webdriver?MIval=aa-crossview.apg&".
    "OID=" . $allpanels_id[$order_increment]."\"".
    ">". $panel . "</a>".
    " panel, LG: " . $lg . ", units: " . $allpanels_metric[$order_increment] .
    "</b></font></td>";
	$order_increment++;
      } else { $order_increment++; }
    }
    print "</tr>\n";
    print "</table></form>\n\n";

    ###
    ### call the applet
    ###
    print
      "<APPLET \n".
	" code\t   = \"mapplet.class\"\n".
	  " archive  = \"mapplet-1.0.jar\"\n".
	    " codebase = \"/client_apps/Map/\"\n".
	      " height\t = \"$g_height\"\n".
		" width\t  = \"$g_width\"\n".
		  " mayscript>\n";

    #passing panel order
    print "<param name = \"panel_order\" value = \"";
    for $panel (@allpanels) { print $panel . "|"; }
    print "\">\n";
      for $panel (@panels){

      if(defined $Q->param($panel.'_ztotal') ){ #&& $Q->param($panel.'_ztotal')>0){
	print "<param name = \"" . $panel . "_ztotal\"\t value = ". $Q->param($panel.'_ztotal').">\n";
      }
    }
    if (defined $edit_panel && $edit_panel) {
      print "<param name = \"" . $edit_panel . "_ztotal\"\t value = ". $Q->param($edit_panel.'_ztotal').">\n";
    }
    if (! defined  $Q->param('OID')){$Q->param('OID', ''); }
    print   "<param name = \"marker_url\"\t value = \"/cgi-bin/webdriver?MIval=aa-markerview.apg&OID=\">\n".

      "<param name = \"panel_url\"\t value = \"/cgi-bin/webdriver?MIval=aa-crossview.apg&OID=\">\n".

        "<param name = \"target_frame\"\t value = \"$frame\">\n".
          "<param name = \"selected_marker\"\t value = \"". $Q->param('OID')."\">\n".
		"<param name = \"geno_url\" value = \"/cgi-bin/webdriver?MIval=aa-genotypeview.apg&OID=\">\n".
		     "<param name = \"zoom_url\" value = \"/cgi-bin/view_mapplet.cgi\">\n".
		    "<param name = \"data\"\t\t value = \"$g_data\">\n".
		      "</APPLET><p>\n";
  }
  else{				# mistakes were made...
    ###
    print "<P><H3><font color = red>No Map Data Returned </font></H3><p>\n";
    print $g_error  . "<p>\n";
    print $g_data   . "<p>\n";
  }

  print $note ."<P><P>\n" if ($print_note == 1);

  ### if a first query or  option/edit put select map back
#  if( defined $sm_refresh && $sm_refresh > 0 ) {
#    print $Q->start_form (
#			  -method=>'POST',
#			  -action=>'/cgi-bin/webdriver',
#			  -encoding=>'application/x-www-form-urlencoded',
#			  -name=>'selectform',
#			  -target=>'criteria'
#			 )
#      . $Q->hidden("MIval","aa-mapperselect.apg"). "\n";
#    for $tmp (@allpanels){
#        print $Q->hidden($tmp, (defined $Q->param($tmp))? $Q->param($tmp) :0) . "\n";
#    }
#    if ( ($sm_m ||  $Q->param('name')) && ($sm_refresh == 1) ) {

#      print  $Q->hidden('marker', ($sm_refresh == 1)? $sm_m:  $Q->param('name')). "\n";
#    }else {
#      print  $Q->hidden('loc_lg', ($sm_refresh >= 1)? $sm_lg: $Q->param('lg')) . "\n".
#	$Q->hidden('loc_panel',($sm_refresh >= 1)? $sm_panel:$Q->param('edit_panel')) . "\n".
#	  $Q->hidden('loc ', ($sm_refresh >= 1)? $sm_loc:$Q->param('loc')) . "\n";
#    }
#    print $Q->hidden('map_type','individual')."\n".
#	$Q->end_form . "\n";
#    print "\n<SCRIPT> document.selectform.submit()</SCRIPT>\n";
#  }

  print "<script type=\"text/javascript\" src=\"/footer.js\"></script>";
  print  $Q->end_html."\n";
  $dbh->disconnect;

  exit;
  ################################################################################

  ### functions to get data for maps
  ### first is uniquery attempts to find an appropiate map from several panels for
  ### a single unique marker on a particular linkage group

  ### get_between is used to get a map for a single panel between known locations
  ### get_zoom
  ### get_lg returns a single linkage group for a panel

  ################################################################################
  ################################################################################
  ################################################################################
  sub uni_query {
    my ($lg, $zdbid, $pan) = @_; # , $types
    my @panel_set;
    my @dud;
    my $data = '';
    if((defined $pan) && $pan){
      push (@panel_set, $pan);
    }else{@panel_set = @panels;}

    #table paneled_markers  (
    # 1) zdb_id varchar(50),
    # 2) abbrev varchar(10),
    # 3) mtype varchar(10),
    # 4) or_lg integer,
    # 5) lg_location decimal(8,2),
    # 6) metric varchar(5),
    # 7) target_abbrev varchar(10),
    # 8) mghframework boolean
    # 9) map_name

    my $stmt1 =
      'INSERT INTO PANLG ' .
      '   SELECT zdb_id, abbrev, mtype, target_abbrev, lg_location, or_lg, ' .
      '          case mghframework ' .
      '            when "t" then "t"::char(1) ' .
      '            when "f" then "f"::char(1) ' .
      '            else NULL ' .
      '          end, ' .
      '          metric '.
      '    FROM paneled_markers '.
      "    WHERE target_abbrev = ?" . #panel
      '      AND or_lg = ? '.
      "      AND mtype IN (\'$types\');";

    my $stmt2 = 'SELECT FIRST 1 * FROM PANLG WHERE zdb_id = ?;';

    # Note that this query in its original format was deadly to the informix
    # server.  The original format used an ABS() function call instead of a
    # case.  We aren't sure if the new format will fix the problem or not.
    #
    # Update on that: Removiing the ABS did not remove the problem.  IBM
    # suggested changing the mghframework column from boolean to char.
    # Not sure if that will fix the problem either.

    my $stmt3 =
      'SELECT zdb_id, abbrev, mtype, target_abbrev, lg_location, or_lg,' .
      '       mghframework, metric,  abs(lg_location - ?) distance ' .
 #     '       case when lg_location - ? >= 0 then lg_location - ? ' .
 #     '            else ? - lg_location ' .
 #     '       end distance ' .
      '  FROM PANLG ' .
      '  ORDER BY distance;';

    my $stmt4 = 'INSERT INTO pool SELECT * FROM PANLG WHERE lg_location >= ? AND lg_location <= ? ;';

    my $stmt5 =
      'SELECT MIN(lg_location),MAX(lg_location) FROM PANLG t WHERE t.zdb_id IN '.
	'(SELECT zdb_id FROM pool);';
    my $stmt6 = 'INSERT INTO fool SELECT * FROM PANLG WHERE lg_location >= ? AND lg_location <= ? ;';
    my $stmt7 =
      'SELECT  * FROM pool '.	#DISTINCT?
	'UNION '.
	  'SELECT  * FROM fool '. #DISTINCT?
	    'ORDER BY 4,6,5,3,2,7,1,8;';
    #zdb_id,abbrev,mtype,target_abbrev,lg_location,or_lg,mghframework,metric,map_name
    # 1      2       3     4            5            6      7          8     9

    my $rc = 0;
    my ($loc, $lo, $hi);
    my @row;
    my $local_zoom = 0;
    my $ztotal = 1;
    #$note = $note . "\n IN UNIQUERY \n<br>";
    $dbh->{AutoCommit} = 0;	# enable transactions, if possible
    $dbh->{RaiseError} = 1;
    eval {#zdb_id 1, abbrev 2, mtype 3,  target_abbrev 4, lg_location 5, or_lg 6, mghframework 7,metric 8, map_name 9
      $dbh->do("CREATE TEMP TABLE pool " .
	       "  ( " .
	       "    zdb_id varchar(50), " .
	       "    abbrev varchar(15), " .
	       "    mtype varchar(10), " .
	       "    target_abbrev varchar(10), " .
	       "    lg_location decimal(8,2), " .
	       "    or_lg integer, " .
	       "    mghframework char(1), " .
	       "    metric varchar(5) " .
	       "  ) WITH NO LOG ;");
      $dbh->do("CREATE TEMP TABLE fool " .
	       "  ( " .
	       "    zdb_id varchar(50), " .
	       "    abbrev varchar(15), " .
	       "    mtype varchar(10), " .
	       "    target_abbrev varchar(10), " .
	       "    lg_location decimal(8,2), " .
	       "    or_lg integer, " .
	       "    mghframework char(1), " .
	       "    metric varchar(5) " .
	       "  ) WITH NO LOG ;");
      $dbh->do("CREATE TEMP TABLE PANLG " .
	       "  ( " .
	       "    zdb_id varchar(50), " .
	       "    abbrev varchar(15), " .
	       "    mtype varchar(10), " .
	       "    target_abbrev varchar(10), " .
	       "    lg_location decimal(8,2), " .
	       "    or_lg integer, " .
	       "    mghframework char(1), " .
	       "    metric varchar(5) " .
	       "  ) WITH NO LOG ;");
      # $dbh->do( "CREATE INDEX panlg_zdb_idx ON PANLG(zdb_id)");
      # $dbh->do( "CREATE INDEX panlg_loc_idx ON PANLG(lg_location)");
      #should we try bulding a tmp table for each panel & lg once and keeping it for the transaction .... maybe (24 * 6)  permenant tables i.e HS1,HS2,...
      my $sth1 = $dbh->prepare($stmt1) or return undef ;
      my $sth2 = $dbh->prepare($stmt2) or return undef ;
      my $sth3 = $dbh->prepare($stmt3) or return undef ;
      my $sth4 = $dbh->prepare($stmt4) or return undef ;
      my $sth5 = $dbh->prepare($stmt5) or return undef ;
      my $sth6 = $dbh->prepare($stmt6) or return undef ;
      my $sth7 = $dbh->prepare($stmt7) or return undef ;

      foreach $panel  (@panel_set){
	$ztotal = $sth1->execute($panel, $lg);
	#	$note = $note . "$panel has $ztotal rows for LG $lg<p>\n";

	# try having the default region returned be dependent on
	# the number of markers available in the lg
	#

	if( $ztotal >= $g_zoom) {
	  $local_zoom = $g_zoom;
	}
	else{
	  $rc = $ztotal / 10;
	  $local_zoom = ($rc> 5)? $rc : 5;
	}

	#	$note = $note . "Local Zoom ~ $local_zoom<p> \n";
	$rc  = $sth2->execute($zdbid);
		$note = $note . "probing for $zdbid 's location on $panel ... \n";
	@row = $sth2->fetchrow;
		if( defined($row[4]) ){$note =  $note . "probe retuned with  $row[4] <p>\n";}
		else                  {$note =  $note . "probe retuned nothing <p> \n";}
	if ( @row >= 4 ){
	  	  #$note = $note .  "HIT  on $panel<p>\n";
	  $row[4] =  ($row[4] == 0)? 0 : $row[4]; # clean up 0.00000E+00
	  $loc = $lo = $hi = $row[4];
	  $rc = $sth3->execute($loc); #,$loc,$loc);

	      $note =  $note .  "draping lg ".$lg ." about ". $loc ."<p>\n";
	  $local_zoom = ($Q->param($panel.'_zoom'))? $Q->param($panel.'_zoom') : $local_zoom;

	     $note = $note .  "seeking closest $local_zoom markers to $zdbid at $loc on $panel.<p>\n";

	  for ($zoom = 0; ( $zoom <= $local_zoom) && (@row = $sth3->fetchrow); $zoom ++){
	    if($row[4] < $lo){$lo = ($row[4] == 0)? 0 : $row[4];}
	    if($row[4] > $hi){$hi = ($row[4] == 0)? 0 : $row[4];}
	  }

	     $note = $note . "the interval about $zdbid on lg $lg of $panel runs from $lo to $hi<p>\n";

	  #insert all between and including $lo  $hi into pool of markers near chosen marker
      $hi += .0001; $lo -= .0001; # kludge because bug where (loc >= x AND loc <= x) not (loc == x)
	  $zoom = $sth4->execute($lo, $hi) || die $dbh->errstr;

	  #	  $note = $note . "$zoom rows added to the pool<p>\n";

      #if (0 == $zoom){

	  $g_height = ($g_height < $zoom)? $zoom : $g_height;
	  # emit hidden vars for $panel
	  $g_width += 320;	#bbw
	  $g_printdata = $g_printdata . $panel .'|'.$lg .'|'.$Q->param('OID') .'|'.$lo .'|'.$hi .'|'.$print_type.'|'."\n"; #uniquery1
	  $Q->param($panel,  1);
	  $g_opt =  $g_opt .
	    ### -- do display this primary panel
	    "<INPUT TYPE=\"hidden\" NAME=\"".$panel."\" VALUE=\"1\">\n".
	      ## name of the marker on this $panel
	      $Q->hidden($panel . '_m', $marker) . "\n".
		### send the total number of markers available  so a % of map may be calculated
		### $Q->hidden($panel . '_ztotal', $ztotal) . "\n".
		"<INPUT TYPE=\"hidden\" NAME=\"".$panel."_ztotal\" VALUE=".$ztotal .">\n".
		  $Q->hidden($panel . '_OID', $zdbid) . "\n".
		    ### location of $marker on this $panel
		    $Q->hidden($panel . '_near_loc',$loc). "\n".
		      ### lingage group of $marker on this $panel
		      $Q->hidden($panel . '_lg',     $lg) . "\n".
			### absolute extents of the interval containg $marker
			$Q->hidden($panel . '_lg_lo',  $lo) . "\n".
			  $Q->hidden($panel . '_lg_hi',  $hi) . "\n".
			    ### extents of the interval relative to $marker
			    $Q->hidden($panel . '_m_lo',   $loc - $lo) . "\n".
			      $Q->hidden($panel . '_m_hi',   $hi  - $loc) . "\n".
				 $Q->hidden($panel . '_gene',   $Q->param($panel.'_gene')) . "\n".
		    $Q->hidden($panel . '_est',   $Q->param($panel.'_est')) . "\n".
		      $Q->hidden($panel . '_anon',   $Q->param($panel.'_anon')) . "\n".
			$Q->hidden($panel . '_fish',   $Q->param($panel.'_fish')) . "\n".
				$Q->hidden($panel . '_zoom',   $zoom ) . "\n".
				  $Q->hidden($panel."_from_UNIQUERY_HIT")."\n";
	  $Q->param($panel . '_lg_lo',  $lo);
	  $Q->param($panel . '_lg_hi',  $hi);
	  $Q->param($panel . '_ztotal', $ztotal);
	  $zooms{$panel} = $zoom; #zoom hash
	  $lgs{$panel} = $lg; #lg hash

	}
	else{
	  push (@dud, $panel);
	  #	  $note = $note . "$panel is a dud because $zdbid is not on it<p>\n";
	}
	$rc = $dbh->do("DELETE FROM PANLG WHERE 1==1");
	#	$note = $note . " DELETE TMP for $panel <p>\n";
      }
      ###
      #      $note =  $note . "Finished searching @panel_set for exact matches  trying for close on @dud now<p>\n";
      foreach $panel (@dud) {
	$ztotal = $sth1->execute($panel, $lg);
	#	$note =  $note . "DUD- $panel has $rc rows for lg $lg <p>\n";
	$rc = $sth5->execute();
	($lo ,$hi) =  $sth5->fetchrow();
	if( $lo ){$lo = ($lo <= 0)? 0 : $lo;}
	if($lo &&  ($lo >= 0) ){
	  #	  $note = $note . "HIT on DUD- $panel <p>\n";
	  #	  $note = $note .  "lo $lo , hi $hi<p>\n";
	  #	  $note = $note .  "the interval on LG $lg of $panel runs from $lo to $hi<p>\n";
	  $zoom = $sth6->execute($lo, $hi) || die $dbh->errstr;
	  $zooms{$panel} = $zoom; #zoom hash
	  $lgs{$panel} = $lg; #lg hash

	  #	  $note = $note .  "$zoom rows added to the OTHER pool<p>\n";
	  # if zoom is one it has been suggested we return  maybe the closest 5 markers (zoom out)

	  $g_height = ($g_height < $zoom)? $zoom : $g_height;

	  ### -- do display this secondary panel
	  $g_printdata = $g_printdata . $panel .'|'.$lg .'|'."NULL".'|'.$lo .'|'.$hi .'|'.$print_type.'|'."\n";	#uniquery2
	  $g_width += 320;	#bbw
	  $g_opt =  $g_opt .
	    # $Q->hidden($panel,  1) . "\n".
	    "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"1\">\n".
	      ### send the total number of markers available  so a % of map may be calculated
	      "<INPUT TYPE=\"hidden\" NAME=\"".$panel."_ztotal\" VALUE=".$ztotal .">\n".
		$Q->hidden($panel . '_or', 'Location') . "\n".
		  $Q->hidden($panel . '_lg_or', 'units') . "\n".
		    ### lingage group of $marker on this $panel
		    $Q->hidden($panel . '_lg',     $lg) . "\n".
		      ### absolute extents of the interval containg $marker
		      $Q->hidden($panel . '_lg_lo',  $lo) . "\n".
			$Q->hidden($panel . '_lg_hi',  $hi) . "\n".
 $Q->hidden($panel . '_gene',   $Q->param($panel.'_gene')) . "\n".
		    $Q->hidden($panel . '_est',   $Q->param($panel.'_est')) . "\n".
		      $Q->hidden($panel . '_anon',   $Q->param($panel.'_anon')) . "\n".
			$Q->hidden($panel . '_fish',   $Q->param($panel.'_fish')) . "\n".
			  $Q->hidden($panel . '_near_loc',$lo + ($hi - $lo) / 2 ). "\n".
			    $Q->hidden($panel . '_zoom', $zoom ) . "\n".
			      $Q->hidden($panel."_from_UNIQUERY_MISS")."\n";
	  ### cause this change to be "remembered"?
	  $Q->param($panel . '_lg_lo',  $lo);
	  $Q->param($panel . '_lg_hi',  $hi);
	  $Q->param($panel . '_ztotal', $ztotal);
	  #can't offer relative markers since we do not know from where
	}
	else{
	  ### -- do  NOT display this looser panel
	  $Q->param($panel, 0);
	  $Q->hidden($panel . '_ztotal', 0);
	  #	  $note = $note .  "complete MISS on  $panel<p>\n";
	}
	$rc = $dbh->do("DELETE FROM PANLG WHERE 1==1");
      }
      $rc = $sth7->execute();
      while(@row = $sth7->fetchrow){
	$row[4] =  ($row[4] == 0)? 0 : $row[4]; # clean up 0.00000E+00
	#zdb_id,  abbrev, mtype,  target_abbrev, lg_location, or_lg,  mghframework, metric
	# 0        1       2       3               4           5       6               7
	$lines = "$row[0]|$row[1]|$row[2]|$row[3]|$row[4]|$row[5]|$row[6]|$row[7]|\n";
	$data = $data . $lines;
      }

      $dbh->commit;		# commit the changes if we get this far (drops temps)
    };				#end eval
    if ($@) {
      warn "Transaction aborted because $@";
      $dbh->commit;		#  so this will drop the temp(s) as well
    }
    $data;
  }
  # end sub uniquery

  ################################################################################
  ### input:  panel_abbrev, lg, lo & hi locations, and marker types
  ### output: markers between and including the hi and lo locations in applet param format
  ###
  ### note this is the default refresh machenism, and called by 'get near marker'
  ### speed ups here would be have an effect on all secondary maps and some primary

  sub get_between {
    my ($panel, $lg, $types, $lg_lo, $lg_hi) = @_;
    $sql =
	"SELECT zdb_id,abbrev,mtype,target_abbrev,lg_location,or_lg, " .
	"       case mghframework " .
	"         when 't' then 't'::char(1) " .
	"         when 'f' then 'f'::char(1) " .
	"         else NULL " .
	"       end, " .
	"       metric " .
	"FROM paneled_markers  ".
	"WHERE target_abbrev = \'$panel\' ".  # $panel
	  "AND or_lg = \'$lg\' " .	              # $lg
	    "AND mtype in (\'$types\' ) ".    # $types
	      "AND lg_location >= \'$lg_lo\' ".   # $lo
		"AND lg_location <= \'$lg_hi\' ". # $hi
		    "ORDER BY lg_location, abbrev;";

    #$note = $note . $sql . "<p>\n";
    my $curbetween = $dbh->prepare($sql) || return undef;
    $zoom  = $curbetween->execute  (); #  $panel, $lg, $types , $mlo, $mhi);
    if (! defined  $Q->param('OID')){$Q->param('OID', ''); }
    if ($zoom == 0){
      $g_printdata = $g_printdata . $panel .'|'.$lg .'|'.$Q->param('OID').'|'.$lg_lo .'|'.$lg_hi .'|'.$print_type.'|'."\n"; #get between
      while(@row = $curbetween->fetchrow){
	$row[4] =  ($row[4] == 0)? 0 : $row[4]; # clean up 0.00000E+00
	$g_data = $g_data . "$row[0]|$row[1]|$row[2]|$row[3]|$row[4]|$row[5]|$row[6]|$row[7]|\n";
	$zoom++;
      }
    }
    #$note = $note . "\n BETWEEN found |$zoom| markers between |$lg_lo|  and |$lg_hi| <p>\n ";
    $zoom;
  }

  ################################################################################
  ### input: marker_zdbid, lg,  panel_abbrev, (desired number of of markers) location, (and marker types global)
  ### output: the hi and lo locations that best contain the desired number of markers nearest to location.
  ###
  sub get_zooms {
    my ($zdbid, $lg, $panel, $zoom, $loc) =@_;
    if( ! defined $loc || !$loc){
      #      $note = $note . "finding the location of $zdbid on lg $lg of $panel<p>\n";
      $cur = $dbh->prepare("SELECT lg_location FROM paneled_markers WHERE target_abbrev = ? ".
			   " AND or_lg = ? AND zdb_id = ? " );
      $rc = $cur->execute($panel, $lg, $zdbid);
      @row = $cur->fetchrow();
      $loc = $row[0];
    }
    $lg_lo = $loc;
    $lg_hi = $loc;
    #    $note = $note . "get_ZOOM looking around $loc<p>\n";

    # Note: rewrote the following query to avoid an ABS() call.  We have reason
    # to suspect that ABS() calls might be causing the informix server to crash.
    #
    # Update on that: Removiing the ABS did not remove the problem.  Which
    # means that this particular query probably never had a problem.

    $sql =
      " SELECT lg_location, " .
      "        case when lg_location - $loc >= 0 then lg_location - $loc " .
      "             when lg_location - $loc <  0 then $loc - lg_location " .
      "             else NULL " .
      "        end distance " .
      "   FROM paneled_markers ".
      "   WHERE target_abbrev = ? AND or_lg = ? ".
      "     AND mtype in ( \'$types\') ".
      "   ORDER BY distance;";

    $cur = $dbh->prepare($sql);
    $rc = $cur->execute( $panel, $lg);

    while( ($zoom-- >= 0) && (@row = $cur->fetchrow) ){
      if($row[0] > $lg_hi){ $lg_hi = $row[0];}
      if($row[0] < $lg_lo){ $lg_lo = $row[0];}
    }
    #$note = $note . "get_ZOOM  found |$lg_lo| and |$lg_hi|<p>\n";
    $lg_lo = $lg_lo <= 0? 0: $lg_lo;  ## get rid of 0.0E0
    #$note = $note . "get_ZOOM  found |$lg_lo| and |$lg_hi|<p>\n";
    $g_opt = $g_opt . $Q->hidden($panel.'_from_ZOOM',$zoom ) . "\n";
    return ($lg_lo, $lg_hi);
  }

  ################################################################################
  ### input: a panel abbrev and a lg number
  ### output: entire linkage group in applet param format
  ### side effects: print_form and option_form updated, applet width and heigth adjusted
  sub lg_query {
    my ( $panel ,$lg ) =@_;
    my $data = '';
    my $ztotal = 1;
    $note = $note . "IN LG QUERY<p>\n";
    $sql =
	"SELECT zdb_id,abbrev,mtype,target_abbrev,lg_location,or_lg, " .
	"       case mghframework " .
	"         when 't' then 't'::char(1) " .
	"         when 'f' then 'f'::char(1) " .
	"         else NULL " .
	"       end, " .
	"       metric " .
	"  FROM paneled_markers " .
	"  WHERE target_abbrev = ? ".
	"    AND or_lg = ? ".
	"    AND mtype in  ( \'$types\') ".
	"  ORDER BY lg_location, abbrev;";

    $cur = $dbh->prepare($sql) ;
    $rc = $cur->execute($panel, $lg );
    $lo = 0; $ztotal = 0;
    while(@row = $cur->fetchrow){
      $row[4] =  ($row[4] == 0)? 0 : $row[4]; # clean up 0.00000E+00
      #zdb_id,  abbrev, mtype,  target_abbrev, lg_location, or_lg,  mghframework, metric
      # 0        1       2       3               4           5       6               7
      $lines = "$row[0]|$row[1]|$row[2]|$row[3]|$row[4]|$row[5]|$row[6]|$row[7]|\n";
      $data = $data . $lines;
      $ztotal ++;
      $hi = $row[4];
    }
    $g_printdata = $g_printdata . $panel .'|'.$lg .'|NULL|'.$lo .'|'.$hi .'|'.$print_type.'|'."\n"; # lg_query
    $g_width += 320;		#bbw
    $Q->param($panel,1);
    $Q->param($panel.'_ztotal',$ztotal);
    $g_opt = $g_opt .
      "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"1\">\n".
	"<INPUT TYPE=\"hidden\" NAME=\"$panel".'_zoom'."\" VALUE=\"$ztotal\">\n".
	  "<INPUT TYPE=\"hidden\" NAME=\"".$panel."_ztotal\" VALUE=".$ztotal .">\n".
	    $Q->hidden($panel.'_lg' , $lg)."\n".
	      $Q->hidden($panel.'_lg_lo' , $lo)."\n".
		 $Q->hidden($panel . '_gene',   $Q->param($panel.'_gene')) . "\n".
		    $Q->hidden($panel . '_est',   $Q->param($panel.'_est')) . "\n".
		      $Q->hidden($panel . '_anon',   $Q->param($panel.'_anon')) . "\n".
			$Q->hidden($panel . '_fish',   $Q->param($panel.'_fish')) . "\n".
            $Q->hidden($panel . '_bac',   $Q->param($panel.'_bac')) . "\n".
		$Q->hidden($panel.'_lg_hi' , $hi)."\n".
		  $Q->hidden($panel.'_near_loc', ($lo + ($hi - $lo) / 2) ) . "\n" ;
    $Q->hidden($panel.'_from_LINKAGEGROUP', 1);
    $g_height = ($g_height < $ztotal)? $ztotal : $g_height;
    $data;
  }
  ###############################################################################

  ###  several helper functions that query the db for additional  support information

  ###############################################################################
  ### input:  a marker zdbid
  ### output: a lingage group number that the marker is in
  sub get_OIDs_lg {
    my  ($zdbid) = @_;
    $sql =
    "SELECT first 1 or_lg " .
	"FROM paneled_markers WHERE zdb_id = \'$zdbid\'; " ;
	 #"group by or_lg ".
     #  "order by count(or_lg) DESC;";
    $cur = $dbh->prepare($sql) ;
    $rc = $cur->execute();
    @row = $cur->fetchrow;
    $lg = shift(@row);
    $lg;
  }

  ##############################################################################
  ### input:  a marker zdbid
  ### output: a lingage group number that the marker is in
  sub  get_OIDs_abbrev{
    my ($zdbid)= @_;
    $sql =
    "SELECT first 1 abbrev " .
	"FROM paneled_markers WHERE zdb_id = \'$zdbid\'; ";
    $cur = $dbh->prepare($sql) ;
    $rc = $cur->execute();
    @row = $cur->fetchrow;
    $marker = shift(@row);
    $marker;
  }

  ################################################################################
  ### input:   panel lg location
  ### output:  zdbid $ marker_abbrev --not further than any other marker to location.
  ### expects: $types as a global
  sub get_closest {
    my ($panel, $lg, $loc) = @_;
    undef @row;
    $cur = $dbh->prepare(
			 "select first 1 zdb_id, abbrev, lg_location, abs(lg_location - $loc) ".
			 " from paneled_markers ".
			 " where target_abbrev = \'$panel\' ".
			 " and or_lg = \'$lg\' ".
			 " AND mtype in ( \'$types\' ) ".
			 " order by 4;"
			);
    $rc= $cur->execute();
    @row=$cur->fetchrow;
    ($row[0],$row[1],$row[2]);
  }
  ################################################################################
  ### input:   panel lg types
  ### output:  total number of markers available on entire backbone
  ###
  sub get_total {
    my($panel,$lg,$types) = @_;

    $sql =
      "SELECT COUNT(*) FROM paneled_markers ".
	"WHERE target_abbrev == \'$panel\' ".
	  "AND or_lg = $lg ".
	    " AND mtype in ( \'$types\' );";

    $cur = $dbh->prepare ($sql );
    $rc= $cur->execute( );
    @row=$cur->fetchrow;
    $row[0];
  }

  ################################################################################
  ### check unique returns either a refference to a row-set or undef.
  ### undef means there is nothing of the sort anywhere in the db.
  ### a row-set may have one or more rows
  ### a row-set with one row is unique
  ### a row set with multiple rows can be shown to the user and they can pick.
  ###
  ### this function will have to accomodate full names and previous names

    sub check_uniq {              #assumes $marker is defined , and maybe panel & lg
    my ($marker, $panel, $lg) = @_; ### type?
    ### try and make an exact match first if it works run with it
    ### if not we will try "contains matching" on the panel & lg first
    ### then on the whole db.
    my $count = 0;  my $array_ref = '';
    if( (defined $lg) && $lg && ($lg ne "??") && ($lg > 0) && ($lg <= 25) ) {
      $note = $note .  "Is |$marker| exactly unique on |$panel| |$lg| officially?<p>\n ";
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM  paneled_markers  ".
	"WHERE abbrev  = \'$marker\' AND target_abbrev in (\'$panel\') ".
	  "AND mtype IN (\'$types\')  " .
	    "AND or_lg =  \'$lg\'; ";

    } elsif( defined  $panel) {
      $note = $note .  "Is |$marker| exactly unique on |$panel| offically?<p>\n ";
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM paneled_markers ".
	"WHERE abbrev  = \'$marker\' AND target_abbrev in (\'$panel\') " .
	  "AND mtype IN ( \'$types\' ); ";

    } else  {
      $note = $note . "Is |$marker| exactly unique in ZFIN officially?<p>\n ";
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM paneled_markers ".
	  "WHERE abbrev  = \'$marker\' AND mtype IN ( \'$types\' ) ;";

    }
    $cur = $dbh->prepare($sql);
    $rc = $cur->execute();
    $array_ref = $cur->fetchall_arrayref();
    #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
    $count = (defined @$array_ref)? @$array_ref : 0;
    $note = $note . "found $count offically approved symbols that match<p>\n";
    #--------------------------------------------------------------------------
    if(($count < 1) ) {
      ### did NOT find any exact match--
      ### look for associated accessions with the current constraints
      $note = $note .  "\nIs it ACCESSION NUMBER? \n";
      if( (defined $lg) && $lg && ($lg ne "??") && ($lg > 0) && ($lg <= 25) ) {
      $note = $note .  "Is |$marker| exactly unique on |$panel| |$lg| officially?<p>\n ";
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM  paneled_markers, db_link,foreign_db_contains, foreign_db, foreign_db_data_type ".
	 "WHERE dblink_acc_num  = \'$marker\' AND target_abbrev in (\'$panel\') AND fdbcont_fdb_db_id = fdb_db_pk_id ".
	  "AND mtype IN (\'$types\') AND or_lg =  \'$lg\' AND zdb_id = dblink_linked_recid AND fdbcont_fdbdt_id = fdbdt_pk_id ".
      "AND dblink_fdbcont_zdb_id =  fdbcont_zdb_id AND fdbdt_super_type = \'sequence\' ".
      "AND fdbdt_data_type != \'Polypeptide\' and fdbcont_organism_common_name = \'zebrafish\';";

      } elsif( defined  $panel) {
      $note = $note .  "Is |$marker| exactly unique on |$panel| offically?<p>\n ";
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM paneled_markers, db_link,foreign_db_contains, foreign_db, foreign_db_data_type ".
	 "WHERE dblink_acc_num   = \'$marker\' AND target_abbrev in (\'$panel\') AND fdbcont_Fdb_db_id = fdb_db_pk_id " .
	  "AND mtype IN ( \'$types\' ) AND zdb_id = dblink_linked_recid AND fdbcont_fdbdt_id ".
      "AND dblink_fdbcont_zdb_id =  fdbcont_zdb_id AND fdbdt_super_type = \'sequence\'".
      "AND fdbdt_data_type != \'Polypeptide\' and fdbcont_organism_common_name = \'zebrafish\';";

      } else  {
      $note = $note . "Is |$marker| exactly unique in ZFIN officially?<p>\n ";
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM paneled_markers, db_link,foreign_db_contains, foreign_db, foreign_db_data_type ".
	  "WHERE dblink_acc_num   = \'$marker\' AND mtype IN ( \'$types\' ) AND zdb_id = dblink_linked_recid AND fdbcont_fdb_db_id=fdb_db_pk_id ".
      "AND dblink_fdbcont_zdb_id =  fdbcont_zdb_id AND fdbdt_super_type = \'sequence\' AND fdbcont_fdbdt_id = fdbdt_pk_id ".
      "AND fdbdt_data_type != \'Polypeptide\' and fdbcont_organism_common_name = \'zebrafish\';";
      }
      $cur = $dbh->prepare($sql);$cur->execute();
      $array_ref = $cur->fetchall_arrayref();
      #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
      $count = (defined @$array_ref)? @$array_ref : 0;
      $note = $note . "found $count similar official symbols<p>\n";
    }

    #--------------------------------------------------------------------------
    if(($count < 1) ) {
      ### did NOT find any exact match--
      ### first look for "contains" with the current constraints
      $note = $note .  "\nLOOKING for CONTAINS \n";
      if( (defined $lg) && $lg && ($lg ne "??") && ($lg > 0) && ($lg <= 25)) {
	    $note = $note . "Is |$marker| similar & unique on |$panel|& |$lg| offically?<p>\n";
	    $sql = "SELECT UNIQUE zdb_id, or_lg FROM paneled_markers ".
	    "WHERE abbrev  like \'\%$marker\%\' AND target_abbrev = (\'$panel\') ".
		"AND mtype IN ( \'$types\' ) AND or_lg = \'$lg\';  ";


      } elsif( defined  $panel ) {
        $note = $note . "Is |$marker| similar & unique on |$panel| offically?<p>\n";
	    $sql = "SELECT UNIQUE zdb_id, or_lg FROM paneled_markers ".
	    "WHERE abbrev like \'\%$marker\%\' AND target_abbrev = (\'$panel\') ".
		"AND mtype IN ( \'$types\' ) ; " ;

      } else  {
	    $note = $note . "Is |$marker| similar & unique in ZFIN offically?<p>\n";
	    $sql = "SELECT UNIQUE zdb_id, or_lg FROM paneled_markers ".
	    "WHERE abbrev  like \'\%$marker\%\' AND mtype IN ( \'$types\' )  ;";

	  }
      $cur = $dbh->prepare($sql);$cur->execute();
      $array_ref = $cur->fetchall_arrayref();
      #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
      $count = (defined @$array_ref)? @$array_ref : 0;
      $note = $note . "found $count similar official symbols<p>\n";
    }
    #--------------------------------------------------------------------------
    if(($count < 1) ) { ############ if still no hits try all_map_names
        if( (defined $lg) && $lg && ($lg ne "??") && ($lg > 0) && ($lg <= 25) ) {
	  $note = $note . "Is |$marker| exactly unique on |$panel| |$lg| unoffically?<p>\n ";
	  $sql = "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	    "FROM all_map_names pmn, paneled_markers pm ".
	      "WHERE allmapnm_zdb_id = pm.zdb_id ".
	        "AND allmapnm_name  = \'$marker\' AND pm.target_abbrev in (\'$panel\') ".
		  "AND mtype IN (\'$types\') AND pm.or_lg =  \'$lg\' ; " ;

        } elsif( defined  $panel) {
	  $note = $note .  "|$marker| exactly unique on |$panel| unoffically?<p>\n ";
	  $sql =
	    "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	      "FROM all_map_names pmn, paneled_markers pm ".
		"WHERE allmapnm_zdb_id = pm.zdb_id AND allmapnm_name  = \'$marker\' ".
		  "AND pm.target_abbrev in (\'$panel\') AND mtype IN (\'$types\') ; " ;

        } else  {
	  $note = $note . "|$marker| exactly unique in ZFIN  unoffically?<p>\n ";
	  $sql =
	    "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	      "FROM all_map_names pmn, paneled_markers pm ".
		"WHERE allmapnm_zdb_id = pm.zdb_id ".
		  "AND allmapnm_name = \'$marker\' AND mtype IN (\'$types\') ; " ;

        }
        $cur = $dbh->prepare($sql);$rc = $cur->execute();
        $array_ref = $cur->fetchall_arrayref();
        #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
        $count = (defined @$array_ref)? @$array_ref : 0;
        $note = $note . "found $count <p>\n";
    }


    #--------------------------------------------------------------------------
    if(($count < 1) ) {
        ### did NOT find any exact match--
        ### first look for "contains" with the current constraints
        #$note = $note .  "\nLOOKING for CONTAINS \n";
        if( (defined $lg) && $lg && ($lg ne "??") && ($lg > 0) && ($lg <= 25)) {
	  $note = $note . "|$marker| similar & unique on |$panel|& |$lg|?<p>\n";
	  $sql =
	    "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	      "FROM all_name_ends, all_map_names pmn, paneled_markers pm ".
	        "WHERE allmapnm_zdb_id = pm.zdb_id ".
		  "AND allmapnm_serial_id = allnmend_allmapnm_serial_id ".
 		    "AND allnmend_name_end_lower like \'$marker\%\' ".
		      "AND pm.target_abbrev = \'$panel\' ".
		        "AND mtype IN ( \'$types\' ) AND pm.or_lg = \'$lg\' ;" ;

        } elsif( defined  $panel ) {
	  $note = $note . "|$marker| similar & unique on |$panel|?<p>\n";
	  $sql =
	    "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	      "FROM all_name_ends, all_map_names pmn, paneled_markers pm ".
	        "WHERE allmapnm_zdb_id = pm.zdb_id ".
                  "AND allmapnm_serial_id = allnmend_allmapnm_serial_id ".
		    "AND allnmend_name_end_lower like \'$marker\%\' ".
		      "AND pm.target_abbrev = \'$panel\' ".
                        "AND mtype IN (\'$types\') ; " ;

        } else  {
	  $note = $note . "|$marker| similar & unique in ZFIN??<p>\n";
	  $sql =
	    "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	      "FROM all_name_ends, all_map_names pmn, paneled_markers pm ".
	        "WHERE allmapnm_zdb_id = pm.zdb_id ".
                  "AND allmapnm_serial_id = allnmend_allmapnm_serial_id ".
		    "AND allnmend_name_end_lower like \'$marker\%\' ".
                      "AND mtype IN (\'$types\')  ;" ;

	}
        $cur = $dbh->prepare($sql);
        $cur->execute();
        $array_ref = $cur->fetchall_arrayref();
        #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
    }
    #print '<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">\n\n<p> <pre>\n' . $note ."/n</pre><p>\n";
    $array_ref;
  }				# end check unique

  ################################################################################
  ### boring constants
  sub pass_hidden {
    if(defined $Q->param('loc_lg')&& !$Q->param('lg') ){
      $Q->param('lg',$Q->param('loc_lg'));
    }
    $Q->delete('edit_panel','');

    my $buf ="";
    my $i = 0;
    for $panel (@allpanels){
        $buf = $buf .
        $Q->hidden('panel'.$allpanels_order[$i], $panel )."\n".
        $Q->hidden($panel.'_units',$allpanels_metric[$i] )."\n";
        $i++;
    }
    $buf = $buf .
	    $Q->hidden('OID',$Q->param('OID') )."\n".
		$Q->hidden('lg',$Q->param('lg'))."\n".
	    #$Q->hidden('userid',$Q->param('userid'))."\n".
        $Q->hidden('userid',$Q->param('zfin_login'))."\n".
		$Q->hidden('refresh_map','1')."\n".
		$Q->hidden('edit_panel','')."\n"
	;
    $buf;
  }				# end sub pass_hidden

  ################################################################################
}				#end mod it
0;
