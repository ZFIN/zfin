#! /private/bin/perl -w
#
# check_undefined_environment.pl
#
# This script looks for the experiments with no experiment condition and send it to curator who curated the data.
# For FogBugz case 1944.


use MIME::Lite;
use DBI;

#------------------ Send Result ----------------
#
#
sub sendResult ($$$){

  my $SUBJECT=$_[0];
  my $MAILTO=$_[1];
  my $TXTFILE=$_[2];

  # Create a new multipart message:
  my $msg = new MIME::Lite
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  attach $msg
   Type     => 'text/plain',
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg->print(\*SENDMAIL);

  close(SENDMAIL);
}

sub sendEnv ($$){

  my $SUBJECT=$_[0];
  my $MAILTO=$_[1];

  # Create a new multipart message:
  my $msg = new MIME::Lite
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg->print(\*SENDMAIL);

  close(SENDMAIL);
}

#=======================================================
#
#   Main
#

#set environment variables

chdir "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/";

print "checking for undefined environments ... \n";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password) or die "Cannot connect to Informix database: $DBI::errstr\n";

$sql = "select exp_zdb_id, exp_name, exp_source_zdb_id
          from experiment, curation
         where not exists (select 'x' from experiment_condition where exp_zdb_id = expcond_exp_zdb_id) and exp_name <> '_Generic-control' and exp_source_zdb_id = cur_pub_zdb_id and cur_closed_date is not null and cur_closed_date is not null ;";

$cur = $dbh->prepare($sql);
$cur ->execute();

$cur->bind_columns(\$expZdbId,\$expName,\$pubId);

%experimentNames = ();
%pubIds = ();

while ($cur->fetch()) {
   $experimentNames{$expZdbId} = $expName;
   $pubIds{$expZdbId} = $pubId;
}

$cur->finish();

open (REPORT, ">undefinedEnvReport.txt") || die "Cannot open undefinedEnvReport.txt : $!\n";

$ctTotal = 0;
foreach $key (sort keys %pubIds) {
   $ctTotal++;
   $publicationId = $pubIds{$key};
   $expimentName = $experimentNames{$key};

   $sql = "select distinct cur_curator_zdb_id, full_name, email
             from experiment,curation, person
            where cur_pub_zdb_id = exp_source_zdb_id
              and cur_curator_zdb_id = zdb_id
              and not exists (select 'x' from experiment_condition
                               where exp_zdb_id = expcond_exp_zdb_id)
                                 and exp_name <> '_Generic-control'
                                 and cur_pub_zdb_id = exp_source_zdb_id
                                 and cur_topic in ('Antibodies', 'Expression', 'Features (Mutant)', 'Genotype', 'Phenotype', 'Transcripts', 'Transgenic Construct')
                                 and exp_zdb_id = ?
                                 and exp_source_zdb_id = ?; " ;
 
   $cur = $dbh->prepare($sql);
   $cur ->execute($key,$publicationId);  
   $cur->bind_columns(\$curatorId,\$curatorName,\$curatorEmail);
   
   %curatorNames = ();
   %curatorEmails = ();
   $ct = 0;
   while ($cur->fetch()) {
      $ct++;
      $curatorEmail =~ s/\s+//;
      &sendEnv("Undefined environment on $dbname is found: $expimentName {$key} with $publicationId", "$curatorEmail");      
      $curatorNames{$curatorId} = $curatorName;
      $curatorEmails{$curatorId} = $curatorEmail;
      last if $ct > 100;
    }   
   
   $cur->finish(); 

   print REPORT "\nMonthly report of undefined environments\n\n" if $ctTotal == 1;
   print REPORT "curator Id      \tpublication Id        \texperiment Id     \texperiment name       \tcurator email               \n" if $ctTotal == 1;
   print REPORT "----------------\t----------------------\t------------------\t----------------------\t---------------------------\n" if $ctTotal == 1;
   foreach $curatorZdbId (keys %curatorNames) {
     print REPORT "$curatorZdbId\t$publicationId\t$key\t$expimentName        \t$curatorEmails{$curatorZdbId}\n";   
   }
}


$dbh->disconnect(); 
close (REPORT);

print "\n$ctTotal undefined environments found and sent to curator(s)\n\n\n";

&sendResult("Monthly undefined environment check report: $ctTotal found on $dbname", "xshao\@zfin.org","./undefinedEnvReport.txt");
  
exit;
