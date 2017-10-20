#!/private/bin/perl
#  This script creates a file that ZFIN sends to Stanford. The file is tab
#  delimited with 2 columns, each gene that has a GO annotations and its corresponding UniProt#  ID.
#  We must send the file via email to GO after running the script. A reminder
#  email, containing the path to the file, is sent to a member of ZFIN.

use DBI;

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

$dir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/GO/";
chdir "$dir";
$outFile = "gp2protein.zfin";


&gp2proteinReport();

$dbh->disconnect();

exit;

#---------------------------------------------------------------------------
#
sub gp2proteinReport()
{
    system("/bin/rm -f $outFile");
    system("/bin/rm -f gp2protein_from_go");

    $url = "http://viewvc.geneontology.org/viewvc/GO-SVN/trunk/gp2protein/gp2protein.zfin.gz";

    system("/local/bin/wget $url -O gp2protein_from_go.gz");
    system("/local/bin/gunzip gp2protein_from_go.gz");

    open (GPFROMGO, "gp2protein_from_go") or die "Cannot open gp2protein_from_go : $!\n";
    while ($line = <GPFROMGO>) {
       $versionNumber = $1 if $line =~ m/!Version:\s+([0123456789\.]+)/;
    }
    close GPFROMGO;

    $versionNumber += 0.001;    

    open (REPORT, ">$outFile") or die "cannot open report";
    printf REPORT "!Version: %.3f\n", $versionNumber;
    print REPORT "!Date: ".`/bin/date +%Y/%m/%d`;
    print REPORT "!From: ZFIN (zfin.org) \n";
    print REPORT "! \n";

#    FB: 2675
#    Show all gene marker ZdbIDs.
#    For the Gene marker with UniProt associations show the
#    uniprot id with the longest sequence. (union 1)
#    For Genes without a UniProt  (union 2)
#    or with no association (union3)
#    only show the ZdbID.
#
    my $cur = $dbh->prepare("
select
distinct m.mrkr_zdb_id,fdb_db_name,dbl.dblink_acc_num,dbl.dblink_length
from marker m,db_link dbl, foreign_db_contains fdbc, foreign_db
where m.mrkr_zdb_id = dbl.dblink_linked_recid
and dbl.dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and m.mrkr_zdb_id like 'ZDB-GENE-%'
and fdb_db_name = 'UniProtKB'
and fdbcont_fdb_db_id = fdb_db_pk_id
union
select
distinct m.mrkr_zdb_id, '' as b1, '' as b2, 0
from marker m 
join db_link dbl on m.mrkr_zdb_id = dbl.dblink_linked_recid 
join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
where m.mrkr_zdb_id like 'ZDB-GENE-%'
and not exists
(
   select
   'x'
   from db_link dbl , foreign_db_contains fdbc, foreign_db
   where dbl.dblink_linked_recid=m.mrkr_zdb_id
   and fdbc.fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id
   and m.mrkr_zdb_id like 'ZDB-GENE-%'
   and fdb_db_name = 'UniProtKB'
   and fdbcont_fdb_db_id = fdb_db_pk_id
)
union
select
distinct m.mrkr_zdb_id, '' as b3, '' as b4, 0
from marker m
where m.mrkr_zdb_id like 'ZDB-GENE-%'
and not exists
(
   select
   'x'
   from db_link dbl , foreign_db_contains fdbc
   where dbl.dblink_linked_recid=m.mrkr_zdb_id
   and fdbc.fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id
   and m.mrkr_zdb_id like 'ZDB-GENE-%'
)
order by 1
                                ;"
			   );
    $cur->execute;
    my($mrkr_id,$db_name,$acc_num,$db_length);
    $cur->bind_columns(\$mrkr_id,\$db_name,\$acc_num,\$db_length);

    my %zdbIDs = ();
    my %zdbPids = ();
    my %dbLengths = () ;
    while ($cur->fetch) {
      $zdbIDs{$mrkr_id} = 1;
      if($db_name eq "UniProtKB") {
          if (!exists $zdbPids{$mrkr_id}) {
            $zdbPids{$mrkr_id} = $acc_num;
            $dbLengths{$acc_num} = $db_length;
          } else {
            $uniprotId = $zdbPids{$mrkr_id};
            if ($dbLengths{$uniprotId} <= $db_length){
             $zdbPids{$mrkr_id} = $acc_num    ;
             $dbLengths{$acc_num} = $db_length ;
            };
          }
      }
      else{
            $zdbPids{$mrkr_id} = "NONE";
      }
    }

    foreach $k (sort keys %zdbIDs) {
      if($zdbPids{$k} eq "NONE"){
          $v1 = "";
      }
      else{
          $v1 = "UniProtKB:" . $zdbPids{$k} if exists $zdbPids{$k};
      }
      print REPORT "ZFIN:$k\t$v1\n";
    }
    close(REPORT);
}

