#!/private/bin/perl -w

# FILE: upload.cgi
# PREFIX: made decision to not use prefix, as it makes this page
# more confusing.

# DESCRIPTION:
# This is a cgi, called from image-upload.apg, imageupdate.apg,
# xpatcuration.apg, and pubcuration.apg. It implements upload 
# to filesytem functionality.  This script interacts with the database, 
# and does under some conditions, create ZDB-IDs.  However, it does not
# update or insert into any tables.  It merely does the work of adding
# and moving files to filesytem.  Updating tables and inserting records
# must be accomplished by the page this cgi redirects to upon successful
# completion.

# INPUT VARS:
# $redirect_url: ("redirect_url" must be the name of the hidden redirect 
# parameter send by the calling apg page).  The apg page where this cgi 
# should return to upon successful completion.  
#
# $cookie (automatically sent if logged in, from the browser): valid cookie 
# from ZFIN that signifies a user is logged in.  Logged-in permissions 
# can vary.
#
# $filename ("upload" must be the name of the 'browse' box on a http form
# that allows uploading): name of the browsed file ready for upload.

# Note: any parameters for redirection
# need to be defined within this apg if they are required for proper 
# redirection.  For example: if this script is redirecting to new-image.apg
# it sends the $height, $width, and $OID as parameters.  If the script
# is redirecting to image-update.apg, it sends the above parameters, plus
# $attr, $attr_type and several others required by image-update.apg. 

# OPTIONAL VARIABLES
# $OID: the zdb_id of the record whose image or pdf is being updated.
# $attr: see image-update.apg for details
# $attr_type: see image-update.apg for details
# $old_value: see image-update.apg for details
# $attr_comments: see image-update.apg for details
# $redirect_OID: the OID necessary to redirect the user to the appropriate
# page.  ie: xpat_id if redirecting to xpatcuration.apg.

# OUTPUT VARS/OUTPUT/EFFECTS:
# if an image is uploaded, and no $OID passed in, script gets
# $OID from get_id database function, renames the file to that OID.suffix
# adds file and thumbnail to filesystem.
#
# if a pdf is uploaded, script gets $OID from apg page, renames file to  
# $OID.pdf to filesystem.
#
# if $OID is passed in, script assumes that file being uploaded
# should be renamed to $OID.suffix, again adds thumbnail and image if image
# file, adds pdf if pdf file.
# 
# in all cases, script redirects to another ZFIN apg page.

use CGI qw(:standard);
use DBI;
use English;

$CGI::POST_MAX= 1024 * 1024 * 20 ; # limits post size to 10 meg

$mailprog = '/usr/lib/sendmail -t -oi -oem';

# remove any existing error reports 

#-----------------Begin_Variables------------------#

# set database environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{PATH}= "/bin:/local/apps/netpbm/bin";

# permission variables

$person_id = "";
$access = "";
$owner_id = "";
$cookie = "";

# variables for constructing the page that this cgi dumps the user to after
# error-free completion.

# redirect_OID_param is the OID used in the apg being redirected to. (could be
# image or xpat or pub zdb_id)

$redirect_OID_param = "&OID=";
$redirect_new_OID_param = "&xpcur_G_image_OID=";

# redirect_new_OID_param is the prefix for the image or pub OID being sent
# back to the redirected/calling apg page. 

$redirect_fimgnew_suffix_param = "&fimgnew_suffix=";
$redirect_suffix_param = "&suffix=";
$redirect_xpcurup_suffix_param = "&xpcurup_suffix=";

$redirect_fimgnew_height_param = "&fimgnew_height=";
$redirect_fimgnew_width_param = "&fimgnew_width=";

$redirect_xpcurup_width_param = "&xpcurup_width=";
$redirect_xpcurup_height_param = "&xpcurup_height=";

$redirect_height_param = "&height=";
$redirect_width_param = "&width=";

$redirect_attr_param = "&attr=";
$redirect_attr_type_param = "&attr_type=";

$redirect_old_value_param = "&old_value=";
$redirect_comments_param ="&comments=";
$redirect_table_param = "&table=";

$redirect_pub_file_param ="&pub_file=";

$redirect_new_value_param="&new_value=";
$redirect_zdb_id_param ="&zdb_id=";
$redirect_build = "";

$new_image_redirect = "aa-new-image.apg";
$xpat_redirect = "aa-curation.apg";
$update_image_redirect = "aa-do-imageupdate.apg";
$xpcur_G_image_OID_param = "&xpcur_G_image_OID=";
$xpcur_G_fig_param = "&xpcur_G_image_fig=";
$xpcur_G_fig = "";
$xpcur_G_image_label_param="&xpcur_G_image_label=";
$xpcur_G_image_label="";

# optional redirct parameters, have to 'strip' newline characters
# off of new_value and old_value in order to send them as a url.
# this is not accomplished by the database because fimg_comments is an
# lvarchar field, and scrub char does not check lvarchar, blob, html
# fields as these fields usually are supposed to have formatting as such.

# set attr_type equal to 1 so that we can check if it got set by the calling
# apg rather than this cgi.

$attr = "";
$attr_type = "1";
$old_value = "" ;
$attr_comments = "";
$zdb_id = "";
$MIval = "";
$new_value = "";
$table = ""; 
$pub_file = "";

# directory for saving uploaded files.

$upload_dir = "";

# variables for checking filename nomenclature and existance in the filesystem

$img_jpg_suffix = ".jpg";
$img_jpeg_suffix = ".jpeg";
$img_gif_suffix = ".gif";
$pdf_suffix = ".pdf";
$suffix = "";
$pdf_prefix = "ZDB-PUB-";
$image_prefix = "ZDB-IMAGE-";

# don't want to overwrite a filename/OID that is a zdb-id (specifically for 
# updating existing image or pub records).
# variables for checking/creating zdb_ids.

$OID_type = "";
$OID_in_DB = "";
$OID_from_apg_page= "";
$OID = "";

# separate out the filename so we can make a thumbnail like:
# ZDB-IMAGE-010101-1_thumb.jpg and an image with annotation like:
# ZDB-IMAGE-010101-1_annot.jpg
# variables for filename processing

$filename_no_suffix = "";
$add_thumb = "_thumb";
$add_annot = "_annot" ;
$newthumb = "";
$image_thumb_exists ="";
$image_annot_exists ="";
$image_file_exists="";
$date="";

# height, width variables.

$height = 0;
$width = 0;

#--------------------End_Variables------------------#


#--------------------Begin_Sub_Routines-------------#

sub confirmLogin () {

    if ($ENV{'HTTP_COOKIE'}){ # get cookie sent from browser, Zfin only sends 
	                      # one cookie.

	$cookie = $ENV{'HTTP_COOKIE'};

	# split the cookie into 2 parts, its cookie name (at ZFIN, that's
	# zfin_login, and its value).

	my $semicolon = ";";
	my $space = " ";
	$cookie = $cookie.$semicolon.$space;
	
	@pairs = split(/; /, $cookie);

        foreach $pair (@pairs) {
          ($name, $value) = split(/=/, $pair);
	  
          $find_cookie{$name} = $value;
        }

        $ZDB_cookie = $find_cookie{"zfin_login"};

	#($name, $value) = split (/=/, $cookie);
	#$ZDB_cookie = $value ;
	
        # connect to the database and see if the passed cookie also
        # has the right set of permissions.

        my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
			       '', 
			       '', 
			       {AutoCommit => 1,RaiseError => 1}
			       )
	    || &emailError("Failed while connecting to <!--|DB_NAME|--> ");

	my $cur = $dbh->prepare("select zdb_id, access
                                   from zdb_submitters
                                    where cookie = '$ZDB_cookie';"
				);
	$cur->execute;
	my ($db_person_OID, $db_access);
	$cur->bind_columns(\$db_person_OID, \$db_access);
	while ($cur->fetch()) {
	    $person_id = $db_person_OID;
	    $access = $db_access;
        }
        $dbh->disconnect();

	# make sure person_id is not null, if it is null, then cookie
	# was not found in zdb_submitters

	if ($person_id eq "") {
	 
	    &access_error('no person_id listed in zdb_submitters');

	} # end if person_id is null

    } # end if cookie

    else {
	
	&access_error ('no cookies');        
    }
}

sub getHeightWidth()
  { # get the height, width of image--if netpbm package fails, return 0,0
      $_[0] =~  m/([A-Za-z\d\-\_\$\+\=\~\.\,\ \/]+)/;
      my $imageFile = $1;
      
      my $tmpDir     = "/tmp/get_image_stats.$PROCESS_ID";
      my $pnmFile    = "$tmpDir/pnmfile";
      my $statsFile  = "$tmpDir/stats";
      my $stderrFile = "$tmpDir/err";
      my $retVal = 0;
      
      my $dirPerms = oct(777);
      mkdir($tmpDir, $dirPerms);

      system("/local/apps/netpbm/bin/anytopnm $imageFile > $pnmFile 2> /dev/null");
      system("/local/apps/netpbm/bin/pnmfile $pnmFile > $statsFile 2> $stderrFile");

      # If anything was written to stderr then give up.

      if (-s $stderrFile) {
	      $retVal = 1;
      }
      
      else {

	  # Parse standard out looking for width and height

	  open(STATSFILE, $statsFile);

	  my $line;

	  while ( defined(STATSFILE) && ($line = <STATSFILE>) && ! $width) {
	      if ($line =~ / by /) {
		  my @tokens1 = split(/,/, $line);
		  my @tokens2 = split(/\s+/, pop(@tokens1));
		  $width  = $tokens2[1];
		  $height = $tokens2[3];
	      }
	  }
      }

system("/bin/rm -rf $tmpDir") and die "can't remove $tmpDir";

} # end getHeightWidth

sub getRecordOwnership () {

    # for FX, we have duplicate tables, fish_image and fx_fish_image_private.
    # zdb_ids for images are distinct between tables.
    # thus, the owner of an image could either be in fish_image or 
    # fx_fish_image_private--2 tables have to be passed in.  This will go
    # away when FX changes are committed
    
    $vColumnOwner = $_[0];
    $vTableName = $_[1];
    $vColumnZdb = $_[2];
    $vOID = $_[3];
#    $vAltColumnOwner = $_[4];
#    $vAltTableName = $_[5];
#    $vAltColumnZdb = $_[6];
    
    my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
			   '', 
			   '', 
			   {AutoCommit => 1,RaiseError => 1}
	       	           )
	|| &emailError("Failed while connecting to <!--|DB_NAME|--> ");
    
    my $cur = $dbh->prepare("select $vColumnOwner
                               from $vTableName
                               where $vColumnZdb = '$vOID';"
 #                            union
 #                               select $vAltColumnOwner
 #                                 from $vAltTableName
 #                                 where $vAltColumnZdb = '$vOID';"
			    );
    
    $cur->execute;
    my ($owner);
    $cur->bind_columns(\$owner);
    while ($cur->fetch()) {
	$owner_id = $owner
        }
    $dbh->disconnect();
}

sub getOID () { # open a handle on the db to get a new zdb_id and a name for 
    # the file despite its assigned name on upload.
    
    my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
			   '', 
			   '', 
			   {AutoCommit => 1,RaiseError => 1}
	       	           )
	|| &emailError("Failed while connecting to <!--|DB_NAME|--> ");
    
    my $cur = $dbh->prepare("select get_id('$OID_type') as OID
                                 from single;"
			    );
    $cur->execute;
    my ($DBOID);
    $cur->bind_columns(\$DBOID);
    while ($cur->fetch()) {
	$OID = $DBOID;
    }
    $dbh->disconnect();
    
} # end getOID


sub checkForOID () { # check that the zdb_id of the filename 
    # is in the database. 
    
    $vTableName = $_[0];
    $vColumn = $_[1];
   # $vAltColumn = $_[2];
   # $vAltTableName = $_[3];
    $vOID = $_[2];
    
    my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
			   '', 
			   '', 
			   {AutoCommit => 1,RaiseError => 1}
			   )
	|| &emailError("Failed while connecting to <!--|DB_NAME|--> ");
    
    my $cur = $dbh->prepare("select $vColumn as OID
                                 from $vTableName
                                 where $vColumn = '$vOID';"
                              # union
                              #   select $vAltColumn as OID
                              #     from $vAltTableName
                              #     where $vAltColumn = '$vOID';"
			    );
    $cur->execute;
    my ($existOID);
    $cur->bind_columns(\$existOID);
    while ($cur->fetch()) {
	$OID_in_DB = $existOID;
    }
    $dbh->disconnect();	      
    
} # end checkForID


sub uploadFile() { # upload the file taking the directory (pdf or image)
                   # to upload to, as a parameter.
    
    $vFilename = $_[1];
    $vDir = $_[0];

    # "upload" comes from the webdatablade/html form: image-upload.apg,
    # pubcuration.apg, and imageupdate.apg
    
    $upload_filehandle = $query->upload("upload");
    
    open UPLOADFILE,"> $vDir/$vFilename"|| 
	die "could not open $vDir/$vFilename";
    
    # have to use binmode b/c we're on unix and loading from windows 
    # filesystem.
    
    binmode UPLOADFILE;
    
    while ( <$upload_filehandle> )
    {
	print UPLOADFILE;
    }
    close UPLOADFILE;

} # end uploadFile

sub getSuffix() { # gets the last 4 or 5 digits from the filename to determine 
                  # where to put the file in the filesystem.
    
  # jpeg suffix is longer than jpg,gif, so do a special case suffix check
    
    if (substr($filename,-5) eq $img_jpeg_suffix) {
	
	$suffix = lc(substr($filename,-5));
    }
    
    else {
	
	$suffix = lc(substr($filename,-4)); 
    }
    
} # end getSuffix

sub makeFiles () {# uploads the files, builds a thumbnail, gets the height 
		  # and width of the file, redirects the user to the 
		  # appropriate apg or static page.
		  
    $suffix = lc($suffix) ;
    $filename = $OID.$suffix;

    &uploadFile($upload_dir, $filename);

    system("/bin/chmod 775 $upload_dir/$filename*") and die "can't change mod 775 $upload_dir/$filename" ;

    # build a thumbnail file name if its not a pdf

    if ($suffix ne $pdf_suffix) {
	
        $newthumb = $OID.$add_thumb.$suffix;

        $filename = "/".$filename;
	
        &getHeightWidth($upload_dir.$filename);
	
        $filename = substr($filename,1);

        # make a thumbnail

        system("<!--|ROOT_PATH|-->/server_apps/sysexecs/make_thumbnail/make_thumbnail.sh $upload_dir/$filename $upload_dir/$newthumb") and die "can't make thumbnail";

        # give web users read/execute permission
		
        system("/bin/chmod 644 $upload_dir/$OID*") and die "can't chmod $upload_dir/$OID*" ;
		
        # redirect to the correct apg page based on the passed-in redirect_url.

	if ( (substr($redirect_url,-15)) eq $xpat_redirect) { # if the redirect_OID parameter is not null

	    $redirect_OID = $query->param("redirect_OID");
	    $xpcur_G_fig = $query->param("xpcur_G_image_fig");
	    $xpcur_G_image_label = $query->param("xpcur_G_image_label");

	    $xpat_fig =~ s/\s/\_/g;
	    $xpat_image_label =~ s/\s/\_/g;

	    if ($redirect_OID !~ m/ZDB\-\D{1,10}\-\d{1,6}\-\d{1,5}/) {
		&access_error("redirect_OID not a ZDB_id");
	    }

	    $redirect_new_OID_param = $xpcur_G_image_OID_param ;
	    
	    $update_param = "&xpcur_c_xpatcuration_update=update"; 

	    $redirect_build = $redirect_url.$redirect_OID_param.$redirect_OID.$redirect_new_OID_param.$OID.$redirect_xpcurup_suffix_param.$suffix.$redirect_xpcurup_height_param.$height.$redirect_xpcurup_width_param.$width.$xpcur_G_fig_param.$xpcur_G_fig.$xpcur_G_image_label_param.$xpcur_G_image_label.$update_param;
	    
	    if ($attr_type ne "text" &&
		$attr_type ne "textarea" &&
		$attr_type ne "pic" &&
		$attr_type ne "1" &&
		$redirect_build !~ m/.apg/) {
		
		&filename_error ($attr_type.'attr_type not ZFIN type!') ;
		
	    }
	    else {

		print $query->redirect ("$redirect_build");
	    
		system ("/bin/rm -f /tmp/upload_report") and die "can't rm /tmp/upload_report.txt";
		exit;
	    }

	}
	else  { # if redirect is not xpatcur, but is an image still

	    $redirect_build = $redirect_url.$redirect_OID_param.$OID.$redirect_fimgnew_suffix_param.$suffix.$redirect_fimgnew_height_param.$height.$redirect_fimgnew_width_param.$width.$redirect_attr_param.$attr.$redirect_attr_type_param.$attr_type.$redirect_table_param.$table.$redirect_zdb_id_param.$zdb_id.$redirect_comments_param.$attr_comments.$redirect_old_value_param.$old_value.$redirect_new_value_param.$new_value;

	
	    if ($attr_type ne "text" &&
		$attr_type ne "textarea" &&
		$attr_type ne "pic" &&
		$attr_type ne "1" &&
		$redirect_build !~ m/.apg/) {
		
		&filename_error ($attr_type.$redirect_build.'attr_type, redirect not ZFIN type!') ;

	    }

	    
	    system ("/bin/rm -f /tmp/upload_report") and die "can't /bin/rm -f /tmp/upload_report";
	    print $query->redirect ("$redirect_build");
	    exit;
	}


    } #end if OID not pub
    
    else { # if OID is a pub zdb id
	      
	    $pub_file = $filename ;
	    $redirect_build = $redirect_url.$redirect_OID_param.$OID.$redirect_pub_file_param.$pub_file ;
	    if ($redirect_build !~ m/.apg/) {
		&access_error ($redirect_build.'redirect_build not ZFIN type!') ;
	    }
	    system ("/bin/rm -f /tmp/upload_report") and die "can't /bin/rm -f /tmp/upload_report";
	    print $query->redirect ("$redirect_build");
	    exit;
    }
    
} # end makeFiles 

sub access_error () { # throws up an error message and sends email
                      # if users access is not correct.
    $vHint = $_[0];
    
    print $query->header ();
    
    print "<HTML><BODY>";
    print "<title>ZFIN Upload</title>";
    print "<p> !! File was *NOT* Uploaded, access denied.  <br><br>
              You must be the owner of an image file to update it,
              and you must have permission to upload new files. <br><br>
              Please contact ZFIN for further information: 
              staylor\@cs.uoregon.edu !!<br><br>";

    &emailError ("login confirmation failed: $vHint $ENV{'HTTP_COOKIE'} $person_id $filename <!--|DB_NAME|-->");
    print "</body>";
    print "</html>";
    
    system ("/bin/rm -f /tmp/upload_report") and die "can't /bin/rm -f /tmp/upload_report" ;
    exit;

}

sub filename_error() { # standard error for file nomenclature problems
    $vHint = $_[0];

    print $query->header ();

    print "<HTML><BODY>";
    print "<title>ZFIN Upload</title>";

    print "<p> !! File was *NOT* Uploaded, please consult file 
          nomenclature !!<br><br>";
    print "File must have one of the following extensions: 
         .jpg, .jpeg, or .gif<br><br>";
    print "If you are trying to replace an existing image, 
          please use the update pages.<br><br>"; 
    print "The maximum filesize accepted by this page is 1024K.
          <br><br>$vHint";
    system ("/bin/rm /tmp/upload_report") and die "can't /bin/rm /tmp/upload_report"  ;

    &emailError ("File not uploaded: $person_id $vHint $filename <!--|DB_NAME|-->");
    print "</body>";
    print "</html>";
   
    system ("/bin/rm -f /tmp/upload_report") and die "Cannot /bin/rm /tmp/upload_report";
    exit;

} # end filename_error

sub emailError() { # email error to db_owner
    &writeReport($_[0]);
    &sendReport();

} # end emailError

sub writeReport() { # creates an upload report to send to db_owner
    open (REPORT, ">>/tmp/upload_report") or die "cannot open report";
    print REPORT "$_[0] \n\n";
    close (REPORT);
} # end writeReport

sub sendReport() { # does the email creation
    open(MAIL, "| $mailprog") || die "cannot open mailprog $mailprog, stopped";
    open(REPORT, "/tmp/upload_report") || die "cannot open report";

    print MAIL "To: <!--|DB_OWNER|-->\@cs.uoregon.edu\n";
    print MAIL "Subject: Upload Report\n";
    while($line = <REPORT>)
    {
      print MAIL $line;
    }
    close (REPORT);
    close (MAIL);

} # end SendReport

sub getDate {
    ($sec,$min,$hour,$day,$mon,$year) = (localtime)[0,1,2,3,4,5];
    $year = $year + 1900 ;

    $date = $sec."_".$min."_".$hour."_".$day."_".$mon."_".$year; 

}
#-------------------End_Sub_Routines-----------------#

#-------------------BEGIN_MAIN-----------------------#

# open a new cgi session, get the filename and redirect_url from apg page

$query = new CGI();

# confirm that a cookie from ZFIN has been passed.

&confirmLogin;

$redirect_url = $query->param("redirect_url");

if ($redirect_url !~ m/.apg/) {
    &filename_error('redirect url not ZFIN-esque '.$redirect_url); 
}
$filename = $query->param("upload");

# if a blank image file is uploaded, return to calling apg with no 
# changes to that page.  However, on the image update pages, if there is
# no file in the upload box, then we need to capture and pass the non-image
# variables.

if (!($filename) && 
   (substr($redirect_url, -21) ne $update_image_redirect) &&
    ($redirect_url ne "")) {   

    system ("/bin/rm -f /tmp/upload_report") and die "can not /bin/rm /tmp/upload_report" ;
    print $query->redirect ($ENV{'HTTP_REFERER'});
    exit;
}
# if the redirect is do-imageupdate.apg and we don't have an image,
# then we need to do other updates besides uploading a file--just
# take the parameters from the calling apg and pass them on to do-imageupdate. 

elsif (!($filename) && 
       (substr($redirect_url, -21) eq $update_image_redirect)) {

    $attr = $query->param("attr");
    $attr_type = $query->param("attr_type");
    $old_value = $query->param("old_value");
    $attr_comments = $query->param("comments");
    $new_value = $query->param("new_value");
    $table = $query->param("table");
    
    	    
    if ($attr_type ne "text" &&
	$attr_type ne "textarea" &&
	$attr_type ne "pic" &&
	$attr_type ne "1") {
	
	&filename_error ($attr_type.'attr_type not ZFIN type!') ;
	
    }


    # make sure the table variable is one specified by the calling
    # apg page. 

    if ($table eq "image") {

    #"fx_fish_image_private" || $table eq

	$zdb_id = $query->param("zdb_id");
	
	if ($zdb_id !~ m/img_zdb_id/){
	    #|fimgp_zdb_id

	    &filename_error($zdb_id.'zdb_id is not ZFIN pattern (case 1)') ;
	}

	$OID_from_apg_page = $query->param("OID");

	if ($OID_from_apg_page !~ m/ZDB\-\D{1,10}\-\d{1,6}\-\d{1,5}/){
	    &filename_error($OID_from_apg_page.'tableOID is not ZFIN pattern (case 2)') ;
	}

	# make sure we're passing an image, don't want to grab params from
	# non-image apg pages. 
	# 2006-12-4 : frodo redo : curation page apg is PUB

	if (substr($OID_from_apg_page, 0,10) eq $image_prefix || substr($OID_from_apg_page, 0,8) eq $pdf_prefix) {
	    
	    $OID = $OID_from_apg_page ;

	    # if its a image, and its a text area annotations, then need to 
	    # replace the newlines, carriage returns, and spaces with 
	    # their ascii equivilents, else the url will not be accepted
	    # by webdriver.

	    if (($attr_type eq 'textarea')&&
		($old_value ne ""||$new_value ne "")) {

		# carriage returns (replace one or more occurances of '\r'
		# with nothing
		$old_value =~ s/\r{1,}//g;
		$new_value =~ s/\r{1,}//g;

		# new lines, %0a is the hex symbol for newline
		$old_value =~ s/\n{1,}/%0a/g;
		$new_value =~ s/\n{1,}/%0a/g;

		#spaces, %20 is the hex symbol for space
		$old_value =~ s/' '{1,}/%20/g;
		$new_value =~ s/' '{1,}/%20/g;
		
	    }

	    # the redirect if no image to upload, just hold and pass params
	    
	    $redirect_build = $redirect_url.$redirect_OID_param.$OID.$redirect_attr_param.$attr.$redirect_attr_type_param.$attr_type.$redirect_table_param.$table.$redirect_zdb_id_param.$zdb_id.$redirect_comments_param.$attr_comments.$redirect_old_value_param.$old_value.$redirect_new_value_param.$new_value; 
	    
	    if ($redirect_build !~ m/.apg/) {
		&access_error ($redirect_build.'redirect_build not ZFIN type!') ;
	    }

	    system ("/bin/rm -f /tmp/upload_report") and die "can not /bin/rm /tmp/upload_report";

	    print $query->redirect ($redirect_build);

	    exit;
	}

	else { # if the prefix isn't an image, then return an error
	
	    &filename_error('page prefix does not match OID prefix');
	
	}

    }

    else {
	
	&filename_error('table not right'.$table);
   
    }
}

else { # filename isn't null or redirect isn't do-imageupdate.apg
       # do the rest of the work, assume valid new/update file submitted.

    # chop everything off the filename before the last / 
    # and to the end of the file.  Example: 
    # /staylor/ZDB-IMAGE-010101-1.jpg becomes ZDB-IMAGE-010101-1.jpg.

    $filename =~ s/.*[\/\\](.*)/$1/;
    
    # make sure the suffix is acceptable (could use passed-in parameters
    # to check file-type.  But passed-in parameters get their value from
    # file suffix, so might as well check the suffix directly).
    
    if (
	lc(substr($filename,-4)) eq $img_jpg_suffix || 
	lc(substr($filename,-4)) eq $pdf_suffix ||
	lc(substr($filename,-4)) eq $img_gif_suffix ||
	lc(substr($filename,-5)) eq $img_jpeg_suffix ) {
	
	
#------------------BEGIN_IMAGES--------------------#

	# check that the the suffix is in lc(.jpg, .jpeg, .gif, .pdf)

	if (
	    lc(substr($filename,-5)) eq $img_jpeg_suffix ||
	    lc(substr($filename,-4)) eq $img_gif_suffix ||
	    lc(substr($filename,-4)) eq $img_jpg_suffix){
	    
	    # while apache recognizes /imageLoadUp and points
	    # urls to right directory, Perl needs the complete filename.
	    # loadup_full_path points to /research/zcentral/loadUp
	    # image_load points to /imageLoadUp.

	    $upload_dir = "<!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->";
	    
	    if ((substr($redirect_url,-16) eq $new_image_redirect)||
		(substr($redirect_url,-15) eq $xpat_redirect)) {
		
		# if the redirect is new-image.apg or xpatcuration
		# and suffix ok then get an OID.  Assume xpatcuration
		# always wants a new OID for every uploaded image.
		# Load the file with that OID+suffix as a filename.
	    
		$OID_type = "IMAGE";
	       
		if (!($access)) { # if no cookie, $access variable not set
		    # so no progress can be made.  At this 
		    # point, anyone with even a 'GUEST' access
		    # level can add new images as everyone
		    # who gets to the new-image.apg, gets at 
		    # least a 'GUEST' cookie.
		    &access_error ('no_access');
		}
	    
		elsif ((substr($redirect_url,-15) eq $xpat_redirect)
		       &&($access eq "root")) { 
                    # else if $access is not null AND redirect is xpatcuration

		    &getOID; # get a new OID
		    
		    &getSuffix; # get the file suffix
		    
		    &makeFiles; # upload files, make thumbnail, redirect user
		
		} # end else $access is root and redirect_url is xpat

		elsif (substr($redirect_url,-16) eq $new_image_redirect &&
		       $access) {
		    # if its a new image, we can't set access to root
		    # but does need access

		    &getOID; # get a new OID
		    
		    &getSuffix; # get the file suffix
		    
		    &makeFiles; # upload files, make thumbnail, redirect user
		} # end else access is not null and redirect is 
		  # new_image_redirect
		
		else {

		    &access_error ('redirect_url is wrong or access is wrong');

		} # end access/redirect checks

	    } # end if suffix ok and redirect is either new-image.apg
	      # or xpatcuration.apg
	    
	    else { # redirect is not "aa-new-image.apg" and its an image file
	       	    
		if (($access eq "submit")||
		    ($access eq "root")) { # if user is logged in
		    
		    # get the OID of the calling apg.
		
		    $OID_from_apg_page = $query->param("OID");

		    if ($OID_from_apg_page !~ m/ZDB\-\D{1,10}\-\d{1,6}\-\d{1,5}/){
			&filename_error($OID_from_apge_page.'OID is not ZFIN pattern (case 3)') ;
		    }
		
		    if (substr($OID_from_apg_page, 0,10) ne $image_prefix){
			
			&filename_error('try to load image from pdf page');
		    }
		
		    # get the owner of the OID from the apg page
		
		    else {
		
			&getRecordOwnership("img_owner_zdb_id",
					    "image", 
					    "img_zdb_id",
					    $OID_from_apg_page);
					 #   "fimgp_owner_zdb_id",
					 #   "fx_fish_image_private",
					 #   "fimgp_zdb_id");
			
			&getSuffix; # get the file suffix
			
			if ($access eq "root") {
			    
			    $filename = $OID_from_apg_page.$suffix;
			    
			    $filename_no_suffix = $OID_from_apg_page;
			    
			    $OID = $OID_from_apg_page;
			   
			    # move the existing file to a bkup directory
			    
			    $image_file_exists = system("/bin/test 'grep $filename <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/'");
			    
			    &getDate ;
			    
			    if ($image_file_exists == 0) {	            
				
				&checkForOID ('image',
					      'img_zdb_id',
					     # 'fimgp_zdb_id',
					     # 'fx_fish_image_private',
					      $OID); 
				    
				$image_thumb_exists = system("/bin/test 'grep $file_thumb <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/'");  
				    
				$image_annot_exists = system("/bin/test 'grep $file_annot <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/'");
				
				# returns $OID_in_DB if OID exists in the 
				# database
				
				# if the OID does not exist in the tables, 
				# or if the OID exists,
				# but the redirect-url 
				# takes the user to the 'new' image page, 
				# then something is wrong
				
				if ((!($OID_in_DB))||
				    ($redirect_url eq "/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-new-image.apg")){
				    &filename_error('!OID_in_DB');
				    
				}  # end OID_in_DB 
				
				# if the filename is ok, and the 
				# ZDB-id exists, 
				# go ahead and make the new files, 
				# which means the
				# old file should exist in the 
				# bkup directory
				
				else { 
				    # OID in db and redirect != 
				    # new_image.apg
				   
				    system("/bin/mv <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/$filename_no_suffix* <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/bkup/") and die "can not mv <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/$filename_no_suffix ";
				    $attr=$query->param("attr");
				    $attr_type=$query->param("attr_type");
				    $old_value=$query->param("old_value");
				    $attr_comments=$query->param("comments");
				    
				    &makeFiles;
				    
				} # end OID in db and redirect != 
				# new_image.apg
				
			    } # ends if image file exists
			   
			
			} # ends if owner is person		
		    
			else { # owner is not cookie owner
			
			    &access_error ('owner is not cookie owner'); 
			}
			
		    } # ends oid_from_apg_page prefix is zdb-image
		
		} # ends access is root
	    
		else { # access is not root or submit and trying to update
		
		    &access_error ('access is not root, or submit');
		}
		
	    }  # ends else redirect isn't new-image.apg
	
	} # ends suffix in .jpg, .jpeg, or .gif

#--------------------END_IMAGES---------------------#

#--------------------BEGIN_PDFs---------------------#
	
	else { # if the filename ends with .pdf.
	    
	    if ($access ne 'root') { # if user does not have root access

		&access_error ('pdf access is not root');

	    }

	    else { # user has proper access to load PDFs
	    
		$upload_dir = "<!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->";
		
		$OID_from_apg_page = $query->param("OID");
	    
		# the next check ensures that you can't upload a pdf
		# from an image site.

		if (substr($OID_from_apg_page,0,8) ne $pdf_prefix){
		
		    &filename_error('oid from apg ne pdf_prefix');
		
		} # end pdf_prefix ok

		&getSuffix; # get the file suffix
	    
		$filename = $OID_from_apg_page.$suffix;
	    
		$filename_no_suffix = $OID_from_apg_page;
	    
		$OID = $OID_from_apg_page;
	    
		# test if file with OID from apg exists in the filesystem 

		$pdf_file_exists = system("/bin/test 'grep -a $filename_no_suffix* <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/'");

		&checkForOID ('publication',
			      'zdb_id',
			     # 'sngl_one',
			     # 'single',
			      $OID);
		
		# returns $OID_in_DB if OID exists in the database

		if (($OID_in_DB ne "") && ($redirect_url ne "/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-new_image.apg")) {
		 
		    # image_file_exists in filesystem and db already, so move
		    # the one that exists to make room for the new one.
		
		    if ($pdf_file_exists == 0) { # already has pdf, move it.

			&getDate ;

			system("/bin/mv <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/$filename* <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/bkup/");
		    
			&makeFiles;

		    } # end move files

		    else { # no file in filesystem, go ahead and make one

			# if the filename is ok, and the ZDB-id exists, 
			# go ahead 
			# and make the new files, which means the
			# old file should exist in the bkup directory

			&makeFiles;

		    } # end makeFiles
		

		} # end OID in db and redirect not new_imge 
	    
	    } # ends has proper access to load pdfs

	} # ends suffix in .pdf

#----------------------END_PDFs----------------------#
 
    } # ends big if #1 

    else { # if the filename not in .jpg, .jpeg. .gif, .pdf, report an error
    
	&filename_error($filename.'filename not in .jpg, .jpeg. .gif, .pdf');

    } # ends file suffix error 
} # ends else filename is not null and redirect isn't do-imageupdate.apg

print "</body>";
print "</html>";
system ("/bin/rm -f /tmp/upload_report") and die "can not /bin/rm -f /tmp/upload_report" ;
exit;
#-----------------END_MAIN---------------------------#
