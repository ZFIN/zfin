#!/private/bin/perl 
#  Script to create RefSeq/Entrez links in the database
#  This script assumes directory GenPept exists in the 
#  same folder, and file fetch-genpept.r exist in ../GenPept/.


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
system("rm -f gene*");
system("rm -f *.unl");
system("rm -f loc2*");
system("rm -f zebrafish*");
system("rm -f gene_with_multiple_linked_recid.unl");

#get new RefSeq files
&downloadRefSeqFiles();


system("/local/bin/gunzip -f *.gz");
system("../GenPept/fetch-genpept.pl");
system("/private/bin/rebol -sq ../GenPept/parse-genpept.r");

$count = 0;
$retry = 1;
#wait until the files are decompressed
while( !(
          (-e "gene_info") &&
          (-e "loc2ref") &&
          (-e "loc2acc") &&
          (-e "zebrafish.gnp") &&
          (-e "zebrafish.gbff") 
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
#      print "retry gunzip\n";
      system("/local/bin/gunzip -f *.gz");
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
	  -e "loc2acc.unl" &&
	  -e "loc2ref.unl" &&
	  -e "loc2acclen.unl" &&
	  -e "loc2UG.unl" &&
	  -e "../GenPept/prot_len_acc.unl"
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
#      print "retry parseRefSeq.pl\n";
      system("parseRefSeq.pl");
    }
    else
    {
      &emailError("Failed to make .unl files.") 
    }
  }
}

#load RefSeq links
$sys_status = system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> load_refSeq.sql");
if ($sys_status > 0)
{
    &emailError("Failure in load_refseq.pl", "<!--|VALIDATION_EMAIL_AD|-->, <!--|DB_OWNER|-->\@cs.uoregon.edu");
}


&dblinksReport();
&reportOmimDups();

&reportFile('gene_with_multiple_linked_recid.unl','RefSeq Multiples', 'tech');
&reportFile('conflict_dblink.unl','Marker/DbLink Conflicts', 'tech');
&reportFile('hs_ortho_abbrev_conflict.unl','Human Ortho Conflicts','bio');
&reportFile('mm_ortho_abbrev_conflict.unl','Mouse Ortho Conflicts','bio');

&sendReport('tech');
&sendReport('bio','<!--|VALIDATION_EMAIL_GENE|-->');

$dbh->disconnect();

exit;



sub emailError()
  {
    &writeReport("tech", $_[0]);
    
    if ($_[1]) {  &sendReport($_[1]); }
    else {  &sendReport("tech"); }
    
    exit;
  }

sub downloadRefSeqFiles()
  {
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/gene/DATA/gene2refseq.gz -O loc2ref.gz");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/gene/DATA/gene2accession.gz -O loc2acc.gz");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/gene/DATA/gene2unigene -O loc2UG");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.rna.gbff.gz -O zebrafish.gbff.gz");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.protein.gpff.gz -O zebrafish.gnp.gz");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/gene/DATA/gene_info.gz");
  }

sub writeReport()
  {
    $file = &getReportName($_[0]);
    
    open (REPORT, ">>$file") or die "cannot open $file";
    flock(REPORT,2);
    print REPORT "$_[1] \n\n";
    close (REPORT);
  }

sub openReport()
  {
    system("/bin/rm -f report*");
    system("touch report");
    system("touch report_ortho");
    dblinksReport();
  }


sub dblinksReport()
  {
    open (REPORT, ">>report") or die "can not open report";

    print REPORT "DBLINKS\n";

    my $cur = $dbh->prepare('select count(*), fdb_db_name
                             from db_link, foreign_db_contains, foreign_db
                             where dblink_fdbcont_zdb_id = fdbcont_zdb_id
                              and fdbcont_fdb_db_id = fdb_db_pk_id
                               and fdb_db_name in ("RefSeq","Entrez Gene","UniGene","Genbank","GenPept")
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


sub reportOmimDups()
  {
    open (REPORT, ">>report") or die "can not open report";

    print REPORT "OMIM dups\n";

    my $cur = $dbh->prepare('select mrkr_abbrev
                               from db_link, orthologue, marker, 
                                       foreign_db_contains, foreign_db
                              where dblink_fdbcont_zdb_id = fdbcont_zdb_id
                                and fdb_db_name = "OMIM"
                                and fdbcont_fdb_db_id = fdb_db_pk_id
                                and dblink_linked_recid = zdb_id
                                and c_gene_id = mrkr_zdb_id
                              group by mrkr_abbrev
                             having count(*) > 1;'
			   );
    $cur->execute;
    my($mrkr_abbrev);
    $cur->bind_columns(\$mrkr_abbrev);
    while ($cur->fetch)
    {
      print REPORT "$mrkr_abbrev\n";
    }
    print REPORT "\n";
    close(REPORT);
  }

sub reportFile()
  {
    $vFile = $_[0];
    $vTitle = $_[1];
    $vReport = $_[2];
    my $vText = "";
    
    if (-e $vFile)
    {
        open (FILE, "$vFile");
    
        $vText .= "\n";
        $vText .= "$vTitle\n";

        while($line = <FILE>)
        {
          $vText .= $line;
        }

        $vText .= "\n";
    
        close (FILE);
        &writeReport($vReport,$vText);
    }
    else
    {
        &writeReport($vReport,"\nCannot open $vFile\n");
    }    
  }
  

sub sendReport()
  {
    $file = &getReportName($_[0]);
    
    open(MAIL, "| $mailprog") || die "cannot open mailprog $mailprog, stopped";
    open(REPORT, $file) || die "cannot open report $file";

    if ($_[1]) {
      $email = $_[1];
      $email =~ s/\\@/@/;
      print MAIL "To: $email\n";
    }
    else {
      print MAIL "To: <!--|DB_OWNER|-->\@cs.uoregon.edu, bsprunge\@cs.uoregon.edu\n";
    }
    
    print MAIL "Subject: refseq/ortho report\n";
    while($line = <REPORT>)
    {
      print MAIL $line;
    }
    close (REPORT);
    close (MAIL);
  }

sub getReportName ()
  {
    if ($_[0] eq "tech") {  return "report";}
    elsif ($_[0] eq "bio") { return "report_ortho";}
    
    return "report";      
  }
