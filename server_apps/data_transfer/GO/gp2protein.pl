#!/private/bin/perl
#  This script creates a file that ZFIN sends to Stanford. The file is tab
#  delimitted with 2 columns, each gene that has a GO annotations and its corresponding UniProt#  ID.
#  We must send the file via email to GO after running the script. A reminder
#  email, containing the path to the file, is sent to a member of ZFIN.

use DBI;

# fetch previous revision

use lib './';
use FetchCVSRevision;

my $url = "http://cvsweb.geneontology.org/cgi-bin/cvsweb.cgi/go/gp2protein/gp2protein.zfin.gz?rev=";
my $rev = FetchCVSRevision->fcvsr_get($url);
if (! $rev) {$rev = "unknown";} else {$rev += 0.001;}

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
    printf REPORT "!Version: %.3f\n",$rev;
    print REPORT "!Date: ".`/usr/bin/date +%Y/%m/%d`;
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
    my $cur = $dbh->prepare('
select
distinct m.mrkr_zdb_id,fdb_db_name,dbl.dblink_acc_num,dbl.dblink_length
from marker m,db_link dbl, foreign_db_contains fdbc, foreign_db
where m.mrkr_zdb_id = dbl.dblink_linked_recid
and dbl.dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and m.mrkr_zdb_id like "ZDB-GENE-%"
and fdb_db_name = "UniProtKB"
and fdbcont_fdb_db_id = fdb_db_pk_id
union
select
distinct m.mrkr_zdb_id, "","",0
from marker m join db_link dbl on m.mrkr_zdb_id= dbl.dblink_linked_recid join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
where m.mrkr_zdb_id like "ZDB-GENE-%"
and not exists
(
   select
   "t"
   from db_link dbl , foreign_db_contains fdbc, foreign_db
   where dbl.dblink_linked_recid=m.mrkr_zdb_id
   and fdbc.fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id
   and m.mrkr_zdb_id like "ZDB-GENE-%"
   and fdb_db_name = "UniProtKB"
   and fdbcont_fdb_db_id = fdb_db_pk_id
)
union
select
distinct m.mrkr_zdb_id,"","",0
from marker m
where m.mrkr_zdb_id like "ZDB-GENE-%"
and not exists
(
   select
   "t"
   from db_link dbl , foreign_db_contains fdbc
   where dbl.dblink_linked_recid=m.mrkr_zdb_id
   and fdbc.fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id
   and m.mrkr_zdb_id like "ZDB-GENE-%"
)
order by m.mrkr_zdb_id
                                ;'
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

