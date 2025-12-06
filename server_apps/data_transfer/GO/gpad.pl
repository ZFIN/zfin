#!/opt/zfin/bin/perl

use DBI;
use Try::Tiny;
use FindBin;
use lib "$FindBin::Bin/../../perl_lib/";
use ZFINPerlModules qw(assertEnvironment);
assertEnvironment('PGHOST', 'DB_NAME', 'ROOT_PATH');



my $rootpath = $ENV{'ROOT_PATH'};
my $dbname = $ENV{'DB_NAME'};
$username = "";
$password = "";

### open a handle on the db
my $dbhost = $ENV{'PGHOST'};
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

chdir "$rootpath/server_apps/data_transfer/GO";

system("gunzip gpad2.0.zfin.gz");

try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $dbname -a -f gpad2.0.sql");
} catch {
  warn "Failed at gpad2.0.sql - $_";
  exit -1;
};

open (UNLGPAD, ">gpad2.0.zfin") or die "Cannot open gpad.zfin";

print UNLGPAD "!gpa-version: 2.0\n";
print UNLGPAD "!date-generated: ".`/bin/date +%Y/%m/%d`;
print UNLGPAD "!generated-by: ZFIN (zfin.org) \n";
print UNLGPAD "! \n";

open (GPADDUMP, "gpad.zfin") or die ("gpad2.0 sql dump failed");
while ($gpadline = <GPADDUMP>) {
    print UNLGPAD "$gpadline"
}

close (UNLGPAD);

try {
  ZFINPerlModules->doSystemCommand("gzip gpad2.0.zfin");
} catch {
  warn "Failed at gzip gpad2.0.zfin - $_";
  exit -1;
};
