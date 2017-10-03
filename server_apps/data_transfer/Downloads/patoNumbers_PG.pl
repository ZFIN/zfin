#!/private/bin/perl -w
#  patoNumbers.pl
#-----------------------------------------------------------------------
# The script is called from the general script to create data files for public download.
#
# For FB case 9069, phenotype for ZF genes with Human orthology seems missing data
# Since there are some queries in patoNumbers.sql not working, use this Perl script to 
# do some text processing would fix the problem.

use DBI;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

# call patoNumbers.sql to prepare some download files and some pre-processed files
system("psql -d <!--|DB_NAME|--> -a -f patoNumbers_PG.sql");

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password) 
    or die "Cannot connect to database: $DBI::errstr\n";

# get the ZDB Gene Id/NCBI Gene Id pairs
$cur = $dbh->prepare("select distinct dblink_linked_recid, dblink_acc_num from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1';");
$cur->execute();
my ($zdbGeneId, $NCBIgeneId);
$cur->bind_columns(\$zdbGeneId,\$NCBIgeneId);

%ZDBgeneIdNCBIgeneIds = ();
$ctGeneIds = 0;
while ($cur->fetch()) {
   $ZDBgeneIdNCBIgeneIds{$zdbGeneId} = $NCBIgeneId;
   $ctGeneIds++;
}

# get the ZDB ortholog Id/NCBI Gene Id of the human ortholog pairs
$cur = $dbh->prepare("select distinct ortho_zdb_id, oef_accession_number from ortholog,ortholog_external_reference where oef_fdbcont_Zdb_id = 'ZDB-FDBCONT-040412-27' and ortho_zdb_id = oef_ortho_zdb_id;");
$cur->execute();
my ($ZDBorthologId, $humanOrthoNCBIgeneId);
$cur->bind_columns(\$ZDBorthologId,\$humanOrthoNCBIgeneId);

$ctHumanGeneIds = 0;
%ZDBorthologIdhumanOrthoNCBIgeneIds = ();
while ($cur->fetch()) {
   $ZDBorthologIdhumanOrthoNCBIgeneIds{$ZDBorthologId} = $humanOrthoNCBIgeneId;
   $ctHumanGeneIds++;
}

$cur->finish(); 

$dbh->disconnect(); 

$prephenofile = "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/preprocessed_pheno.txt";
$phenofile = "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/pheno_fish.txt";



open (PREPHENO, "$prephenofile") ||  die "Cannot open $prephenofile : $!\n";
open (PHENO, ">$phenofile") || die "can not open $phenofile: $!\n";

@prephenoLines = <PREPHENO>;

$ctTotalPrephenoLines = 0;
foreach $prephenoLine (@prephenoLines) {
   $ctTotalPrephenoLines++;
   chop($prephenoLine);
   @prephenoFields = split(/\|/, $prephenoLine);
   $zdbgeneId = $prephenoFields[1];
   if (exists($ZDBgeneIdNCBIgeneIds{$zdbgeneId})) {
      $NCBIzfGeneId = $ZDBgeneIdNCBIgeneIds{$zdbgeneId};
   } else {
      $NCBIzfGeneId = " ";
   }
   $humanOrthoZdbId = $prephenoFields[0];
   if (exists($ZDBorthologIdhumanOrthoNCBIgeneIds{$humanOrthoZdbId})) {   
       $NCBIhumanGeneId = $ZDBorthologIdhumanOrthoNCBIgeneIds{$humanOrthoZdbId};
   } else {
       $NCBIhumanGeneId = " ";
   }
   $mrkr_abbrev = $prephenoFields[2];
   $a_ont_id = $prephenoFields[3];
   $e1superName = $prephenoFields[4];
   $b_ont_id = $prephenoFields[5];
   $e1subName = $prephenoFields[6];
   $c_ont_id = $prephenoFields[7];
   $e2superName = $prephenoFields[8];
   $d_ont_id = $prephenoFields[9];
   $e2subName = $prephenoFields[10];
   $e_ont_id =$prephenoFields[11];
   $qualityName = $prephenoFields[12];
   $phenos_tag = $prephenoFields[13];
   $a_relationship_id = $prephenoFields[14];
   $a_relationship_id = "" if !defined $prephenoFields[14];
   $a_relationship_name = $prephenoFields[15];
   $a_relationship_name = "" if !defined $prephenoFields[15];
   $b_relationship_id = $prephenoFields[16];
   $b_relationship_id = "" if !defined $prephenoFields[16];
   $b_relationship_name = $prephenoFields[17];
   $b_relationship_name = "" if !defined $prephenoFields[17];
   $quality_id = $prephenoFields[18];

   print PHENO "$zdbgeneId\t$NCBIzfGeneId\t$NCBIhumanGeneId\t$mrkr_abbrev\t$b_ont_id\t$e1subName\t$a_relationship_id\t$a_relationship_name\t$a_ont_id\t$e1superName\t$quality_id\t$qualityName\t$phenos_tag\t$d_ont_id\t$e2subName\t$b_relationship_id\t$b_relationship_name\t$c_ont_id\t$e2superName\n";
}

close (PREPHENO);
close (PHENO);

#system("rm -f $prephenofile");

$preorthofile = "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/preprocessed_ortho.txt";
$orthofile = "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/ortho.txt";

open (PREORTHO, "$preorthofile") ||  die "Cannot open $preorthofile : $!\n";
open (ORTHO, ">$orthofile") || die "can not open $orthofile: $!\n";

@preorthoLines = <PREORTHO>;

foreach $preorthoLine (@preorthoLines) {
   chop($preorthoLine);
   @preorthoFields = split(/\|/, $preorthoLine);
   $zdbgeneId = $preorthoFields[1];
   if (exists($ZDBgeneIdNCBIgeneIds{$zdbgeneId})) {
       $NCBIzfGeneId = $ZDBgeneIdNCBIgeneIds{$zdbgeneId};
   } else {
       $NCBIzfGeneId = " ";
   }
   $humanOrthoZdbId = $preorthoFields[0];
   if (exists($ZDBorthologIdhumanOrthoNCBIgeneIds{$humanOrthoZdbId})) {   
        $NCBIhumanGeneId = $ZDBorthologIdhumanOrthoNCBIgeneIds{$humanOrthoZdbId};
   } else {
        $NCBIhumanGeneId = " ";
   }
   $mrkr_abbrev = $preorthoFields[2];
   $ortho_abbrev = $preorthoFields[3];
   
   print ORTHO "$zdbgeneId\t$mrkr_abbrev\t$NCBIzfGeneId\t$ortho_abbrev\t$NCBIhumanGeneId\n";
}

close (PREORTHO);
close (ORTHO);

system("rm -f $preorthofile");


exit;


