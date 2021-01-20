#!/opt/zfin/bin/perl

use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;
use Try::Tiny;

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/GO";

system("/local/bin/gunzip gpad2.0.zfin.gz");

try {
  ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f gpad2.0.sql");
} catch {
  warn "Failed at gpad2.0.sql - $_";
  exit -1;
};

open (UNLGPAD, ">gpad2.0.zfin") or die "Cannot open gpad.zfin";

print UNLGPAD "!gpa-version: 2.0\n";
print UNLGPAD "!Date: ".`/bin/date +%Y/%m/%d`;
print UNLGPAD "!From: ZFIN (zfin.org) \n";
print UNLGPAD "! \n";

open (GPADDUMP, "gpad.zfin") or die ("gpad2.0 sql dump failed");
while ($gpadline = <GPADDUMP>) {
    print UNLGPAD "$gpadline"
}

close (UNLGPAD);

try {
  ZFINPerlModules->doSystemCommand("/local/bin/gzip gpad2.0.zfin");
} catch {
  warn "Failed at gzip gpad2.0.zfin - $_";
  exit -1;
};

system("/local/bin/gunzip gpad2.0.zfin");