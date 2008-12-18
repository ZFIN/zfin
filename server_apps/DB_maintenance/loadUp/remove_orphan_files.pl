#!/private/bin/perl -w

# FILE: remove_orphan_files.pl
# PREFIX: remorph_

# DESCRIPTION: checks database for list of files that should
# exist in the file system.  Moves orphaned files from main loadUp 
# directories to bkup file.
# Calls rsync.pl to sync production and development
# Script will check whichever db is named in the sourced .tt file of the 
# calling environment.  When running daily on production, it will check 
# zfindb.  

# INPUT VARS: none
# OUTPUT VARS: none

# OUTPUT: 
# Email to informix telling of changes made, or email from cron
# showing the 'print' output from this script.
# If there are files in the filesystem that aren't listed in the 
# database, then these files will be moved to a backup directory.
# Finally calls rsync.pl which will sync development and production
# filesystems.

# use Mime::lite to send email

use MIME::Lite;

#-------------------VARIABLES------------------------#

# set the environmental variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

# variables for parsing lists of files from database

$remorph_new_line = "\n";
$remorph_jpg_extension = ".jpg";
$remorph_jpeg_extension = ".jpeg";
$remorph_gif_extension = ".gif";
$remorph_pdf_extension = ".pdf";
$remorph_count_images = 0;
$remorph_count_pdfs = 0;

#------------------END VARIABLES----------------------#

#------------------BEGIN SUBROUTINES------------------#

sub sendLoadReport ($) { # routine to send email to owner of db 

# . is concantenate
# $_[x] means to take from the array of values passed to the fxn, the 
# number indicated: $_[0] takes the first member.
  
  my $SUBJECT="File Sync: ".$_[0];
  my $MAILTO=$_[1];
  my $TXTFILE=$_[2];

  # Create a new multipart message:
  $msg1 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
      Type    => 'multipart/mixed';

  attach $msg1 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg1->print(\*SENDMAIL);
  close (SENDMAIL);
}

#---------------------END SUBROUTINES-------------------#

#---------------------BEGIN MAIN------------------------#

# remove old files generated the last time this script was executed.
# system calls return '0' if successful, and not-0 if they fail.
# so, we have to use the 'and' condition to determine if the system
# call had an error.

system("/bin/rm -f /tmp/filesystem_pdfs_not_in_database.unl") 
    and die "can not remove /tmp/filesystem_pdfs_not_in_database.unl";

system("/bin/rm -f /tmp/filesystem_images_not_in_database.unl") 
    and die "can not remove /tmp/filesystem_images_not_in_database.unl";

system("/bin/rm -f /tmp/orphan_file_report.txt") and die "can not rm orphan_file_report.txt";
system("/bin/rm -f /tmp/file_list_image") and die "can not rm file_list_image";
system("/bin/rm -f /tmp/file_list_pdf") and die "can not rm file_list_pdf";
system("/bin/rm -f /tmp/fl_image_modified") and die "can not rm fl_image_modified";
system("/bin/rm -f /tmp/fl_pdf_modified") and die "can not rm fl_pdf_modified";
system("/bin/rm -f /tmp/moved_image_files.unl") and die "can not rm moved_image_files.unl";
system("/bin/rm -f /tmp/moved_pdf_files.unl") and die "can not rm moved_pdf_files.un";
system("/bin/rm -f /tmp/orphan_image_files.unl") and die "can not rm orhpan_image_files.unl";
system("/bin/rm -f /tmp/orphan_pdf_files.unl") and die "can not rm orphan_pdf_files.unl";

# make a list of all the image files available for viewing in the filesystem
# loadup_full_path points to /research/zprod/loadUp (on production)
# image_load points to /research/zcentral/loadUp/ImageLoadUp (on development)

system ("/bin/ls -1 <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|--> > /tmp/file_list_image") 
    and die "can not ls -1 /tmp/file_list_image";

system ("/bin/chmod 644 /tmp/file_list_image") and die "can not chmod on /tmp/file_list_image";

# make a list of all the pdf files available for viewing in the fielsystem
# loadup_full_path points to /research/zprod/loadUp (on production)
# pdf_load points to /research/zcentral/loadUp/PDFLoadUp (on development)

system ("/bin/ls -1 <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|--> > /tmp/file_list_pdf") 
    and die "can not ls -1 /tmp/file_list_pdf";

system ("/bin/chmod 644 /tmp/file_list_pdf") and die "can not chmod /tmp/file_list_pdf";

# file_list_image is the file of all image files available on filesystem

open (FILE_LIST_IMAGE, "< /tmp/file_list_image")
    or die "Cannot open the file_list_image file\n";

# fl_image_modified is the file containing all image files with 
# a pipe seperator added.

open FL_IMAGE_MODIFIED, "> /tmp/fl_image_modified"
    or die "Cannot open the fl_image_modified file\n";

# same comments as above, except with PDFs.

open (FILE_LIST_PDF, "< /tmp/file_list_pdf")
    or die "Cannot open the file_list_pdf file\n";

open FL_PDF_MODIFIED, "> /tmp/fl_pdf_modified" 
    or die "Cannot open the fl_pdf_modified file\n";

print "compiling modified file lists\n";

#---------------COMPILING MODIFIED FILE LISTS---------------#

# read file_list_image into an array and do line-by-line filename processing

@remorph_imageLines = <FILE_LIST_IMAGE>;

# for each filename, replace * if found with nothing 
# and add a pipe seperator to make a load file for informix
# each line will be processed and printed (with a newline appended to end).

foreach $remorph_image_line (@remorph_imageLines) {
    $remorph_image_line =~ s/$remorph_new_line/|/g ;

    # print filenames to a new file (used to load into db), if they aren't 
    # thumbnail or annotation files, and if they aren't the 'bkup' or 'medium' directories.
    
    # the medium directory was created to hold mid-sized images.  They should have the same
    # zdb-ids as their full sized counter-parts.  We create the list of orphans from the 
    # main image file, then apply that list to the medium directory as well. This is the same
    # behavior that is used for thumbnail and annotation images, but in another directory.
    
    # we'll later parse out just the ZDB-id for the files that are orphaned
    # and move all files named with this ZDB-id (including _annot and _thumb
    # files into a backup directory).  we then move to the medium directory and then move
    # the orphaned images in that one to the medium/bkup direcotry.

    # Thus, we don't need to process the _thumb, /medium, or _annot files individually.

    if (!($remorph_image_line =~ /\_/) && !($remorph_image_line =~ /bkup/) && !($remorph_image_line =~ /medium/)){	
	$remorph_file_to_print = $remorph_image_line.$remorph_new_line ;
	print FL_IMAGE_MODIFIED $remorph_file_to_print;
    }
}

# same comments except for pdfs.  We shouldn't have to worry about
# thumbnail or annot files in the pdf directory, but we are paranoid.

@remorph_pdfLines = <FILE_LIST_PDF>;

foreach $remorph_pdf_line (@remorph_pdfLines) {
    $remorph_pdf_line =~ s/$remorph_new_line/|/g ;

    # if pdf_line doesn't have an underscore and it doesn't have the string
    # 'bkup' in it, then go ahead and add it to the file list.
    # bkup/ is a directory, and _ means its a thumbnail or annotation--or
    # in the case of pdfs, a mistake.

    if (!($remorph_pdf_line =~ /\_/) && !($remorph_pdf_line =~ /bkup/)){
	print FL_PDF_MODIFIED "$remorph_pdf_line\n";
    }
}

#---------------------LOADING FILES TO DB---------------#

print "loading...\n";

# load the files created by the above steps into the database using 
# the load_upload_file_list.sql script

system ("/private/apps/Informix/informix/bin/dbaccess <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/loadUp/load_upload_file_list.sql >out 2> /tmp/orphan_file_report.txt");

system ("/bin/chmod 644 /tmp/filesystem_images_not_in_database.unl") 
    and die "Can not chmod the filesystem_images_not_in_database.unl";

system ("/bin/chmod 644 /tmp/filesystem_pdfs_not_in_database.unl") 
    and die "Can not chmod the filesystem_images_not_in_database.unl";

close FL_PDF_MODIFIED;
close FL_IMAGE_MODIFIED;
close FILE_LIST_PDF;
close FILE_LIST_IMAGE;

system ("/bin/chmod 644 /tmp/fl_image_modified") and die "can not chmod fl_image_modified";

system ("/bin/chmod 644 /tmp/fl_pdf_modified") and die "can not chmod fl_pdf_modified";

open (ORPHAN_IMAGE_FILES, "< /tmp/orphan_image_files.unl") 
    or die "Cannot open the orphan_image_file.unl file\n";

open (ORPHAN_PDF_FILES, "< /tmp/orphan_pdf_files.unl") 
    or die "Cannot open the orphan_image_file.unl file\n";

open MOVED_IMAGE_FILES, "> /tmp/moved_image_files.unl"
    or die "Cannot open the moved_image_files.unl file\n";

open MOVED_PDF_FILES, "> /tmp/moved_pdf_files.unl"
    or die "Cannot open the moved_pdf_files.unl file\n";

print "moving orphan image files... \n";

#--------------MOVING ORPHAN FILES----------------#

# chop off the pipe-separator from the db unload files
# replace newlines and file extensions with nothing so we can move all
# files with the orphaned ZDB-id prefix (this is important for images as
# we'll have annotations and thumbnails that need moving when their parent
# image is moved.

@remorph_orphanImageLines = <ORPHAN_IMAGE_FILES>;

foreach $remorph_orphan_image_line (@remorph_orphanImageLines) {
    # replace pipe with nothing
    $remorph_orphan_image_line =~ s/\|//g ;

    # replace new line with nothing
    $remorph_orphan_image_line =~ s/$remorph_new_line//g ;

    # replace file extensions with nothing--should be left with just a
    # ZDB-id
    $remorph_orphan_image_line =~ s/$remorph_jpg_extension//i;
    $remorph_orphan_image_line =~ s/$remorph_jpeg_extension//i;
    $remorph_orphan_image_line =~ s/$remorph_gif_extension//i;

    # now $remorph_orphan_image_line is like a zdb_id, so we can move all
    # files beginning with that orphan_zdb_id to the bkup directory
    

    system ("/bin/mv <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/$remorph_orphan_image_line.* <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/bkup/" )
and die "can not move image file";

    # medium images are in a separate directory, so do the same thing for those files as we did for files
    # in the imageLoadUp directory itself.  medium/ has a bkup dir as well.

    system ("/bin/mv <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/medium/$remorph_orphan_image_line.* <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/medium/bkup/" )
and die "can not move medium image file";

    $remorph_dash_star = "_*";
    $remorph_image_ext = $remorph_orphan_image_line.$remorph_dash_star;

    system ("/bin/mv <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/$remorph_image_ext <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/bkup/" )
and die "can not move thumb image file";

    # print out the moved files to a report 

    print MOVED_IMAGE_FILES "$remorph_orphan_image_line\n";
}

close MOVED_IMAGE_FILES;

system ("/bin/chmod 644 /tmp/moved_image_files.unl") and die "can not chmod moved_image_files";

# do the same thing with pdf files

print "moving pdf files...\n";

@remorph_pdfLines = <ORPHAN_PDF_FILES>;
foreach $remorph_orphan_pdf_line (@remorph_pdfLines) {
    
    $remorph_orphan_pdf_line =~ s/\|//g ;
    $remorph_orphan_pdf_line =~ s/$remorph_new_line//g ;
    $remorph_orphan_pdf_line =~ s/$remorph_pdf_extension//i;

    system ("/bin/mv <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/$remorph_orphan_pdf_line.* <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/bkup/") and die "can not mv pdfs";    

     print MOVED_PDF_FILES "orphan_pdf_line\n";
}

close MOVED_PDF_FILES;

system ("/bin/chmod 644 /tmp/moved_pdf_files.unl") and die "can not chmod moved_pdf_files";

print "closing files...\n" ;

#----------------CLOSE ALL FILES----------------------#

close ORPHAN_IMAGE_FILES ;

close ORPHAN_PDF_FILES;

# count the number of lines read from the file, if > 0, send email, otherwise
# print that there are no new images/pdfs in the cron output email.

#----------------COUNT LINES FROM THE FILES-----------#

#---------------EMAIL DATABSE IMAGES/PDFS WITH NO FILESYSTEM files-----#

open(NO_FS_IMAGE, "< /tmp/filesystem_images_not_in_database.unl") or die "can't open $remorph_file";

$remorph_count_images++ while <NO_FS_IMAGE>;

# count_images now holds the number of lines read, want to do count
# so that email does not get sent unless there is actual output

  if ($remorph_count_images < 1) {
      print "No new db images without files count: $remorph_count_images \n" ;
  }
else {
      &sendLoadReport("No Filesystem Image",
                      "<!--|DB_OWNER|-->\@cs.uoregon.edu", 
		      "/tmp/filesystem_images_not_in_database.unl") ;
  }

close NO_FS_IMAGE;

$remorph_count_images = 0;


open(NO_FS_PDF, "< /tmp/filesystem_pdfs_not_in_database.unl") or die "can't open $remorph_file";

$remorph_count_pdfs++ while <NO_FS_PDF>;

# count_images now holds the number of lines read, want to do count
# so that email does not get sent unless there is actual output

  if ($remorph_count_pdfs < 1) {
      print "No new db pdfs without files count: $remorph_count_images \n" ;
  }
else {
      &sendLoadReport("No Filesystem PDF","<!--|DB_OWNER|-->\@cs.uoregon.edu", 
		      "/tmp/filesystem_pdfs_not_in_database.unl") ;
  }

close NO_FS_PDF;

$remorph_count_pdfs = 0;

#----------------EMAIL IMAGES/PDFS NOT IN DATABASE--------------#

open(ORPHAN_IMAGE_FILES, "< /tmp/orphan_image_files.unl") or die "can't open $remorph_file";

$remorph_count_images++ while <ORPHAN_IMAGE_FILES>;

# count_images now holds the number of lines read, want to do count
# so that email does not get sent unless there is actual output

  if ($remorph_count_images < 1) {
      print "No new orphan images count: $remorph_count_images \n" ;
  }
else {
      &sendLoadReport("Orphan Images","<!--|DB_OWNER|-->\@cs.uoregon.edu", 
		      "/tmp/orphan_image_files.unl") ;
  }

close ORPHAN_IMAGE_FILES;

print "count orphan pdfs \n";

open(ORPHAN_PDF_FILES, "< /tmp/orphan_pdf_files.unl") or die "can't open $remorph_file";

$remorph_count_pdfs++ while <ORPHAN_PDF_FILES>;

# count_pdfs now holds the number of lines read from the orphan pdf file

  if ($remorph_count_pdfs < 1) {
      print "No new orphan pdfs  count: $remorph_count_pdfs \n" ;
  }
  else {
    &sendLoadReport("Orphan PDFs","<!--|DB_OWNER|-->\@cs.uoregon.edu", 
		"/tmp/orphan_pdf_files.unl") ;
  }

close ORPHAN_PDF_FILES;

#----------------------END MAIN------------------------#

#----------------------START RSYNC---------------------#

print "starting rsync...\n" ;

# call rsync to sync production filesystem (after the orphan files have been
# moved) to the development filesystem.  This means removing files that aren't
# on development and are on production, and adding files to development
# that are on production but not on development.  Rsync.pl skips the bkup 
# directory.

system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/loadUp/rsync.pl") and die "can not execute rsync.pl";

exit;
