#! /private/bin/perl -w 


use DBI;
use MIME::Lite;
#require "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/disable_updates.pl";
# runs from the curation database environment

# curation db is the db in the environment it's running
# $0 loadDb
# $1 path to loadDb code 
# $2 source file for loadDb code
# $3 absolute path of load to run

my $curationDb = "<!--|DB_NAME|-->" ;
my $loadDb = $_[0];
my $pathToCodeCheckout = $_[1];
my $sourceFile= $_[2];
# my $scriptsToRunFile = $_[3];

# lock curation db
# unload curation db
# load load db
# run load
# unload load db
my $revokeTableLine;
my $grantTableLine;

my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
		       '',
		       '',
		       {AutoCommit => 0, RaiseError => 1}
		       )
  || errorExit("Failed while connecting to <!--|DB_NAME|--> ");


system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/disable_updates.pl") && die "disable_updates.pl failed";

chmod 0755, "/tmp/revoke_tables_permission.sql" ;

chmod 0755, "/tmp/grant_table_permission.sql" ;

open (REVOKE,"/tmp/revoke_tables_permission.sql") or die "can not open revoke_tables_permission.sql for reading";

while ($revokeTableLine = <REVOKE>) {
    $dbh->prepare("$revokeTableLine");
    $dbh->execute;
}

##system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/unload_production.sh") && die "unload_production.sh failed";

#system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/loadDb.sh $loadDb /research/zunloads/databases/zfindb/ $pathToCodeCheckout $sourceFile") && die "loadDb.sh failed";

open (GRANT,"/tmp/grant_table_permission.sql") or die "can not open grant_table_permission.sql for reading";

my $connectString ;
$connectString = "DBI:Informix:$loadDb";

my $dbhLoadDb = DBI->connect($connectString,
		       '',
		       '',
		       {AutoCommit => 0, RaiseError => 1}
		       )
  || errorExit("Failed while connecting to $loadDb");

while ($grantTableLine = <GRANT>) {
    $dbhLoadDb->prepare("$grantTableLine");
    $dbhLoadDb->execute;
}

open (COMMANDFILE,"$pathToCheckout/server_apps/DB_maintenance/loadAndSwitchJobs.txt") or die "can not open <!--|ROOT_PATH|-->/server_apps/DB_maintenance/loadAndSwitchJobs.txt for reading";

my $command;

while ($command = <COMMANDFILE>) {
    print $command."\n";
    system("$command") && die "$command load failed.";

}
exit 0;
