#!/local/bin/perl 
#  Script to create RefSeq/LocusLink links in the database

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

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/RefSeq/";

#Initial state of database
&openReport();

#remove old RefSeq files
system("rm LL.out*");
system("rm *.unl");
system("rm loc2*");

#get new RefSeq files
&downloadLocusLinkFiles();

$count = 0;
$retry = 1;
#verify the files are downloaded
while( !(
          (-e "LL.out_dr.gz") &&
          (-e "LL.out_hs.gz") && 
          (-e "LL.out_mm.gz") && 
          (-e "loc2ref") &&
          (-e "loc2UG")
        ) 
     )
{
  $count++;
  if ($count > 10)
  {
    if ($retry) 
    {
      $count = 0;
      $retry = 0;
      &downloadLocusLinkFiles();
    }
    else
    {
      &emailError("Failed to download LocusLink files.") 
    }
  }
}

#decompress files
system("gunzip *.gz");

$count = 0;
$retry = 1;
#wait until the files are decompressed
while( !(
          (-e "LL.out_dr") &&
          (-e "LL.out_hs") && 
          (-e "LL.out_mm") 
        ) 
     ) 
{
  $count++;
  if ($count > 10)
  {
    if ($retry) 
    {
      $count = 0;
      $retry = 0;
      print "retry gunzip\n";
      system("gunzip -f *.gz");
    }
    else
    {
      &emailError("Gunzip failed to extract the necessary LL files.") 
    }
  }
}

#parse RefSeq files into informix load files
system("parseRefSeq.pl");

$count = 0;
$retry = 1;
#wait until load files are made
while( !( 
          -e "ll_id.unl" && 
          -e "ll_hs_id.unl" && 
	  -e "ll_mm_id.unl" &&
	  -e "refseq_acc.unl" &&
	  -e "loc2UG.unl" 
	 )
     )
{
  $count++;
  if ($count > 10)
  {
    if ($retry) 
    {
      $count = 0;
      $retry = 0;
      print "retry parseRefSeq.pl\n";
      system("parseRefSeq.pl");
    }
    else
    {
      &emailError("Failed to make .unl files.") 
    }
  }
}

#load RefSeq links
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> load_refSeq.sql");

&dblinksReport();
&sendReport();

exit;



sub emailError()
  {
    &writeReport($_[0]);
    &sendReport();
    exit;
  }

sub downloadLocusLinkFiles()
  {
    system("wget ftp://ftp.ncbi.nih.gov/refseq/LocusLink/loc2ref -O loc2ref");
    system("wget ftp://ftp.ncbi.nih.gov/refseq/LocusLink/loc2UG -O loc2UG");
    system("wget ftp://ftp.ncbi.nih.gov/refseq/LocusLink/LL.out_dr.gz");
    system("wget ftp://ftp.ncbi.nih.gov/refseq/LocusLink/LL.out_hs.gz");
    system("wget ftp://ftp.ncbi.nih.gov/refseq/LocusLink/LL.out_mm.gz");
  }

sub writeReport()
  {
    open (REPORT, ">>report") or die "cannot open report";
    print REPORT "$_[0] \n\n";
    close (REPORT);
  }

sub openReport()
  {
    system("/bin/rm report");
    system("touch report");
    dblinksReport();
  }


sub dblinksReport()
  {
    open (REPORT, ">>report") or die "can not open report";

    print REPORT "DBLINKS\n";

    my $cur = $dbh->prepare('select count(*), db_name
                             from db_link
                             where db_name = "RefSeq"
                                or db_name = "LocusLink"
                                or db_name = "UniGENE"
                                or db_name = "OMIM"
                             group by db_name;'
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

sub sendReport()
  {
    open(MAIL, "| $mailprog") || die "cannot open mailprog $mailprog, stopped";
    open(REPORT, "report") || die "cannot open report";

    print MAIL "To: bsprunge\@cs.uoregon.edu\n";
    print MAIL "Subject: (refseq) report\n";
    while($line = <REPORT>)
    {
      print MAIL $line;
    }
    close (REPORT);
    close (MAIL);
    $dbh->disconnect();
  }
