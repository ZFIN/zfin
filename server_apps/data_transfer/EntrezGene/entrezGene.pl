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


####system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/*.gz");

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/Danio_rerio.gene_info.gz");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/gene2accession.gz");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/zebrafish.protein.faa.gz");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/zebrafish.rna.fna.gz");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/release*.accession2geneid.gz");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/protein.fa.gz");


###system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/*.tab");


system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/entrezid_GBnt_GBaa_GBdna.tab");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/entrezid_refseq_refpept.tab");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/entrezid_ugc.tab");

###system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/*.unl");

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/entrezid_zdbid_lg_type.unl");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/refseq_len.unl");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/genpeptRP_len.unl");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/genpept_len.unl");
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/EntrezGenPept_acc.unl");

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/GenPept.fasta");

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/gene2unigene");

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/statsEntrezGeneLoad");

$statsfile = "<!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/statsEntrezGeneLoad";

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/checkDupGENEs.txt");

$entrezGeneDupAccs = "<!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/checkDupGENEs.txt" ;

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/refSeqGeneListLost");

$refSeqGeneListLost = "<!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/refSeqGeneListLost";

system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/refSeqGeneListNewlyAdded");

$refSeqGeneListNewlyAdded = "<!--|ROOT_PATH|-->/server_apps/data_transfer/EntrezGene/refSeqGeneListNewlyAdded";

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
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenBank";';

$numDblinksGenBankBefore = countData($sql);

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

#--------------------------------------------------------------------------------------------------

$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "RefSeq"
           and dblink_acc_num like "NM_%"
           and dblink_linked_recid like "ZDB-GENE-%";';

$numGenesRefSeqNMBefore = countData($sql);


$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "RefSeq"
           and dblink_acc_num like "NP_%"
           and dblink_linked_recid like "ZDB-GENE-%";';

$numGenesRefSeqNPBefore = countData($sql);


$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "Gene"
           and dblink_linked_recid like "ZDB-GENE-%";';

$numGenesEntrezGeneBefore = countData($sql);


$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenBank"
           and dblink_linked_recid like "ZDB-GENE-%";';

$numGenesGenBankBefore = countData($sql);

#--------------------------------------------------------------------------------------------------

### open a handle on the db
$handle = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die ("Failed while connecting to <!--|DB_NAME|-->");


$refSeq_gene_list_query_before = 'select m.mrkr_zdb_id,  m.mrkr_abbrev 
                      from marker m where  m.mrkr_zdb_id like "ZDB-GENE-%" 
                       and exists (select "t" from db_link, foreign_db_contains, foreign_db 
                                             where dblink_linked_recid = m.mrkr_zdb_id 
                                               and dblink_fdbcont_zdb_id = fdbcont_zdb_id 
                                               and fdbcont_fdb_db_id = fdb_db_pk_id and fdb_db_name = "RefSeq");';


# execute the query

$cur = $handle->prepare($refSeq_gene_list_query_before);

$cur->execute;

my($refSeq_gene_id,$refSeq_gene_abbrev);

$cur->bind_columns(\$refSeq_gene_id,\$refSeq_gene_abbrev);

%resSeq_genes_before = ();
$totalRefSeqGenesBefore = 0;
while ($cur->fetch) {
   $totalRefSeqGenesBefore++;
   $resSeq_genes_before{$refSeq_gene_id} = $refSeq_gene_abbrev;
}

$handle->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";




open STATS, '>', $statsfile or die "can not open statsEntrezGeneLoad" ;

print STATS "number of db_link records               \t";
print STATS "before load\t";
print STATS "after load\t";
print STATS "percentage change\n";
print STATS "----------------------------------------\t-----------\t-----------\t-------------------------\n";
close (STATS);

chdir("<!--|SOURCEROOT|-->/server_apps/data_transfer/EntrezGene") or &logError("cannot chdir to SOURCEROOT/server_apps/data_transfer/EntrezGene");

$dir = "<!--|ROOT_PATH|-->";

@dirPieces = split(/www_homes/,$dir);

$dbname = $dirPieces[1];
$dbname =~ s/\///;

$dbname = "kinetix" if ($dbname eq "zfin.org"); 

print $dbname;
print "\n\n";


system("bin/rm -f log1");

system("/bin/rm -f log2");

print "\n\nStarted EntrezGene load....\n\n\n";

$dbname = "kinetix" if ($dbname eq "zfin.org");

$cmd = "load_entrez_wrapper.sh $dbname commit > log1 2>log2";

print $cmd;
print "\n\n";

system("$cmd");

## system("./cleanUpSecondaryGeneAccessions.pl");



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
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenBank";';

$numDblinksGenBankAfter = countData($sql);

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


#--------------------------------------------------------------------------------------------------

$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "RefSeq"
           and dblink_acc_num like "NM_%"
           and dblink_linked_recid like "ZDB-GENE-%";';

$numGenesRefSeqNMAfter = countData($sql);


$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "RefSeq"
           and dblink_acc_num like "NP_%"
           and dblink_linked_recid like "ZDB-GENE-%";';

$numGenesRefSeqNPAfter = countData($sql);


$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "Gene"
           and dblink_linked_recid like "ZDB-GENE-%";';

$numGenesEntrezGeneAfter = countData($sql);

$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenBank"
           and dblink_linked_recid like "ZDB-GENE-%";';

$numGenesGenBankAfter = countData($sql);

open STATS, '>>', "$statsfile" or die "can not open statsEntrezGeneLoad again" ;

print STATS "RefSeq                                  \t";
print STATS "$numDblinksRefSeqBefore   \t";
print STATS "$numDblinksRefSeqAfter   \t";
printf STATS "%.2f\n", ($numDblinksRefSeqAfter - $numDblinksRefSeqBefore) / $numDblinksRefSeqBefore * 100 if ($numDblinksRefSeqBefore > 0);
 
print STATS "Entrez Gene                             \t";
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

print STATS "GenBank                                 \t";
print STATS "$numDblinksGenBankBefore  \t";
print STATS "$numDblinksGenBankAfter  \t";
printf STATS "%.2f\n", ($numDblinksGenBankAfter - $numDblinksGenBankBefore) / $numDblinksGenBankBefore * 100 if ($numDblinksGenBankBefore > 0);

print STATS "RefSeq missing len                      \t";
print STATS "$numDblinksMissingLenRefSeqBefore       \t";
print STATS "$numDblinksMissingLenRefSeqAfter       \t";
printf STATS "%.2f\n", ($numDblinksMissingLenRefSeqAfter - $numDblinksMissingLenRefSeqBefore) / $numDblinksMissingLenRefSeqBefore * 100 if ($numDblinksMissingLenRefSeqBefore > 0);

print STATS "GenPept missing len                     \t";
print STATS "$numDblinksMissingLenGenPeptBefore        \t";
print STATS "$numDblinksMissingLenGenPeptAfter       \t";
printf STATS "%.2f\n", ($numDblinksMissingLenGenPeptAfter - $numDblinksMissingLenGenPeptBefore) / $numDblinksMissingLenGenPeptBefore * 100 if ($numDblinksMissingLenGenPeptBefore > 0);

print STATS "\n\n\n";

print STATS "number of genes                          \t";
print STATS "before load\t";
print STATS "after load\t";
print STATS "percentage change\n";
print STATS "----------------------------------------\t-----------\t-----------\t-------------------------\n";

print STATS "with RefSeq NM_                         \t";
print STATS "$numGenesRefSeqNMBefore   \t";
print STATS "$numGenesRefSeqNMAfter   \t";
printf STATS "%.2f\n", ($numGenesRefSeqNMAfter - $numGenesRefSeqNMBefore) / $numGenesRefSeqNMBefore * 100 if ($numDblinksRefSeqBefore > 0);

print STATS "with RefSeq NP_                         \t";
print STATS "$numGenesRefSeqNPBefore   \t";
print STATS "$numGenesRefSeqNPAfter   \t";
printf STATS "%.2f\n", ($numGenesRefSeqNPAfter - $numGenesRefSeqNPBefore) / $numGenesRefSeqNPBefore * 100 if ($numGenesRefSeqNPBefore > 0);
 
print STATS "with Entrez Gene                        \t";
print STATS "$numGenesEntrezGeneBefore        \t";
print STATS "$numGenesEntrezGeneAfter       \t";
printf STATS "%.2f\n", ($numGenesEntrezGeneAfter - $numGenesEntrezGeneBefore) / $numGenesEntrezGeneBefore * 100 if ($numGenesEntrezGeneBefore > 0);

print STATS "with GenBank                            \t";
print STATS "$numGenesGenBankBefore        \t";
print STATS "$numGenesGenBankAfter       \t";
printf STATS "%.2f\n", ($numGenesGenBankAfter - $numGenesGenBankBefore) / $numGenesGenBankBefore * 100 if ($numGenesGenBankBefore > 0);

close (STATS);



### open a handle on the db
$handle = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die ("Failed while connecting to <!--|DB_NAME|-->");


$refSeq_gene_list_query_after = 'select m.mrkr_zdb_id,  m.mrkr_abbrev 
                      from marker m where  m.mrkr_zdb_id like "ZDB-GENE-%" 
                       and exists (select "t" from db_link, foreign_db_contains, foreign_db 
                                             where dblink_linked_recid = m.mrkr_zdb_id 
                                               and dblink_fdbcont_zdb_id = fdbcont_zdb_id 
                                               and fdbcont_fdb_db_id = fdb_db_pk_id and fdb_db_name = "RefSeq");';


# execute the query

$cur = $handle->prepare($refSeq_gene_list_query_after);

$cur->execute;

my($refSeq_gene_id_new,$refSeq_gene_abbrev_new);

$cur->bind_columns(\$refSeq_gene_id_new,\$refSeq_gene_abbrev_new);

%resSeq_genes_after = ();
$totalRefSeqGenesAfter = 0;
while ($cur->fetch) {
   $totalRefSeqGenesAfter++;
   $resSeq_genes_after{$refSeq_gene_id_new} = $refSeq_gene_abbrev_new;
}

$handle->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";


print "\ntotalRefSeqGenesBefore: $totalRefSeqGenesBefore\ttotalRefSeqGenesAfter:  $totalRefSeqGenesAfter\n\n";

open LOSTREFSEQGENES, '>', $refSeqGeneListLost or die "can not open refSeqGeneListLost" ;

print LOSTREFSEQGENES "\ngenes used to have RefSeq but don't any longer after the load\n";
print LOSTREFSEQGENES "-------------------------------------------------------------------------------------------------\n";


@keysBefore = sort { lc($resSeq_genes_before{$a}) cmp lc($resSeq_genes_before{$b}) } keys %resSeq_genes_before;
$numLostGenes = 0;
foreach $key (@keysBefore) {
  $value = $resSeq_genes_before{$key};
  if(!exists $resSeq_genes_after{$key}) {
      print LOSTREFSEQGENES "$key\t$value\n";
      $numLostGenes++;
  }
}

print LOSTREFSEQGENES "-------------------------------------------------------------------------------------------------\n";
print LOSTREFSEQGENES "total:$numLostGenes \n";

close (LOSTREFSEQGENES);


open NEWREFSEQGENES, '>', $refSeqGeneListNewlyAdded or die "can not open refSeqGeneListNewlyAdded" ;


print NEWREFSEQGENES "\ngenes used to NOT have RefSeq but now have RefSeq after the load\n";
print NEWREFSEQGENES "-------------------------------------------------------------------------------------------------\n";


@keysAfter = sort { lc($resSeq_genes_after{$a}) cmp lc($resSeq_genes_after{$b}) } keys %resSeq_genes_after;
$numGainedGenes = 0;
foreach $key (@keysAfter) {
  $value = $resSeq_genes_after{$key};
  if(!exists $resSeq_genes_before{$key}) {
      print NEWREFSEQGENES "$key\t$value\n";
      $numGainedGenes++;
  }
}

print NEWREFSEQGENES "-------------------------------------------------------------------------------------------------\n";
print NEWREFSEQGENES "total:$numGainedGenes \n";


close (NEWREFSEQGENES);


&sendMail("Auto from $dbname: entrezGene.pl : ","<!--|SWISSPROT_EMAIL_REPORT|-->","stats","$statsfile");
&sendMail("Auto from $dbname: entrezGene.pl : ","<!--|SWISSPROT_EMAIL_ERR|-->","log1","log1");
&sendMail("Auto from $dbname: entrezGene.pl : ","<!--|SWISSPROT_EMAIL_ERR|-->","log2","log2");

&sendMail("Auto from $dbname: entrezGene.pl : ","<!--|SWISSPROT_EMAIL_REPORT|-->","genes lost association with RefSeq","$refSeqGeneListLost");

&sendMail("Auto from $dbname: entrezGene.pl : ","<!--|SWISSPROT_EMAIL_REPORT|-->","genes newly associated with RefSeq","$refSeqGeneListNewlyAdded");
&sendMail("Auto from $dbname: entrezGene.pl : ","<!--|PATO_EMAIL_CURATOR|-->","genes with more than one accession number","$entrezGeneDupAccs");
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







