#!/private/bin/perl -w

# FILE: remove_orphan_files.pl
# PREFIX: made decision to not use prefix, as it makes this page
# more confusing.

# DESCRIPTION: checks database for list of files that should
# exist in the file system.  Moves orphaned files from main loadUp 
# directories to bkup file.
# calls rsync.pl to sync production and development

# INPUT VARS: none
# OUTPUT VARS: email to informix telling of changes made.

# use Mime::lite to send email

use MIME::Lite;

# set the environmental variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

# remove old files

system("rm -f /tmp/orphan_file_report.txt") ;

system("rm -f /tmp/file_list_image") ;

system("rm -f /tmp/file_list_pdf") ;

system("rm -f /tmp/fl_image_modified") ;

system("rm -f /tmp/fl_pdf_modified") ;

system("rm -f /tmp/moved_image_files.unl") ;

system("rm -f /tmp/moved_pdf_files.unl") ;

system("rm -f /tmp/orphan_image_files.unl") ;

system("rm -f /tmp/orphan_pdf_files.unl") ;


sub sendLoadReport ($) { # routine to send email to owner of db 

#. is concantenate
#$_[x] means to take from the array of values passed to the fxn, the 
#number indicated: $_[0] takes the first member.
  
  my $SUBJECT="File Sync:".$_[0];
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

# variables for parsing lists of files from database

$new_line = "\n";
$jpg_extension = ".jpg";
$jpeg_extension = ".jpeg";
$gif_extension = ".gif";
$pdf_extension = ".pdf";

# make a list of all the image files available for viewing in the filesystem
# loadup_full_path points to /research/zprod/loadUp (on production)
# image_load points to /research/zcentral/loadUp/ImageLoadUp (on production)

system ("ls -1 <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|--> > /tmp/file_list_image");

system ("/bin/chmod 755 /tmp/file_list_image");

# make a list of all the pdf files available for viewing in the fielsystem
# loadup_full_path points to /research/zprod/loadUp (on production)
# pdf_load points to /research/zcentral/loadUp/PDFLoadUp (on production)

system ("ls -1 <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|--> > /tmp/file_list_pdf");
system ("/bin/chmod 755 /tmp/file_list_pdf");

# file_list_image is the file of all image files available on filesystem

open (FILE_LIST_IMAGE, "< /tmp/file_list_image")
    or die "Cannot open the file_list_image file:$!\n";

# fl_image_modified is the file containing all image files with their 
# executable * removed and a pipe seperator added.

open FL_IMAGE_MODIFIED, "> /tmp/fl_image_modified"
    or die "Cannot open the fl_image_modified file:$!\n";


# same comments as above, except with PDFs.

open (FILE_LIST_PDF, "< /tmp/file_list_pdf")
    or die "Cannot open the file_list_pdf file:$!\n";

open FL_PDF_MODIFIED, "> /tmp/fl_pdf_modified" 
    or die "Cannot open the fl_pdf_modified file:$!\n";

print "compiling modified file lists\n";

# read file_list_image into an array and do line-by-line filename processing

@imageLines = <FILE_LIST_IMAGE>;

# for each filename, replace * with | to make a load file for informix

foreach $image_line (@imageLines) {
    $image_line =~ s/\*/\|/g ;
    $image_line =~ s/$new_line//g ;

    # print filenames to a new file (used to load into db), if they aren't 
    # thumbnail or annotation files, and if they aren't the 'bkup' directory

    if (!($image_line =~ /\_/) && !($image_line =~ /bkup/)){	
	$file_to_print = $image_line.$new_line ;
	print FL_IMAGE_MODIFIED $file_to_print;
    }
}

system ("/bin/chmod 755 /tmp/fl_image_modified");

# same comments except for pdfs, and we don't have to worry about
# thumbnail or annot files in the pdf directory.

@pdfLines = <FILE_LIST_PDF>;

foreach $pdf_line (@pdfLines) {
    $pdf_line =~ s/\*/\|/g ;
    $pdf_line =~ s/\n//g ;

    # if pdf_line doesn't have an underscore and it doesn't have the string
    # 'bkup' in it, then go ahead and add it to the file list.
    # bkup/ is a directory, and _ means its a thumbnail or annotation

    if (!($pdf_line =~ /\_/) && !($pdf_line =~ /bkup/)){
	print FL_PDF_MODIFIED "$pdf_line\n";
    }
}

system ("/bin/chmod 755 /tmp/fl_pdf_modified");

print "loading...\n";

# load the files created by the above steps into the database using 
# the load_files.sql script

system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> load_files.sql >out 2> /tmp/orphan_file_report.txt");

close FL_PDF_MODIFIED;
close FL_IMAGE_MODIFIED;
close FILE_LIST_PDF;
close FILE_LIST_IMAGE;

open (ORPHAN_IMAGE_FILES, "< /tmp/orphan_image_files.unl") 
    or die "Cannot open the orphan_image_file.unl file:$!\n";

open (ORPHAN_PDF_FILES, "< /tmp/orphan_pdf_files.unl") 
    or die "Cannot open the orphan_image_file.unl file:$!\n";

open MOVED_IMAGE_FILES, "> /tmp/moved_image_files.unl"
    or die "Cannot open the moved_image_files.unl file:$!\n";

open MOVED_PDF_FILES, "> /tmp/moved_pdf_files.unl"
    or die "Cannot open the moved_pdf_files.unl file:$!\n";


print "moving orphan files.\n";

# chop off the pipe-separator from the db unload files
# replace newlines and file extensions with nothing

@orphanImageLines = <ORPHAN_IMAGE_FILES>;

foreach $orphan_image_line (@orphanImageLines) {
    $orphan_image_line =~ s/\|//g ;
    $orphan_image_line =~ s/$new_line//g ;
    $orphan_image_line =~ s/$jpg_extension//i;
    $orphan_image_line =~ s/$jpeg_extension//i;
    $orphan_image_line =~ s/$gif_extension//i;

    # now $orphan_image_line is like a zdb_id, so we can move all
    # files beginning with that orphan_zdb_id to the bkup directory

    system ("/bin/mv <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/$orphan_image_line* <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/bkup/" );

    # print out the moved files to a report 

    print MOVED_IMAGE_FILES "$orphan_image_line\n";
}
system ("/bin/chmod 755 /tmp/moved_image_files");

# do the same thing with pdf files

@pdfLines = <ORPHAN_PDF_FILES>;
foreach $orphan_pdf_line (@pdfLines) {
    
    $orphan_pdf_line =~ s/\|//g ;
    $orphan_pdf_line =~ s/\n//g ;
    $orphan_pdf_line =~ s/$pdf_extension//i;

    system ("/bin/mv <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/$orphan_pdf_line* <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/bkup/");    
     print MOVED_PDF_FILES "orphan_pdf_line\n";
  
}

system ("/bin/chmod 755 /tmp/moved_pdf_files");

print "closing files...\n" ;

close ORPHAN_IMAGE_FILES ;

close ORPHAN_PDF_FILES;

close MOVED_IMAGE_FILES;

close MOVED_PDF_FILES;

&sendLoadReport("Image Files Moved","<!--|DB_OWNER|-->\@cs.uoregon.edu", 
		"/tmp/orphan_image_files.unl") ;

&sendLoadReport("PDF Files Moved","<!--|DB_OWNER|-->\@cs.uoregon.edu", 
		"/tmp/orphan_pdf_files.unl") ;

print "starting rsync...\n" ;

# call rsync to sync production filesystem (after the orphan files have been
# moved) to the development filesystem.  This means removing files that aren't
# on development and are on production, and adding files to development
# that are on production but not on development.  Rsync.pl skips the bkup 
# directory.

system("rsync.pl") ;

exit;
