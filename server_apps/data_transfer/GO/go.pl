#!/opt/zfin/bin/perl
#
#
#  This script creates a file that ZFIN sends to Stanford. The file is tab
#  delimited with 14 columns, each GO term/gene association on a separate
#  line.
#  One tech people would get error report if any. One curator would get
#  gene_association.zfin file and gp2protein.zfin file in email attachment.

use DBI;
use Try::Tiny;
use FindBin;
use lib "$FindBin::Bin/../../perl_lib/";
use ZFINPerlModules qw(assertEnvironment);
assertEnvironment('ROOT_PATH', 'PGHOST', 'DB_NAME');

# set environment variables
my $dbname = $ENV{'DB_NAME'};
my $rootpath = $ENV{'ROOT_PATH'};
my $dbhost = $ENV{'PGHOST'};

system("/bin/rm -f ids.unl");

$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

chdir "$rootpath/server_apps/data_transfer/GO";

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
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $dbname -a -f gofile.sql");
} catch {
  warn "Failed at gofile.sql - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $dbname -a -f gpad2.0.sql");
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
  #Use awk to remove duplicate lines before gzipping
  ZFINPerlModules->doSystemCommand("awk '!seen[$0]++' gene_association.zfin | gzip > gene_association.zfin.gz");
  ZFINPerlModules->doSystemCommand("rm gene_association.zfin");
} catch {
  warn "Failed at gzip gene_association.zfin - $_";
  exit -1;
};
try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $dbname -a -f gofile2.sql");
} catch {
  warn "Failed at gofile2.sql - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $dbname -a -f gofile2_all.sql");
} catch {
  warn "Failed at gofile2_all.sql - $_";
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
  ZFINPerlModules->doSystemCommand("/bin/rm -f gene_association2.2_automated_only.zfin.gz");
} catch {
  warn "Failed at rm -f gene_association2.2_automated_only.zfin.gz - $_";
  exit -1;
};


try {
  ZFINPerlModules->doSystemCommand("awk '!seen[$0]++' gene_association2.2.zfin | gzip > gene_association2.2.zfin.gz");
  ZFINPerlModules->doSystemCommand("rm gene_association2.2.zfin");
} catch {
  warn "Failed at gzip gene_association2.2.zfin - $_";
  exit -1;
};

try {
  ZFINPerlModules->doSystemCommand("gzip gene_association2.2_automated_only.zfin");
} catch {
  warn "Failed at gzip gene_association2.2_automated_only.zfin - $_";
  exit -1;
};


exit;

