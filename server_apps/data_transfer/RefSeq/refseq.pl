#!/local/bin/perl 
#  Script to create RefSeq/LocusLink links in the database
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
system("rm -f LL*");
system("rm -f *.unl");
system("rm -f loc2*");
system("rm -f zebrafish*");
system("rm -f gene_with_multiple_linked_recid.unl");

#get new RefSeq files
&downloadLocusLinkFiles();


system("/local/bin/gunzip -f *.gz");
#system("/private/bin/rebol -sq ../GenPept/fetch-genpept.r");

$count = 0;
$retry = 1;
#wait until the files are decompressed
while( !(
          (-e "LL.out_dr") &&
          (-e "LL.out_hs") && 
          (-e "LL.out_mm") &&
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
system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> load_refSeq.sql");

&dblinksReport();
&reportOmimDups();

&reportFile('gene_with_multiple_linked_recid.unl','RefSeq Multiples');
&reportFile('ortho_with_multiple_acc_num.unl','Orthologue Multiples')
&reportFile('conflict_dblink.unl','Marker/DbLink Conflicts');

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
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/LocusLink/loc2ref -O loc2ref");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/LocusLink/loc2acc -O loc2acc");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/LocusLink/loc2UG -O loc2UG");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.rna.gbff.gz -O zebrafish.gbff.gz");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.protein.gpff.gz -O zebrafish.gnp.gz");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/LocusLink/LL.out_dr.gz");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/LocusLink/LL.out_hs.gz");
    system("/local/bin/wget -q ftp://ftp.ncbi.nih.gov/refseq/LocusLink/LL.out_mm.gz");
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
    dblinksReport();
  }


sub dblinksReport()
  {
    open (REPORT, ">>report") or die "can not open report";

    print REPORT "DBLINKS\n";

    my $cur = $dbh->prepare('select count(*), fdbcont_fdb_db_name
                             from db_link, foreign_db_contains
                             where dblink_fdbcont_zdb_id = fdbcont_zdb_id
                               and fdbcont_fdb_db_name in ("RefSeq","LocusLink","UniGene","OMIM","GenBank","GenPept")
                             group by fdbcont_fdb_db_name;'
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
                               from db_link, orthologue, marker, foreign_db_contains
                              where dblink_fdbcont_zdb_id = fdbcont_zdb_id
                                and fdbcont_fdb_db_name = "OMIM"
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
    
    open (REPORT, ">>report") or die "can not open report";
    open (FILE, "$vFile") or die "can not open $vFile";
    
    print REPORT "\n";
    print REPORT "$vTitle\n";

    while($line = <FILE>)
    {
      print REPORT $line;
    }

    print REPORT "\n";
    
    close (FILE);
    close (REPORT);
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
