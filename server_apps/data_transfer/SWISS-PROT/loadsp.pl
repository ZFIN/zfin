#!/opt/zfin/bin/perl

#
# loadsp.pl
#
# Run this script to do SWISS-PROT load.
# It calls many of the subroutine of SWISS-PROT load.
#
# It assumes that the following 6 files are in place (copied from a testdb targert directory):
#

# okfile
# ok2file
# spkw2go
# interpro2go
# ec2go

use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;
use Try::Tiny;
use POSIX;

# ----------------- Send Error Report -------------
# Parameter
#   $    Error message

sub sendErrorReport ($) {

  my $SUBJECT = "Auto from $dbname SWISS-PROT:".$_[0];
  ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_ERR|-->',"$SUBJECT","report.txt");

}

sub countData {

  my $ctsql = @_[0];
  my $nRecords = 0;

  my $dbname = "<!--|DB_NAME|-->";
  my $dbhost = "<!--|PGHOST|-->";
  my $username = "";
  my $password = "";

  ### open a handle on the db
  my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

  my $sth = $dbh->prepare($ctsql) or die "Prepare fails";

  $sth -> execute() or die "Could not execute $sql";

  while (my @row = $sth ->fetchrow_array()) {
    $nRecords++;
  }

  $dbh->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";

  return ($nRecords);
}

#=======================================================
#
#   Main
#


#set environment variables
$dbname = "<!--|DB_NAME|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/";


#remove old files

system("rm -f ./ccnote/*");
system("rmdir ./ccnote");
system("rm -f *.ontology");
system("rm -f *.unl");
system("rm -f *.txt");
system("mkdir ./ccnote");

print "WARNING!!! no okfile provided. \n" if (!-e "okfile");

print "WARNING!!! no ok2file provided. \n" if (!-e "ok2file");

print "WARNING!!! no spkw2go provided. \n" if (!-e "spkw2go");

print "WARNING!!! no interpro2go provided. \n" if (!-e "interpro2go");

print "WARNING!!! no ec2go provided. \n" if (!-e "ec2go");

if (!-e "okfile" || !-e "ok2file" || !-e "spkw2go" || !-e "interpro2go" || !-e "ec2go") {
   print "One or more required file(s) not exisiting. Exit.\n";
   exit;
}

print("Preparing metrics at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . "\n");

#--------------------------- record counts before loading starts ----------------------------
$sql = "select * from db_link where dblink_info like '%Swiss-Prot%';";

$numDblinkBefore = countData($sql);

$sql = "select distinct extnote_zdb_id
          from external_note, db_link
         where extnote_data_zdb_id = dblink_zdb_id
           and dblink_info like '%Swiss-Prot%';";
$numExternalNoteBefore = countData($sql);

$sql = "select distinct dblink_linked_recid
          from external_note note1, db_link
         where note1.extnote_data_zdb_id = dblink_zdb_id
           and dblink_info like '%Swiss-Prot%'
           and exists (select 'x' from external_note note2
                        where note2.extnote_data_zdb_id = note1.extnote_data_zdb_id
                          and note2.extnote_zdb_id != note1.extnote_zdb_id);";
$numMarkersWithRedundantDblkNoteBefore = countData($sql);

$sql = "select mrkrgoev_zdb_id
          from marker_go_term_evidence
         where mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO');";
$numIEABefore = countData($sql);

$sql = "select mrkrgoev_zdb_id
          from marker_go_term_evidence
         where mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes = 'ZFIN SP keyword 2 GO';";

$numIEASP2GOBefore = countData($sql);

$sql = "select mrkrgoev_zdb_id
          from marker_go_term_evidence
         where mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes = 'ZFIN InterPro 2 GO';";

$numIEAInterPro2GOBefore = countData($sql);

$sql = "select mrkrgoev_zdb_id
          from marker_go_term_evidence
         where mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes = 'ZFIN EC acc 2 GO';";

$numIEAEC2GOBefore = countData($sql);


$sql = "select distinct term_zdb_id from marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
           and mrkrgoev_term_zdb_id = term_zdb_id;";
$numIEAtermsBefore = countData($sql);

$sql = "select distinct term_zdb_id from marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
           and term_ontology = 'cellular_component'
           and mrkrgoev_term_zdb_id = term_zdb_id;";

$numIEAtermComponentBefore = countData($sql);

$sql = "select distinct term_zdb_id from marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
           and term_ontology = 'molecular_function'
           and mrkrgoev_term_zdb_id = term_zdb_id;";

$numIEAtermFunctionBefore = countData($sql);

$sql = "select distinct term_zdb_id from marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
           and term_ontology = 'biological_process'
           and mrkrgoev_term_zdb_id = term_zdb_id;";

$numIEAtermProcessBefore = countData($sql);

$sql = "select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;";

$numMrkrBefore = countData($sql);

$sql = "select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
           and term_ontology = 'cellular_component'
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;";

$numMrkrComponentBefore = countData($sql);

$sql = "select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
           and term_ontology = 'molecular_function'
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;";

$numMrkrFunctionBefore = countData($sql);

$sql = "select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_notes in ('ZFIN SP keyword 2 GO', 'ZFIN InterPro 2 GO', 'ZFIN EC acc 2 GO')
           and term_ontology = 'biological_process'
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;";

$numMrkrProcessBefore = countData($sql);

print("Running sp_addbackattr.sql at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . "\n");
try {
  system("psql -d <!--|DB_NAME|--> -a -f sp_addbackattr.sql >addBackAttributionReport.txt");
} catch {
  chomp $_;
  &sendErrorReport("Failed to execute sp_addbackattr.sql - $_");
  exit -1;
};

print "\n delete records source from last SWISS-PROT loading.\n";

try {
  system("psql -d <!--|DB_NAME|--> -a -f sp_delete.sql >deletereport.txt");
} catch {
  chomp $_;
  &sendErrorReport("Failed to execute sp_delete.sql - $_");
  exit -1;
};

# good records for loading
# concatenate okfile ok2file

system("cat ok2file >> okfile");


# ----------- Parse the SWISS-PROT file ----------------
print "\n sp_parser.pl okfile \n";
system ("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/sp_parser.pl okfile");

$count = 0;
$retry = 1;
# wait till parsing is finished
while( !( -e "dr_dblink.unl" &&
          -e "ac_dalias.unl" &&
          -e "cc_external.unl" &&
	  -e "kd_spkeywd.unl" )) {

  $count++;
  if ($count > 10)
  {
    if ($retry)
    {
      $count = 0;
      $retry = 0;
      print "retry sp_parser.pl\n";
      system("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/sp_parser.pl okfile");
    }
    else
    {
      &sendErrorReport("Failed to run sp_parser.pl");
      exit;
    }
  }
}

system("ls *.unl");

# ------------ Parse spkw2go ---------------
print "\nsptogo.pl spkw2go\n";
system ("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/sptogo.pl spkw2go");
$count = 0;
$retry = 1;
# wait till parsing is finished
while( !( -e "sp_mrkrgoterm.unl")) {

  $count++;
  if ($count > 10)
  {
    if ($retry)
    {
      $count = 0;
      $retry = 0;
      print "retry sptogo.pl\n";
      system("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/sptogo.pl spkw2go");
    }
    else
    {
      &sendErrorReport("Failed to run sptogo.pl");
      exit;
    }
  }
}

# ------------ Parse interpro2go ---------------
print "\niptogo.pl interpro2go\n";
system ("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/iptogo.pl interpro2go");
$count = 0;
$retry = 1;
# wait till parsing is finished
while( !( -e "ip_mrkrgoterm.unl")) {

  $count++;
  if ($count > 10)
  {
    if ($retry)
    {
      $count = 0;
      $retry = 0;
      print "retry iptogo.pl\n";
      system("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/iptogo.pl interpro2go");
    }
    else
    {
      &sendErrorReport("Failed to run iptogo.pl");
      exit;
    }
  }
}


# ------------ Parse ec2go ---------------

print "\nectogo.pl ec2go\n";
system ("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/ectogo.pl ec2go");
$count = 0;
$retry = 1;
# wait till parsing is finished
while( !( -e "ec_mrkrgoterm.unl")) {

  $count++;
  if ($count > 10)
  {
    if ($retry)
    {
      $count = 0;
      $retry = 0;
      print "retry ectogo.pl\n";
      system("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/ectogo.pl ec2go");
    }
    else
    {
      &sendErrorReport("Failed to run ectogo.pl");
      exit;
    }
  }
}


# ------------ Loading ---------------------
print "\nloading... at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . "\n";
try {
  system("psql -d <!--|DB_NAME|--> -a -f sp_load.sql > report.txt");
} catch {
  chomp $_;
  &sendErrorReport("Failed to execute sp_load.sql - $_");
  exit -1;
};

#--------------------------- record counts after loading finishes ----------------------------

print "\nPreparing post-load metrics at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . "\n";
$sql = "select * from db_link where dblink_info like '%Swiss-Prot%';";

$numDblinkAfter = countData($sql);

$sql = "select distinct extnote_zdb_id
          from external_note, db_link
         where extnote_data_zdb_id = dblink_zdb_id
           and dblink_info like '%Swiss-Prot%';";
$numExternalNoteAfter = countData($sql);

$sql = "select distinct dblink_linked_recid
          from external_note note1, db_link
         where note1.extnote_data_zdb_id = dblink_zdb_id
           and dblink_info like '%Swiss-Prot%'
           and exists (select 'x' from external_note note2
                        where note2.extnote_data_zdb_id = note1.extnote_data_zdb_id
                          and note2.extnote_zdb_id != note1.extnote_zdb_id);";
$numMarkersWithRedundantDblkNoteAfter = countData($sql);

$sql = "select mrkrgoev_zdb_id
          from marker_go_term_evidence
         where mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3');";

$numIEAAfter = countData($sql);

$sql = "select mrkrgoev_zdb_id
          from marker_go_term_evidence
         where mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id = 'ZDB-PUB-020723-1';";

$numIEASP2GOAfter = countData($sql);

$sql = "select mrkrgoev_zdb_id
          from marker_go_term_evidence
         where mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id = 'ZDB-PUB-020724-1';";

$numIEAInterPro2GOAfter = countData($sql);

$sql = "select mrkrgoev_zdb_id
          from marker_go_term_evidence
         where mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id = 'ZDB-PUB-031118-3';";

$numIEAEC2GOAfter = countData($sql);


$sql = "select distinct term_zdb_id from marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3')
           and mrkrgoev_term_zdb_id = term_zdb_id;";
$numIEAtermsAfter = countData($sql);

$sql = "select distinct term_zdb_id from marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3')
           and term_ontology = 'cellular_component'
           and mrkrgoev_term_zdb_id = term_zdb_id;";

$numIEAtermComponentAfter = countData($sql);

$sql = "select distinct term_zdb_id from marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3')
           and term_ontology = 'molecular_function'
           and mrkrgoev_term_zdb_id = term_zdb_id;";

$numIEAtermFunctionAfter = countData($sql);

$sql = "select distinct term_zdb_id from marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3')
           and term_ontology = 'biological_process'
           and mrkrgoev_term_zdb_id = term_zdb_id;";

$numIEAtermProcessAfter = countData($sql);

$sql = "select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3')
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;";

$numMrkrAfter = countData($sql);

$sql = "select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3')
           and term_ontology = 'cellular_component'
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;";

$numMrkrComponentAfter = countData($sql);

$sql = "select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3')
           and term_ontology = 'molecular_function'
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;";

$numMrkrFunctionAfter = countData($sql);

$sql = "select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term
         where term_ont_id like 'GO%'
           and mrkrgoev_evidence_code = 'IEA'
           and mrkrgoev_annotation_organization = '5'
           and mrkrgoev_source_zdb_id in ('ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3')
           and term_ontology = 'biological_process'
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;";

$numMrkrProcessAfter = countData($sql);

open (POSTLOADREPORT, '>postUniProtLoadStatistics.txt') or die "Cannot open postUniProtLoadStatistics.txt: $!";

printf POSTLOADREPORT "%-45s\t", "count of records associated with UniProt";
printf POSTLOADREPORT "%-11s\t", "before load";
printf POSTLOADREPORT "%-11s\t", "after load";
printf POSTLOADREPORT "%-17s\n", "percentage change";
print POSTLOADREPORT "---------------------------------------------\t-----------\t-----------\t-----------------\n";

printf POSTLOADREPORT "%-45s\t", "db_link records";
printf POSTLOADREPORT "%11s\t", "$numDblinkBefore";
printf POSTLOADREPORT "%11s\t", "$numDblinkAfter";
printf POSTLOADREPORT "%17.2f\n", ($numDblinkAfter - $numDblinkBefore) / $numDblinkBefore * 100 if ($numDblinkBefore > 0);

printf POSTLOADREPORT "%-45s\t", "external_note with db_link";
printf POSTLOADREPORT "%11s\t", "$numExternalNoteBefore";
printf POSTLOADREPORT "%11s\t", "$numExternalNoteAfter";
printf POSTLOADREPORT "%17.2f\n", ($numExternalNoteAfter - $numExternalNoteBefore) / $numExternalNoteBefore * 100 if ($numExternalNoteBefore > 0);


printf POSTLOADREPORT "%-45s\t", "genes with duplicated db_link notes";
printf POSTLOADREPORT "%11s\t", "$numMarkersWithRedundantDblkNoteBefore";
printf POSTLOADREPORT "%11s\t", "$numMarkersWithRedundantDblkNoteAfter";
printf POSTLOADREPORT "   not calculated\n";

print POSTLOADREPORT "---------------------------------------------\t-----------\t-----------\t-----------------\n";

printf POSTLOADREPORT "%-45s\t", "marker_go_term_evidence IEA records";
printf POSTLOADREPORT "%11s\t", "$numIEABefore";
printf POSTLOADREPORT "%11s\t", "$numIEAAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEAAfter - $numIEABefore) / $numIEABefore * 100 if ($numIEABefore > 0);

printf POSTLOADREPORT "%-45s\t", "marker_go_term_evidence records from SP";
printf POSTLOADREPORT "%11s\t", "$numIEASP2GOBefore";
printf POSTLOADREPORT "%11s\t", "$numIEASP2GOAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEASP2GOAfter - $numIEASP2GOBefore) / $numIEASP2GOBefore * 100 if ($numIEASP2GOBefore > 0);

printf POSTLOADREPORT "%-45s\t", "marker_go_term_evidence records from IP";
printf POSTLOADREPORT "%11s\t", "$numIEAInterPro2GOBefore";
printf POSTLOADREPORT "%11s\t", "$numIEAInterPro2GOAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEAInterPro2GOAfter - $numIEAInterPro2GOBefore) / $numIEAInterPro2GOBefore * 100 if ($numIEAInterPro2GOBefore > 0);

printf POSTLOADREPORT "%-45s\t", "marker_go_term_evidence records from EC";
printf POSTLOADREPORT "%11s\t", "$numIEAEC2GOBefore";
printf POSTLOADREPORT "%11s\t", "$numIEAEC2GOAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEAEC2GOAfter - $numIEAEC2GOBefore) / $numIEAEC2GOBefore * 100 if ($numIEAEC2GOBefore > 0);

print POSTLOADREPORT "---------------------------------------------\t-----------\t-----------\t-----------------\n";

printf POSTLOADREPORT "%-45s\t", "go terms with IEA annotation";
printf POSTLOADREPORT "%11s\t", "$numIEAtermsBefore";
printf POSTLOADREPORT "%11s\t", "$numIEAtermsAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEAtermsAfter - $numIEAtermsBefore) / $numIEAtermsBefore * 100 if ($numIEAtermsBefore > 0);

printf POSTLOADREPORT "%-45s\t", "component go terms with IEA";
printf POSTLOADREPORT "%11s\t", "$numIEAtermComponentBefore";
printf POSTLOADREPORT "%11s\t", "$numIEAtermComponentAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEAtermComponentAfter - $numIEAtermComponentBefore) / $numIEAtermComponentBefore * 100 if ($numIEAtermComponentBefore > 0);

printf POSTLOADREPORT "%-45s\t", "function go terms with IEA";
printf POSTLOADREPORT "%11s\t", "$numIEAtermFunctionBefore";
printf POSTLOADREPORT "%11s\t", "$numIEAtermFunctionAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEAtermFunctionAfter - $numIEAtermFunctionBefore) / $numIEAtermFunctionBefore * 100 if ($numIEAtermFunctionBefore > 0);

printf POSTLOADREPORT "%-45s\t", "process go terms with IEA";
printf POSTLOADREPORT "%11s\t", "$numIEAtermProcessBefore";
printf POSTLOADREPORT "%11s\t", "$numIEAtermProcessAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEAtermProcessAfter - $numIEAtermProcessBefore) / $numIEAtermProcessBefore * 100 if ($numIEAtermProcessBefore > 0);

print POSTLOADREPORT "---------------------------------------------\t-----------\t-----------\t-----------------\n";

printf POSTLOADREPORT "%-45s\t", "markers with IEA annotation";
printf POSTLOADREPORT "%11s\t", "$numMrkrBefore";
printf POSTLOADREPORT "%11s\t", "$numMrkrAfter";
printf POSTLOADREPORT "%17.2f\n", ($numMrkrAfter - $numMrkrBefore) / $numMrkrBefore * 100 if ($numMrkrBefore > 0);

printf POSTLOADREPORT "%-45s\t", "markers with IEA annotation component";
printf POSTLOADREPORT "%11s\t", "$numIEAtermComponentBefore";
printf POSTLOADREPORT "%11s\t", "$numIEAtermComponentAfter";
printf POSTLOADREPORT "%17.2f\n", ($numIEAtermComponentAfter - $numIEAtermComponentBefore) / $numIEAtermComponentBefore * 100 if ($numIEAtermComponentBefore > 0);

printf POSTLOADREPORT "%-45s\t", "markers with IEA annotation function";
printf POSTLOADREPORT "%11s\t", "$numMrkrFunctionBefore";
printf POSTLOADREPORT "%11s\t", "$numMrkrFunctionAfter";
printf POSTLOADREPORT "%17.2f\n", ($numMrkrFunctionAfter - $numMrkrFunctionBefore) / $numMrkrFunctionBefore * 100 if ($numMrkrFunctionBefore > 0);

printf POSTLOADREPORT "%-45s\t", "markers with IEA annotation process";
printf POSTLOADREPORT "%11s\t", "$numMrkrProcessBefore";
printf POSTLOADREPORT "%11s\t", "$numMrkrProcessAfter";
printf POSTLOADREPORT "%17.2f\n", ($numMrkrProcessAfter - $numMrkrProcessBefore) / $numMrkrProcessBefore * 100 if ($numMrkrProcessBefore > 0);

close (POSTLOADREPORT);
print "All done at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . "\n";

#------------------ Send statistics of changes of record counts with the load ----------------
$subject = "Auto from $dbname: " . "post UniProt load statistics";
ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_CURATOR|-->',"$subject","postUniProtLoadStatistics.txt");
ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_ERR|-->',"$subject","postUniProtLoadStatistics.txt");

#------------------ Send log of the load ----------------
$subject = "Auto from $dbname: " . "UniProt load log";
ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_ERR|-->',"$subject","report.txt");

print "\n create_file_for_swiss_prot.pl at " . strftime("%Y-%m-%d %H:%M:%S", localtime(time())) . "\n";
system ("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/create_file_for_swiss_prot.pl");

exit;
