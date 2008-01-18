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
    
    system("wget -q ftp://ftp.ebi.ac.uk/pub/contrib/dbarrell/zfin.dat -O zfin.dat");
    open (INP, "zfin.dat") || die "Can't open zfin.dat : $!\n";
    @lines=<INP>;
    close(INP);

    %lengths = ();
    $ct1 = 0;
    foreach $line (@lines) {
      $ct1++;
      if ($line =~ m/^ID\s+(\w.+)_DANRE.*\s+(\d+)\sAA/) {
        $id = $1;
        $len = $2;
        $lengths{$id};
      } 
     } 
    
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
                                and fdbcont_fdb_db_name = "UniProt";'
			   );
    $cur->execute;
    my($mrkr_id,$db_name,$acc_num);
    $cur->bind_columns(\$mrkr_id,\$db_name,\$acc_num);
    
    my %zdbIDs = ();
    my %zdbPids = ();
    ###my %invalidIDs = ();
    while ($cur->fetch) {
      $zdbIDs{$mrkr_id} = 1;
      if($db_name eq "UniProt") {
   ### if(exists $validPids{$acc_num}) {
	  if (!exists $zdbPids{$mrkr_id}) {
	    $zdbPids{$mrkr_id} = $acc_num;
	  } else {
	    $uniprotId = $zdbPids{$mrkr_id};
            $zdbPids{$mrkr_id} = $acc_num if ($lengths{$uniprotId} <= $lengths{$acc_num});
	  }
   ### } else {
   ###     $invalidIDs{$acc_num} = 1;
   ### }
      }
    }
    
    foreach $k (sort keys %zdbIDs) {
      $v1 = "UniProt:" . $zdbPids{$k} if exists $zdbPids{$k};
      print REPORT "ZFIN:$k\t$v1\n";     
    }
    
    close(REPORT);
}
