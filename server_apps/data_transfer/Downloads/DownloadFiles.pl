#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create data files for public download.
#
# We extract several different kinds of information:
#
# All genetic markers (includes genes, ests, sslps, etc.)
#	zfin id, name, symbol, type
#
# Synonyms  (for any item in all genetic markers file) There may be multiple lines
#   per zfin id
#	zfin id, synonym
#
# Orthology - separate files for:
#   zebrafish - human
#	zfin id , zebrafish symbol, human symbol, OMIM id, Entrez Gene id
#   zebrafish - mouse
#	zfin id , zebrafish symbol, mouse symbol, MGI id, Entrez Gene id
#   zebrafish - fly
#	zfin id,  zebrafish symbol, fly symbol,  Flybase id
#   zebrafish - yeast
#	zfin id,  zebrafish symbol, yeast symbol,  SGD id
#
# Gene Ontology-
#	A copy of the file we send to GO.
#
# Gene Expression
#	gene zfin id , gene symbol, probe zfin id, probe name, expression type,
#       expression pattern zfin id, pub zfin id, genotype zfin id,
#       experiment zfin id#
# Mapping data
#	zfin id, symbol, panel symbol, LG, loc, metric
#
# Sequence data - separate files for GenBank, RefSeq, EntrezGene, Unigene,
# SWISS-PROT, Interpro
#	zfin id, symbol, accession number
#
# Genotypes
#	zfin id, allele/construct, type, gene symbol, corresponding zfin gene id
#
# Morpholino
#       zfin id of gene, gene symbol, zfin id of MO, MO symbol, public note

use DBI;


# define GLOBALS

# set environment variables

$ENV{"DBDATE"}="Y4MD-";
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"JAVA_HOME"}="/private/apps/java";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads";

$downloadStagingDir = "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging";
if (-e $downloadStagingDir) {

    system("rm -rf <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/*");

}
else {
    mkdir $downloadStagingDir;

}

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> DownloadFiles.sql") and die "there was an error in the DownloadFiles.sql";
system("./patoNumbers.pl") and die "there was an error in patoNumbers.pl";

system("./generateStagedAnatomy.pl") and die "there was an error in generateStagedAnatomy.pl";



### open a handle on the db
$dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die ("Failed while connecting to <!--|DB_NAME|-->");


### FB case 8651, Include Publication in Morpholino Data Download

$sql = 'select gn.mrkr_zdb_id, a.szm_term_ont_id, gn.mrkr_abbrev, mo.mrkr_zdb_id, b.szm_term_ont_id, mo.mrkr_abbrev,
	       seq_sequence, mo.mrkr_comments
          from marker gn, marker mo, marker_sequence, marker_relationship, so_zfin_mapping a, so_zfin_mapping b
         where gn.mrkr_zdb_id = mrel_mrkr_2_zdb_id
           and mo.mrkr_zdb_id = mrel_mrkr_1_zdb_id
           and a.szm_object_type = gn.mrkr_type
           and b.szm_object_type = mo.mrkr_type
           and (mrel_mrkr_2_zdb_id[1,9] = "ZDB-GENE-")-- note ommits pseudogenes, hope that was deliberate
           and mrel_mrkr_1_zdb_id[1,12] = "ZDB-MRPHLNO-"
           and mrel_type = "knockdown reagent targets gene"
           and mo.mrkr_zdb_id = seq_mrkr_zdb_id
      order by gn.mrkr_abbrev;';

$cur = $dbh->prepare($sql);
$cur->execute();
$cur->bind_columns(\$geneId, \$a_szm_term_ont_id, \$gene, \$MoId, \$b_szm_term_ont_id, \$Mo, \$MoSeq, \$note); 

$MOfileWithPubsAndNoHTMLtags = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/Morpholinos.txt';

open (MOWITHPUBS, ">$MOfileWithPubsAndNoHTMLtags") || die "Cannot open $MOfileWithPubsAndNoHTMLtags : $!\n";

while ($cur->fetch()) {
    # remove HTML tags and back slash from the public note column of the download file of Morpholino data
    if ($note) {
      $note =~ s/<[^<>]+>//g;
      $note =~ s/\\//g;
    } else {
        $note = "";
    }
            
    print MOWITHPUBS "$geneId\t$a_szm_term_ont_id\t$gene\t$MoId\t$b_szm_term_ont_id\t$Mo\t$MoSeq\t";

    @pubIds = ();
    my ($pub);
    $innerSql = "select ra.recattrib_source_zdb_id
                 from record_attribution ra
                 where ra.recattrib_data_zdb_id = ?
                 union
                 select ra.recattrib_source_zdb_id
                 from record_attribution ra , marker_relationship mr
                 where mr.mrel_mrkr_2_zdb_id = ?
                 and ra.recattrib_data_zdb_id = mr.mrel_zdb_id
                 union
                 select ra.recattrib_source_zdb_id
                 from record_attribution ra , marker_relationship mr
                 where mr.mrel_mrkr_1_zdb_id = ?
                 and ra.recattrib_data_zdb_id = mr.mrel_zdb_id";
        
    $curInner = $dbh->prepare($innerSql);
    $curInner->execute($MoId, $MoId, $MoId);
    $curInner->bind_columns(\$pub);
    while ($curInner->fetch()) {
         push(@pubIds, $pub);
    }
    $pubs = join(",", sort(@pubIds));
    print MOWITHPUBS "$pubs\t$note\n";
}

close MOWITHPUBS;

## generate a file with antibodies and associated expression experiment
## ZFIN-5654
$sql = '
 select xpatex_atb_zdb_id, atb.mrkr_abbrev, xpatex_gene_zdb_id as gene_zdb,
	"" as geneAbbrev, xpatex_assay_name, xpatex_zdb_id as xpat_zdb,
	xpatex_source_zdb_id, fish_zdb_id, genox_exp_zdb_id
 from expression_experiment, fish_experiment, fish, marker atb
 where xpatex_genox_Zdb_id = genox_zdb_id
 and genox_fish_zdb_id = fish_Zdb_id
 and atb.mrkr_zdb_id = xpatex_atb_zdb_id
   and xpatex_gene_zdb_id is null
 AND not exists (Select "x" from clone
      where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
      and clone_problem_type = "Chimeric")
UNION
 select xpatex_atb_zdb_id, atb.mrkr_abbrev, xpatex_gene_zdb_id as gene_zdb,
	gene.mrkr_abbrev as geneAbbrev, xpatex_assay_name, xpatex_zdb_id as xpat_zdb,
	xpatex_source_zdb_id, fish_zdb_id, genox_exp_zdb_id
 from expression_experiment, fish_experiment, fish, marker atb, marker gene
 where xpatex_genox_Zdb_id = genox_zdb_id
 and genox_fish_zdb_id = fish_Zdb_id
 and atb.mrkr_zdb_id = xpatex_atb_zdb_id
 and gene.mrkr_zdb_id = xpatex_gene_zdb_id
   and xpatex_gene_zdb_id is not null
 and gene.mrkr_abbrev not like "WITHDRAWN:"
 AND not exists (Select "x" from clone
      where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
      and clone_problem_type = "Chimeric");
';
$cur = $dbh->prepare($sql);
$cur->execute();
$cur->bind_columns(\$abId, \$abSym, \$geneId, \$geneSym, \$xpType, \$xpId, \$pubId, \$fishId, \$envId); 


$abXpatFishFile = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/abxpat_fish.txt';

open (ABXPFISH, ">$abXpatFishFile") || die "Cannot open $abXpatFishFile : $!\n";

while ($cur->fetch()) {
    # remove back slash from the gene ID column of the download file
    if ($geneId) {
        $geneId =~ s/\\//g;
    } else {
        $geneId = "";
    }
            
    print ABXPFISH "$abId\t$abSym\t$geneId\t$geneSym\t$xpType\t$xpId\t$pubId\t$fishId\t$envId\n";
}

close ABXPFISH;

## ZFIN-5649 
$wtXpatFishWithBackSlash = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/wildtype-expression_fish2.txt';

open (WTXPATFISHSLASH, $wtXpatFishWithBackSlash) || die "Can't open $wtXpatFishWithBackSlash : $!\n";

@lines=<WTXPATFISHSLASH>;
$wtXpatFish = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/wildtype-expression_fish.txt';

open (WTXPATFISH,  ">$wtXpatFish") || die "Can't open: $wtXpatFish $!\n";
foreach $line (@lines) {
  $line =~ s/\\//g;
  print WTXPATFISH "$line";
}

close WTXPATFISHSLASH;
close WTXPATFISH;

system("rm <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/wildtype-expression_fish2.txt");

$TALENfileWithPubsAndNoHTMLtags = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/TALEN.txt';

open (TALENWITHPUBS, ">$TALENfileWithPubsAndNoHTMLtags") || die "Cannot open $TALENfileWithPubsAndNoHTMLtags : $!\n";

$sql = 'select gn.mrkr_zdb_id, a.szm_term_ont_id, gn.mrkr_abbrev, talen.mrkr_zdb_id, b.szm_term_ont_id, talen.mrkr_abbrev,
	       seq_sequence, seq_sequence_2, talen.mrkr_comments
          from marker gn, marker talen, marker_sequence, marker_relationship, so_zfin_mapping a, so_zfin_mapping b
         where gn.mrkr_zdb_id = mrel_mrkr_2_zdb_id
           and talen.mrkr_zdb_id = mrel_mrkr_1_zdb_id
           and a.szm_object_type = gn.mrkr_type
           and b.szm_object_type = talen.mrkr_type
           and mrel_mrkr_2_zdb_id[1,9] = "ZDB-GENE-" -- note ommits pseudogenes, hope that was deliberate
           and mrel_mrkr_1_zdb_id[1,10] = "ZDB-TALEN-"
           and mrel_type = "knockdown reagent targets gene"
           and talen.mrkr_zdb_id = seq_mrkr_zdb_id
      order by gn.mrkr_abbrev;';

$cur = $dbh->prepare($sql);
$cur->execute();
$cur->bind_columns(\$geneId, \$a_szm_term_ont_id, \$gene, \$talen_id, \$b_szm_term_ont_id, \$talen, \$talen_seq1, \$talen_seq2, \$note); 

while ($cur->fetch()) {
    # remove HTML tags and back slash from the public note column of the download file of TALEN data
    if ($note) {
      $note =~ s/<[^<>]+>//g;
      $note =~ s/\\//g;
    } else {
        $note = "";
    }
            
    print TALENWITHPUBS "$geneId\t$a_szm_term_ont_id\t$gene\t$talen_id\t$b_szm_term_ont_id\t$talen\t$talen_seq1\t$talen_seq2\t";

    %pubIds = ();
    $numOfPubs = 0;
    my ($pub);
    $innerSql = "select distinct recattrib_source_zdb_id from record_attribution where recattrib_data_zdb_id = " . "\"" . $talen_id . "\";";
        
    $curInner = $dbh->prepare($innerSql);
    $curInner->execute();
    $curInner->bind_columns(\$pub);
    while ($curInner->fetch()) {
         $pubIds{$pub} = 1;
         $numOfPubs++;
    }

    if ($numOfPubs > 0) {
        $numOfPubsCt = $numOfPubs;
        foreach $key (sort keys %pubIds) {
           $numOfPubsCt--;
           if ($numOfPubsCt == 0) {
               print TALENWITHPUBS "$key\t$note\n";
           } else {
               print TALENWITHPUBS "$key,";
           }
        }
    } else {
        print TALENWITHPUBS "$note\n";
    }    
}

close TALENWITHPUBS;

$CRISPRfileWithPubsAndNoHTMLtags = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/CRISPR.txt';

open (CRISPRWITHPUBS, ">$CRISPRfileWithPubsAndNoHTMLtags") || die "Cannot open $CRISPRfileWithPubsAndNoHTMLtags : $!\n";

$sql = 'select gn.mrkr_zdb_id, a.szm_term_ont_id, gn.mrkr_abbrev, crispr.mrkr_zdb_id, b.szm_term_ont_id, crispr.mrkr_abbrev,
	       seq_sequence, crispr.mrkr_comments
          from marker gn, marker crispr, marker_sequence, marker_relationship, so_zfin_mapping a, so_zfin_mapping b
         where gn.mrkr_zdb_id = mrel_mrkr_2_zdb_id
           and crispr.mrkr_zdb_id = mrel_mrkr_1_zdb_id
           and a.szm_object_type = gn.mrkr_type
           and b.szm_object_type = crispr.mrkr_type
           and mrel_mrkr_2_zdb_id[1,9] = "ZDB-GENE-" -- note ommits pseudogenes, hope that was deliberate
           and mrel_mrkr_1_zdb_id[1,11] = "ZDB-CRISPR-"
           and mrel_type = "knockdown reagent targets gene"
           and crispr.mrkr_zdb_id = seq_mrkr_zdb_id
      order by gn.mrkr_abbrev;';

$cur = $dbh->prepare($sql);
$cur->execute();
$cur->bind_columns(\$geneId, \$a_szm_term_ont_id, \$gene, \$crispr_id, \$b_szm_term_ont_id, \$crispr, \$crispr_seq, \$note); 

while ($cur->fetch()) {
    # remove HTML tags and back slash from the public note column of the download file of CRISPR data
    if ($note) {
      $note =~ s/<[^<>]+>//g;
      $note =~ s/\\//g;
    } else {
        $note = "";
    }
            
    print CRISPRWITHPUBS "$geneId\t$a_szm_term_ont_id\t$gene\t$crispr_id\t$b_szm_term_ont_id\t$crispr\t$crispr_seq\t";

    %pubIds = ();
    $numOfPubs = 0;
    my ($pub);
    $innerSql = "select distinct recattrib_source_zdb_id from record_attribution where recattrib_data_zdb_id = " . "\"" . $crispr_id . "\";";
        
    $curInner = $dbh->prepare($innerSql);
    $curInner->execute();
    $curInner->bind_columns(\$pub);
    while ($curInner->fetch()) {
         $pubIds{$pub} = 1;
         $numOfPubs++;
    }

    if ($numOfPubs > 0) {
        $numOfPubsCt = $numOfPubs;
        foreach $key (sort keys %pubIds) {
           $numOfPubsCt--;
           if ($numOfPubsCt == 0) {
               print CRISPRWITHPUBS "$key\t$note\n";
           } else {
               print CRISPRWITHPUBS "$key,";
           }
        }
    } else {
        print CRISPRWITHPUBS "$note\n";
    }    
}

close CRISPRWITHPUBS;

$curInner->finish();


# FB case 7670, add Source field to antibodies.txt download file

$antibodyFile = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/antibodies2.txt';

open (AB, "$antibodyFile") || die "Cannot open antibodies2.txt : $!\n";
@lines=<AB>;
close(AB);


$antibodyFileWithSupplier = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/antibodies.txt';

open (ABSOURCE, ">$antibodyFileWithSupplier") || die "Cannot open $antibodyFileWithSupplier : $!\n";


foreach $line (@lines) {

    chop($line);
    undef (@fields);
    @fields = split(/\s+/, $line);

    $antibodyId = $fields[0];

    $cur = $dbh->prepare('select distinct l.name from int_data_supplier, lab l where idsup_data_zdb_id = ? and idsup_supplier_zdb_id = l.zdb_id union select distinct c.name from int_data_supplier, company c where idsup_data_zdb_id = ? and idsup_supplier_zdb_id = c.zdb_id;');
    $cur->execute($antibodyId,$antibodyId);
    my ($supplier);
    %suppliers = ();
    $numOfSuppliers = 0;
    $cur->bind_columns(\$supplier);
    while ($cur->fetch()) {
         $suppliers{$numOfSuppliers} = $supplier;
         $numOfSuppliers++;
    }

    print ABSOURCE "$line\t";

    if ($numOfSuppliers > 0) {
        $numOfSuppliersCt = $numOfSuppliers;
        foreach $key (sort { $suppliers{$a} cmp $suppliers{$b} } keys %suppliers) {
           $numOfSuppliersCt--;
           $value = $suppliers{$key};
           if ($numOfSuppliersCt == 0) {
               print ABSOURCE "$value\n";
           } else {
               print ABSOURCE "$value, ";
           }
        }
    } else {
        print ABSOURCE "\t \n";
    }

}

close ABSOURCE;
close AB;

$cur->finish();

$dbh->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";


# FB case 8886, remove HTML tags from the download file of Sanger Alleles

$sangerAllelesWithHTMLtags = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/saAlleles2.txt';

open (SAHTML, $sangerAllelesWithHTMLtags) || die "Can't open $sangerAllelesWithHTMLtags : $!\n";

@lines=<SAHTML>;
$sangerAlleles = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/saAlleles.txt';

open (SA,  ">$sangerAlleles") || die "Can't open: $sangerAlleles $!\n";
foreach $line (@lines) {
  $line =~ s/<[^<>]+>//g;
  print SA "$line";
}

close SAHTML;
close SA;

# remove temporary files

system("rm <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/saAlleles2.txt");

$huAllelesHtml = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/huAlleles2.txt';

open (HUALLELEHTML, $huAllelesHtml) || die "Can't open $huAllelesHtml : $!\n";

@lines=<HUALLELEHTML>;
$huAlleles = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/huAlleles.txt';

open (HUALLELE,  ">$huAlleles") || die "Can't open: $huAlleles $!\n";
foreach $line (@lines) {
  $line =~ s/<[^<>]+>//g;
  print HUALLELE "$line";
}

close HUALLELEHTML;
close HUALLELE;

system("rm <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/huAlleles2.txt");


# This part checks for any failed download files (those with 0 bytes), and ends the script if it finds some.


system("rm /tmp/<!--|DB_NAME|-->emptyFiles.txt");
$emptyFilesList = '/tmp/<!--|DB_NAME|-->emptyFiles.txt';

open (EMPTY, ">$emptyFilesList") || die "Can't open $emptyFilesList !\n";

$dir = '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging';

opendir(DH, $dir) or die $!;

while (my $file = readdir(DH)) {

    my $filesize = -s $dir."/".$file || 0;

    next if ($filesize > 0);

    print "empty file! : $file"."\n"; 
    print EMPTY $file." FILE IS EMPTY! \n";
}
close EMPTY;

if (!(-z $emptyFilesList)) {
    die "there are files with 0 data!";
}

# move files to production location -- assume all are good, as the file check above did not end the script


system("rm -rf <!--|ROOT_PATH|-->/home/data_transfer/Downloads/*.txt");
system("rm -rf <!--|ROOT_PATH|-->/home/data_transfer/Downloads/*.unl");
system("rm -rf <!--|ROOT_PATH|-->/home/data_transfer/Downloads/intermineData/*");

system("cp <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/*  <!--|ROOT_PATH|-->/home/data_transfer/Downloads/") and die "can not cp files to production location";


system("<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/intermineData/dumper.sh") and die "error running dumper.sh";

system("/private/bin/ant -f <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/build.xml archive-download-files");
