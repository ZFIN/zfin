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
use Try::Tiny;

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

try {
  ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f gofile.sql");
} catch {
  warn "Failed at gofile.sql - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f gpad2.0.sql");
} catch {
  warn "Failed at gpad2.0.sql - $_";
  exit -1;
};
try {
  ZFINPerlModules->doSystemCommand("./goparser.pl");
} catch {
  warn "Failed at goparser.pl - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("./gpad.pl");
} catch {
  warn "Failed at gpad.pl - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("/bin/rm -f gene_association.zfin.gz");
} catch {
  warn "Failed at rm -f gene_association.zfin.gz - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("/local/bin/gzip gene_association.zfin");
} catch {
  warn "Failed at gzip gene_association.zfin - $_";
  exit -1;
};
try {
  ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f gofile2.sql");
} catch {
  warn "Failed at gofile2.sql - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("./goparser2.2.pl");
} catch {
  warn "Failed at goparser2.2.pl - $_";
  exit -1;
};

# Note: this file gene_association2.2.zfin.gz ONLY serves the annotations that are not curated by ZFIN
# (either curated in ZFIN or in Nocuta). It should not be used for serving all GO annotations to ZFIN
# users via GAF2.2 format.  See: https://docs.google.com/document/d/1q5tXBJgbXSmrrN0ME12qLxQm7KrFhsu_wSkkj3fmBkA/edit

try {
  ZFINPerlModules->doSystemCommand("/bin/rm -f gene_association2.2.zfin.gz");
} catch {
  warn "Failed at rm -f gene_association2.2.zfin.gz - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("/local/bin/gzip gene_association2.2.zfin");
} catch {
  warn "Failed at gzip gene_association2.2.zfin - $_";
  exit -1;
};


exit;

