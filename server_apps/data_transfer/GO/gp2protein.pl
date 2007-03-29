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

exit;

#-------------------------------------------------------------
#
sub gp2proteinReport()
{
    system("/bin/rm -f $outFile");
    open (REPORT, ">$outFile") or die "cannot open report";
    print REPORT "!Version: \$"."Revision\$ \n";
    print REPORT "!Date: \$"."Date\$ \n";
    print REPORT "!From: ZFIN (zfin.org) \n";
    print REPORT "! \n";

    my $cur = $dbh->prepare('select distinct mrkr_zdb_id,
                                    dblink_acc_num
                             from   marker,db_link, foreign_db_contains
                             where  mrkr_zdb_id = dblink_linked_recid
                               and  dblink_fdbcont_zdb_id = fdbcont_zdb_id 
                               and  fdbcont_fdb_db_name = "UniProt"
                             order by mrkr_zdb_id;'
			   );
    $cur->execute;
    my($mrkr_id, $acc_num);
    $cur->bind_columns(\$mrkr_id,\$acc_num);
    my $gene = '';
    my $start = 1;
    while ($cur->fetch) {
	if ($gene eq $mrkr_id) {
	    print REPORT ";UniProt:$acc_num";
	}
	else {
	    print REPORT "\n" if !$start;
	    print REPORT "ZFIN:$mrkr_id\tUniProt:$acc_num";
	    $gene=$mrkr_id;
	}
	$start = 0 if $start;
    }
    print REPORT "\n";
    close(REPORT);
}



