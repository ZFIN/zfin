#!/private/bin/perl 
#  Script to create ZGC links in the database

use DBI;
$mailprog = '/usr/lib/sendmail -t -oi -oem';

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/ZGC/";

#remove old files
system("/bin/rm -f zLib_not_found");
system("/bin/rm -f *.previous");
system("/bin/rm -f *.copy");


#make room for the new version 
system("touch StaticCloneList");
system("/bin/mv -f StaticCloneList StaticCloneList.previous");


#download the new version
system("/local/bin/wget -q 'http://zgc.nci.nih.gov/Reagents/StaticCloneList?PAGE=0&ORG=Dr&STATUS=Confirmed' -O StaticCloneList");


#Compare new vs. old... load changes
$vCloneDiff = `diff StaticCloneList StaticCloneList.previous`;


if ($vCloneDiff ne "")
{
  #Get the latest library list
  system('zLib.pl');

  #Initial state of database
  &openReport();

  #parse files
  system("parse_clones.pl");

  #delete blank lines
  $system_command = 'sed -e ' ."'". '/^$/d' ."'". ' StaticCloneList.unl > StaticCloneList.unl.copy';
  system($system_command);
  system("sed -n -f zgc.sed StaticCloneList > StaticCloneList.unl");

  
  #load links
  $sys_status = system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> zgc_pre_load_preparation.sql");
  if ($sys_status > 0)
  {
      &emailError("Failed to create ZGC tables.", "<!--|VALIDATION_EMAIL_AD|-->");
  }
  
  $sys_status = system("echo 'execute procedure p_zgc_load()' | $ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|-->");
  if ($sys_status < 0)
  {  
      system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> zgc_post_load_cleanup.sql");
      &emailError("Failed to load ZGC data.", "<!--|VALIDATION_EMAIL_AD|-->");
  }  
  system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> zgc_post_load_cleanup.sql");

  &zgcReport(); 
  &reportFile('zLib_not_found.unl','Non-ZFIN library'); 
  &reportFile('zLib_vector_not_found.unl','Vector Not Found'); 
  &reportFile('zName_mismatch.unl','Mismatched zgc genes'); 
#  &reportFile('gene_candidates.unl','Feed clones into BLAST'); 
  &reportFile('refseq_relation.unl','GenBank number is on a gene in ZFIN but the clone is unassigned by ZGC.'); 
 
  &sendReport();
}

exit;



sub emailError()
  {
    &writeReport($_[0]);
    &sendReport();
    exit;
  }

sub writeReport()
  {
    open (REPORT, ">>report") or die "cannot open report";
    print REPORT "$_[0] \n\n";
    close (REPORT);
  }

sub openReport()
  {
    system("/bin/rm -f report");
    system("touch report");
    zgcReport();
  }


sub zgcReport()
  {
    open (REPORT, ">>report") or die "can not open report";

    print REPORT "DBLINKS\n";

    my $cur = $dbh->prepare('select count(*), fdb_db_name AS BC_Links
                             from db_link, foreign_db_contains, foreign_db
                             where dblink_fdbcont_zdb_id = fdbcont_zdb_id
                               and fdb_db_name = "GenBank"
                               and fdbcont_fdb_db_id = fdb_db_pk_id
                               and dblink_acc_num[1,2] = "BC"
                             group by fdb_db_name;'
			   );
    $cur->execute;
    my($db_count, $db_name);
    $cur->bind_columns(\$db_count,\$db_name);
    while ($cur->fetch)
    {
      print REPORT "$db_name\t$db_count\n";
    }
    print REPORT "\n";
    close(REPORT);
  }



sub reportFile()
  {
    $vFile = $_[0];
    $vTitle = $_[1];
    
    open (REPORT, ">>report") or die "can not open report";
    
    print REPORT "\n";
    print REPORT "$vTitle\n";
    
    if (-e $vFile) 
    {
      open (FILE, "$vFile") or die "can not open $vFile";

      while($line = <FILE>)
      {
        print REPORT $line;
      }
      close (FILE);
    }

    print REPORT "\n";
    
    close (REPORT);
  }


sub sendReport()
  {
    open(MAIL, "| $mailprog") || die "cannot open mailprog $mailprog, stopped";
    open(REPORT, "report") || die "cannot open report";

    print MAIL "To: bsprunge\@cs.uoregon.edu, tomc\@cs.uoregon.edu\n"; 
    print MAIL "Subject: ZGC Report\n";
    while($line = <REPORT>)
    {
      print MAIL $line;
    }
    close (REPORT);
    close (MAIL);
    $dbh->disconnect();
  }

