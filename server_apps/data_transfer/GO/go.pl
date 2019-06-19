#!/opt/zfin/bin/perl
#
#
#  This script creates a file that ZFIN sends to Stanford. The file is tab
#  delimited with 14 columns, each GO term/gene association on a separate
#  line.
#  One tech people would get error report if any. One curator would get
#  gene_association.zfin file and gp2protein.zfin file in email attachment.

use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

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

ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f gofile.sql");

ZFINPerlModules->doSystemCommand("./goparser.pl");

ZFINPerlModules->doSystemCommand("/bin/rm -f gene_association.zfin.gz");

ZFINPerlModules->doSystemCommand("/local/bin/gzip gene_association.zfin");

exit;
