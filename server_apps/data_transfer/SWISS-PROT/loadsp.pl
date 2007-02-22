#!/private/bin/perl 

#
# loadsp.pl
#
# Run this script to do SWISS-PROT load. 
# It contains all the subroutine of SWISS-PROT load.
 
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

#------------------ Send Running Result ----------------
# No parameter
#
sub sendRunningResult {
		
 #----- One mail send out the checking report----

  my $SUBJECT="Auto: SWISS-PROT check report";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";
  my $TXTFILE="./checkreport.txt";
 
  # Create a new multipart message:
  $msg2 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg2 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg2->print(\*SENDMAIL);

  
 #----- Another mail send out problem files ----

  my $SUBJECT="Auto: SWISS-PROT problem file";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";     
  my $ATTFILE = "allproblems.txt";

  # Create another new multipart message:
  $msg3 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg3 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg3->print(\*SENDMAIL);

 #----- Another mail send out pubmed numbers that are not in ZFIN ----

  my $SUBJECT="Auto: PubMed not in ZFIN";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";     
  my $ATTFILE = "pubmed_not_in_zfin";

  # Create another new multipart message:
  $msg4 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg4 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg4->print(\*SENDMAIL);

 #----- Another mail send out accession numbers that are not attributed ----

  my $SUBJECT="Auto: Accession w/o attribution";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";     
  my $ATTFILE = "accession_with_no_attribution";

  # Create another new multipart message:
  $msg5 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg5 
    Type     => 'application/octet-stream',
    Encoding => 'base64',
    Path     => "./$ATTFILE",
    Filename => "$ATTFILE";

  # Output the message to sendmail
  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg5->print(\*SENDMAIL);

  close(SENDMAIL);
}

#------------------- Download -----------

sub downloadGOtermFiles () {

   system("wget -q ftp://ftp.ebi.ac.uk/pub/contrib/dbarrell/zfin.dat -O zfin.dat");
   system("wget -q http://www.geneontology.org/external2go/spkw2go -O spkw2go");
   system("wget -q http://www.geneontology.org/external2go/interpro2go -O interpro2go");
   system("wget -q http://www.geneontology.org/external2go/ec2go -O ec2go");
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

print "WARNING!!! no ok2file provided. \n" if (!-e "ok2file");

#remove old files
 
system("rm -f ./ccnote/*");
system("rmdir ./ccnote");
system("rm -f *.ontology");
system("rm -f *2go");
system("rm -f prob*");
system("rm -f okfile");
system("rm -f pubmed_not_in_zfin");
system("rm -f *.unl");
system("rm -f *.txt");
system("rm -f *.dat");
system("mkdir ./ccnote");


&downloadGOtermFiles();


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
print "\n sp_check.pl zfin.dat >checkreport.txt \n";
system ("sp_check.pl zfin.dat >checkreport.txt" );

$count = 0;
$retry = 1;
# wait till checking is finished
while( !( -e "okfile" && 
          -e "problemfile")) {

  $count++;
  if ($count > 10)
  {
    if ($retry) 
    {
      $count = 0;
      $retry = 0;
      print "retry sp_check.pl\n";
      system("sp_check.pl zfin.dat >checkreport.txt ");
    }
    else
    {
      &sendErrorReport("Failed to run sp_check.pl");
      exit;
    }
  }  
}

# concatenate all the sub problem files
system("cat prob0 prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8> allproblems.txt");
system("cat ok2file >> okfile");


# ----------- Parse the SWISS-PROT file ----------------
print "\n sp_parser.pl okfile \n";
system ("sp_parser.pl okfile");

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
      system("sp_parser.pl okfile");
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
system ("sptogo.pl spkw2go");
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
      system("sptogo.pl spkw2go");
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
system ("iptogo.pl interpro2go");
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
      system("iptogo.pl interpro2go");
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
system ("ectogo.pl ec2go");
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
      system("ectogo.pl ec2go");
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

open F, "out" or die "Cannot open out";
if (<F>) {
   &sendErrorReport("Failed to load SWISS_PROT records");
  exit;
}
close F;

#create new go_association file

&sendRunningResult();

#----------- Match the obsolete/secondary go terms in the translation file -----
print "\n deal with obsolete / secondary go terms \n";

system ("sp_badgo_report.pl");


exit;



