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

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> DownloadFiles.sql");
system("./patoNumbers.pl");

system("./generateStagedAnatomy.pl");


### open a handle on the db
$dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die ("Failed while connecting to <!--|DB_NAME|-->");


### FB case 8651, Include Publication in Morpholino Data Download

$MOfileWithNoPubAndWithHTMLtags = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos2.txt';

open (MO, "$MOfileWithNoPubAndWithHTMLtags") || die "Cannot open Morpholinos2.txt : $!\n";
@lines=<MO>;
close(MO);

$MOfileWithPubsAndNoHTMLtags = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos.txt';

open (MOWITHPUBS, ">$MOfileWithPubsAndNoHTMLtags") || die "Cannot open $MOfileWithPubsAndNoHTMLtags : $!\n";


foreach $line (@lines) {

    chop($line);
    undef (@fields);
    @fields = split(/\|/, $line);

    $geneId = $fields[0];
    $a_szm_term_ont_id = $fields[1];
    $gene = $fields[2];
    $MoId = $fields[3];
    $b_szm_term_ont_id = $fields[4];
    $Mo = $fields[5];
    $MoSeq = $fields[6];
    $note = " ";
    $note = $fields[7];

    # remove HTML tags and back slash from the public note column of the download file of Morpholino data
    if ($note) {
      $note =~ s/<[^<>]+>//g;
      $note =~ s/\\//g;
    } else {
        $note = "";
    }


    $cur = $dbh->prepare('select distinct recattrib_source_zdb_id from record_attribution, marker_sequence where recattrib_data_zdb_id = mrkrseq_zdb_id and mrkrseq_mrkr_zdb_id = ? order by recattrib_source_zdb_id;');
    $cur->execute($MoId);
    my ($pub);
    %pubIds = ();
    $numOfPubs = 0;
    $cur->bind_columns(\$pub);
    while ($cur->fetch()) {
         $pubIds{$numOfPubs} = $pub;
         $numOfPubs++;
    }

    $cur->finish();

    print MOWITHPUBS "$geneId\t$a_szm_term_ont_id\t$gene\t$MoId\t$b_szm_term_ont_id\t$Mo\t$MoSeq\t";

    if ($numOfPubs > 0) {
        $numOfPubsCt = $numOfPubs;
        foreach $key (keys %pubIds) {
           $numOfPubsCt--;
           $value = $pubIds{$key};
           if ($numOfPubsCt == 0) {
               print MOWITHPUBS "$value\t$note\n";
           } else {
               print MOWITHPUBS "$value,";
           }
        }
    } else {
        print MOWITHPUBS "\t$note\n";
    }

}

close MOWITHPUBS;
close MO;


# FB case 7670, add Source field to antibodies.txt download file

$antibodyFile = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibodies2.txt';

open (AB, "$antibodyFile") || die "Cannot open antibodies2.txt : $!\n";
@lines=<AB>;
close(AB);


$antibodyFileWithSupplier = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibodies.txt';

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

    $cur->finish();

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

$dbh->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";


# FB case 8886, remove HTML tags from the download file of Sanger Alleles

$sangerAllelesWithHTMLtags = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/saAlleles2.txt';

open (SAHTML, $sangerAllelesWithHTMLtags) || die "Can't open $sangerAllelesWithHTMLtags : $!\n";

@lines=<SAHTML>;
$sangerAlleles = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/saAlleles.txt';

open (SA,  ">$sangerAlleles") || die "Can't open: $sangerAlleles $!\n";
foreach $line (@lines) {
  $line =~ s/<[^<>]+>//g;
  print SA "$line";
}

close SAHTML;
close SA;

# remove temporary files

system("rm <!--|ROOT_PATH|-->/home/data_transfer/Downloads/saAlleles2.txt");

system("rm <!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos2.txt");

system("./FBcase8787.pl");

# This part checks for any failed download files (those with 0 bytes), and ends the script if it finds some.


system("rm /tmp/emptyFiles.txt");
my $emptyFilesList = '/tmp/emptyFiles.txt';

open (EMPTY, ">$emptyFilesList") || die "Can't open $emptyFilesList !\n";

my $dir = '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/';
my $filesize = -s "/tmp/emptyFiles.txt";

print $filesize."\n";

if ($filesize eq "0"){
    print "empty file"."\n";
}

opendir(DH, $dir) or die $!;

while (my $file = readdir(DH)) {
    
    $filesize = -s $dir.$file;
    #print $file." ".$filesize."\n";
    next if ($filesize > 0);
    #print "empty file! : $file"."\n"; 
    print EMPTY $file." FILE IS EMPTY! \n";
}
close EMPTY;

if (!(-z $emptyFilesList)) {
    die "there are files with 0 data!";
}


# move files to production location -- assume all are good, as the file check above did not end the script.

system("/private/bin/ant -f <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/build.xml archive-download-files");
