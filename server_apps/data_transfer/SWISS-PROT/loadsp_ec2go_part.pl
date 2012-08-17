#!/private/bin/perl 

#
# loadsp_ec2gopart.pl
#
# Run this script to do ec2go part only.
 
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


#------------------- Download -----------

sub downloadGOtermFiles () {
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

#remove old files

system("rm -f *2go");

&downloadGOtermFiles();

print "\n delete records source from last ec2go loading.\n";
system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> sp_delete_ec2gopart.sql >out 2>report.txt");


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
system ("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> sp_load_ec2gopart.sql >out 2> report2.txt");

open F, "out" or die "Cannot open out";
if (<F>) {
   &sendErrorReport("Failed to load ec2go part of SWISS_PROT records");
  exit;
}
close F;

#create new go_association file


#----------- Match the obsolete/secondary go terms in the translation file -----
print "\n deal with obsolete / secondary go terms \n";

system ("sp_badgo_report_ec2gopart.pl");

exit;



