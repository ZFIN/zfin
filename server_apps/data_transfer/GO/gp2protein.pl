#!/private/bin/perl 
#  This script creates a file that ZFIN sends to Stanford. The file is tab 
#  delimitted with 2 columns, each gene that has a GO annotations and its corresponding UniProt#  ID.
#  We must send the file via email to GO after running the script. A reminder
#  email, containing the path to the file, is sent to a member of ZFIN. 

use DBI;

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
  || emailError("Failed while connecting to <!--|DB_NAME|-->"); 

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/GO/";
chdir "$dir";
$outFile = "gp2protein.zfin";


&gp2proteinReport();

$dbh->disconnect();
      
###undef %validPids;
exit;

#---------------------------------------------------------------------------
#
sub gp2proteinReport()
{
    system("/bin/rm -f $outFile");
    open (REPORT, ">$outFile") or die "cannot open report";
    ###open (ERRREPORT, ">invalid_id_gp2protein.zfin") or die "cannot open invalid_id_gp2protein.zfin";
    print REPORT "!Version: \$"."Revision\$ \n";
    print REPORT "!Date: \$"."Date\$ \n";
    print REPORT "!From: ZFIN (zfin.org) \n";
    print REPORT "! \n";

    my $cur = $dbh->prepare('select distinct mrkr_zdb_id,fdbcont_fdb_db_name,dblink_acc_num
                               from marker,db_link, foreign_db_contains
                              where mrkr_zdb_id = dblink_linked_recid
                                and dblink_fdbcont_zdb_id = fdbcont_zdb_id
                                and mrkr_zdb_id like "ZDB-GENE-%"
                                and fdbcont_fdb_db_name in ("UniProt","RefSeq","GenPept");'
			   );
    $cur->execute;
    my($mrkr_id,$db_name,$acc_num);
    $cur->bind_columns(\$mrkr_id,\$db_name,\$acc_num);
    
    my %zdbIDs = ();
    my %zdbPids = ();
    my %zdbRefSeqs = ();
    my %zdbGenPepts = ();
    ###my %invalidIDs = ();
    while ($cur->fetch) {
      $zdbIDs{$mrkr_id} = 1;
      if($db_name eq "UniProt") {
   ### if(exists $validPids{$acc_num}) {
	  if (!exists $zdbPids{$mrkr_id}) {
	    $zdbPids{$mrkr_id} = "UniProt:$acc_num";
	  } else {
            $zdbPids{$mrkr_id} = "$zdbPids{$mrkr_id};UniProt:$acc_num";
	  }
   ### } else {
   ###     $invalidIDs{$acc_num} = 1;
   ### }
      } elsif($db_name eq "RefSeq" && $acc_num =~ m/^NP_\d+$/) {
          if (!exists $zdbRefSeqs{$mrkr_id}) {  
            $zdbRefSeqs{$mrkr_id} = "NCBI_NP:$acc_num";
          } else {
            $zdbRefSeqs{$mrkr_id} = "$zdbRefSeqs{$mrkr_id};NCBI_NP:$acc_num";
          }
      } elsif($db_name eq "GenPept") {
          if (!exists $zdbGenPepts{$mrkr_id}) {  
            $zdbGenPepts{$mrkr_id} = "NCBI_GP:$acc_num";
          } else {
            $zdbGenPepts{$mrkr_id} = "$zdbGenPepts{$mrkr_id};NCBI_GP:$acc_num";
          }
      }
    }
    
    foreach $k (sort keys %zdbIDs) {
      $v1 = $zdbPids{$k}.";" if exists $zdbPids{$k};
      $v2 = $zdbRefSeqs{$k}.";" if exists $zdbRefSeqs{$k};
      $v3 = $zdbGenPepts{$k} if exists $zdbGenPepts{$k};
      print REPORT "ZFIN:$k\t$v1$v2$v3\n";     
    }
    
    close(REPORT);
}
