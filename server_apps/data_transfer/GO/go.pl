#!/private/bin/perl
#
#
#  This script creates a file that ZFIN sends to Stanford. The file is tab
#  delimited with 14 columns, each GO term/gene association on a separate
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
    print SENDMAIL "Subject: Auto from $dbname GO file generation error\n";

    print SENDMAIL "$_[0]\n";
    close(SENDMAIL);
    exit;
}

#--------------- Main --------------------------------

#set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$dbname = "<!--|DB_NAME|-->";
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/GO";

sendErrorReport ("gofile.sql failed") if
    system ("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> gofile.sql");

sendErrorReport ("goparser.pl failed") if system ("./goparser.pl");

sendErrorReport ("/bin/rm -f gene_association.zfin.gz") if
    system ("/bin/rm -f gene_association.zfin.gz");

sendErrorReport ("/local/bin/gzip gene_association.zfin failed") if
    system ("/local/bin/gzip gene_association.zfin");

##sendErrorReport ("gp2protein.pl failed") if
  ##  system ("./gp2protein.pl");

##sendErrorReport ("/bin/rm -f gp2protein.zfin.gz") if
  ##  system ("/bin/rm -f gp2protein.zfin.gz");

##sendErrorReport ("/local/bin/gzip gp2protein.zfin failed") if
  ##  system ("/local/bin/gzip gp2protein.zfin");

exit;
