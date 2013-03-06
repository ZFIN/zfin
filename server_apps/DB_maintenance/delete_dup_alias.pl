#!/private/bin/perl

# FILE: delete_dup_alias.pl
# for FB case 7609
# delete the marker alias which are the same as marker symbol, but keep the history
# report sent to Xiang and Informix

use MIME::Lite;


#=======================================================
#
#   Main
#


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/";

#remove old files
 
system("rm -f report_deleted_redundant_alias");
system("rm -f deleted_redundant_alias");
system("rm -f log1");
system("rm -f log2");

$dir = "<!--|ROOT_PATH|-->";

@dirPieces = split(/www_homes/,$dir);

$dbname = $dirPieces[1];
$dbname =~ s/\///;

print "$dbname\n\n";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> delete_dup_alias.sql >log1 2> log2");

system("cat deleted_redundant_alias log1 log2 > report_deleted_redundant_alias");

sendLoadOutput("$dbname") if (-e "report_deleted_redundant_alias");

exit;


#------------------ Send report ----------------
# 
#
sub sendLoadOutput($) {

  my $SUBJECT="Auto from " . $_[0] . " : deleted marker alias which are the same as marker symbol ";
  my $MAILTO="<!--|SWISSPROT_EMAIL_ERR|-->";
  my $TXTFILE="./report_deleted_redundant_alias";
 
  # Create a new multipart message:
  my $msg = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg->print(\*SENDMAIL);

  close(SENDMAIL);
}

