#! /private/bin/perl -w 

# running from swirl checkout, on swirl environment.  Swirl is the 'curation' db.  Hoover is the db to run the jobs on.
# curation db is the db in the environment it's running
# $0 loadDb
# $1 path to loadDb code 
# $2 source file for loadDb code
# $3 absolute path of load to run
# /www_homes/swirl/server_apps/DB_maintenance/loadAndSwitch.pl hoovdb /research/zusers/staylor/hoover/ZFIN_WWW/ /research/zusers/staylor/hoover/ZFIN_WWW/commons/env/hoover.env

use DBI;

my $curationDb = "<!--|DB_NAME|-->" ;

my $loadDb = $ARGV[0];

my $pathToCodeCheckout = $ARGV[1];

my $sourceFile= $ARGV[2];

my $pathToUnload = "/research/zunloads/databases/<!--|DB_NAME|-->";

my $revokeTableLine;
my $grantTableLine;

my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
		       '',
		       '',
		       {AutoCommit => 0, RaiseError => 1}
		       )
          || errorExit("Failed while connecting to <!--|DB_NAME|--> ");


### MAIN #######

&disableUpdates();
&loadDb();
&runScripts();


exit 0;
#### END MAIN ##

sub disableUpdates() {
    my $flag = $dbh->prepare ("update zdb_flag set zflag_is_on = 't' where zflag_name = 'disable updates'");

    $flag->execute;
    $dbh->commit;

#    system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/disable_updates.pl") && die "disable_updates.pl failed";

#    chmod 0755, "/tmp/revoke_tables_permission.sql" ;

#    chmod 0755, "/tmp/grant_table_permission.sql" ;

#    open (REVOKE,"/tmp/revoke_tables_permission.sql") or die "can not open revoke_tables_permission.sql for reading";

#    while ($revokeTableLine = <REVOKE>) {
#	my $cur =$dbh->prepare("$revokeTableLine");
#	$cur->execute;
#    }

}


sub loadDb() {

#    my $connectString ;
#    $connectString = "DBI:Informix:$loadDb";

#    my $dbhLoadDb = DBI->connect($connectString,
#		       '',
#		       '',
#		       {AutoCommit => 0, RaiseError => 1}
#		       )
#	|| errorExit("Failed while connecting to $loadDb");


    system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/unload_production.sh") && die "unload_production.sh failed";

    system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/loadDb.sh $loadDb $pathToUnload $pathToCodeCheckout $sourceFile") && die "loadDb.sh failed.";

#    open (GRANT,"/tmp/grant_table_permission.sql") or die "can not open grant_table_permission.sql for reading";

#    while ($grantTableLine = <GRANT>) {
#	my $curGrant = $dbhLoadDb->prepare("$grantTableLine");
#	$curGrant->execute;
#    }

}

sub runScripts() {

    open (COMMANDFILE,"$pathToCodeCheckout/server_apps/DB_maintenance/loadAndSwitchJobs.txt") or die "can not open <!--|ROOT_PATH|-->/server_apps/DB_maintenance/loadAndSwitchJobs.txt for reading";

    my $command;

    while ($command = <COMMANDFILE>) {
	print $command."\n";
	system("$command") && die "$command load failed.";

    }
}

