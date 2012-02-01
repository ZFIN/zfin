#! /private/bin/perl -w

use DBI;

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$globalResultFile = "/tmp/warehouse_regeneration"."<!--|DB_NAME|-->";
$martName = "fish mart";
@whoIsZfin = `who_is_zfin.sh`;
$whoIsZfinDb = $whoIsZfin[0];
print "zfin is: $whoIsZfinDb";
@whoIsNotZfin = `who_is_not_zfin.sh`;
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

$dbhZfin = DBI->connect("DBI:Informix:$whoIsZfinDb",
		       '',
		       '',
		       {AutoCommit => 1, RaiseError => 1}
		       )
          or die ("Failed while connecting to $whoIsZfin\n");

$dbhNotZfin = DBI->connect("DBI:Informix:$whoIsNotZfinDb",
		       '',
		       '',
		       {AutoCommit => 1, RaiseError => 1}
		       )
          or die ("Failed while connecting to $whoIsNotZfin\n");

### MAIN ###
`/bin/rm -rf $globalResultFile`;
&disableUpdates();
&loadDb();
&runWarehouse();
&integrityChecks("fish mart");
&updateWarehouseTracking("fish mart");
&enableUpdates();
&swapZfin();

sub getEnvFileName {
    my $env = "error";

    #print $whoIsNotZfinDb;
    my $sth = $dbhNotZfin->prepare("select denm_env_file_name from database_env_name_matrix where denm_db_name = '$whoIsNotZfinDb';");
    $sth->execute() or die "could not execute";
    $sth->bind_columns(\$env);
    $sth->dump_results();
    for ($env) {
        s/^\s+//;
        s/\s+$//;
    }
    #print $env."\n";
    return $env;
}

sub swapZfin(){

    system("switch_zfin_swirl_and_hoover.sh") && die "switch zfin to new warehouse failed.";
    
}
sub disableUpdates() {
    my $flag = $dbhZfin->prepare ("update zdb_flag set zflag_is_on = 't' where zflag_name = 'disable updates'");
    $flag->execute;

}

sub enableUpdates() {
    my $flag = $dbhNotZfin->prepare ("update zdb_flag set zflag_is_on = 'f' where zflag_name = 'disable updates'");
    $flag->execute;

}

sub loadDb() {
    # get env name from db table.
    my $envName;
    my $dirName;
    $envName = &getEnvFileName();
    ## CHANGE TO LOADDB.SH ##
    $dirName= `getUnloadDir.sh`;
    for ($dirName) {
        s/^\s+//;
        s/\s+$//;
    }
    system("/private/ZfinLinks/Commons/bin/unloaddb.pl $whoIsZfinDb /research/zunloads/databases/$whoIsZfinDb/$dirname/");
    $dbhNotZfin->disconnect;
    system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/loadDb.sh $whoIsNotZfinDb /research/zunloads/databases/$whoIsZfinDb/ <!--|SOURCEROOT|--> <!--|SOURCEROOT|-->/commons/env/$envName") ;
    if ($? ne 0){
	die "loadDb.sh failed";
    }
    $dbhNotZfin = DBI->connect("DBI:Informix:$whoIsNotZfinDb",
		       '',
		       '',
		       {AutoCommit => 1, RaiseError => 1}
		       )
          or die ("Failed while connecting to $whoIsNotZfin\n");
    #cp unload to zfindb location.
}

sub runWarehouse() {
       system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/fishMart/runFishMart.sh $whoIsNotZfinDb");
    if ($? ne 0){
	die "runFishMart.sh died";
    }
}

sub execSql {

  my $sql = shift;
  my $nRecords = 0;
 
  my $sth = $dbhNotZfin->prepare($sql) or die "Prepare fails";
  $sth -> execute() or die "could not execute";
  my @row = $sth->fetchrow_array();
  $nRecords = $row[0];
  return ($nRecords);
}

sub updateWarehouseReleaseTracking {
    my $sql = shift;
    my $sth = $dbhNotZfin->prepare($sql) or die "Prepare fails";
    $sth -> execute() or die "could not execute";
}

sub integrityChecks($){
    my $wrt_fail ;
    $wrt_fail = 'update warehouse_run_tracking set (wrt_mart_load_successful, wrt_last_loaded_date) = ("f",current year to second) where wrt_mart_name = "$martName"';

    my $sql = 'select count(*) as counter from fish_annotation_search';
    my $fishAnnotationSearchCount = execSql ($sql, undef);

    if ($fishAnnotationSearchCount < 16000) {
	open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
	print RESULT "fish_annotation_search table does not have enough records.< 16000";
	close(RESULT);
	&sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: FAIL','');
	updateWarehouseReleaseTracking($wrt_fail, undef);
	die "fish_annotation_search table does not have enough records.< 16000";	
    }
    print "fish_annotation_search has enough records.\n";
    $sql = 'select count(*) as counter from fish_annotation_search where fas_genotype_group is null and fas_genox_group is null';
    my $genotypeGenoxAvailable = execSql ($sql, undef);

    if ($genotypeGenoxAvailable >0) {
	open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
	print RESULT "$genotypeGenoxAvailable records are missing both a genotype and a genox_group";
	close(RESULT);
	&sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: FAIL','');
	updateWarehouseReleaseTracking($wrt_fail, undef);
	die "$genotypeGenoxAvailable records are missing both a genotype and a genox_group";
    }
    print "fish_annotation_Search geno/genox ok.\n";
    $sql = 'select count(*) as counter from fish_annotation_search where fas_all is null';
    my $checkFasAllNotNull = execSql($sql, undef);

    if ($checkFasAllNotNull >0) {
	open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
	print RESULT "$checkFasAllNotNull records have null fas_all values";
	close(RESULT);
	&sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: FAIL','');
	updateWarehouseReleaseTracking($wrt_fail, undef);
	die "$checkFasAllNotNull records have null fas_all values";
    }
    print "fas_all populated ok.\n";
    $sql = 'select count(*) as counter from gene_feature_result_view where gfrv_construct_zdb_id is null and gfrv_construct_abbrev_order is not null';
    my $constructAbbrevOrderMismatchCount = execSql($sql, undef);

    if ($constructAbbrevOrderMismatchCount >0) {
	open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
	print RESULT "$constructAbbrevOrderMismatchCount records have mismatched construct abbrev/order mismatches in gene_feature_result_view";
	close(RESULT);
	&sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: FAIL','');
	updateWarehouseReleaseTracking($wrt_fail, undef);
	die "$constructAbbrevOrderMismatchCount records have mismatched construct abbrev/order mismatches in gene_feature_result_view";
    }
    print "construct abbrevs and orders in gene_feature_result_view ok.\n";
    $sql = 'select count(*) as counter from fish_annotation_search where fas_fish_significance < 999999';
    my $fishSignificanceCount = execSql($sql, undef);

    if ($fishSignificanceCount < 10) {
	open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
	print RESULT "only $fishSignificanceCount records have significance < 999999";
	close(RESULT);
	&sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: FAIL','');
	updateWarehouseReleaseTracking($wrt_fail, undef);
	die "only $fishSignificanceCount records have significance < 999999";
    }
    print "fish_significance in fish_annotation_search ok.\n";
    $sql = 'select count(*) as counter from gene_feature_result_view';
    my $geneFeatureResultViewCount = execSql($sql, undef);

    if ($geneFeatureResultViewCount < 20312) {
	open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
	print RESULT "gene_feature_result_view table does not have enough records. < 20312";
	close(RESULT);
	&sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: FAIL','');
	updateWarehouseReleaseTracking($wrt_fail, undef);
	die "gene_feature_result_view table does not have enough records. < 20312";
    }
   print "gene_feature_result_view has enough records.\n";
    $sql = 'select count(*) as counter from figure_term_fish_search';
    my $figureTermFishSearchCount = execSql($sql, undef);

    if ($figureTermFishSearchCount < 13997) {
	open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
	print RESULT "figure_term_fish_search table does not have enough records.< 13997";
	close(RESULT);
	&sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: FAIL','');
	updateWarehouseReleaseTracking($wrt_fail, undef);
	die "figure_term_fish_search table does not have enough records.< 13997";
    }
   print "figure_term_fish_search has enough records ok.\n";
    $sql = 'select count(*) as counter from fish_annotation_search where bts_contains(fas_all, "shha")';

    my $btsContainssshhaCount = execSql($sql, undef);
    if ($btsContainssshhaCount < 81){
	open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
	print RESULT "bts index on fish_annotation_search is inacurate: shha < 81";
	close(RESULT);
	updateWarehouseReleaseTracking($wrt_fail, undef);
	&sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: FAIL','');
	die "bts index on fish_annotation_search is inacurate: shha < 81";
    }
   print "fas_all bts_index ok.\n";
}

sub updateWarehouseTracking($){

    my $sqlUpdate = 'update warehouse_run_tracking set (wrt_mart_load_successful, wrt_last_loaded_date) = ("t",current year to second) where wrt_mart_name = "$martName"';
    open RESULT, ">$globalResultFile" or die "Cannot open the file to write check result.";
    print RESULT "$martName updated successfully.";
    close(RESULT);
    updateWarehouseReleaseTracking($sqlUpdate, undef);
    &sendMail('<!--|VALIDATION_EMAIL_DBA|-->','warehouse generation on NotZfinDb: SUCCESS','');
}

sub sendMail(@) {

     my $sendToAddress = shift;
     my $subject = shift;
     my $msg = shift;

     open MAIL, "|/usr/lib/sendmail -t";

     print MAIL "To: $sendToAddress\n";
     print MAIL "Subject: warehouse generation: $subject\n";

     print MAIL "$msg\n";
     print MAIL "\n\n";

     # paste all the result records
     open RESULT, "<$globalResultFile" or die "Cannot open the result file for read.\n";
     while (<RESULT>) {
       print MAIL;
     }
     close RESULT;
 
     close MAIL;
     
     return();
   }
