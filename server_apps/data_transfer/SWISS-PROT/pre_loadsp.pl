#!/private/bin/perl 
use strict ; 

#
# pre_loadsp.pl
#
 
use MIME::Lite;


#------------------- Download -----------

sub downloadGOtermFiles () {
    &process_vertabrates ;

   system("wget -q http://www.geneontology.org/external2go/spkw2go -O spkw2go");
   system("wget -q http://www.geneontology.org/external2go/interpro2go -O interpro2go");
   system("wget -q http://www.geneontology.org/external2go/ec2go -O ec2go");
 }

# ----------------- Send Error Report -------------
# Parameter
#   $    Error message 

sub sendErrorReport ($) {
  
  my $SUBJECT="Auto SWISS-PROT:".$_[0];
  my $MAILTO="<!--|SWISSPROT_EMAIL_ERR|-->";
  my $TXTFILE="./report.txt";
 
  # Create a new multipart message:
  my $msg1 = new MIME::Lite 
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
  my $msg2 = new MIME::Lite 
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
  my $msg3 = new MIME::Lite 
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

 #----- Another mail send out problem files ----

  my $SUBJECT="Auto: PubMed not in ZFIN";
  my $MAILTO="<!--|SWISSPROT_EMAIL_REPORT|-->";     
  my $ATTFILE = "pubmed_not_in_zfin";

  # Create another new multipart message:
  my $msg4 = new MIME::Lite 
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

  close(SENDMAIL);
}


# ====================================
#
# Extracts only zfin data from vertebrates.
#
sub process_vertabrates{
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
           ){
        $buffer = $buffer .  $line  ; 
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
system ("<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/sp_check.pl zfin.dat >checkreport.txt" );

my $count = 0;
my $retry = 1;
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
system("cat prob1 prob2 prob3 prob4 prob5 prob6 prob7 prob8 > allproblems.txt");

&sendRunningResult();

exit;



