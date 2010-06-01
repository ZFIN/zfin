#!/private/bin/perl
#
#
#  This script creates a file that ZFIN sends to Stanford. The file is tab
#  delimitted with 14 columns, each GO term/gene association on a seperate
#  line.
#  One tech people would get error report if any. One curator would get
#  gene_association.zfin file and gp2protein.zfin file in email attachment.

use MIME::Lite;


# ----------------- Send Error Report -------------
# Parameter
#   $    Error message
sub sendErrorReport ($) {
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi") || die "Cannot open mailprog!";
    print SENDMAIL "To: <!--|GO_EMAIL_ERR|-->\n";
    print SENDMAIL "Subject: GO file generation error\n";

    print SENDMAIL "$_[0]\n";
    close(SENDMAIL);
    exit;
}

#------------------ Send Result Files----------------
# No parameter
#
sub sendResults {

 #----- One mail send out the gene_association file----

  my $SUBJECT="Auto: gene_association.zfin.gz file";
  my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";
  my $ATTFILE ="gene_association.zfin.gz";

  # Create a new multipart message:
  $msg1 = new MIME::Lite
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg1
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg1->print(\*SENDMAIL);


 #----- Another mail send out problem files ----

  my $SUBJECT="Auto: gp2protein.zfin file";
  my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";
  my $ATTFILE = "gp2protein.zfin";

  # Create another new multipart message:
  $msg2 = new MIME::Lite
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg2
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg2->print(\*SENDMAIL);

  close(SENDMAIL);
}


#--------------- Main --------------------------------

#set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/GO";

sendErrorReport ("gofile.sql failed") if
    system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> gofile.sql");

sendErrorReport ("goparser.pl failed") if system ("./goparser.pl");

sendErrorReport ("check-gene-association.pl reports error") if
    system ("./check-gene-association.pl -d gene_association.zfin");

sendErrorReport ("/bin/rm -f gene_association.zfin.gz") if
    system ("/bin/rm -f gene_association.zfin.gz");

sendErrorReport ("/local/bin/gzip gene_association.zfin failed") if
    system ("/local/bin/gzip gene_association.zfin");

sendErrorReport ("gp2protein.pl failed") if
    system ("./gp2protein.pl");

sendResults();

exit;
