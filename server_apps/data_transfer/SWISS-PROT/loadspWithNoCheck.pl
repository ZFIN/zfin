#!/private/bin/perl 

#
# loadspWithNoCheck.pl
#
# Run this script to do SWISS-PROT load. 
# It calls many of the subroutine of SWISS-PROT load.
#
# It assumes that the following files are in place:
#
# zfin.dat
# okfile
# ok2file
# spkw2go
# interpro2go
# ec2go

use DBI;  
use MIME::Lite;

# ----------------- Send Error Report -------------
# Parameter
#   $    Error message 

sub sendErrorReport ($) {
  
  my $SUBJECT="Auto SWISS-PROT:".$_[0];
  my $MAILTO="<!--|SWISSPROT_EMAIL_ERR|-->";
  my $TXTFILE="./report.txt";
 
  # Create a new multipart message:
  $msg1 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
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

#------------------ Send statistics of changes of record counts with the load ----------------
# No parameter
#
sub sendStatistics {
		
  my $SUBJECT="Auto: post UniProt load statistics";
  my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";     
  my $ATTFILE = "postUniProtLoadStatistics.txt";

  # Create another new multipart message:
  $msg6 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg6 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg6->print(\*SENDMAIL);

  close(SENDMAIL);
}

#------------------- Download -----------

sub downloadGOtermFiles () {
    &process_vertebrates ;

   system("wget -q http://www.geneontology.org/external2go/spkw2go -O spkw2go");
   system("wget -q http://www.geneontology.org/external2go/interpro2go -O interpro2go");
   system("wget -q http://www.geneontology.org/external2go/ec2go -O ec2go");
 }


sub process_vertebrates{
    system("wget -q ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/uniprot_trembl_vertebrates.dat.gz -O uniprot_trembl_vertebrates.dat.gz");
    system("gunzip uniprot_trembl_vertebrates.dat.gz");
    system("cp uniprot_trembl_vertebrates.dat pre_zfin.dat");
    system("wget -q ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/taxonomic_divisions/uniprot_sprot_vertebrates.dat.gz -O uniprot_sprot_vertebrates.dat.gz");
    system("gunzip uniprot_sprot_vertebrates.dat.gz");
    system("cat uniprot_sprot_vertebrates.dat >> pre_zfin.dat");

    open(DAT, "pre_zfin.dat") || die("Could not open file!");

    open OUTPUT, ">zfin.dat" or die "Cannot open zfin.dat";

    # find each "//\n" and test to see if its a zfin record.
    my $buffer = "" ; 
    my $line ; 
    foreach $line(<DAT>){
       if( 
           $line !~ m/CC   -------/
           and 
           $line !~ m/CC   Copyrighted/ 
           and 
           $line !~ m/CC   Distributed/ 
           and
           $line !~ m/DR   ZFIN; ZDB-GENE/
           ){
           $buffer = $buffer .  $line  ; 
       }
       if($line=~ m/DR   ZFIN; ZDB-GENE/){
           $buffer = $line .  $buffer ; 
       }
       if($line=~ m/\/\/\n/){
           if($buffer=~ m/OS   Danio rerio/){
               print OUTPUT $buffer; 
           }
           $buffer = "" ;  # reset the buffer
       }
    }

    close(DAT) ; 
    close(OUTPUT) ; 
}

sub countData {

  my $ctsql = @_[0];
  my $nRecords = 0;

  ### open a handle on the db
  my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die ("Failed while connecting to <!--|DB_NAME|-->");


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
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/";

print "WARNING!!! no zfin.dat provided. \n" if (!-e "zfin.dat");

print "WARNING!!! no okfile provided. \n" if (!-e "okfile");

print "WARNING!!! no ok2file provided. \n" if (!-e "ok2file");

print "WARNING!!! no spkw2go provided. \n" if (!-e "spkw2go");

print "WARNING!!! no interpro2go provided. \n" if (!-e "interpro2go");

print "WARNING!!! no ec2go provided. \n" if (!-e "ec2go");

#remove old files
 
system("rm -f ./ccnote/*");
system("rmdir ./ccnote");
system("rm -f *.ontology");
#system("rm -f *2go");
#system("rm -f prob*");
#system("rm -f okfile");
#system("rm -f pubmed_not_in_zfin");
system("rm -f *.unl");
system("rm -f *.txt");
#system("rm -f *.dat");
system("mkdir ./ccnote");


### &downloadGOtermFiles();

#--------------------------- record counts before loading starts ----------------------------
$sql = 'select * from db_link where dblink_info like "%Swiss-Prot%";';

$numDblinkBefore = countData($sql);

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA";';
$numIEABefore = countData($sql);         

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA" 
           and mrkrgoev_contributed_by = "ZFIN SP keyword 2 GO";';
           
$numIEASP2GOBefore = countData($sql);

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA" 
           and mrkrgoev_contributed_by = "ZFIN InterPro 2 GO";';
           
$numIEAInterPro2GOBefore = countData($sql);

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA" 
           and mrkrgoev_contributed_by = "ZFIN EC acc 2 GO";';
           
$numIEAEC2GOBefore = countData($sql);

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA" 
           and mrkrgoev_contributed_by = "ZFIN EC acc 2 GO";';
           
$numIEAEC2GOBefore = countData($sql);

$sql = 'select distinct term_zdb_id from marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and mrkrgoev_term_zdb_id = term_zdb_id;';
$numIEAtermsBefore = countData($sql);           

$sql = 'select distinct term_zdb_id from marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "cellular_component" 
           and mrkrgoev_term_zdb_id = term_zdb_id;';

$numIEAtermComponentBefore = countData($sql);  

$sql = 'select distinct term_zdb_id from marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "molecular_function" 
           and mrkrgoev_term_zdb_id = term_zdb_id;';

$numIEAtermFunctionBefore = countData($sql);  

$sql = 'select distinct term_zdb_id from marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "biological_process" 
           and mrkrgoev_term_zdb_id = term_zdb_id;';

$numIEAtermProcessBefore = countData($sql);           

$sql = 'select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;';

$numMrkrBefore = countData($sql); 

$sql = 'select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "cellular_component" 
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;';

$numMrkrComponentBefore = countData($sql); 

$sql = 'select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "molecular_function" 
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;';

$numMrkrFunctionBefore = countData($sql); 

$sql = 'select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "biological_process" 
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;';

$numMrkrProcessBefore = countData($sql); 


#--------------- Delete records from last SWISS-PROT loading-----
print "\n delete records source from last SWISS-PROT loading.\n";
system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> sp_delete.sql >out 2>report.txt");
open F, "out" or die "Cannot open out file";
if (<F>) {
 
   &sendErrorReport("Failed to delete old records");
   exit;
}
close F;
 
# --------------- Check SWISS-PROT file --------------
# good records for loading are placed in "okfile"
### print "\n sp_check.pl zfin.dat >checkreport.txt \n";
### system ("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/sp_check.pl zfin.dat >checkreport.txt" );



# concatenate all the sub problem files
### system("cat prob0 prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8> allproblems.txt");

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
print "\nloading...\n";
system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> sp_load.sql >out 2> report.txt");

#--------------------------- record counts after loading finishes ----------------------------

$sql = 'select * from db_link where dblink_info like "%Swiss-Prot%";';

$numDblinkAfter = countData($sql);

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA";';
$numIEAAfter = countData($sql);         

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA" 
           and mrkrgoev_contributed_by = "ZFIN SP keyword 2 GO";';
           
$numIEASP2GOAfter = countData($sql);

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA" 
           and mrkrgoev_contributed_by = "ZFIN InterPro 2 GO";';
           
$numIEAInterPro2GOAfter = countData($sql);

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA" 
           and mrkrgoev_contributed_by = "ZFIN EC acc 2 GO";';
           
$numIEAEC2GOAfter = countData($sql);

$sql = 'select mrkrgoev_zdb_id 
          from marker_go_term_evidence 
         where mrkrgoev_evidence_code = "IEA" 
           and mrkrgoev_contributed_by = "ZFIN EC acc 2 GO";';
           
$numIEAEC2GOAfter = countData($sql);

$sql = 'select distinct term_zdb_id from marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and mrkrgoev_term_zdb_id = term_zdb_id;';
$numIEAtermsAfter = countData($sql);           

$sql = 'select distinct term_zdb_id from marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "cellular_component" 
           and mrkrgoev_term_zdb_id = term_zdb_id;';

$numIEAtermComponentAfter = countData($sql);  

$sql = 'select distinct term_zdb_id from marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "molecular_function" 
           and mrkrgoev_term_zdb_id = term_zdb_id;';

$numIEAtermFunctionAfter = countData($sql);  

$sql = 'select distinct term_zdb_id from marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "biological_process" 
           and mrkrgoev_term_zdb_id = term_zdb_id;';

$numIEAtermProcessAfter = countData($sql);           

$sql = 'select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;';

$numMrkrAfter = countData($sql); 

$sql = 'select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "cellular_component" 
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;';

$numMrkrComponentAfter = countData($sql); 

$sql = 'select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "molecular_function" 
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;';

$numMrkrFunctionAfter = countData($sql); 

$sql = 'select distinct mrkr_zdb_id from marker, marker_go_term_evidence, term 
         where term_ont_id like "GO%" 
           and mrkrgoev_evidence_code = "IEA"
           and term_ontology = "biological_process" 
           and mrkrgoev_term_zdb_id = term_zdb_id
           and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id;';

$numMrkrProcessAfter = countData($sql); 

open (POSTLOADREPORT, '>postUniProtLoadStatistics.txt') or die "Cannot open postUniProtLoadStatistics.txt: $!";

print POSTLOADREPORT "count of records associated with UniProt\t";
print POSTLOADREPORT "before load\t";
print POSTLOADREPORT "after load\t";
print POSTLOADREPORT "percentage change\n";
print POSTLOADREPORT "----------------------------------------\t-----------\t-----------\t-------------------------\n";

print POSTLOADREPORT "db_link records                         \t";
print POSTLOADREPORT "$numDblinkBefore   \t";
print POSTLOADREPORT "$numDblinkAfter   \t";
printf POSTLOADREPORT "%.2f\n", ($numDblinkAfter - $numDblinkBefore) / $numDblinkBefore * 100;

print POSTLOADREPORT "----------------------------------------\t-----------\t-----------\t-------------------------\n";

print POSTLOADREPORT "marker_go_term_evidence IEA records     \t";
print POSTLOADREPORT "$numIEABefore      \t";
print POSTLOADREPORT "$numIEAAfter      \t";
printf POSTLOADREPORT "%.2f\n", ($numIEAAfter - $numIEABefore) / $numIEABefore * 100;

print POSTLOADREPORT "marker_go_term_evidence records from SP \t";
print POSTLOADREPORT "$numIEASP2GOBefore   \t";
print POSTLOADREPORT "$numIEASP2GOAfter   \t";
printf POSTLOADREPORT "%.2f\n", ($numIEASP2GOAfter - $numIEASP2GOBefore) / $numIEASP2GOBefore * 100;

print POSTLOADREPORT "marker_go_term_evidence records from IP \t";
print POSTLOADREPORT "$numIEAInterPro2GOBefore   \t";
print POSTLOADREPORT "$numIEAInterPro2GOAfter   \t";
printf POSTLOADREPORT "%.2f\n", ($numIEAInterPro2GOAfter - $numIEAInterPro2GOBefore) / $numIEAInterPro2GOBefore * 100;

print POSTLOADREPORT "marker_go_term_evidence records from EC \t";
print POSTLOADREPORT "$numIEAEC2GOBefore        \t";
print POSTLOADREPORT "$numIEAEC2GOAfter        \t";
printf POSTLOADREPORT "%.2f\n", ($numIEAEC2GOAfter - $numIEAEC2GOBefore) / $numIEAEC2GOBefore * 100;

print POSTLOADREPORT "----------------------------------------\t-----------\t-----------\t-------------------------\n";

print POSTLOADREPORT "go terms with IEA annotation            \t";
print POSTLOADREPORT "$numIEAtermsBefore        \t";
print POSTLOADREPORT "$numIEAtermsAfter        \t";
printf POSTLOADREPORT "%.2f\n", ($numIEAtermsAfter - $numIEAtermsBefore) / $numIEAtermsBefore * 100;

print POSTLOADREPORT "component go terms with IEA             \t";
print POSTLOADREPORT "$numIEAtermComponentBefore           \t";
print POSTLOADREPORT "$numIEAtermComponentAfter           \t";
printf POSTLOADREPORT "%.2f\n", ($numIEAtermComponentAfter - $numIEAtermComponentBefore) / $numIEAtermComponentBefore * 100;

print POSTLOADREPORT "function go terms with IEA              \t";
print POSTLOADREPORT "$numIEAtermFunctionBefore        \t";
print POSTLOADREPORT "$numIEAtermFunctionAfter        \t";
printf POSTLOADREPORT "%.2f\n", ($numIEAtermFunctionAfter - $numIEAtermFunctionBefore) / $numIEAtermFunctionBefore * 100;

print POSTLOADREPORT "process go terms with IEA               \t";
print POSTLOADREPORT "$numIEAtermProcessBefore         \t";
print POSTLOADREPORT "$numIEAtermProcessAfter         \t";
printf POSTLOADREPORT "%.2f\n", ($numIEAtermProcessAfter - $numIEAtermProcessBefore) / $numIEAtermProcessBefore * 100;

print POSTLOADREPORT "----------------------------------------\t-----------\t-----------\t-------------------------\n";

print POSTLOADREPORT "markers with IEA annotation                \t";
print POSTLOADREPORT "$numMrkrBefore        \t";
print POSTLOADREPORT "$numMrkrAfter        \t";
printf POSTLOADREPORT "%.2f\n", ($numMrkrAfter - $numMrkrBefore) / $numMrkrBefore * 100;

print POSTLOADREPORT "markers with IEA annotation component     \t";
print POSTLOADREPORT "$numIEAtermComponentBefore           \t";
print POSTLOADREPORT "$numIEAtermComponentAfter           \t";
printf POSTLOADREPORT "%.2f\n", ($numIEAtermComponentAfter - $numIEAtermComponentBefore) / $numIEAtermComponentBefore * 100;

print POSTLOADREPORT "markers with IEA annotation function      \t";
print POSTLOADREPORT "$numMrkrFunctionBefore        \t";
print POSTLOADREPORT "$numMrkrFunctionAfter        \t";
printf POSTLOADREPORT "%.2f\n", ($numMrkrFunctionAfter - $numMrkrFunctionBefore) / $numMrkrFunctionBefore * 100;

print POSTLOADREPORT "markers with IEA annotation process      \t";
print POSTLOADREPORT "$numMrkrProcessBefore         \t";
print POSTLOADREPORT "$numMrkrProcessAfter         \t";
printf POSTLOADREPORT "%.2f\n", ($numMrkrProcessAfter - $numMrkrProcessBefore) / $numMrkrProcessBefore * 100;

print "All done \n";
close (POSTLOADREPORT);

&sendStatistics();

exit;



