#!/private/bin/perl -wT
 {   ### mod it
  #use strict;
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
  #open(IN, ">> /tmp/mapplet_beta.log") || die "input dump failed";
  #print IN  strftime('%r %A %B %d %Y', localtime) ."\t";
  ### capture where the call was made from
  #$_ = $Q->referer(); /MIval=aa-/; print IN $'."\t";
  #foreach $name ($Q->param()){print IN "$name=".$Q->param($name)."|"};
  #print IN "\n\n";
  #close IN;


  ### the hard coded env paths need a better idea-[thanks Dave]
  $ENV{INFORMIXDIR}      = '<!--|INFORMIX_DIR|-->';
  $ENV{INFORMIXSERVER}   = '<!--|INFORMIX_SERVER|-->';
  $ENV{INFORMIXSQLHOSTS} = '<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->';
  ### open a handle on the db
  my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->', '', '', {AutoCommit => 1, RaiseError => 1})
  || die "Failed while connecting to <!--|DB_NAME|--> "; #$DBI::errstr";

  my @allpanels=();		        ### the panels the db knows about
  my @panels=();			    ### panel this map actualy attempts to use
  my $panels_string = '';       ### convinant way to pass the list into sql statments
  my @allpanels_order=();		### an enumeration of all panels
  my @allpanels_metric=();		### cM or cR  for each panel
  my @allpanels_id =('ZDB-REFCROSS-010114-1'); ### zdb_id for ZMAP Panel
  my $panel ='';			    ### current panel
  my ($sql,$junk,$tmp);		    ### throw away vars
  my $cur = $dbh->prepare('select abbrev,disp_order,metric from panels order by disp_order asc');
  $cur->execute;
  $cur->bind_col(1, \$panel);
  $cur->bind_col(2, \$junk);
  $cur->bind_col(3, \$tmp);
  while ($cur->fetch) {
    if ( $panel ne 'ZMAP') {
      push (@allpanels ,$panel );
      push (@allpanels_order ,$junk );
      push (@allpanels_metric ,$tmp );
    }
  }

  ### the known marker types
  my $types = 'SSLP';
  my $anon_type = "RAPD\',\'RFLP\',\'BAC_END\',\'PAC_END\',\'STS\',\'SNP";
  my $gene_type = "GENE\',\'GENEP";
  my $est_type  = "EST\',\'CDNA";
  my $bac_type  = "BAC\',\'PAC";
  my $fish_type = "GENO\',\'MUTANT";

  ###
  ### WARNING and ERROR CODES to be passed to the Options page
  ###

  my $no_data    = 1;

  ### number of mappings above and below $marker to gather (aproximatly)
  ### running out of linkage group or running into a bin will affect the number of mappings returned;
  my $g_zoom = 60;
  my $zoom  = 60;       ### the target number of markers for any one backbone
  my %zooms;            ### hash of zoom values
  my $lg;               ### the linkgage group containing $marker
  my $lgs;              ### hash of linkage groups
  my $loc;              ### the location of $marker
  my $lo;               ### name above $loc on map
  my $hi;               ### name below $loc on map
  my $m_lo;             ### offset above $loc on map
  my $m_hi;             ### offset below $loc on map
  my $lg_lo;            ### $loc - $m_lo;
  my $lg_hi;            ### $loc + $m_hi;

  my @row;              ### a row returned from a sql query
  my $g_OID ='';        ### the zdb_id of marker we found -- for applet, and options to ID unique name
  my $lines ;           ### all the rows returned from a paticular sql query with fields terminated with '|'
  my $g_data ='';       ### all the $lines returned from all the sql queries --- param to the applet
  my $g_printdata ='';  ### all the $lines returned from all the sql queries --- param to the applet
  my $print_type =0;    ### a code for types to print --1 gene, 2 est, 4 anon, 8 fish (0 == 15)
  my $g_height = 1;     ### the maximum number of (distinct) rows returned by any query --- anticipated heigth of the applet
  my $g_width =  1024;  ### fixed width
  my $g_zdbid;
  my $g_lg;
  my $zdbid = '';       ### the zdbid comming in from a search marker page.

  my $or;               ### is used to flag marker or lg options,
  my $lg_or;            ### used to choose between near and between when location is choosen
  my $rowref;           ### a refference to an array of rows;-- use  %$rowref   $%$rowref[0]

  my $g_error = 0;      ### global error

  my $edit_panel;       ### if given, the panel that is being changed

  my ($sm_m, $sm_lg, $sm_panel, $sm_loc, $sm_refresh); #values Which might be used to repopulate the select map form


  ### incase we want the mapplet to open somewhere particular some day
  my $frame = '_top';

  ### holds the maximum number of markers that contain the given name on any panel.
  ### changing~
  my $unique = 0;

  ### the page that will be output, either mapplet or error message or
  ### option help generation, ...
  my $g_opt= '';        # options form (global)

  ### used to pass scafolding notes to the web page
  my $note = "Begin Notes(M)<p>\n";

  ###
  ### Emit a Blank Page if called with no paramerers
  ###
  if( ! $Q->param ) {
    print  $Q->header .
      $Q->start_html("This Page Is Intentionaly Left Blank... go figure").
        "aren't you suppose to be somewhere else?\n".
        $Q->end_html."\n";
        exit;
  }

  ### Begin Frame_Set Exists -- frames are gone so they exist
  ###
  ### isolate panels of interest, (undefined is not false)
  ### and a panel to be edited (if one is available)
  ###
  if( defined $Q->param('edit_panel') and  $Q->param('edit_panel'))  {
    $edit_panel = $Q->param('edit_panel');
    if(!($edit_panel =~ 'ZMAP')){
        $panels_string = $panels_string .$edit_panel ."\',\'";
    }
  } else { $edit_panel = 'ZMAP';}

  $note = $note . "All Panels \n<p>";
  for $panel (@allpanels)  {
    $note = $note . $panel ."\n <p>";
    if( (defined $Q->param($panel))
	&& ($Q->param($panel) == 1)
	&& ($panel ne $edit_panel )){ push(@panels, $panel);
				      $Q->param($panel,1);
				      $panels_string = $panels_string . $panel . "\',\'";
				    }
    elsif($panel ne $edit_panel )  {$Q->param($panel,0);}
  }
  #if no panels are implicated -- select them all
  if( length(@panels) < 1&& (!$edit_panel)) {
    #$note = $note ." short sheeted! \n <P>";
    for $panel (@allpanels) {
      push(@panels, $panel);
      $Q->param($panel,1);
      $panels_string = $panels_string .$panel ."\',\'";
    }
  }
  chop $panels_string; chop $panels_string; chop $panels_string;

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


  if( (defined $Q->param("view_map")) && ($g_error == 0) )  {
    ### view_map is defined and no error reported
    ### types is never defined by an external page so use them all.
    $types =
"SSLP\',\'RAPD\',\'RFLP\',\'STS\',\'SNP\',\'GENE\',\'GENEP\',\'BAC\',\'PAC\',\'BAC_END\',\'PAC_END\',\'EST\',\'CDNA\',\'GENO\',\'MUTANT";
    # $types =  $types.",\'".$anon_type.",\'".$gene_type .",\'".$est_type.",\'".$bac_type.",\'".$fish_type."\'";
    if( ( !(defined $Q->param("OID")) ) || ($Q->param("OID") eq '') ) {
        ### should I parse it as  ZDB-type-date-number  ???
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
            #print "UNIQUE " . $unique;

            $note = $note . "\trow ref length " . $unique . "\n";

            ###
            ### yow!  too many answers
            ###
            if( $unique > 1) {#defined @$rowref[1] ){
                ### not unique shunt off to search result page
                #$note = $note . $unique . " ->Too Many Choices  <p>\n";
                my $bot = LWP::UserAgent->new();
                my $req = POST 'http://<!--|DOMAIN_NAME|-->/<!--|WEBDRIVER_PATH_FROM_ROOT|-->',
                [
                compare=> 'contains',
                marker_type=> 'all',
                lg=> 0,
                refcross=> 'NULL',
                plinks=> 'on',
                action=> 'SEARCH',
                paged_by=> 'mapper',
                map_type=> 'merged',
                MIval=> 'aa-newmrkrselect.apg',
                query_results=> 'exist',
                input_name=> "$marker",
                ZDB_authorize=> $Q->cookie('ZDB_authorize')
                ];
                print "Content-Type: text/html; charset=ISO-8859-1\r\n\r\n";
                my $res = $bot->request($req);
                # check the outcome
                if ($res->is_success) {print $res->content . "\n";}
                else { print "Error: " . $res->status_line . "\n";}

                # debugging
                #open(IN, ">> /tmp/tomc_zmapplet.log") || die "input dump failed";
                #print IN "\n*****************************************************".
                #      $note. "\nrowref\t@$rowref \n";
                #close IN;

                exit 1;
           }
            ###
            ### huh? got nothing, advance to re-try do not pass map
            ###
            elsif(  $unique < 1) { #! defined $rowref ) {
                print $Q->header(). "\n".
                $Q->start_html(-TITLE => "ZFIN View ZMAP", -bgcolor=> 'white')."\n".
                "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/header.js'></script>" ."\n";
                mapper_select(Q);
                print  "<p><p><p><p>No mapping data is available for ".
                "\"<font color=red><i><b>$marker</b></i></font>\"\n<p><p><p>".
                "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script>";
                exit 1;
            }
	###
	### ding! add the lg and zdbid for the unique marker found (or approximated with "contains")
	###
	else {
      $unique = 1;
	  $zdbid  = $$rowref[0][0];
	  $lg     = $$rowref[0][1];
	  #$note = $note . $marker. " -> " .$zdbid . " on LG ". $lg . " Found as unique enough<br>\n";
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
	($zdbid, $marker, $loc) = get_closest_on( $sm_panel, $sm_lg, $sm_loc );

	if(defined $zdbid) { $Q->param("OID",$zdbid);}
    $sm_m = get_OIDs_abbrev($zdbid);
    if ($sm_refresh != 2) { $sm_refresh = 1;}
	if(defined $marker) { $Q->param('marker=',$marker);}
	$Q->param("lg", $Q->param("loc_lg"));
    #selectmap gets re-populated with a  map location
    $sm_refresh = 2;
      }
      $g_zdbid =  $Q->param("OID");
      #$sm_refresh = 1;
    }
    #$note =  $note ."@panels and ". $Q->param("loc_lg")." <p>\n";

    ### We now know that there are not multiple interperations of $marker
    ### because there is a OID otherwise
    ### they would have been shunted off to either redo or search results page.
    ###

    if( (defined $Q->param("OID")) && ($Q->param("OID")) )  {
      $g_OID = $zdbid  =  $Q->param("OID");
      $sm_m = get_OIDs_abbrev($zdbid);
      if (! $sm_refresh || $sm_refresh != 2) { $sm_refresh = 1;}

      if( defined $Q->param("lg") && $Q->param("lg") ) {$lg =  $Q->param("lg");}
      else {			### need a linkage group
	#get OID lg
	$lg = get_OIDs_lg($zdbid) ;
	#$note = $note . "found $marker to be unique on LG $lg   \n";
      }
      $sm_lg = $lg;
      #$note = $note . "Arrived from somewhere that resolves to marker $zdbid on $lg <p>\n";
      $note = $note . "Will look for $types on $panels_string<p>\n";

      ### find closest markers to zdbid on given panels
      ### expects globals  $types and  @panels to exist

      uni_query( $lg, $zdbid ,$panels_string);
    }

    ###
    ### or they need the whole LG on selected panels...
    ###

    elsif( (defined $Q->param("loc_lg")) && ($Q->param("loc_lg")) ) {
      # asking for a whole linkage group
      $sm_lg = $lg = $Q->param("loc_lg");
      $Q->param('lg',$lg);
      #$g_height = 0;
      #$note = $note . "LG QUERY <p>\n";
      #$g_data =  $g_data . lg_query ( $panels_string ,$lg );
      foreach $panel (@panels) {
	$Q->param($panel.'_ztotal',1);
    #$note = $note . "being asked for all of $lg on $panel <p>\n";
	$g_data =  $g_data . lg_query ( $panel,$lg );   # should this be allowed?
    $zooms{$panel} = $zoom; #zoom hash KS
	$lgs{$panel} = $lg; #lg hash KS
	#$note = $note ." ztotal for lg $lg on $panel  is ". $Q->param($panel.'_ztotal') . "<p>\n";
      }
    }
    if(!$g_data) {		### all empty backbones
      #$note = $note . "\tNo markers found on @panels  <p>\n";
      if ($g_error < 1) {$g_error = 0;}
    }
    if ($sm_refresh != 2) { $sm_refresh = 1;}
  }### end coming from a "view_map" source  with no errors
  ###############################################################################
  ###
  ###
  ### comming from OPTIONS page zoom/edit/show  or a 'multi source' page
  ###
  ###

  # was refresh

  ### there is at most one panel left and that is the one being altered
  ### could be "SEEK" in which case we punt for now (if OID on lg: return n-closest, else return lg)
  ### could be "ZOOM" in which case the zoom we grab the n' closest to OID
  ### could be "EDIT" in which case the path of the old options page is followed
  ### could be a HIDE in which case we are done.

  elsif  ($g_error == 0)  {	# comming from options page

    $panel = 'ZMAP';
    $types='SSLP'; $print_type = 0;
    if($Q->param($panel.'_gene') && $Q->param($panel.'_gene')==1) {$types = "$types\',\'$gene_type"; $print_type += 1;}
    if($Q->param($panel.'_est')  && $Q->param($panel.'_est')==1)      {$types = "$types\',\'$est_type"; $print_type += 2; }
    if($Q->param($panel.'_anon') && $Q->param($panel.'_anon')==1) {$types = "$types\',\'$anon_type"; $print_type += 4; }
    if($Q->param($panel.'_fish') && $Q->param($panel.'_fish')==1)      {$types = "$types\',\'$fish_type"; $print_type += 8; }
    if($Q->param($panel.'_bac')  && $Q->param($panel.'_bac')==1)    {$types = "$types\',\'$bac_type"; $print_type += 16; }
    if($print_type == 0) { $types = "SSLP\',\'RAPD\',\'RFLP\',\'STS\',\'GENE\',\'GENEP\',\'EST\',\'CDNA\',\'BAC\',\'PAC\',\'BAC_END\',\'PAC_END\',\'GENO\',\'MUTANT"; $print_type += 31;}

    #$note = $note . " will be finding  $types  markers on <br>\n|". $panels_string  ."|<br>\n";
    $Q->param($panel, 1);

    if( ! defined $Q->param($panel.'_zoom') && ! defined $Q->param($panel.'_or') ) {
      ### SEEK(un-hide)
      #$note = $note . " <br>SEEK! on |". $panels_string  ."|<p>\n";

      $lines = '';
      if( $Q->param('OID')) {
	    #$note = $note . "check for |".$Q->param('OID')."| on LG |". $Q->param('lg') . "|\n";
	    ### expects global $types to exist
	    $lines = uni_query( $Q->param('lg'), $Q->param('OID'), $panels_string ); ###
        #if(! defined $Q->param('name') ) {
            $sm_m = get_OIDs_abbrev($Q->param('OID'));
            #$Q->param('name',$sm_m);
            $sm_refresh = 1;
        #}
      }
      if(! $lines) {
	$Q->param($panel.'_ztotal',1);# avoid div by zero
	#$note = $note . "Get all of LG |". $Q->param('lg') . "|\n";
	$g_data =  $g_data . lg_query( $panels_string, $Q->param('lg') );
	$zooms{$panel} = $zoom; #zoom hash
	$lgs{$panel} = $lg; #lg hash
      }else {
	$g_data = $g_data . $lines;
	#$note = $note ."got some near ".$Q->param('OID')."<p>\n";
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
    elsif( defined $Q->param( $panel.'_zoom') && $Q->param($panel.'_zoom') < 0) { ### ZOOM
      #$note = $note . " ZOOM! \n";
      $zoom = abs( $Q->param($panel.'_zoom'));
      $zoom = ($zoom > $Q->param($panel.'_ztotal')) ? $Q->param($panel.'_ztotal'): $zoom;
      $Q->param($panel.'_zoom', $zoom );
      #$note = $note . " ZOOMING towards  |$zoom| markers on |$panels_string|<p>\n";
      $lg_lo = $Q->param($panel.'_lg_lo');
      $lg_hi = $Q->param($panel.'_lg_hi');
      $lg = $Q->param($panel.'_lg');

      if( ! defined $Q->param($panel.'_OID'))  {
	if (defined $Q->param( "OID")) {
	  $Q->param($panel.'_OID',$Q->param( "OID") )
	}
	else {
	  ($zdbid, $marker, $loc) = get_closest (
                         $panel ,
                         $Q->param($panel.'_lg'),
                         $lg_lo + ($lg_hi - $lg_lo) / 2.0 );
	  $Q->param($panel.'_OID', $zdbid);
      $sm_m = $marker;
      $sm_refresh = 1;

	  #$note = $note . "PICKING |$zdbid| (|$marker|) on |$panels_string| at |$loc| to zoom about<p>";
	}
      }
      else { $loc = defined $Q->param($panel.'_loc')? $Q->param($panel.'_loc'): undef; }
      $zdbid = $Q->param($panel.'_OID');

      #$note = $note ." ZOOMPARAMS:: |$zdbid|, |$lg|, |$panels_string|, |$zoom|, |$loc| <br> pre-lo $lg_lo 7 pre-hi $lg_hi <br>\n";
      ($lg_lo, $lg_hi) = get_zooms ($zdbid, $lg, $panels_string, $zoom, $loc );
      #$note = $note . " Zoom is  centered on ". $zdbid. " at |$loc|<br>\n";
      #$note = $note ."ZOOMRETURNS:: |$lg_lo|, |$lg_hi| <p>\n";
      $Q->delete($panel.'_lg_lo','');
      $Q->delete($panel.'_lg_hi','');
      $zoom = get_between($panels_string, $lg, $types, $lg_lo, $lg_hi);
	  $zooms{$panel} = $zoom; #zoom hash
	  $lgs{$panel} = $lg; #lg hash
      if ($zoom >= 0) {
        $junk = get_bins($panels_string, $lg, $types, $lg_lo, $lg_hi);

	$g_height = ($g_height < $junk)? $junk : $g_height;
	### add room in applet for another backbone
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
      } else  {			### empty backbone
	### close the option block for this panel?
	$error &= $no_data;
	$Q->param($panel, 0);	# turn off check box for this panel in mapperselect form
      }
    }# ~ zoom #####################
    else {			### EDIT
      # there is a potential change in the number of markers on the backbone, heck they can change lgs
      # so reset ztotal
      $lg      = $Q->param($panel."_lg");
      if ( $lg =~/\?\?/) { undef $lg;}
      else {$ztotal =  get_total($panels_string, $lg, $types);}

      $Q->param($panel.'_ztotal', $ztotal );

      #$note = $note . " EDIT! \n";
      $or_flag = $Q->param($panel."_or");
      $lg_lo   = $Q->param($panel."_lg_lo");
      $lg_hi   = $Q->param($panel."_lg_hi");
      $loc     = $Q->param($panel."_near_loc");
      if (!$lg) { $lg = $Q->param('loc_lg');}

      ######################################################################################
      if($or_flag =~ /Location/)  { # location flag set
	#$note = $note . "\tLOCATION <p>\n";
	$lg_or_flag =  $Q->param($panel. '_lg_or');

	if (
	    (($lg_or_flag =~ /units/) && ((! defined $lg_lo) || (! defined  $lg_hi)) )
	    ||
	    (($lg_or_flag =~ /near/ ) && (! defined  $loc))
	   ) {#must be asking for the whole LG
	  $Q->param($panel.'_ztotal',1);
	  #$note = $note . "Get all of LG |". $Q->param('lg') . "|\n";
	  $g_data =  $g_data . lg_query( $panels_string, $lg ); #(types)
	  $zooms{$panel} = $zoom; #zoom hash
	  $lgs{$panel} = $lg; #lg hash
	}
	elsif($lg_or_flag =~ /units/)  { # lg flag is units
	  #$note = $note . "\t\t\tOPTION FORM BETWEEN <p>\n";
	  $zoom = get_between($panels_string, $lg, $types, $lg_lo, $lg_hi);
	  $zooms{$panel} = $zoom; #zoom hash
	  $lgs{$panel} = $lg; #lg hash
	  if ($zoom > 0) {
         $junk = get_bins($panels_string, $lg, $types, $lg_lo, $lg_hi);
	    $g_height = ($g_height < $junk)? $junk : $g_height;
	    ### add room in applet for another backbone
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
	  } else  {		### empty backbone
	    ### close the option block for this panel?
	    $error &= $no_data;
	    $g_opt =  $g_opt . "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"0\">\n"; #$Q->hidden($panel, 0). "\n";
	    $Q->param($panel, 0); # turn off check box for this panel in mapperselect form

	  }
	}			# ~ lg_flag is units
				### the call to get_between should:
				###   be adding rows to g_data
				###   adjusting lo & hi to what it finds
				###  adding bbw to applet width
				###  $or_flag stays Location

	##########################################
	elsif($lg_or_flag =~ /near/ )  { #  just turn the 'near query' into a 'marker query'
	  #$note = $note . "\t\tNEAR $loc on <p>\n";
	  ($zdbid,$marker,$loc) = get_closest($panel,$lg,$loc);
	  $Q->param($panel."_OID", $zdbid);
	  $Q->param($panel."_m", $marker );
	  $Q->param($panel."_loc", $loc);
	  $Q->param($panel.'_m_lo','');
	  $Q->param($panel.'_m_hi','');
	  $or_flag = 'Marker';
      $sm_m = $marker;
      $sm_refresh = 1;
	}
      }# end location set
	###################################################################################
    if( $or_flag  && ($or_flag =~ /Marker/ ))  { # or_flag  is 'Marker'
	#$note = $note . " MARKER on $panel  <p>\n";
	### need to have (unique)marker _m_lo & _m_hi (distances)
	### find if marker exists uniquely on this panel

	$marker = $Q->param($panel."_m");
	$m_lo =   $Q->param($panel."_m_lo");
	$m_hi =   $Q->param($panel."_m_hi");

	if(! $zdbid ) {
	  #$note = $note . "checking $marker, $panel , $lg for uniqueness \n";
      undef $rowref;
	  $rowref  = check_uniq($marker, $panels_string );
	  if( (defined $rowref) && (! defined @$rowref[1])){
        $unique = 1;
	    $zdbid = $$rowref[0][0];
	    $lg =    $$rowref[0][1];
	    $ztotal =  get_total($panels_string, $lg, $types);
	    $Q->param( $panel.'_lg_near', $loc);
	    #$Q->param($panel."_lg", $lg);
        $sm_m= get_OIDs_abbrev($zdbid);
        $sm_refresh = 1;
	  }  else   { $unique = 1; }
	  if($unique == 1) {
	    if( (! $m_lo ) || (! $m_hi ))  { # hi=lo so call uni_query()
	      #***********************************************************
	      $lo = $hi = $m_hi = $m_lo = '';
	      $lg_hi = $lg_lo = $error = '0';

	      ### expects $types & $zoom as globals
	      $lines = uni_query($lg, $zdbid,$panels_string);
	      #$note = $note . "\tLooking on $panel \n";
	      if( $lines ) {	### if we have some map to display; =~ tr/\0// /
		### add room in applet for another backbone
		$g_data = $g_data . $lines;
		#$note = $note . "\tFound markers on $panel  <p>\n";
		$g_opt =  $g_opt .
		  ### name of the marker on this $panel
		  $Q->hidden($panel . '_m', $marker) . "\n".
		    $Q->hidden($panel. '_OID', $zdbid) . "\n";
           $sm_m = $marker;
        $sm_refresh = 1;

	      } else  {		### empty backbone
		### close the option block for this panel
		#		$note = $note . "\tNo markers found on $panel  <p>\n";
		$error  &=  $no_data;
	      }
	    }			###############################
	    else {		# has a specified range
	      $loc = $Q->param($panel.'_lg_near');
	      $lg_lo = $loc - $m_lo;
	      $lg_hi = $loc + $m_hi;
	      $zoom = get_between($panels_string, $lg, $types, $lg_lo, $lg_hi);
	      $zooms{$panel} = $zoom; #zoom hash
	      $lgs{$panel} = $lg; #lg hash
	      if ($zoom > 0) {
           $junk = get_bins($panels_string, $lg, $types, $lg_lo, $lg_hi);
		$g_height = ($g_height < $junk)? $junk : $g_height;
		### make sure we have the correct total markers
		$ztotal = get_total($panels_string, $lg, $types);
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
				  } else { # zoom == 0	 ### empty backbone
				    ### close the option block for this panel?
				    $error &= $no_data;
				    $Q->param($panel, 0); # turn off check box for this panel in mapperselect form
				  }
	    }			# range specified
	  }			# unique not == 1
	}			# if not zdb_id

	##################################
	$sm_refresh = 2;
      }	#or_flag is marker!


     elsif($g_data =~ '') {		     	### just hid a panel
       #if ( defined $Q->param("marker") )
       #{
       #  $sm_m = $Q->param("marker");
       #  $sm_refresh = 1;
       #}
       #elseif ( defined $Q->param("OID") )
       #{
       #  $sm_m = get_OIDs_abbrev($Q->param("OID"));
       #  $sm_refresh = 1;
       #}
       #$note = $note . "HIDE! <br>\n";
       $lg_lo = $Q->param($panel.'_lg_lo') ;
       $lg_hi = $Q->param($panel.'_lg_hi') ;
       $lg =    $Q->param($panel.'_lg') ;
       $zoom = get_between( $panels_string, $lg, $types, $lg_lo, $lg_hi ) ;
       $zooms{$panel} = $zoom; #zoom hash
	   $lgs{$panel} = $lg; #lg hash
       if ($zoom > 0) {
        $junk = get_bins($panels_string, $lg, $types, $lg_lo, $lg_hi);
	 $g_height = ($g_height < $junk)? $junk : $g_height ;

     if( (defined $edit_panel) && !($edit_panel =~'ZMAP') ){
        ### -- do un-hide this panel
        #$note = $note . "make that UN-HIDE! <br>\n";
        $g_opt =  $g_opt .
	    "<INPUT TYPE=\"hidden\" NAME=\"$edit_panel\" VALUE=\"1\">\n";
     }
	 $g_opt =  $g_opt .
	   ### -- do display this panel
	   "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"1\">\n".
	     "<INPUT TYPE=\"hidden\" NAME=\"".$panel."_ztotal\" VALUE=".$Q->param($panel.'_ztotal') .">\n".
	       $Q->hidden($panel . '_lg',  $lg) . "\n".
		 $Q->hidden($panel . '_lg_lo', $lg_lo) . "\n".
		   $Q->hidden($panel . '_lg_hi', $lg_hi) . "\n".
		     $Q->hidden($panel.'_zoom',$zoom ) . "\n".
		       $Q->hidden($panel.'_from_BETWEEN', 1) . "\n" ;


       }
       else{			### empty backbone
	 ### close the option block for this panel?
	 #$error &= $no_data		;
	 $g_opt =  $g_opt . "<INPUT TYPE=\"hidden\" NAME=\"$panel\" VALUE=\"0\">\n" ;
	 $Q->param($panel, 0)		; # turn off check box for this panel in mapperselect form
       }

       ### find and forward any other hidden vars for this particular panel
       #pass_hidden_panel($panel)	;
       my @vars =
	 ('_OID','_m','_or','_near_loc','_m_lo','_m_hi','_gene','_est','_bac','_anon','_fish','_ztotal') ;
       my $var				;
       foreach $var(@vars){
	 if($Q->param($panel.$var)){
	   $g_opt = $g_opt .  $Q->hidden($panel. $var,  $Q->param($panel. $var)). "\n"	;
	 }
       }
       $g_opt = $g_opt . $Q->hidden($panel."_from_HIDE")."\n" ; # bread crumbs
     }
       $sm_refresh = 2;

  }				### end else edit
    ### if no mappings were found on this subset of panels indicate it
    if (! $g_data  && ( $g_error < 1 ))  {$g_error &= 1;}
  }				# end from options


  ################################################################################
  ################################################################################
  ################################################################################
  ### build up the "Mapplet" page
  #$note = $note . " <p> END NOTE <P>\n";
  print $Q->header . "\n";
  print $Q->start_html(-TITLE => "ZFIN View ZMAP", -bgcolor=> 'white')."\n";
  print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/header.js'></script>";

  ### based on error codes emit the dynamic part of the page
  if ($g_error == 0 && $g_data)  {
    $g_height *= 18;
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
				  -action=>'/<!--|CGI_BIN_DIR_NAME|-->/map-options.pl',
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

    my $POprint = 'ZMAP|';
    #for $panel (@panels) { $POprint = $POprint . $panel . "|"; }### kevin's code

    my $from_panels = "";
    for $panel (@panels) { $from_panels = $from_panels . $panel . "|"; }### kevin's code

    print "</td><td>" . $Q->start_form (
				  -method=>'GET',
				  -action=>'/<!--|CGI_BIN_DIR_NAME|-->/print_map.cgi',
				  -encoding=>'application/x-www-form-urlencoded',
				  -name=>'print_map',
				  -target=>'print'
				 ). "\n".
				   $Q->submit(-name=>"New Window with Printer Friendly  Map") . "\n".
				     $Q->hidden("height",$g_height)."\n".
				       $Q->hidden("width" ,$g_width)."\n".
  				         $Q->hidden("panel_order",$POprint)."\n".
					   $Q->hidden("from_panels",$from_panels)."\n".
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

    $panel = 'ZMAP';
    #for $panel (@allpanels) {
      if ((defined $Q->param($panel.'_ztotal')) && ($Q->param($panel) == '1')) {
	$ztot = $Q->param($panel.'_ztotal');
	$z = $zooms{$panel};
	$lg = $lgs{$panel};

	#zoom out
	$newz = $z + 40;
	if ($newz > $ztot) { $newz = $ztot; }
	print "\n<td><input type=button value=\"Zoom Out\"".
    " onClick=\"document.optform.edit_panel.value='".$panel."';".
    " document.optform.".$panel."_zoom.value = '-".$newz."';".
    " document.optform.refresh_map.value = 1; ".
    " document.optform.action='view_zmapplet.cgi'; ".
    " document.optform.submit();\">\n\n";

	#draw percentage

	$percent = int $z * 100 / $ztot;
	if ($percent < 1)
	  { $percent = 1; }
	if ($percent > 100)
	  { $percent = 100; }
	print "&nbsp;" . $percent . "% &nbsp;";

	#zoom in
	if ($z > 25) { $newz = $z - 40; } else { $newz = $z - 10; }
	if ($newz < 5) {$newz = 5;}

	print "<input type=button value=\"Zoom In\" ".
    " onClick=\"document.optform.edit_panel.value='".$panel ."';".
    " document.optform.".$panel."_zoom.value = '-".$newz ."';".
    " document.optform.refresh_map.value = 1;".
    " document.optform.action='view_zmapplet.cgi';".
    " document.optform.submit();\">\n\n";

	print "<br><font size=-1><b>&nbsp;&nbsp;".
    "<a href=\"http://<!--|DOMAIN_NAME|-->/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-crossview.apg&OID=".
    $allpanels_id[$order_increment]."\"".
    ">". $panel . "</a>".
    " panel, LG: ".$lg .", units: cM".
    "</b></font></td>";
	$order_increment++;
      } else { $order_increment++; }
    #}
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
    print "<param name = \"panel_order\" value = \"ZMAP\|\">\n";
    #for $panel (@allpanels) { print $panel . "|"; }
    #print "\">\n";
    #    for $panel (@panels) {
    if(defined $Q->param($panel.'_ztotal') ){ #&& $Q->param($panel.'_ztotal')>0){
      print "<param name = \"" . $panel . "_ztotal\"\t value = ". $Q->param($panel.'_ztotal').">\n";
    }
    #    }

    #    if (defined $edit_panel && $edit_panel) {
    #      print "<param name = \"" . $edit_panel . "_ztotal\"\t value = ". $Q->param($edit_panel.'_ztotal').">\n";
    #    }
    if (! defined  $Q->param('OID')){$Q->param('OID', ''); }
    print   "<param name = \"marker_url\"\t value = \"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-markerview.apg&OID=\">\n".

      "<param name = \"panel_url\"\t value = \"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-crossview.apg&OID=\">\n".

      "<param name = \"target_frame\"\t value = \"$frame\">\n".
	 "<param name = \"selected_marker\"\t value = \"". $Q->param('OID')."\">\n".
	      "<param name = \"geno_url\" value = \"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-genotypeview.apg&OID=\">\n".
		  "<param name = \"zoom_url\" value = \"/<!--|CGI_BIN_DIR_NAME|-->/view_zmapplet.cgi\">\n".
		    "<param name = \"data\"\t\t value = \"$g_data\">\n".
		      "</APPLET><p>\n";
  }  else {			# mistakes were made...
    ###
    mapper_select(Q);
    print "<P><H3><font color = red>No Map Data Returned </font></H3><p>\n";
    #print $g_error  . "<p>\n";
    #print $g_data   . "<p>\n";
  }
  #print $note ."<P><P>\n";

  ### if a first query or  option/edit put select map back
  ### if it was a zoom  it should not need to change
#  if( defined $sm_refresh && $sm_refresh > 0)  {
#    print $Q->start_form (
#			  -method=>'POST',
#			  -action=>'/<!--|WEBDRIVER_PATH_FROM_ROOT|-->',
#			  -encoding=>'application/x-www-form-urlencoded',
#			  -name=>'selectform',
#			  -target=>'criteria'
#			 )
#	. $Q->hidden("MIval","aa-mapperselect.apg"). "\n";

#    for $tmp (@allpanels){
#      print $Q->hidden($tmp, (defined $Q->param($tmp))? $Q->param($tmp) :0) . "\n";
#    }
#    if ( ($sm_m ||  $Q->param('name')) ){ #&& ($sm_refresh == 1))  {
#      #print  $Q->hidden('marker', ($sm_refresh==1 )? $sm_m:  $Q->param('name')). "\n";
#      print  $Q->hidden('marker', (defined $sm_m )? $sm_m:  $Q->param('name')). "\n";
#    }else {
#      #print  $Q->hidden('loc_lg', ($sm_refresh >= 1)? $sm_lg : $Q->param('lg')) . "\n".
#	  #$Q->hidden('loc_panel',($sm_refresh >= 1)? $sm_panel : $Q->param('edit_panel')) . "\n".
#	  #$Q->hidden('loc ', ($sm_refresh >= 1)? $sm_loc : $Q->param('loc')) . "\n";

#      print  $Q->hidden('loc_lg', (defined $sm_lg)? $sm_lg : $Q->param('lg')) . "\n".
#	  $Q->hidden('loc_panel',(defined $sm_panel)? $sm_panel : $Q->param('edit_panel')) . "\n".
#	  $Q->hidden('loc ', (defined $sm_loc)? $sm_loc : $Q->param('loc')) . "\n";

#    }

#    print $Q->hidden('map_type','merged')."\n".
#    #$Q->submit("continue...")."\n".
#    $Q->end_form() . "\n";
#    print "\n<SCRIPT> document.selectform.submit()</SCRIPT>\n";
#  }


  print "<script language='JavaScript' src='http://<!--|DOMAIN_NAME|-->/footer.js'></script>";
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
    # my @panel_set;
    my @dud;
    my $data = '';
    #if((defined $pan) && $pan){
    #  push (@panel_set, $pan);
    #}else{@panel_set = @panels;}

    #table zmap_pub_pan_mark  (
    # 0         1) zdb_id varchar(50),
    #### 1      2) mname varchar(80),
    # 2         3) abbrev varchar(40),
    # 3         4) mtype varchar(10),
    # 4         5) or_lg integer,
    # 5         6) lg_location decimal(8,2),
    # 6         7) metric varchar(5),
    # 7         8) target_abbrev varchar(10),
    #### 8      9) target_id varchar(50),
    #### 9      10)private "informix".boolean,
    #### 0      11)owner varchar(50),
    #### 1      12)scores varchar(200),
    #### 2      13)framework_t "informix".boolean,
    #### 3      14)entry_date datetime year to fraction(3),
    # 4         15)mghframework "informix".boolean)

    #    table "tomc".zmap_pub_pan_mark
    #  (
    #    zdb_id varchar(50),                         ZDB-RAPD-980526-17
    #    abbrevp varchar(45),                        13y910_MOP
    #    mtype varchar(10),                          RAPD
    #    or_lg integer,                              1
    #    lg_location decimal(8,2),                   32.83
    #    metric varchar(5),                          cM
    #    target_abbrev varchar(10),                  ZMAP
    #    mghframework "informix".boolean,            f
    #    target_id varchar(50),                      ZDB-REFCROSS-010114-1
    #    panel_id varchar(50),                       ZDB-REFCROSS-980526-5
    #    mname varchar(80),                          13Y910
    #    abbrev varchar(40),                         13y910
    #    entry_date datetime year to fraction(3)     2001-03-21 16:00:04.000
    #  );


    my $stmt1 =
      'INSERT INTO PANLG ' .
      '  SELECT zdb_id,abbrev,mtype,target_abbrev,lg_location,or_lg, ' .
      '         case mghframework ' .
      '           when "t" then "t"::char(1) ' .
      '           when "f" then "f"::char(1) ' .
      '           else NULL ' .
      '         end, ' .
      '         metric, abbrevp '.
	'FROM zmap_pub_pan_mark '.
	  "WHERE panel_abbrev in (\'$pan\' )" . # panels
	    'AND or_lg = ? '.
	      "AND mtype IN (\'$types\');";

    my $stmt2  = 'SELECT FIRST 1 * FROM PANLG WHERE zdb_id = ? order by lg_location asc;';
    my $stmt22 = 'SELECT FIRST 1 * FROM PANLG WHERE zdb_id = ? order by lg_location desc;';

    # Note that this query in its original format was deadly to the informix
    # server.  The original format used an ABS() function call instead of a
    # case.  We aren't sure if the new format will fix the problem or not.
    #
    # Update on that: Removiing the ABS did not remove the problem.  IBM
    # suggested changing the mghframework column from boolean to char.
    # Not sure if that will fix the problem either.

    my $stmt3 =
      'SELECT zdb_id, abbrev, mtype, target_abbrev, lg_location, or_lg,' .
      '       mghframework, metric, map_name, ' .
      '       case when lg_location - ? >= 0 then lg_location - ? ' .
      '            else ? - lg_location ' .
      '       end distance ' .
      '  FROM PANLG ' .
      '  ORDER BY distance;';

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
    eval  {
		#                                zdb_id 1,          abbrev 2,          mtype 3,          target_abbrev 4,          lg_location 5,           or_lg 6,      mghframework 7,      metric 8
      $dbh->do("CREATE TEMP TABLE pool " .
	       "  ( " .
	       "    zdb_id varchar(50)," .
	       "    abbrev varchar(15)," .
	       "    mtype varchar(10)," .
	       "    target_abbrev varchar(10)," .
	       "    lg_location decimal(8,2)," .
	       "    or_lg integer," .
	       "    mghframework char(1)," .
	       "    metric varchar(5)," .
	       "    map_name varchar(25)" .
	       "  ) " .
	       "  WITH NO LOG ;");
      $dbh->do("CREATE TEMP TABLE fool " .
	       "  ( " .
	       "    zdb_id varchar(50)," .
	       "    abbrev varchar(15)," .
	       "    mtype varchar(10)," .
	       "    target_abbrev varchar(10)," .
	       "    lg_location decimal(8,2)," .
	       "    or_lg integer," .
	       "    mghframework char(1)," .
	       "    metric varchar(5)," .
	       "    map_name varchar(25)" .
	       "  ) " .
	       "  WITH NO LOG ;");
      $dbh->do("CREATE TEMP TABLE PANLG " .
	       "  ( " .
	       "    zdb_id varchar(50)," .
	       "    abbrev varchar(15)," .
	       "    mtype varchar(10)," .
	       "    target_abbrev varchar(10)," .
	       "    lg_location decimal(8,2)," .
	       "    or_lg integer," .
	       "    mghframework char(1)," .
	       "    metric varchar(5)," .
	       "    map_name varchar(25)" .
	       "  ) " .
	       "  WITH NO LOG ;");
      # $dbh->do( "CREATE INDEX panlg_zdb_idx ON PANLG(zdb_id)");
      # $dbh->do( "CREATE INDEX panlg_loc_idx ON PANLG(lg_location)");

      #should we try bulding a tmp table for each panel & lg once and keeping it for the transaction .... maybe (24 * 6)  permenant tables i.e HS1,HS2,...
      my $sth1 = $dbh->prepare($stmt1) or $note = $note . "SQL stmt1 failed <b> " .$stmt1 ."\n<br>";
      my $sth2 = $dbh->prepare($stmt2) or $note = $note . "SQL stmt 2 failed <b> " .$stmt2 ."\n<br>";
      my $sth22 = $dbh->prepare($stmt22) or $note = $note . "SQLstmt 22  failed <b> " .$stmt22 ."\n<br>";  #or return undef ;
      my $sth3 = $dbh->prepare($stmt3) or $note = $note . "SQL stmt 3 failed <b> " .$stmt3 ."\n<br>";      #or return undef ;

      $ztotal = $sth1->execute( $lg);
      #$note = $note . "$pan has $ztotal rows for LG $lg<p>\n";

      # try having the default region returned be dependent on
      # the number of markers available in the lg
      # make sure the zoom includes all instances of zdb_id (on this lg)
      #

      if( $ztotal >= $g_zoom) {
	$local_zoom = $g_zoom;
      }
      else{
	$rc = $ztotal / 20;
	$local_zoom = ($rc> 5)? $rc : 5;
      }

      #$note = $note . "Local Zoom ~ $local_zoom<p> \n";
      $panel ='ZMAP';

      #$note = $note . "probing for $zdbid 's location... \n";
      $rc  = $sth2->execute($zdbid);
      @row = $sth2->fetchrow;
      if( defined($row[4]) ){$note =  $note . "probe retuned with  $row[4] <p>\n";}
      else                  {$note =  $note . "probe retuned nothing <p> \n";}
      if ( @row >= 4 ) {
	#$note = $note .  "HIT  on $panel<p>\n";
	$row[4] =  ($row[4] == 0)? 0 : $row[4]; # clean up 0.00000E+00
	$loc = $lo = $hi = $row[4];

	$rc = $sth3->execute($loc,$loc,$loc);
	#$note =  $note .  "draping lg ".$lg ." about ". $loc ."<p>\n";

	$local_zoom = abs(($Q->param($panel.'_zoom'))? $Q->param($panel.'_zoom') : $local_zoom);

	#$note = $note .  "seeking closest $local_zoom markers to $zdbid at $loc on $pan.<p>\n";
	for ($zoom = 0; ( $zoom <= $local_zoom) && (@row = $sth3->fetchrow); $zoom ++){
	  if   ($row[4] < $lo){ $lo = ($row[4] == 0)? 0 : $row[4]; }
	  elsif($row[4] > $hi){ $hi = ($row[4] == 0)? 0 : $row[4]; }
	}

	$hi += .0001; $lo -= .0001; # kludge because bug where (loc >= x AND loc <= x) not (loc == x)
	#$note = $note . $lo . " -> " . $hi ."\n<br>";
    $rc  = $sth22->execute($zdbid);
	@row = $sth22->fetchrow;
	$hi = ($hi < $row[4])?$row[4]:$hi;
	#$note = $note . $lo . " --> " . $hi ."\n<br>";

	$zoom = get_between($pan, $lg, $types, $lo, $hi);
    $junk =    get_bins($pan, $lg, $types, $lo, $hi);
	$g_height = ($g_height < $junk)? $junk : $g_height;

	# emit hidden vars for $panel
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
      $dbh->commit;		# commit the changes if we get this far (drops temps)
    };				#end eval
    if ($@) {
      warn "Transaction aborted because $@";
      $dbh->commit;		#  so this will drop the temp(s) as well
    }
    #$note = $note . "g_data is " . length($g_data) . " Long \n<br>";
    $g_data;
  }				# end sub uniquery

  ################################################################################
  ### input:  panel_abbrev, lg, lo & hi locations, and marker types
  ### output: markers between and including the hi and lo locations in applet param format
  ###
  ### note this is the default refresh machenism, and called by 'get near marker'
  ### speed ups here would be have an effect on all secondary maps and some primary

  sub get_between {
    my ($panel, $lg, $types, $lg_lo, $lg_hi) = @_;
    $sql =
	"SELECT zdb_id,abbrev,mtype,target_abbrev,lg_location,or_lg," .
	"       case mghframework " .
	"         when 't' then 't'::char(1) " .
	"         when 'f' then 'f'::char(1) " .
	"         else NULL " .
	"       end, " .
	"       metric,abbrevp  ".
	"  FROM zmap_pub_pan_mark  ".
	"  WHERE panel_abbrev in (\'$panel\') ". # $panels
	"    AND or_lg = \'$lg\' " . # $lg
	    "AND mtype in (\'$types\' ) ". # $types
	      "AND lg_location >= \'$lg_lo\' ".	# $lo
		"AND lg_location <= \'$lg_hi\' ". # $hi
		  "ORDER BY lg_location, abbrev;";

    #$note = $note ."\n".  $sql . "<p>\n";
    my $curbetween = $dbh->prepare($sql) || return undef;
    $zoom  = $curbetween->execute  (); #  $panel, $lg, $types , $mlo, $mhi);
    if (! defined  $Q->param('OID')){$Q->param('OID', ''); }
    if ($zoom == 0)  {
      $g_printdata = $g_printdata . 'ZMAP' .'|'.$lg .'|'.$Q->param('OID').'|'.$lg_lo .'|'.$lg_hi .'|'.$print_type.'|'."\n"; #get between
      while(@row = $curbetween->fetchrow){
	$row[4] =  ($row[4] == 0)? 0 : $row[4]; # clean up 0.00000E+00
	$g_data = $g_data . "$row[0]|$row[8]|$row[2]|$row[3]|$row[4]|$row[5]|$row[6]|$row[7]|\n";
	$zoom++;
      }
    }

    foreach $junk (@panels)  {
      $g_opt =  $g_opt . "<INPUT TYPE=\"hidden\" NAME=\"".$junk."\" VALUE=\"1\">\n";
    }
    $g_opt =  $g_opt . "<INPUT TYPE=\"hidden\" NAME=\"ZMAP_lg\"VALUE=\"$lg\">\n";

    #$note = $note . "\n BETWEEN found |$zoom| markers between |$lg_lo|  and |$lg_hi| on |$lg|<p>\n ";
    $zoom;
  }

  ################################################################################
  ### input:  panel_abbrev, lg, lo & hi locations, and marker types
  ### output: number of bins in the interval
  ###

  sub get_bins {
    my ($panel, $lg, $types, $lg_lo, $lg_hi) = @_;

    $sql =
    "SELECT  count(distinct lg_location) ".
      "FROM zmap_pub_pan_mark  ".
	"WHERE panel_abbrev in (\'$panel\') ". # $panels
	  "AND or_lg = \'$lg\' " . # $lg
	    "AND mtype in (\'$types\' ) ". # $types
	      "AND lg_location >= \'$lg_lo\' ".	# $lo
		"AND lg_location <= \'$lg_hi\' ". # $hi
		 ";";# "GROUP BY lg_location;";

    #$note = $note ."\n".  $sql . "<p>\n";

    my $curbins = $dbh->prepare($sql) ||  return undef;

    $rc = $curbins->execute  (); #  $panel, $lg, $types , $mlo, $mhi);

    #$note = $note ."\n result code was $rc ". "<p>\n";

    @row = $curbins->fetchrow();

    $junk = $row[0];

    #$note = $note ."\nfound $junk disinct bins". "<p>\n";
    $junk;
  }

  ################################################################################
  ### input: marker_zdbid, lg,  panel_abbrev, (desired number of of markers) location, (and marker types global)
  ### output: the hi and lo locations that best contain the desired number of markers nearest to location.
  ###
  sub get_zooms  {
    my ($zdbid, $lg, $panel, $zoom, $loc) = @_;
    if( ! defined $loc || !$loc)  {
      #$note = $note . "finding the location of $zdbid on lg $lg of $panel<p>\n";
      $cur = $dbh->prepare("SELECT lg_location FROM zmap_pub_pan_mark WHERE panel_abbrev in (\'$panel\') ".
			   " AND or_lg = ? AND zdb_id = ? " );
      $rc = $cur->execute($lg, $zdbid);
      @row = $cur->fetchrow();
      $loc = $row[0];
    }
    $lg_lo = $loc;
    $lg_hi = $loc;
    #$note = $note . "get_ZOOM looking around $loc<p>\n";
    $sql =
      " SELECT lg_location, " .
      "        case when lg_location - $loc >= 0 then lg_location - $loc " .
      "             else $loc - lg_location " .
      "        end distance " .
      "   FROM zmap_pub_pan_mark " .
      "   WHERE panel_abbrev in ( \'$panel\' ) AND or_lg = ? ".
      "     AND mtype in ( \'$types\' ) ".
      "   ORDER BY distance;";

    $cur = $dbh->prepare($sql);
    $rc = $cur->execute ($lg);

    while( ($zoom-- >= 0) && (@row = $cur->fetchrow) ) {
      if($row[0] > $lg_hi){ $lg_hi = $row[0];}
      if($row[0] < $lg_lo){ $lg_lo = $row[0];}
    }
    #$note = $note . "get_ZOOM  found |$lg_lo| and |$lg_hi|<p>\n";
    $lg_lo = $lg_lo <= 0? 0: $lg_lo; ## get rid of 0.0E0
    #$note = $note . "get_ZOOM  found |$lg_lo| and |$lg_hi|<p>\n";
    $g_opt = $g_opt . $Q->hidden('ZMAP_from_ZOOM', $zoom ) . "\n";
    return ($lg_lo, $lg_hi);
  }

  ################################################################################
  ### input: a panel abbrev and a lg number
  ### output: entire linkage group in applet param format
  ### side effects: print_form and option_form updated, applet width and heigth adjusted
  sub lg_query  {
    my ( $panel ,$lg ) = @_;
    my $data = '';
    my $ztotal = 1;
    #$note = $note . "IN LG QUERY<p>\n";
    $sql =
	"SELECT zdb_id,abbrev,mtype,target_abbrev,lg_location,or_lg," .
	"       case mghframework " .
	"         when 't' then 't'::char(1) " .
	"         when 'f' then 'f'::char(1) " .
	"         else NULL " .
	"       end, " .
	"       metric,abbrevp ".
	"  FROM zmap_pub_pan_mark " .
	"  WHERE panel_abbrev in (\'$panel\') ".
	"    AND or_lg = ? ".
	    "AND mtype in  ( \'$types\') ".
	      "ORDER BY lg_location, abbrev;";

    $cur = $dbh->prepare($sql) ;
    $rc = $cur->execute($lg );
    $lo = 0; $ztotal = 0;
    while(@row = $cur->fetchrow){
      $row[4] =  ($row[4] == 0)? 0 : $row[4]; # clean up 0.00000E+00
      #zdb_id,  abbrev, mtype,  target_abbrev, lg_location, or_lg,  mghframework, metric
      # 0        1       2       3               4           5       6               7
      $lines = "$row[0]|$row[8]|$row[2]|$row[3]|$row[4]|$row[5]|$row[6]|$row[7]|\n";
      $data = $data . $lines;
      $ztotal ++;
      $hi = $row[4];
    }
    $g_printdata = $g_printdata .'ZMAP' .'|'.$lg .'|NULL|'.$lo .'|'.$hi .'|'.$print_type.'|'."\n"; # lg_query

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
    $junk = get_bins($panels_string, $lg, $types, $lo, $hi);
    $g_height = ($g_height < $junk)? $junk : $g_height;
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
	"FROM zmap_pub_pan_mark WHERE zdb_id = \'$zdbid\';";
    # "group by or_lg ".
    #  "order by count(or_lg) DESC;" ;

    $cur = $dbh->prepare($sql) ;
    $rc = $cur->execute();
    @row = $cur->fetchrow;
    $lg = shift(@row);
    $lg;
  }

################################################################################
### input:  a marker zdbid
### output: a lingage group number that the marker is in
sub  get_OIDs_abbrev{
  my ($zdbid)= @_;
  $sql =
  "SELECT first 1 abbrev " .
    "FROM zmap_pub_pan_mark WHERE zdb_id = \'$zdbid\'; ";
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
			 " from zmap_pub_pan_mark ".
			 " where panel_abbrev in (\'$panel\') ".
			 " and or_lg = \'$lg\' ".
			 " AND mtype in ( \'$types\' ) ".
			 " order by 4;"
			);
    $rc= $cur->execute();
    @row=$cur->fetchrow;
    ($row[0],$row[1],$row[2]);
  }

 ################################################################################
  ### input:   panel lg location
  ### output:  zdbid $ marker_abbrev --not further than any other marker to location.
  ### expects: $types as a global
  sub get_closest_on {
    my ($panel, $lg, $loc) = @_;
    undef @row;
    $cur = $dbh->prepare(
			 "select first 1 zdb_id, abbrev, lg_location, abs(lg_location - $loc) ".
			 " from zmap_pub_pan_mark ".
			 " where target_abbrev == \'$panel\' ".
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
  sub get_total  {
    my($panel,$lg,$types) = @_;

    $sql =
      "SELECT COUNT(*) FROM zmap_pub_pan_mark ".
	"WHERE panel_abbrev in (\'$panel\') ".
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
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM  zmap_pub_pan_mark  ".
	  "WHERE mname  = \'$marker\' AND panel_abbrev in (\'$panel\') ".
      "AND mtype IN (\'$types\') AND or_lg =  \'$lg\'; ";
    } elsif( defined  $panel) {
      $note = $note .  "Is |$marker| exactly unique on |$panel| offically?<p>\n ";
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM zmap_pub_pan_mark ".
	  "WHERE mname  = \'$marker\' AND panel_abbrev in (\'$panel\') ".
      "AND mtype IN ( \'$types\' ); ";
    } else  {
      $note = $note . "Is |$marker| exactly unique in ZFIN officially?<p>\n ";
      $sql = "SELECT UNIQUE zdb_id, or_lg FROM zmap_pub_pan_mark ".
	  "WHERE mname  = \'$marker\' AND mtype IN ( \'$types\' ) ;";
    }
    $cur = $dbh->prepare($sql);
    $rc = $cur->execute();
    $array_ref = $cur->fetchall_arrayref();
    #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
    $count = (defined @$array_ref)? @$array_ref : 0;
    #$note = $note . "found $count offically approved symbols that match<p>\n";
    #--------------------------------------------------------------------------
    if(($count < 1) ) {
      ### did NOT find any exact match--
      ### first look for "contains" with the current constraints
      #$note = $note .  "\nLOOKING for CONTAINS \n";
      if( (defined $lg) && $lg && ($lg ne "??") && ($lg > 0) && ($lg <= 25)) {
	    #$note = $note . "Is |$marker| similar & unique on |$panel|& |$lg| offically?<p>\n";
	    $sql = "SELECT UNIQUE zdb_id, or_lg FROM zmap_pub_pan_mark ".
	    "WHERE mname  like \'\%$marker\%\' AND panel_abbrev = (\'$panel\') ".
		"AND mtype IN ( \'$types\' ) AND or_lg = \'$lg\'; ";
      } elsif( defined  $panel ) {
        #$note = $note . "Is |$marker| similar & unique on |$panel| offically?<p>\n";
	    $sql = "SELECT UNIQUE zdb_id, or_lg FROM zmap_pub_pan_mark ".
	    "WHERE mname like \'\%$marker\%\' AND panel_abbrev = (\'$panel\') ".
		"AND mtype IN ( \'$types\' ); ";
      } else  {
	    #$note = $note . "Is |$marker| similar & unique in ZFIN offically?<p>\n";
	    $sql = "SELECT UNIQUE zdb_id, or_lg FROM zmap_pub_pan_mark ".
	    "WHERE mname  like \'\%$marker\%\' AND mtype IN ( \'$types\' ) ;"
	  }
      $cur = $dbh->prepare($sql);$cur->execute();
      $array_ref = $cur->fetchall_arrayref();
      #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
      $count = (defined @$array_ref)? @$array_ref : 0;
      #$note = $note . "found $count similar official symbols<p>\n";
    }
    #--------------------------------------------------------------------------
    if(($count < 1) ) { ############ if still no hits try all_map_names
        if( (defined $lg) && $lg && ($lg ne "??") && ($lg > 0) && ($lg <= 25) ) {
            #$note = $note . "Is |$marker| exactly unique on |$panel| |$lg| unoffically?<p>\n ";
            $sql = "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	        "FROM all_map_names pmn, zmap_pub_pan_mark pm ".
	        "WHERE allmapnm_zdb_id = pm.zdb_id ".
	        "AND allmapnm_name  = \'$marker\' AND pm.panel_abbrev in (\'$panel\') ".
		    "AND mtype IN (\'$types\') AND pm.or_lg =  \'$lg\'; ";
        } elsif( defined  $panel) {
            #$note = $note .  "|$marker| exactly unique on |$panel| unoffically?<p>\n ";
            $sql =
	            "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	            "FROM all_map_names pmn, zmap_pub_pan_mark pm ".
	            "WHERE allmapnm_zdb_id = pm.zdb_id AND allmapnm_name  = \'$marker\' ".
		        "AND pm.panel_abbrev in (\'$panel\') AND mtype IN (\'$types\'); ";
        } else  {
            #$note = $note . "|$marker| exactly unique in ZFIN  unoffically?<p>\n ";
            $sql =
	            "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	            "FROM all_map_names pmn, zmap_pub_pan_mark pm ".
	            "WHERE allmapnm_zdb_id = pm.zdb_id ".
	            "AND allmapnm_name = \'$marker\' AND mtype IN (\'$types\') ;";
        }
        $cur = $dbh->prepare($sql);$rc = $cur->execute();
        $array_ref = $cur->fetchall_arrayref();
        #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
        $count = (defined @$array_ref)? @$array_ref : 0;
        #$note = $note . "found $count <p>\n";
    }
    if(($count < 1) ) {
        ### did NOT find any exact match--
        ### first look for "contains" with the current constraints
        #$note = $note .  "\nLOOKING for CONTAINS \n";
        if( (defined $lg) && $lg && ($lg ne "??") && ($lg > 0) && ($lg <= 25)) {
	        #$note = $note . "|$marker| similar & unique on |$panel|& |$lg|?<p>\n";
	        $sql =
	        "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	        "FROM all_name_ends, all_map_names pmn, zmap_pub_pan_mark pm ".
	        "WHERE allmapnm_zdb_id = pm.zdb_id ".
                    "AND allmapnm_serial_id = allnmend_allmapnm_serial_id ".
		    "AND allnmend_name_end_lower like \'$marker\%\' ".
		    "AND pm.panel_abbrev = \'$panel\' ".
		    "AND mtype IN ( \'$types\' ) AND pm.or_lg = \'$lg\'; ";
        } elsif( defined  $panel ) {
	        #$note = $note . "|$marker| similar & unique on |$panel|?<p>\n";
	        $sql =
	        "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	        "FROM all_name_ends, all_map_names pmn, zmap_pub_pan_mark pm ".
	        "WHERE allmapnm_zdb_id = pm.zdb_id ".
                    "AND allmapnm_serial_id = allnmend_allmapnm_serial_id ".
		    "AND allnmend_name_end_lower like \'$marker\%\' ".
		    "AND pm.panel_abbrev = \'$panel\' ".
                    "AND mtype IN (\'$types\'); ";
        } else  {
	        #$note = $note . "|$marker| similar & unique in ZFIN??<p>\n";
	        $sql =
	        "SELECT UNIQUE allmapnm_zdb_id, pm.or_lg ".
	        "FROM all_name_ends, all_map_names pmn, zmap_pub_pan_mark pm ".
	        "WHERE allmapnm_zdb_id = pm.zdb_id ".
                    "AND allmapnm_serial_id = allnmend_allmapnm_serial_id ".
		    "AND allnmend_name_end_lower like \'$marker\%\' ".
                    "AND mtype IN (\'$types\') ;";
	    }
        $cur = $dbh->prepare($sql);
        $cur->execute();
        $array_ref = $cur->fetchall_arrayref();
        #$note = $note . @$array_ref . " " .$array_ref->[0][0]." ".$array_ref->[1][0]. "\n\n";
    }
    $array_ref;
  }				# end check unique

  ################################################################################
  ### boring constants
  sub pass_hidden  {
    if(defined $Q->param('loc_lg')&& !$Q->param('lg') ) {
      $Q->param('lg',$Q->param('loc_lg'));
    }
    $Q->delete('edit_panel','');
    my $buf ="";
    my $i = 0;
    my $pan = '';
    for $pan (@allpanels) {
      $buf = $buf .
        $Q->hidden('panel'.$allpanels_order[$i], $pan )."\n".
	  $Q->hidden($pan.'_units',$allpanels_metric[$i] )."\n";
      $i++;
    }
    $buf = $buf .
      $Q->hidden('OID',$Q->param('OID') )."\n".
	$Q->hidden('lg',$Q->param('lg'))."\n".
	  #$Q->hidden('userid',$Q->param('userid'))."\n".
	  $Q->hidden('userid',$Q->param('ZDB_authorize'))."\n".
	    $Q->hidden('refresh_map','1')."\n".
	      $Q->hidden('edit_panel','')."\n"
		;
    $buf;
  }				# end sub pass_hidden

  ################################################################################
}				#end mod it
0;
