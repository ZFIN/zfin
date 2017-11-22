#!/private/bin/perl
#
#
#  This script creates a file that ZFIN sends to Stanford. The file is tab
#  delimited with 14 columns, each GO term/gene association on a separate
#  line.
#  One tech people would get error report if any. One curator would get
#  gene_association.zfin file and gp2protein.zfin file in email attachment.

use MIME::Lite;
use DBI;

# ----------------- Send Error Report -------------
# Parameter
#   $    Error message
sub sendErrorReport ($) {
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi") || die "Cannot open mailprog!";
    print SENDMAIL 'To: <!--|GO_EMAIL_ERR|-->\n';
    print SENDMAIL "Subject: Auto from $dbname GO file generation error\n";

    print SENDMAIL "$_[0]\n";
    close(SENDMAIL);
    exit;
}

#--------------- Main --------------------------------

#set environment variables

system("/bin/rm -f ids.unl");

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/GO";

$cur = $dbh->prepare("select dalias_data_zdb_id, dalias_alias from data_alias where dalias_data_zdb_id like 'ZDB-GENE%' order by dalias_alias;");
$cur->execute();
$cur->bind_columns(\$id1, \$id2);

%identifiers = ();        
while ($cur->fetch()) {   
  if (!exists($identifiers{$id1})) {
     $identifiers{$id1} = $id2;
  } else {
     $identifiers{$id1} = $identifiers{$id1} . "," . $id2;
  }
}

$cur->finish();
$dbh->disconnect();

open (IDS, ">ids.unl") || die "Cannot open ids.unl : $!\n";
foreach $id (keys %identifiers) {
  $v = $identifiers{$id};
  print IDS "$id|$v\n";
}
close IDS;

sendErrorReport ("gofile.sql failed") if
    system ("psql -d <!--|DB_NAME|--> -a -f gofile_PG.sql");

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
