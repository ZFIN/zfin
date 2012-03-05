#! /private/bin/perl -w

use DBI;
use MIME::Lite;

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$jobTitle = "Cron Job: runWarehouse: ";
$globalResultFile = "/tmp/warehouse_regeneration"."<!--|DB_NAME|-->";
$martName = "fish mart";
@whoIsZfin = `<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/who_is.sh`;
$whoIsZfinDb = $whoIsZfin[0];
print "zfin is: $whoIsZfinDb";
@whoIsNotZfin = `<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/who_is_not.sh`;
$whoIsNotZfinDb=$whoIsNotZfin[0];
print "zfin is not: $whoIsNotZfinDb";

for ($whoIsNotZfinDb) {
    s/^\s+//;
    s/\s+$//;
}
for ($whoIsZfinDb) {
    s/^\s+//;
    s/\s+$//;
}

system("/bin/rm -rf *.txt") and die "can not rm txt" ;

$dbhZfin = DBI->connect("DBI:Informix:$whoIsZfinDb",
		       '',
		       '',
		       {AutoCommit => 1, RaiseError => 1}
		       )
          or &logError ("Failed while connecting to $whoIsZfin\n");

$dbhNotZfin = DBI->connect("DBI:Informix:$whoIsNotZfinDb",
		       '',
		       '',
		       {AutoCommit => 1, RaiseError => 1}
		       )
          or &logError ("Failed while connecting to $whoIsNotZfin\n");

### MAIN ###
`/bin/rm -rf $globalResultFile`;
&cronStop();
&disableUpdates();
&loadDb();
&runWarehouse();
&integrityChecks("fish mart");
&updateWarehouseTracking("fish mart");
&enableUpdates();
&swapZfin();
&cronStart();
&success();

sub success() {
    open RESULT, ">$globalResultFile" or die "Cannot open the file to write error.";
    print RESULT "SUCCESS: warehouse has been regenerated.";
    &sendMail("SUCCESS","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse has been regenerated and zfin has switched","$globalResultFile");
    close(RESULT);
    exit 0;
}

sub cronStart($){
    # hardcode cron starting for kinetix and watson and crick only.  This needs attention to be more
    # generic.
    if (("<!--|MACHINE_NAME|-->" eq "kinetix") && ($whoIsZfinDb eq "watsondb")){
	chdir("/research/zprod/users/watson/ZFIN_WWW/server_apps/cron") or &logError("can't chdir to server_apps/cron");
	system("/local/bin/gmake start"); 
	print "last return from gmake start is: $?\n";
    }
     
    if (("<!--|MACHINE_NAME|-->" eq "kinetix") && ($whoIsZfinDb eq "crickdb")){
	chdir("/research/zprod/users/crick/ZFIN_WWW/server_apps/cron") or &logError("can't chdir to server_apps/cron");	
	system("/local/bin/gmake start");
	print "last return from gmake start is: $?\n";
    }
    print "cronStart completed.\n";
}

sub cronStop($) {
    if ("<!--|MACHINE_NAME|-->" eq "kinetix"){
	chdir("<!--|SOURCEROOT|-->/server_apps/cron") or &logError("can't chdir to server_apps/cron");
	system("/local/bin/gmake stop"); 
	print "last return from gmake stop is: $?\n";
    }
}


sub getEnvFileName {
    my $env = "noDbNameFound!";

    #print $whoIsNotZfinDb;
    my $sthEn = $dbhNotZfin->prepare("select denm_env_file_name from database_env_name_matrix where denm_db_name = '$whoIsNotZfinDb';");
    $sthEn->execute() or &logError("could not execute getEnvFileName SQL statement");
    $sthEn->bind_columns(\$env);
    $sthEn->dump_results();
    for ($env) {
        s/^\s+//;
        s/\s+$//;
    }
    print $env."\n";
    return $env;
}

sub swapZfin(){
    chdir("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/") or &logError("can't chdir to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/");
    system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/switch.sh") && &logError("switch zfin to new warehouse failed.");
    print "swap sites execution complete.\n"
}
sub disableUpdates() {
    my $flag = $dbhZfin->prepare ("update zdb_flag set zflag_is_on = 't' where zflag_name = 'disable updates'");
    $flag->execute;
    system("/private/ZfinLinks/Commons/bin/stoptomcat.pl $whoIsNotZfinDb");
    print "$? \n";
    if($? ne 0){
	&logError("stoptomcat.pl failed");
    }
}

sub enableUpdates() {
    my $flag2 = $dbhNotZfin->prepare ("update zdb_flag set zflag_is_on = 'f' where zflag_name = 'disable updates'");
    $flag2->execute;
    print "updates enabled\n";
    print "restarting tomcat\n";
    chdir("/private/ZfinLinks/Commons/bin") or &logError("can't chdir to /private/ZfinLinks/Commons/bin");
    system("/private/ZfinLinks/Commons/bin/starttomcat.pl $whoIsNotZfinDb") ;
    if($? ne 0){
	&logError("starttomcat.pl on $whoIsNotZfinDb failed");
	die;
    }
}

sub loadDb() {
    # get env name from db table.
    my $envName;
    my $dirName;
    $envName = &getEnvFileName();
    chdir("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/") or &logError("can't chdir to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/");
    $dirName= `getUnloadDir.sh`;
    for ($dirName) {
        s/^\s+//;
        s/\s+$//;
    }
    system("/private/ZfinLinks/Commons/bin/unloaddb.pl $whoIsZfinDb <!--|WAREHOUSE_DUMP_DIR|-->/$dirName/");
    print "return from unloaddb.pl is: $?\n";
    $dbhNotZfin->disconnect;
    print "disconnect from notZfin\n";
    system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/loadDb.sh $whoIsNotZfinDb <!--|WAREHOUSE_DUMP_DIR|-->/ <!--|SOURCEROOT|--> <!--|SOURCEROOT|-->/commons/env/$envName") ;
    if ($? ne 0){
	&logError("loadDb.sh failed");
    }
    $dbhNotZfin = DBI->connect("DBI:Informix:$whoIsNotZfinDb",
		       '',
		       '',
		       {AutoCommit => 1, RaiseError => 1}
		       )
          or &logError("Failed while connecting to $whoIsNotZfin\n");
    #cp unload to zfindb location.
}
sub runWarehouse() {
    chdir ("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/") or &logError("can't chdir to server_apps/DB_maintenance/warehouse/fishMart/");
    system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMart.sh $whoIsNotZfinDb > ../warehouseSqlReport.txt");
    print $?;
    if ($? ne 0){
	&logError("runFishMart.sh &logErrord");
    }
    chdir ("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/") or &logError("can't chdir to DB_maintenance/warehouse/");
}

sub execSql {

  my $sql = shift;
  my $nRecords = 0;
 
  my $sthE = $dbhNotZfin->prepare($sql) or &logError("Prepare fails");
  $sthE -> execute() or &logError("could not execute");
  my @row = $sthE->fetchrow_array();
  $nRecords = $row[0];
  return ($nRecords);
}

sub updateWarehouseReleaseTracking {
    my $sql = shift;
    my $sthU = $dbhNotZfin->prepare($sql) or &logError("Prepare fails on update release tracking");
    $sthU -> execute() or &logError("could not execute update release tracking");
}

sub integrityChecks($){
    my $wrt_fail ;
    $wrt_fail = 'update warehouse_run_tracking set (wrt_mart_load_successful, wrt_last_loaded_date) = ("f",current year to second) where wrt_mart_name = "$martName"';

    my $sql = 'select count(*) as counter from fish_annotation_search';
    my $fishAnnotationSearchCount = execSql ($sql, undef);

    if ($fishAnnotationSearchCount < 16000) {
	open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
	print RESULT "fish_annotation_search table does not have enough records.< 16000";
	close(RESULT);
	&sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on NotZfinDb: FAIL","$globalResultFile");
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&logError("fish_annotation_search table does not have enough records.< 16000");	
    }
    print "fish_annotation_search has enough records.\n";
    $sql = 'select count(*) as counter from fish_annotation_search where fas_genotype_group is null and fas_genox_group is null';
    my $genotypeGenoxAvailable = execSql ($sql, undef);

    if ($genotypeGenoxAvailable >0) {
	open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
	print RESULT "$genotypeGenoxAvailable records are missing both a genotype and a genox_group";
	close(RESULT);
	&sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on NotZfinDb: FAIL","$globalResultFile");
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&logError("$genotypeGenoxAvailable records are missing both a genotype and a genox_group");
    }
    print "fish_annotation_Search geno/genox ok.\n";
    $sql = 'select count(*) as counter from fish_annotation_search where fas_all is null';
    my $checkFasAllNotNull = execSql($sql, undef);

    if ($checkFasAllNotNull >0) {
	open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
	print RESULT "$checkFasAllNotNull records have null fas_all values";
	close(RESULT);
	&sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on NotZfinDb: FAIL","$globalResultFile");
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&logError("$checkFasAllNotNull records have null fas_all values");
    }
    print "fas_all populated ok.\n";
    $sql = 'select count(*) as counter from gene_feature_result_view where gfrv_construct_zdb_id is null and gfrv_construct_abbrev_order is not null';
    my $constructAbbrevOrderMismatchCount = execSql($sql, undef);

    if ($constructAbbrevOrderMismatchCount >0) {
	open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
	print RESULT "$constructAbbrevOrderMismatchCount records have mismatched construct abbrev/order mismatches in gene_feature_result_view";
	close(RESULT);
	&sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on NotZfinDb: FAIL","$globalResultFile");
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&logError("$constructAbbrevOrderMismatchCount records have mismatched construct abbrev/order mismatches in gene_feature_result_view");
    }
    print "construct abbrevs and orders in gene_feature_result_view ok.\n";
    $sql = 'select count(*) as counter from fish_annotation_search where fas_fish_significance < 999999';
    my $fishSignificanceCount = execSql($sql, undef);

    if ($fishSignificanceCount < 10) {
	open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
	print RESULT "only $fishSignificanceCount records have significance < 999999";
	close(RESULT);
	&sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on NotZfinDb: FAIL","$globalResultFile");
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&logError("only $fishSignificanceCount records have significance < 999999");
    }
    print "fish_significance in fish_annotation_search ok.\n";
    $sql = 'select count(*) as counter from gene_feature_result_view';
    my $geneFeatureResultViewCount = execSql($sql, undef);

    if ($geneFeatureResultViewCount < 20312) {
	open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
	print RESULT "gene_feature_result_view table does not have enough records. < 20312";
	close(RESULT);
	&sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on NotZfinDb: FAIL","$globalResultFile");
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&logError("gene_feature_result_view table does not have enough records. < 20312");
    }
   print "gene_feature_result_view has enough records.\n";
    $sql = 'select count(*) as counter from figure_term_fish_search';
    my $figureTermFishSearchCount = execSql($sql, undef);

    if ($figureTermFishSearchCount < 13997) {
	open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
	print RESULT "figure_term_fish_search table does not have enough records.< 13997";
	close(RESULT);
	&sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on NotZfinDb: FAIL","$globalResultFile");
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&logError("figure_term_fish_search table does not have enough records.< 13997");
    }
   print "figure_term_fish_search has enough records ok.\n";
    $sql = 'select count(*) as counter from fish_annotation_search where bts_contains(fas_all, "shha")';

    my $btsContainssshhaCount = execSql($sql, undef);
    if ($btsContainssshhaCount < 81){
	open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
	print RESULT "bts index on fish_annotation_search is inacurate: shha < 81";
	close(RESULT);
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on NotZfinDb: FAIL","$globalResultFile");
	&logError("bts index on fish_annotation_search is inacurate: shha < 81");
    }
   print "fas_all bts_index ok.\n";
}

sub updateWarehouseTracking($){

    my $sqlUpdate = 'update warehouse_run_tracking set (wrt_mart_load_successful, wrt_last_loaded_date) = ("t",current year to second) where wrt_mart_name = "$martName"';
    open RESULT, ">$globalResultFile" or &logError("Cannot open the file to write check result.");
    print RESULT "$martName updated successfully.";
    close(RESULT);
    updateWarehouseReleaseTracking($sqlUpdate, undef);
    &sendMail("SUCCESS","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse has been regenerated and zfin has switched","$globalResultFile");
}

sub sendMail($) {

    my $SUBJECT=$_[0] . ": " . $jobTitle .": " .$_[2];
    my $MAILTO=$_[1];
    my $TXTFILE=$_[3];
 
    
    # Create a new multipart message:
    $msg1 = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "<!--|WAREHOUSE_REGN_EMAIL|-->,cmpich\@zfin.org",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';

    attach $msg1 
	Type     => 'text/plain',   
	Path     => "$TXTFILE";

    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg1->print(\*SENDMAIL);
    close (SENDMAIL);
    
}

sub logError() {
    open RESULT, ">$globalResultFile" or die "Cannot open the file to write error.";
    my $line = $_;
    print(RESULT "\n");
    print(RESULT "ERROR: $line\n");
    print(RESULT "\n");
    &cronStart();
    my $flagError = $dbhZfin->prepare ("update zdb_flag set zflag_is_on = 'f' where zflag_name = 'disable updates'");
    $flagError->execute;
    print "\nupdates enabled on $whoIsZfinDb after failure in runWarehouse.pl \n";
    &sendMail("ERROR","<!--|WAREHOUSE_REGN_EMAIL|-->","warehouse generation on $whoIsNotZfinDb: FAIL","$globalResultFile");
    die "runWarehouse died because of $line";

}
