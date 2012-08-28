#!/private/bin/perl
# entrezGene.pl
# 


use DBI;
use MIME::Lite;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/statsEntrezGeneLoad");
system("/bin/touch <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/statsEntrezGeneLoad");

$statsfile = "<!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/statsEntrezGeneLoad";

#--------------------------- record counts before loading starts ----------------------------
$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "RefSeq";';

$numDblinksRefSeqBefore = countData($sql);

$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "Gene";';

$numDblinksGeneBefore = countData($sql); 


$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "UniGene";';

$numDblinksUniGeneBefore = countData($sql); 

$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenPept";';

$numDblinksGenPeptBefore = countData($sql); 

$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where (dblink_length is null or dblink_length = "" or dblink_length = "0") 
           and dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "RefSeq";';
           
$numDblinksMissingLenRefSeqBefore = countData($sql);     

$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where (dblink_length is null or dblink_length = "" or dblink_length = "0") 
           and dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenPept";';
           
$numDblinksMissingLenGenPeptBefore = countData($sql);  

open STATS, '>', $statsfile or die "can not open report statsEntrezGeneLoad" ;

print STATS "db_link records from EntrezGene load     \t";
print STATS "before load\t";
print STATS "after load\t";
print STATS "percentage change\n";
print STATS "----------------------------------------\t-----------\t-----------\t-------------------------\n";
close (STATS);

chdir("<!--|SOURCEROOT|-->/server_apps/data_transfer/EntrezGene") or &logError("cannot chdir to SOURCEROOT/server_apps/data_transfer/EntrezGene");


system("bin/rm -f report");
system("/bin/rm -f errReport");

print "\n\nStarted ....\n\n\n";

system("gmake run_commit > report 2>errReport");


#--------------------------- record counts after loading starts ----------------------------
$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "RefSeq";';

$numDblinksRefSeqAfter = countData($sql);

$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "Gene";';

$numDblinksGeneAfter = countData($sql); 


$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "UniGene";';

$numDblinksUniGeneAfter = countData($sql); 

$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenPept";';

$numDblinksGenPeptAfter = countData($sql); 

$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where (dblink_length is null or dblink_length = "" or dblink_length = "0") 
           and dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "RefSeq";';
           
$numDblinksMissingLenRefSeqAfter = countData($sql);     

$sql = 'select distinct dblink_zdb_id
          from db_link, foreign_db_contains, foreign_db
         where (dblink_length is null or dblink_length = "" or dblink_length = "0") 
           and dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenPept";';
           
$numDblinksMissingLenGenPeptAfter = countData($sql);  


open STATS, '>>', "$statsfile" or die "can not open report statsEntrezGeneLoad again" ;

print STATS "RefSeq                                  \t";
print STATS "$numDblinksRefSeqBefore   \t";
print STATS "$numDblinksRefSeqAfter   \t";
printf STATS "%.2f\n", ($numDblinksRefSeqAfter - $numDblinksRefSeqBefore) / $numDblinksRefSeqBefore * 100 if ($numDblinksRefSeqBefore > 0);
 
print STATS "Gene                                    \t";
print STATS "$numDblinksGeneBefore        \t";
print STATS "$numDblinksGeneAfter       \t";
printf STATS "%.2f\n", ($numDblinksGeneAfter - $numDblinksGeneBefore) / $numDblinksGeneBefore * 100 if ($numDblinksGeneBefore > 0);


print STATS "UniGene                                 \t";
print STATS "$numDblinksUniGeneBefore        \t";
print STATS "$numDblinksUniGeneAfter       \t";
printf STATS "%.2f\n", ($numDblinksUniGeneAfter - $numDblinksUniGeneBefore) / $numDblinksUniGeneBefore * 100 if ($numDblinksUniGeneBefore > 0);

print STATS "GenPept                                 \t";
print STATS "$numDblinksGenPeptBefore   \t";
print STATS "$numDblinksGenPeptAfter   \t";
printf STATS "%.2f\n", ($numDblinksGenPeptAfter - $numDblinksGenPeptBefore) / $numDblinksGenPeptBefore * 100 if ($numDblinksGenPeptBefore > 0);

print STATS "RefSeq missing len                      \t";
print STATS "$numDblinksMissingLenRefSeqBefore       \t";
print STATS "$numDblinksMissingLenRefSeqAfter       \t";
printf STATS "%.2f\n", ($numDblinksMissingLenRefSeqAfter - $numDblinksMissingLenRefSeqBefore) / $numDblinksMissingLenRefSeqBefore * 100 if ($numDblinksMissingLenRefSeqBefore > 0);

print STATS "GenPept missing len                     \t";
print STATS "$numDblinksMissingLenGenPeptBefore        \t";
print STATS "$numDblinksMissingLenGenPeptAfter       \t";
printf STATS "%.2f\n", ($numDblinksMissingLenGenPeptAfter - $numDblinksMissingLenGenPeptBefore) / $numDblinksMissingLenGenPeptBefore * 100 if ($numDblinksMissingLenGenPeptBefore > 0);



close (STATS);

&sendMail("Auto: entrezGene.pl : ","xshao\@zfin.org","stats","$statsfile");
&sendMail("Auto: entrezGene.pl : ","xshao\@zfin.org","report","report");
&sendMail("Auto: entrezGene.pl : ","xshao\@zfin.org","errReport","errReport");

print "\nDone\n\n";

exit;

sub sendMail($) {

    my $SUBJECT=$_[0] .": " .$_[2];
    my $MAILTO=$_[1];
    my $TXTFILE=$_[3]; 
    
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

sub logError() {
    my $line = $_;
    print "\nError\n";
    &sendMail("ERROR with entrezGene.pl","xshao\@zfin.org","$line","$statsfile");
    die "failed because $line";

}

sub countData() {

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


