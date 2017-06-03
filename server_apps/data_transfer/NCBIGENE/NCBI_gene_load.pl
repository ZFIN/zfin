#!/private/bin/perl
#
# NCBI_gene_load.pl
#
# This script loads the following db_link records based on mapped gene records between ZFIn and NCBI:
# 1) NCBI Gene Ids
# 2) UniGene Ids
# 3) RefSeq accessioons (including RefSeq RNA, RefPept, RefSeq DNA)
# 4) GenBank accessions (including GenBank RNA, GenPept, GenBank DNA)
#
# The script execute the prepareNCBIgeneLoad.sql to generate the delete list and a set of ZFIN genes with RNA.
# Then, the script maps ZFIN gene records to NCBI gene records based on
# 1) common GenBank RNA accessions
# 2) common Vega Gene Id
# Then, the script execute the loadNCBIgeneAccs.sql to delete all the db_link records previously loaded
# (accrding to the delete list), and load all the accessions for the gene records mapped.
#
# The values of dblink_length are also processed and loaded.
# And statistics and various reports are generated and emailed.

#
# See README or http://fogbugz.zfin.org/default.asp?W1625
# for a detailed documentation of the steps.

# execute the following to run the scriot in debug mode:
# perl -s NCBI_gene_load.pl -debug

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

system("/bin/date");

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/NCBIGENE/";

$dbname = "<!--|DB_NAME|-->";

#------------------------------------------------
# remove old files
#------------------------------------------------

system("/bin/rm -f prepareLog*");
system("/bin/rm -f loadLog*");
system("/bin/rm -f logNCBIgeneLoad");
system("/bin/rm -f debug*");
system("/bin/rm -f report*");
system("/bin/rm -f toDelete.unl");
system("/bin/rm -f toMap.unl");
system("/bin/rm -f toLoad.unl");
system("/bin/rm -f length.unl");
system("/bin/rm -f noLength.unl");
system("/bin/rm -f seq.fasta");

system("/bin/rm -f zf_gene_info");
system("/bin/rm -f gene2unigene");
system("/bin/rm -f gene2accession");
system("/bin/rm -f RefSeqCatalog");
system("/bin/rm -f RELEASE_NUMBER");

open LOG, '>', "logNCBIgeneLoad" or die "can not open logNCBIgeneLoad: $! \n";

print LOG "Start ... \n";

#-------------------------------------------------------------------------------------------------
# Step 1: Download and decompress NCBI data files
#-------------------------------------------------------------------------------------------------

## only the following RefSeq catalog file may remain unchanged over a period of time
## the rest 3 are changing every day

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/refseq/release/RELEASE_NUMBER");

open (REFSEQRELEASENUM, "RELEASE_NUMBER") ||  die "Cannot open RELEASE_NUMBER : $!\n";

$releaseNum = 0;
while (<REFSEQRELEASENUM>) {
   if($_ =~ m/(\d+)/) {
      $releaseNum = $1;
   }
}

close REFSEQRELEASENUM;

print LOG "RefSeq Catalog Release Number is $releaseNum.\n\n";

$catlogFolder = "ftp://ftp.ncbi.nlm.nih.gov/refseq/release/release-catalog/";

$catalogFile = "RefSeq-release" . $releaseNum . ".catalog.gz";

$ftpNCBIrefSeqCatalog = $catlogFolder . $catalogFile;

&doSystemCommand("/local/bin/wget -N $ftpNCBIrefSeqCatalog");

&doSystemCommand("/local/bin/gunzip -c $catalogFile >RefSeqCatalog");

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz");

&doSystemCommand("/local/bin/gunzip gene2accession.gz");

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2unigene");

&doSystemCommand("/local/bin/wget -O zf_gene_info.gz ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz");

&doSystemCommand("/local/bin/gunzip zf_gene_info.gz");


print LOG "Done with downloading.\n\n";

#-------------------------------------------------------------------------------------------------
# Check if all the downloaded and decompressed NCBI data files are in place.
# If not, stop the process and send email to alert.
#-------------------------------------------------------------------------------------------------

if (!-e "zf_gene_info" || !-e "gene2accession" || !-e "RefSeqCatalog" || !-e "gene2unigene") {
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: ERROR with download";
   print LOG "\nMissing one or more downloaded NCBI file(s)\n\n";
   &reportErrAndExit($subjectLine);
}

# ------------------- global variables, with variable names self-explanatory  ---------------------------------

$pubMappedbasedOnRNA = "ZDB-PUB-020723-3";
$pubMappedbasedOnVega = "ZDB-PUB-130725-2";

$fdcontNCBIgeneId = "ZDB-FDBCONT-040412-1";
$fdcontUniGeneId = "ZDB-FDBCONT-040412-44";
$fdcontGenBankRNA = "ZDB-FDBCONT-040412-37";
$fdcontGenPept = "ZDB-FDBCONT-040412-42";
$fdcontGenBankDNA = "ZDB-FDBCONT-040412-36";
$fdcontRefSeqRNA = "ZDB-FDBCONT-040412-38";
$fdcontRefPept = "ZDB-FDBCONT-040412-39";
$fdcontRefSeqDNA = "ZDB-FDBCONT-040527-1";

#--------------------------------------------------------------------------------------------------------------------
# Step 2: execute prepareNCBIgeneLoad.sql to prepare
#    1) a delete list, toDelete.unl
#    2) a list of ZFIN genes to be mapped, toMap.unl
#--------------------------------------------------------------------------------------------------------------------

$cmd = "$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> prepareNCBIgeneLoad.sql >prepareLog1 2> prepareLog2";
&doSystemCommand($cmd);

print LOG "Done with preparing the delete list and the list for mapping.\n\n";

$subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: prepareLog1 file";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","prepareLog1");

$subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: prepareLog2 file";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","prepareLog2");

# This is a hash to store the zdb ids of db_link record to be deleted; used at later step
# key: dblink zdb id
# value: 1

%toDelete = ();

open (TODELETE, "toDelete.unl") ||  die "Cannot open toDelete.unl : $!\n";

$ctToDelete = 0;
while (<TODELETE>) {
  chomp;
  if ($_) {
    chop;
    $ctToDelete++;
    $dblinkIdToBeDeleted = $_;
    $toDelete{$dblinkIdToBeDeleted} = 1;
  }
}

close TODELETE;

if ($ctToDelete == 0) {
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: the delete list, toDelete.unl, is empty";
   print LOG "\nThe delete list, toDelete.unl is empty. Something is wrong.\n\n";
   &reportErrAndExit($subjectLine);
}

#--------------------------------------------------------------------------------------
# Step 3: record counts
#--------------------------------------------------------------------------------------

### open a handle on the db
$handle = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die "Cannot connect to Informix database: $DBI::errstr\n";

$sql = 'select mrkr_zdb_id, mrkr_abbrev from marker
         where (mrkr_zdb_id like "ZDB-GENE%" or mrkr_zdb_id like "%RNAG%")
           and exists (select "x" from db_link
         where dblink_linked_recid = mrkr_zdb_id
           and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-38","ZDB-FDBCONT-040412-39","ZDB-FDBCONT-040527-1"));';

$curGenesWithRefSeq = $handle->prepare($sql);

$curGenesWithRefSeq->execute;

$curGenesWithRefSeq->bind_columns(\$geneId,\$geneSymbol);

%genesWithRefSeqBeforeLoad = ();
while ($curGenesWithRefSeq->fetch) {
   $genesWithRefSeqBeforeLoad{$geneId} = $geneSymbol;
}

$curGenesWithRefSeq->finish();

$ctGenesWithRefSeqBefore = scalar(keys %genesWithRefSeqBeforeLoad);

# NCBI Gene Id
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numNCBIgeneIdBefore = ZFINPerlModules->countData($sql);

# UniGene
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-44"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numUniGeneBefore = ZFINPerlModules->countData($sql);

#RefSeq RNA
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-38"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numRefSeqRNABefore = ZFINPerlModules->countData($sql);

# RefPept
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-39"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numRefPeptBefore = ZFINPerlModules->countData($sql);

#RefSeq DNA
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040527-1"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numRefSeqDNABefore = ZFINPerlModules->countData($sql);

# GenBank RNA (only those loaded - excluding curated ones)
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-37"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
           and exists(select "x" from record_attribution
                       where recattrib_data_zdb_id = dblink_zdb_id
                         and recattrib_source_zdb_id in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"));';

$numGenBankRNABefore = ZFINPerlModules->countData($sql);

# GenPept (only those loaded - excluding curated ones)
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-42"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
           and exists(select "x" from record_attribution
                       where recattrib_data_zdb_id = dblink_zdb_id
                         and recattrib_source_zdb_id in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"));';

$numGenPeptBefore = ZFINPerlModules->countData($sql);

# GenBank DNA (only those loaded - excluding curated ones)
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-36"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
           and exists(select "x" from record_attribution
                       where recattrib_data_zdb_id = dblink_zdb_id
                         and recattrib_source_zdb_id in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"));';

$numGenBankDNABefore = ZFINPerlModules->countData($sql);

# number of genes with RefSeq RNA
$sql = 'select distinct dblink_linked_recid
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-38"
           and dblink_acc_num like "NM_%"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numGenesRefSeqRNABefore = ZFINPerlModules->countData($sql);

# number of genes with RefPept
$sql = 'select distinct dblink_linked_recid
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-39"
           and dblink_acc_num like "NP_%"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numGenesRefSeqPeptBefore = ZFINPerlModules->countData($sql);

# number of genes with GenBank
$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenBank"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numGenesGenBankBefore = ZFINPerlModules->countData($sql);

#--------------------------------------------------------------------------------------------
# Step 4: Parse zf_gene_info file to get the NCBI records with gene Id, symbol, and Vega Id
#--------------------------------------------------------------------------------------------

# a hash to store Vega Gene Id and the ONLY related NCBI gene Id
# key: Vega Gene Id
# value: NCBI Gene Id

%vegaIdsNCBIids = ();

# a hash to store NCBI Gene Id and the corresponding NCBI gene symbol; will be used in reports
# key: NCBI Gene Id
# value: NCBI Gene Symbol

%NCBIidsGeneSymbols = ();

# a hash to store Vega Gene Ids and the multiple related NCBI gene Ids
# key: Vega Gene Id
# value: reference to an array of NCBI Gene Ids

%vegaIdwithMultipleNCBIids = ();

# a hash to store NCBI Gene Ids and the multiple related Vega gene Ids
# key: NCBI Gene Id
# value: the parsed field of dbXrefs containing multiple Vega Gene Ids

%NCBIgeneWithMultipleVega = ();

$ctlines = $ctVegaIdsNCBI = 0;

open (ZFGENEINFO, "zf_gene_info") ||  die "Cannot open zf_gene_info : $!\n";

#Format: tax_id GeneID Symbol LocusTag Synonyms dbXrefs chromosome map_location description type_of_gene Symbol_from_nomenclature_authority Full_name_from_nomenclature_authority Nomenclature_status Other_designations Modification_date

# Sample record:
# 7955    30037   tnc     CH211-166O17.1  tenc|wu:fk04d02 ZFIN:ZDB-GENE-980526-104|Ensembl:ENSDARG00000021948|Vega:OTTDARG00000032698     5       -       tenascin C      protein-coding  tnc     tenascin C      O       etID309720.5|tenascin   20130529

while (<ZFGENEINFO>) {
  chomp;

  if ($_) {
    $ctlines++;

    ## the first line is just description of the fields (format, as show above), not the data
    next if $ctlines < 2;

    undef @fields;
    @fields = split("\t");

    $taxId = $fields[0];

    ## don't process if it is not zebrafish gene
    next if $taxId ne "7955";

    $NCBIgeneId = $fields[1];
    $symbol = $fields[2];

    $geneSymbolsNCBIids{$symbol} = $NCBIgeneId;
    $NCBIidsGeneSymbols{$NCBIgeneId} = $symbol;

    $synonyms = $fields[4];
    $dbXrefs = $fields[5];
    $chr = $fields[6];
    $typeOfGene = $fields[9];
    $modDate = $fields[14];

    if ($_ =~ m/Vega(.+)Vega(.+)/) {
      $NCBIgeneWithMultipleVega{$NCBIgeneId} = $dbXrefs;
      print LOG "\nMultiple Vega: \n $NCBIgeneId \t $dbXrefs \n";
      next;
    }

    # Sample dbXrefs column: ZFIN:ZDB-GENE-980526-559|Ensembl:ENSDARG00000009351|Vega:OTTDARG00000027061
    # We ignore the ZFIN Gene ZDB Id and we need the Vega Id

    if ($dbXrefs =~ m/Vega:(OTTDARG[0-9]+)/) {  ### if VEGA Id is there
      $VegaIdNCBI = $1;
      $ctVegaIdsNCBI++;

      ## if the Vega Gene Id is found in the hash of those with multiple NCBI gene ids
      ## or, if the Vega Gene Id is found in the hash of those with on;y 1 NCBI gene id,
      ## but the corresponding NCBI gene id is not the same as the NCBI gene id of this row,
      ## it means the Vega Id here must correspond to multiple NCBI gene Ids

      if (exists($vegaIdwithMultipleNCBIids{$VegaIdNCBI}) ||
           (exists($vegaIdsNCBIids{$VegaIdNCBI}) && $vegaIdsNCBIids{$VegaIdNCBI} ne $NCBIgeneId)) {

        ## if the Vega Gene Id is not found in the hash of those with multiple NCBI gene ids yet,
        ## get the corresponding NCBI gene Id from the hash of %vegaIdsNCBIids, put it and
        ## the NCBI gene id of the current row into an anonymous array;
        ## set the reference to this anonymous array as the value of %vegaIdwithMultipleNCBIids

        if (!exists($vegaIdwithMultipleNCBIids{$VegaIdNCBI})) {
            $firstNCBIgeneIdFound = $vegaIdsNCBIids{$VegaIdNCBI};
            $ref_arrayNCBIGenes = [$firstNCBIgeneIdFound,$NCBIgeneId];
            $vegaIdwithMultipleNCBIids{$VegaIdNCBI} = $ref_arrayNCBIGenes;
        } else {
            ## otherwise, get the value of %vegaIdwithMultipleNCBIids, which is a reference to an anonymous array
            ## and push the NCBI gene Id at current row to this array

            $ref_arrayNCBIGenes = $vegaIdwithMultipleNCBIids{$VegaIdNCBI};
            push(@$ref_arrayNCBIGenes, $NCBIgeneId);
        }
      }

      $vegaIdsNCBIids{$VegaIdNCBI} = $NCBIgeneId;
    }
  }

}
close ZFGENEINFO;

$ctlines--;    ## because the first line is just the description of the fileds

open STATS, '>', "reportStatistics" or die "can not open reportStatistics" ;

print LOG "\nTotal number of records on NCBI's Danio_rerio.gene_info file: $ctlines\n\n";
print LOG "\nctVegaIdsNCBI:  $ctVegaIdsNCBI\n\n";

print STATS "\nTotal number of records on NCBI's Danio_rerio.gene_info file: $ctlines\n";
print STATS "\nNumber of Vega Gene Id/NCBI Gene Id pairs on Danio_rerio.gene_info file: $ctVegaIdsNCBI\n\n";

print STATS "On NCBI's Danio_rerio.gene_info file, the following Vega Ids correspond to more than 1 NCBI genes\n\n";
print LOG "On NCBI's Danio_rerio.gene_info file, the following Vega Ids correspond to more than 1 NCBI genes\n";

$ctVegaIdWithMultipleNCBIgene = 0;
foreach $vega (sort keys %vegaIdwithMultipleNCBIids) {
  $ctVegaIdWithMultipleNCBIgene++;
  $ref_arrayNCBIGenes = $vegaIdwithMultipleNCBIids{$vega};
  print LOG "$vega @$ref_arrayNCBIGenes\n";
  print STATS "$vega @$ref_arrayNCBIGenes\n";
}

print LOG "\nctVegaIdWithMultipleNCBIgene = $ctVegaIdWithMultipleNCBIgene\n\n";

##-------------------------------------------------------------------------------------------
## Get and store ZFIN gene zdb id and symbol
## The ZFIN gene symbols will be looked up and printed in various reports

# key: gene zdb id
# value: gene symbol at ZFIN

%geneZDBidsSymbols = ();

$sqlGeneZDBidsSymbols = 'select mrkr_zdb_id, mrkr_abbrev from marker where (mrkr_zdb_id like "ZDB-GENE%" or mrkr_zdb_id like "ZDB-LINCRNAG%" or mrkr_zdb_id like "ZDB-MIRNAG%") and mrkr_abbrev not like "WITHDRAWN%";';

$curGeneZDBidsSymbols = $handle->prepare($sqlGeneZDBidsSymbols);

$curGeneZDBidsSymbols->execute();
$curGeneZDBidsSymbols->bind_columns(\$zdbId,\$symbol);

while ($curGeneZDBidsSymbols->fetch()) {
   $geneZDBidsSymbols{$zdbId} = $symbol;
}

$curGeneZDBidsSymbols->finish();

#----------------------------------------------------------------------------------------------------------------------
# Step 5: Map ZFIN gene records to NCBI gene records based on GenBank RNA sequences
#----------------------------------------------------------------------------------------------------------------------

#-----------------------------------------
# Step 5-1: initial set of ZFIN records
#-----------------------------------------

## the following SQL is used to getl the GenBank RNA accessions as evidence of (supporting) a gene record at ZFIN

$sqlGetSupportingGenBankRNAs = 'select dblink_acc_num
                                  from db_link
                                 where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-37"
                                   and dblink_linked_recid = ?
                                union
                                select dblink_acc_num
                                  from db_link
                                 where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-37"
                                   and dblink_linked_recid not like "ZDB-GENE%" 
                                   and dblink_linked_recid not like "%RNAG%"
                                   and exists(select "x" from marker_relationship
                                               where mrel_mrkr_1_zdb_id = ?
                                                 and mrel_type = "gene encodes small segment"
                                                 and dblink_linked_recid = mrel_mrkr_2_zdb_id);';

$curGetSupportingGenBankRNAs = $handle->prepare($sqlGetSupportingGenBankRNAs);

open (ZFINGENESUPPORTED, "toMap.unl") ||  die "Cannot open toMap.unl : $!\n";

## %supportedGeneZFIN is a hash to store references to arrays of GenBank RNA accession(s) supporting ZDB gene Id
## key:    zdb gene id
## value:  reference to the array of accession(s) that support the gene
## example1:  $supportedGeneZFIN{$zdbGeneId1} = [$acc1, $acc2]
## example2:  $supportedGeneZFIN{$zdbGeneId2} = [$acc3, $acc4, $acc5]

%supportedGeneZFIN = ();

## %supportingAccZFIN is a hash to store references to arrays of ZDB gene Ids supported by GenBank accession
## key:    GenBank RNA accession
## value:  reference to the array of zdb gene id(s) that is supported by the GenBank RNA accession
## example1:  $supportingAccZFIN{$acc1} = [$zdbGene1]            -- potential 1:1 if same on NCBI end
## example2:  $supportingAccZFIN{$acc2} = [$zdbGene2, $zdbGene3] -- 1 acc supporting 2 genes, which won't be used as evidence in mapping

%supportingAccZFIN = ();

$ctSupportedZFINgenes = 0;
while (<ZFINGENESUPPORTED>) {
  chomp;

  if ($_) {
    $ctSupportedZFINgenes++;
    chop;

    $geneZDBidSupported = $_;

    my $GenBankAccAtZFIN;

    $curGetSupportingGenBankRNAs->execute($geneZDBidSupported, $geneZDBidSupported);
    $curGetSupportingGenBankRNAs->bind_columns(\$GenBankAccAtZFIN);

    while ($curGetSupportingGenBankRNAs->fetch()) {

       ## if the array of ZDB gene Ids supported by this GenBank RNA accession has not been created yet

       if (!exists($supportingAccZFIN{$GenBankAccAtZFIN})) {

            ## then create it with the first element (the supported ZDB gene id)

            $ref_arrayGenes = [$geneZDBidSupported];
            $supportingAccZFIN{$GenBankAccAtZFIN} = $ref_arrayGenes;

       } else {  ## otherwise, add this supported ZDB gene Id into the array

            $ref_arrayGenes = $supportingAccZFIN{$GenBankAccAtZFIN};
            push(@$ref_arrayGenes, $geneZDBidSupported);
       }


       ## if the array of the GenBank RNA accession(s) supporting the ZDB gene Id has not been created yet

       if (!exists($supportedGeneZFIN{$geneZDBidSupported})) {

            ## then create it with this supporting GenBank RNA accession

            $ref_arrayAccs = [$GenBankAccAtZFIN];

            $supportedGeneZFIN{$geneZDBidSupported} = $ref_arrayAccs;

       } else {  ## otherwise, add this supporting GenBank RNA accession into the array

            $ref_arrayAccs = $supportedGeneZFIN{$geneZDBidSupported};
            push(@$ref_arrayAccs, $GenBankAccAtZFIN);
       }

    }

  }

}

print LOG "ctSupportedZFINgenes::: $ctSupportedZFINgenes\n\n";

print LOG "Total number of ZFIN gene records supported by GenBank RNA: $ctSupportedZFINgenes\n\n";

close ZFINGENESUPPORTED;

$curGetSupportingGenBankRNAs->finish();

if ($debug) {
  open (DBG1, ">debug1") ||  die "Cannot open debug1 : $!\n";
  foreach $geneAtZFIN (sort keys %supportedGeneZFIN) {
    $ref_arrayAccs = $supportedGeneZFIN{$geneAtZFIN};
    print DBG1 "$geneAtZFIN\t@$ref_arrayAccs \n";
  }
  close DBG1;

  open (DBG2, ">debug2") ||  die "Cannot open debug2 : $!\n";

  foreach $accAtZFIN (sort keys %supportingAccZFIN) {
    $ref_arrayGenes = $supportingAccZFIN{$accAtZFIN};
    print DBG2 "$accAtZFIN\t@$ref_arrayGenes \n";
  }

  close DBG2;
}

#-----------------------------------------------------------------------------------------------------------------------------------
#   Get the real initial mapping set at ZFIN end

## %accZFINsupportingMoreThan1 is a hash storing the references to array of genes for GenBank RNA accessions that supports more than 1 genes at ZFIN
## key:   GenBank RNA accession that supports more than 1 genes at ZFIN
## value: reference to array of more than 1 zdb gene ids supported by the GenBank RNA accession
## example: $accZFINsupportingMoreThan1{$acc1} = [$zdbGeneId1, $zdbGeneId2]
## %accZFINsupportingMoreThan1 is a subset of %supportingAccZFIN

%accZFINsupportingMoreThan1 = ();

## %geneZFINwithAccSupportingMoreThan1 is a hash storing the references to array of GenBank RNA accessions for genes
## that are supported by accession(s) at ZFIN with at least 1 of them supporting more than 1 genes
## key:    gene zdb id of the gene with at least 1 supporting GenBank RNA accessions supporting more than 1 genes
## value:  reference to array of GenBank RNA accessions supporting the gene, at least 1 of which supports other gene(s)
## example:  $geneZFINwithAccSupportingMoreThan1{$zdbGeneId1} = [$acc1, $acc2, $acc3]  (at least 1 of the 3 accs supporting more than 1 genes)
## %geneZFINwithAccSupportingMoreThan1 is a subset of %supportedGeneZFIN

%geneZFINwithAccSupportingMoreThan1 = ();

## %accZFINsupportingOnly1 is a hash storing the genes at ZFIN supported by the GenBank RNA accession that does NOT support another gene
## key:    GenBank RNA accession
## value:  gene zdb id of the gene
## example:  $accZFINsupportingOnly1{$acc1} = zdbGeneId1

%accZFINsupportingOnly1 = ();

##-------------------------------------------------------------------------------------------------------------------------------------
## traverse the hash of %supportingAccZFIN to find and mark those GenBank RNA accessions stored at ZFIN supporting more than 1 genes
## also mark those accs that support only 1 gene at ZFIN
## also mark those genes supported by 2 or more GenBank RNA accessions
##-------------------------------------------------------------------------------------------------------------------------------------

$ctAllSupportingAccZFIN = $ctAccZFINSupportingMoreThan1 = $ctAccZFINSupportingOnly1 = 0;

foreach $acc (keys %supportingAccZFIN) {
   $ctAllSupportingAccZFIN++;

   $ref_arrayOfGenes = $supportingAccZFIN{$acc};

   if ($#$ref_arrayOfGenes > 0) {  ## if the last index > 0, indicating more than 1 genes supported
       $ctAccZFINSupportingMoreThan1++;
       $accZFINsupportingMoreThan1{$acc} = $ref_arrayOfGenes;

       foreach $genesInQuestion (@$ref_arrayOfGenes) {
         $ref_arrayOfAccs = $supportedGeneZFIN{$genesInQuestion};
         $geneZFINwithAccSupportingMoreThan1{$genesInQuestion} = $ref_arrayOfAccs;
       }
   } else {  ## the acc only supports 1 gene

       $ctAccZFINSupportingOnly1++;

       foreach $geneWithAccSupportingOnly1 (@$ref_arrayOfGenes) { ## only 1 element in the array
         $accZFINsupportingOnly1{$acc} = $geneWithAccSupportingOnly1;
       }
   }

}

print STATS "\n\nThe following GenBank RNA accessions found at ZFIN are associated with multiple ZFIN genes.";
print STATS "\nThe ZDB Gene Ids associated with these GenBank RNAs are excluded from mapping and hence the loading.\n\n";

open (DBG3, ">debug3") ||  die "Cannot open debug3 : $!\n" if $debug;
$ctGenBankRNAsupportingMultipleZFINgenes = 0;
foreach $accSupportingMoreThan1 (sort keys %accZFINsupportingMoreThan1) {
  $ref_accSupportingMoreThan1 = $accZFINsupportingMoreThan1{$accSupportingMoreThan1};
  print STATS "$accSupportingMoreThan1\t@$ref_accSupportingMoreThan1\n";
  print DBG3 "$accSupportingMoreThan1\t@$ref_accSupportingMoreThan1\n" if $debug;
  $ctGenBankRNAsupportingMultipleZFINgenes++;
}
close DBG3 if $debug;

print STATS "\nTotal: $ctGenBankRNAsupportingMultipleZFINgenes\n\n";

if ($debug) {
  open (DBG4, ">debug4") ||  die "Cannot open debug4 : $!\n";

  foreach $geneWithAtLeast1accSupportingMoreThan1 (sort keys %geneZFINwithAccSupportingMoreThan1) {
    $ctGenesZFINwithAccSupportingMoreThan1++;
    $ref_accs = $geneZFINwithAccSupportingMoreThan1{$geneWithAtLeast1accSupportingMoreThan1};
    print DBG4 "$geneWithAtLeast1accSupportingMoreThan1\t@$ref_accs\n";
  }

  close DBG4;
}

print LOG "\nThe following should add up \nctAccZFINSupportingOnly1 + ctAccZFINSupportingMoreThan1 = ctAllSupportingAccZFIN \nOtherwise there is bug.\n";
print LOG "$ctAccZFINSupportingOnly1 + $ctAccZFINSupportingMoreThan1 = $ctAllSupportingAccZFIN\n\n";

## if the numbers don't add up, stop the whole process
if ($ctAccZFINSupportingOnly1 + $ctAccZFINSupportingMoreThan1 != $ctAllSupportingAccZFIN) {
   close STATS;
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: some numbers don't add up";
   &reportErrAndExit($subjectLine);
}

$ctGenesZFINwithAccSupportingMoreThan1 = 0;

print LOG "ctGenesZFINwithAccSupportingMoreThan1 = $ctGenesZFINwithAccSupportingMoreThan1\n\n";

#--------------------------------------------------------------------------------------------------------------
# Step 5-2: Get dblink_length values
#
# This section continues to deal with dblink_length field
# There are 3 sources for length:
# 1) the existing dblink_length for GenBank including GenPept records
# 2) the length value of RefSeq sequences on NCBI's RefSeq-release#.catalog file
# 3) calculated length
# 1) and 2) will be done by the following section and before parsing the gene2accession file.
# And the length value will be stored in hash %sequenceLength
# During parsing gene2accession file, accessions still missing length will be stored in a hash named %noLength
# 3) will be done after parsing gene2accession file.
#---------------------------------------------------------------------------------------------------------------

# use the following hash to store db_link sequence accession length
# key: seqence accession
# value: length

%sequenceLength = ();

#---------------------- 1) store the dblink_length of GenBank accessions -----------------------

$sqlGenBankAccessionLength = 'select dblink_acc_num, dblink_length
                                from db_link
                               where dblink_length is not null
                                 and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
                                 and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-37","ZDB-FDBCONT-040412-42","ZDB-FDBCONT-040412-36");';


$curGenBankAccessionLength = $handle->prepare($sqlGenBankAccessionLength);

$curGenBankAccessionLength->execute;

$curGenBankAccessionLength->bind_columns(\$GenBankAcc,\$seqLength);

$ctGenBankSeqLengthAtZFIN = 0;
while ($curGenBankAccessionLength->fetch) {
   $ctGenBankSeqLengthAtZFIN++;
   $sequenceLength{$GenBankAcc} = $seqLength;
}

$curGenBankAccessionLength->finish();

print LOG "\nctGenBankSeqLengthAtZFIN = $ctGenBankSeqLengthAtZFIN\n\n";

#----------------------- 2) parse RefSeq-release#.catalog file to get the length for RefSeq sequences ----------------------

$ctRefSeqLengthFromCatalog = 0;

open (REFSEQCATALOG, "RefSeqCatalog") ||  die "Cannot open RefSeqCatalog : $!\n";

## Sample record (last column is length of the sequence):
## 7955    Danio rerio     NP_001001398.2  89191828        complete|vertebrate_other       PROVISIONAL     205

while (<REFSEQCATALOG>) {
  chomp;

  if ($_) {

    undef @fields;
    undef $refSeqLength;
    @fields = split("\t");

    $taxId = $fields[0];

    ## don't process if it is not zebrafish gene
    next if $taxId ne "7955";

    $refSeqAcc = $fields[2];
    $refSeqAcc =~ s/\.\d+//;     # truncate version number

    $refSeqLength = $fields[6];

    if ($refSeqLength) {
      $ctRefSeqLengthFromCatalog++;
      $sequenceLength{$refSeqAcc} = $refSeqLength
    }

  }

}

print LOG "\nctRefSeqLengthFromCatalog = $ctRefSeqLengthFromCatalog\n\n";

close REFSEQCATALOG;

$ctAccWithLength = 0;
foreach $accWithLength (keys %sequenceLength) {
  $ctAccWithLength++;
}

print LOG "\nctAccWithLength = $ctAccWithLength";

#------------------------------------------------------------------------------------------------------------------------------------
# Step 5-3: initial set of NCBI records
#
# This section of code parses the NCBI's gene2accession file and
# 1) store GenBank RNA accessions as supporting RNA evidence used for mapping
# 2) store GenPept, GenBank DNA, RefSeq RNA, RefPept, and RefSeq DNA accessions as well
# 3) look up length for all these accessions; if not found, put them in the hash %noLength
#------------------------------------------------------------------------------------------------------------------------------------

## %supportedGeneNCBI is a hash to store references to arrays of GenBank RNA accessions supporting one NCBI zebrafish gene
## key:    NCBI zebrafish gene id
## value:  reference to the array of accession(s) that support the gene
## example1:  $supportedGeneNCBI{$ncbiGeneId1} = [$acc1, $acc2]
## example2:  $supportedGeneNCBI{$ncbiGeneId2} = [$acc3, $acc4, $acc5]

%supportedGeneNCBI = ();

## %supportingAccNCBI is a hash to store references to arrays of NCBI zf gene Ids supported by one GenBank accession
## key:    GenBank RNA accession
## value:  reference to the array of NCBI zf gene id(s) that is supported by the GenBank RNA accession
## example1:  $supportingAccNCBI{$acc1} = [$ncbiGeneId1]               -- potential 1:1 if same on ZFIN end
## example2:  $supportingAccNCBI{$acc2} = [$ncbiGeneId2, $ncbiGeneId3] -- 1 acc supporting 2 genes, which won't be used as evidence in mapping

%supportingAccNCBI = ();

# Use the following hashes to store all kinds of RefSeq and GenBank accessions on gene2accession file,
# except for GenBank RNA accessions, which are stored in the hash, %supportingAccNCBI, documented above.

# key: sequence accession
# value: NCBI gene Id

%GenPeptNCBIgeneIds = %GenBankDNAncbiGeneIds = %RefSeqRNAncbiGeneIds = %RefPeptNCBIgeneIds = %RefSeqDNAncbiGeneIds = ();

# Use the following hash to store those sequence accessions with which length value could not be found in the hash, %sequenceLength
# key: sequence accession
# value: NCBI gene Id

%noLength = ();

$ctNoLength = $ctNoLengthRefSeq = $ctlines = $ctZebrafishGene2accession = 0;

print LOG "\nParsing NCBI gene2accession file ... \n\n";

open (GENE2ACC, "gene2accession") ||  die "Cannot open gene2accession : $!\n";

##Format: tax_id GeneID status RNA_nucleotide_accession.version RNA_nucleotide_gi protein_accession.version protein_gi genomic_nucleotide_accession.version genomic_nucleotide_gi start_position_on_the_genomic_accession end_position_on_the_genomic_accession orientation assembly mature_peptide_accession.version mature_peptide_gi Symbol

while (<GENE2ACC>) {
  chomp;

  if ($_) {

    $ctlines++;

    ## the first line is just description of the fields, not the data
    next if $ctlines < 2;

    undef @fields;
    @fields = split("\t");

    $taxId = $fields[0];

    ## don't process if it is not zebrafish gene
    next if $taxId ne "7955";

    $ctZebrafishGene2accession++;

    $NCBIgeneId = $fields[1];

    $status = $fields[2];

    if ($status eq "-") {
        if (ZFINPerlModules->stringStartsWithLetter($fields[3])) {
          $GenBankRNAaccNCBI = $fields[3];
          $GenBankRNAaccNCBI =~ s/\.\d+//;  ## truncate version number

          if (!exists($sequenceLength{$GenBankRNAaccNCBI})) {
            $noLength{$GenBankRNAaccNCBI} = $NCBIgeneId;
            $ctNoLength++;
          }

          ## if the array of the GenBank RNA accession(s) supporting the NCBI gene Id has not been created yet

          if (!exists($supportedGeneNCBI{$NCBIgeneId})) {

              ## then create it with this supporting GenBank RNA accession

              $ref_arrayAccs = [$GenBankRNAaccNCBI];

              $supportedGeneNCBI{$NCBIgeneId} = $ref_arrayAccs;

          } else {  ## otherwise, add this supporting GenBank RNA accession into the array

              $ref_arrayAccs = $supportedGeneNCBI{$NCBIgeneId};

              # add it only when it is not the same as the last item
              push(@$ref_arrayAccs, $GenBankRNAaccNCBI) if $supportedGeneNCBI{$NCBIgeneId}[-1] ne $GenBankRNAaccNCBI;
          }

          ## if the array of NCBI gene Ids supported by this GenBank RNA accession has not been created yet

          if (!exists($supportingAccNCBI{$GenBankRNAaccNCBI})) {

              ## then create it with the first element (the supported NCBI gene id)

              $ref_arrayGenes = [$NCBIgeneId];
              $supportingAccNCBI{$GenBankRNAaccNCBI} = $ref_arrayGenes;

          } else {  ## otherwise, add this supported NCBI gene Id into the array

              $ref_arrayGenes = $supportingAccNCBI{$GenBankRNAaccNCBI};

              # add it only when it is not the same as the last item
              push(@$ref_arrayGenes, $NCBIgeneId) if $supportingAccNCBI{$GenBankRNAaccNCBI}[-1] ne $NCBIgeneId;
          }

        }  ## ending if (stringStartsWithLetter$fields[3]))

        if (ZFINPerlModules->stringStartsWithLetter($fields[5])) {
          $GenPeptAcc = $fields[5];
          $GenPeptAcc =~ s/\.\d+//;
          $GenPeptNCBIgeneIds{$GenPeptAcc} = $NCBIgeneId;

          if (!exists($sequenceLength{$GenPeptAcc})) {
            $noLength{$GenPeptAcc} = $NCBIgeneId;
            $ctNoLength++;
          }
        }

        if (ZFINPerlModules->stringStartsWithLetter($fields[7])) {
          $GenBankDNAacc = $fields[7];
          $GenBankDNAacc =~ s/\.\d+//;
          $GenBankDNAncbiGeneIds{$GenBankDNAacc} = $NCBIgeneId;

          if (!exists($sequenceLength{$GenBankDNAacc})) {
            $noLength{$GenBankDNAacc} = $NCBIgeneId;
            $ctNoLength++;
          }
        }
    } else {  # there is value of "status" field for all RefSeq accessions
        if (ZFINPerlModules->stringStartsWithLetter($fields[3])) {
          $RefSeqRNAacc = $fields[3];
          $RefSeqRNAacc =~ s/\.\d+//;
          $RefSeqRNAncbiGeneIds{$RefSeqRNAacc} = $NCBIgeneId if $RefSeqRNAacc =~ m/^NM/ or $RefSeqRNAacc =~ m/^XM/ or $RefSeqRNAacc =~ m/^NR/ or $RefSeqRNAacc =~ m/^XR/;

          if (!exists($sequenceLength{$RefSeqRNAacc})) {
            $noLength{$RefSeqRNAacc} = $NCBIgeneId;
            $ctNoLength++;
            $ctNoLengthRefSeq++;
          }
        }

        if (ZFINPerlModules->stringStartsWithLetter($fields[5])) {
          $RefPeptAcc = $fields[5];
          $RefPeptAcc =~ s/\.\d+//;
          $RefPeptNCBIgeneIds{$RefPeptAcc} = $NCBIgeneId;

          if (!exists($sequenceLength{$RefPeptAcc})) {
            $noLength{$RefPeptAcc} = $NCBIgeneId;
            $ctNoLength++;
            $ctNoLengthRefSeq++;
          }
        }

        if (ZFINPerlModules->stringStartsWithLetter($fields[7])) {
          $RefSeqDNAacc = $fields[7];
          $RefSeqDNAacc =~ s/\.\d+//;
          $RefSeqDNAncbiGeneIds{$RefSeqDNAacc} = $NCBIgeneId;

          if (!exists($sequenceLength{$RefSeqDNAacc})) {
            $noLength{$RefSeqDNAacc} = $NCBIgeneId;
            $ctNoLength++;
            $ctNoLengthRefSeq++;
          }
        }
    }

  }  # ending if ($status eq "-")

}

close GENE2ACC;

$ctlines--;    ## because the first line is just description of the fileds
print LOG "\n\nNumber of lines on gene2accession file:  $ctlines\n\n";
print LOG "\nctZebrafishGene2accession:  $ctZebrafishGene2accession\n\n";


print LOG "\nctNoLength = $ctNoLength\nctNoLengthRefSeq = $ctNoLengthRefSeq\n\n";

open (DBG5, ">debug5") ||  die "Cannot open debug5 : $!\n" if $debug;

$ctGeneIdsNCBIonGene2accession = 0;
foreach $geneAtNCBI (sort keys %supportedGeneNCBI) {
  $ctGeneIdsNCBIonGene2accession++;
  $ref_arrayAccs = $supportedGeneNCBI{$geneAtNCBI} if $debug;
  print DBG5 "$geneAtNCBI\t@$ref_arrayAccs\n" if $debug;
}

close DBG5 if $debug;

if ($debug) {
  open (DBG6, ">debug6") ||  die "Cannot open debug6 : $!\n";

  foreach $accAtNCBI (sort keys %supportingAccNCBI) {
    $ref_arrayGenes = $supportingAccNCBI{$accAtNCBI};
    print DBG6 "$accAtNCBI\t@$ref_arrayGenes\n";
  }

  close DBG6;
}

print LOG "\nThe number of NCBI genes with supporting GenBank RNA: $ctGeneIdsNCBIonGene2accession\n\n";

print STATS "\n\nThe number of NCBI genes with supporting GenBank RNA: $ctGeneIdsNCBIonGene2accession\n\n";

#---------------------------------------------------------------------------------------------------------------------------------

## %accNCBIsupportingMoreThan1 is a hash storing the references to array of genes for GenBank RNA accessions that supports more than 1 genes at NCBI
## key:   GenBank RNA accession that supports more than 2 genes at NCBI
## value: reference to array of more than 1 NCBI gene ids supported by the GenBank RNA accession
## example: $accNCBIsupportingMoreThan1{$acc1} = [$ncbiGene1, $ncbiGene2]
## %accNCBIsupportingMoreThan1 is a subset of %supportingAccNCBI

%accNCBIsupportingMoreThan1 = ();

## %geneNCBIwithAccSupportingMoreThan1 is a hash storing the references to array of GenBank RNA accessions for genes
## that are supported by accession(s) at NCBI with at least 1 of them supporting more than 1 genes
## key:    NCBI gene id of the gene with at least 1 supporting GenBank RNA accessions supporting more than 1 genes
## value:  reference to array of GenBank RNA accessions supporting the gene, at least 1 of which supports other gene(s)
## example:  $geneNCBIwithAccSupportingMoreThan1{$ncbiGeneId1} = [$acc1, $acc2, $acc3]  (at least 1 of the 3 accs supporting more than 1 genes)
## %geneNCBIwithAccSupportingMoreThan1 is a subset of %supportedGeneNCBI; can be called n's at NCBI;

%geneNCBIwithAccSupportingMoreThan1 = ();

## %accNCBIsupportingOnly1 is a hash storing the genes at NCBI supported by the GenBank RNA accession that does NOT support another gene
## key:    GenBank RNA accession
## value:  NCBI gene id of the gene
## example:  $accNCBIsupportingOnly1{$acc1} = ncbiGeneId1

%accNCBIsupportingOnly1 = ();

##------------------------------------------------------------------------------------------------------------------------------
## traverse the hash of %supportingAccNCBI to find and mark those GenBank RNA accessions at NCBI supporting more than 1 genes
## and also mark those accs that support only 1 gene at NCBI, which is vital for 1:1 mapping between NCBI and ZFIN
## also mark those genes supported by 2 or more GenBank RNA accessions
##------------------------------------------------------------------------------------------------------------------------------

$ctAllSupportingAccNCBI = $ctAccNCBISupportingMoreThan1 = $ctAccNCBISupportingOnly1 = 0;

foreach $acc (keys %supportingAccNCBI) {
   $ctAllSupportingAccNCBI++;

   $ref_arrayOfGenes = $supportingAccNCBI{$acc};

   if ($#$ref_arrayOfGenes > 0) {  ## if the last index > 0, indicating more than 1 genes supported
       $ctAccNCBISupportingMoreThan1++;
       $accNCBIsupportingMoreThan1{$acc} = $ref_arrayOfGenes;

       foreach $genesInQuestion (@$ref_arrayOfGenes) {
         $ref_arrayOfAccs = $supportedGeneNCBI{$genesInQuestion};
         $geneNCBIwithAccSupportingMoreThan1{$genesInQuestion} = $ref_arrayOfAccs;
       }
   } else {  ## the acc only supports 1 gene

       $ctAccNCBISupportingOnly1++;

       foreach $geneWithAccSupportingOnly1 (@$ref_arrayOfGenes) { ## only 1 element in the array
         $accNCBIsupportingOnly1{$acc} = $geneWithAccSupportingOnly1;
       }
   }

}

print STATS "\nThe following GenBank accession found on NCBI's gene2accession file support more than 1 NCBI genes\n";
print LOG "\nThe following GenBank accession found on NCBI's gene2accession file support more than 1 NCBI genes\n";

foreach $accSupportingMoreThan1 (sort keys %accNCBIsupportingMoreThan1) {
   $ref_accSupportingMoreThan1 = $accNCBIsupportingMoreThan1{$accSupportingMoreThan1};
   print STATS "$accSupportingMoreThan1\t@$ref_accSupportingMoreThan1\n";
   print LOG "$accSupportingMoreThan1\t@$ref_accSupportingMoreThan1\n";
}

print STATS "\nThe following NCBI's Gene Ids have at least 1 supporting GenBank accession that supports more than 1 NCBI genes\n";
print LOG "\nThe following NCBI's Gene Ids have at least 1 supporting GenBank accession that supports more than 1 NCBI genes\n";

foreach $geneWithAtLeast1accSupportingMoreThan1 (sort keys %geneNCBIwithAccSupportingMoreThan1) {
   $ref_accs = $geneNCBIwithAccSupportingMoreThan1{$geneWithAtLeast1accSupportingMoreThan1};
   print LOG "$geneWithAtLeast1accSupportingMoreThan1\t@$ref_accs\n";
   print STATS "$geneWithAtLeast1accSupportingMoreThan1\t@$ref_accs\n";
}

print LOG "\nThe following should add up \nctAccNCBISupportingOnly1 + ctAccNCBISupportingMoreThan1 = ctAllSupportingAccNCBI \nOtherwise there is bug.\n";
print LOG "$ctAccNCBISupportingOnly1 + $ctAccNCBISupportingMoreThan1 = $ctAllSupportingAccNCBI\n\n";

## if the numbers don't add up, stop the whole process
if ($ctAccNCBISupportingOnly1 + $ctAccNCBISupportingMoreThan1 != $ctAllSupportingAccNCBI) {
   close STATS;
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: some numbers don't add up";
   &reportErrAndExit($subjectLine);
}

#--------------------------------------------------------------------------
# Step 5-4: get 1:1, 1:N and 1:0 (from ZFIN to NCBI) lists
#
# pass 1 of the mapping: one-way mapping of ZFIN genes onto NCBI genes
#--------------------------------------------------------------------------

## %oneToNZFINtoNCBI is the hash to store the 1:n one-way mapping result of ZDB gene zdb id onto NCBI gene zdb id
## %oneToNZFINtoNCBI include those 1:N (ZFIN to NCBI) and N:N (ZFIN to NCBI)
## key:    zdb gene Id
## value:  referec to the hash of 2 or more NCBI gene Ids that are mapped to zdb gene id
## example: $oneToNZFINtoNCBI{$zdbGeneId1} = \%mappedNCBIgeneIdsSet1

%oneToNZFINtoNCBI = ();

## %oneToOneZFINtoNCBI is the hash to store the 1:1 one-way mapping result of NCBI gene zdb id onto ZFIN gene zdb id
## key:    zdb gene Id
## value:  NCBI gene Id that is mapped to the ZDB gene id and not mapped to another ZDB gene id
## example: $oneToOneZFINtoNCBI{$zdbGeneId1} = $NCBIgeneId1

%oneToOneZFINtoNCBI = ();

## %genesZFINwithNoRNAFoundAtNCBI is the hash to store the ZFIN genes with supporting accessions all of which are not found at NCBI's gene2accession file
## key:    zdb gene Id
## value:  reference to the array of accession(s) that support the gene at ZFIN but not found at NCBI's gene2accession file
## example: $genesZFINwithNoRNAFoundAtNCBI{$zdbGeneId1} = {acc1, acc2}

%genesZFINwithNoRNAFoundAtNCBI = ();

## doing the mapping of ZDB Gene Id to NCBI Gene Id based on the data in the following 3 hashes established before
## 1) %supportedGeneZFIN                     -- ZFIN genes that are supported by GenBank RNA accessions
## 2) %geneZFINwithAccSupportingMoreThan1    -- RNA-supported ZFIN genes having at least 1 RNA accession that supports other ZFIN gene
## those in 1) but not in 2) get processed
## 3) %accNCBIsupportingOnly1                -- GenBank accessions/NCBI gene Id pairs


$ct1to1ZFINtoNCBI = $ct1toNZFINtoNCBI = $ctProcessedZFINgenes = $ctZFINgenesSupported = $ctZFINgenesWithAllAccsNotFoundAtNCBI = 0;

foreach $zfinGene (keys %supportedGeneZFIN) {
   $ctZFINgenesSupported++;

   ## those genes with even just 1 supporting RNA sequence that supports another gene won't be processed
   if (!exists($geneZFINwithAccSupportingMoreThan1{$zfinGene})) {

      $ctProcessedZFINgenes++;

      ## refence to the array of supporting GenBank RNA accessions
      $ref_arrayOfAccs = $supportedGeneZFIN{$zfinGene};

      $ctAccsForGene = 0;
      $mapped1to1 = 1;
      $firstMappedNCBIgeneIdSaved = "None";

      $ct = 0;
      foreach $acc (@$ref_arrayOfAccs) {
        $ct++;

        ## only map ZFIN genes to NCBI genes that are with supporting RNA accessions supporting only 1 NCBI gene
        ## may have supporting acc at ZFIN that is not found at NCBI or not supporting any gene at NCBI; do nothing in such cases

        if (exists($accNCBIsupportingOnly1{$acc}) && !exists($accNCBIsupportingMoreThan1{$acc})) {

          $ctAccsForGene++;
          $NCBIgeneId = $accNCBIsupportingOnly1{$acc};  ## this is the NCBI gene Id that is mapped to ZDB gene Id based on the common RNA acc

          if ($ctAccsForGene == 1) {   # first acc in the supporting acc list for ZFIN gene which is also found at NCBI
              $firstMappedNCBIgeneIdSaved = $NCBIgeneId;
              $ref_mappedNCBIgeneIds = {$firstMappedNCBIgeneIdSaved => $acc};  ## anonymous hash to be put as value in an outer hash
              %NCBIgeneIdsSaved = %$ref_mappedNCBIgeneIds;                ## to be looked up to avoid redundant NCBI gene id
          } else {
              if (!exists($NCBIgeneIdsSaved{$NCBIgeneId})) {  ## if the gene is not found in the save hash, it means mapped to another NCBI gene
                                                              ## do nothing if it is found in the save hash
                  $mapped1to1 = 0;
                  $NCBIgeneIdsSaved{$NCBIgeneId} = $acc;         ## add it to the save hash
                  $ref_mappedNCBIgeneIds->{$NCBIgeneId} = $acc;   ## add it to the hash for mapped NCBI gene ids
              }
          }

        }

      }  # of foreach $acc (@$ref_arrayOfAccs)

      if ($mapped1to1 == 1 && $firstMappedNCBIgeneIdSaved ne "None") {
          $oneToOneZFINtoNCBI{$zfinGene} = $firstMappedNCBIgeneIdSaved;
          $ct1to1ZFINtoNCBI++;
      }

      if ($mapped1to1 == 0) {
          $ct1toNZFINtoNCBI++;
          $oneToNZFINtoNCBI{$zfinGene} = $ref_mappedNCBIgeneIds;
      }

      if ($mapped1to1 == 1 && $firstMappedNCBIgeneIdSaved eq "None") {
         $ctZFINgenesWithAllAccsNotFoundAtNCBI++;
         $genesZFINwithNoRNAFoundAtNCBI{$zfinGene} = $ref_arrayOfAccs;
      }
   }

}    # end of foreach $zfinGene (keys %supportedGeneZFIN)

if ($debug) {
  open (DBG9, ">debug9") ||  die "Cannot open debug9 : $!\n";
  foreach $geneZDBId (sort keys %oneToOneZFINtoNCBI) {
    $ncbiGeneId = $oneToOneZFINtoNCBI{$geneZDBId};
    print DBG9 "$geneZDBId \t $ncbiGeneId\n";
  }

  close DBG9;

  open (DBG10, ">debug10") ||  die "Cannot open debug10 : $!\n";

  foreach $zdbId (sort keys %oneToNZFINtoNCBI) {
    $ref_hashNCBIgenes = $oneToNZFINtoNCBI{$zdbId};
    print DBG10 "$zdbId\t";
    foreach $ncbiId (sort keys %$ref_hashNCBIgenes) {
       print DBG10 "$ncbiId ";
    }
    print DBG10 "\n";
  }

  close DBG10;
}

print LOG "\nctZFINgenesSupported = $ctZFINgenesSupported \nctProcessedZFINgenes = $ctProcessedZFINgenes\n\n";
print LOG "\nct1to1ZFINtoNCBI = $ct1to1ZFINtoNCBI \n ct1toNZFINtoNCBI = $ct1toNZFINtoNCBI\n\n";

print LOG "\nThe following should add up \nct1to1ZFINtoNCBI + ct1toNZFINtoNCBI + ctZFINgenesWithAllAccsNotFoundAtNCBI = ctProcessedZFINgenes \nOtherwise there is bug.\n";
print LOG "$ct1to1ZFINtoNCBI + $ct1toNZFINtoNCBI + $ctZFINgenesWithAllAccsNotFoundAtNCBI = $ctProcessedZFINgenes\n\n";

## if the numbers don't add up, stop the whole process
if ($ct1to1ZFINtoNCBI + $ct1toNZFINtoNCBI + $ctZFINgenesWithAllAccsNotFoundAtNCBI != $ctProcessedZFINgenes) {
   close STATS;
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: some numbers don't add up";
   &reportErrAndExit($subjectLine);
}

open (ONETOZERO, ">reportOneToZero") ||  die "Cannot open ONETOZERO : $!\n" if $debug;

$ctOneToZero = 0;
foreach $geneAtZFIN (sort keys %genesZFINwithNoRNAFoundAtNCBI) {
   $ref_arrayAccsNotFoundAtNCBI = $genesZFINwithNoRNAFoundAtNCBI{$geneAtZFIN} if $debug;
   print ONETOZERO "$geneAtZFIN\t@$ref_arrayAccsNotFoundAtNCBI \n" if $debug;
   $ctOneToZero++;
}

close ONETOZERO if $debug;

print LOG "\nctOneToZero = $ctOneToZero\n\n";

print STATS "\nMapping result statistics: number of 1:0 (ZFIN to NCBI) - $ctOneToZero\n\n";

#--------------------------------------------------------------------------------
# Step 5-5: get 1:1, 1:N and 1:0 (from NCBI to ZFIN) lists
#
# pass 2 of the mapping: one-way mapping of NCBI genes onto ZFIN genes
#--------------------------------------------------------------------------------

## %oneToNNCBItoZFIN is the hash to store the 1:n one-way mapping result of NCBI gene zdb id onto ZFIN gene zdb id
## key:    NCBI gene Id
## value:  referec to the hash of 2 or more ZDB gene Ids that are mapped to NCBI gene id
## example: $oneToNNCBItoZFIN{$ncbiGeneId1} = \%mappedZDBgeneIdsSet1

%oneToNNCBItoZFIN = ();

## %oneToOneNCBItoZFIN is the hash to store the 1:1 one-way mapping results of NCBI gene zdb id onto ZFIN gene zdb id
## %oneToOneNCBItoZFIN include those 1:N (NCBI to ZFIN) and N:N (NCBI to ZFIN)
## key:    NCBI gene Id
## value:  ZDB gene Id that is mapped to the NCBI gene id and not mapped to another NCBI gene id
## example: $oneToOneNCBItoZFIN{$zdbGeneId1} = $NCBIgeneId1

%oneToOneNCBItoZFIN = ();

## %genesNCBIwithAllAccsNotFoundAtZFIN is the hash to store the NCBI genes with supporting accessions all of which are not found at ZFIN
## key:    zdb gene Id
## value:  reference to the array of accession(s) that support the gene at NCBI but not found at ZFIN
## example: $genesNCBIwithAllAccsNotFoundAtZFIN{$zdbGeneId1} = {acc1, acc2}

%genesNCBIwithAllAccsNotFoundAtZFIN = ();

## doing the mapping of NCBI Gene Id to ZFIN Gene Id based on the data in the following 3 hashes populated before
## 1) %supportedGeneNCBI                     -- NCBI genes that are supported by GenBank RNA accessions
## 2) %geneNCBIwithAccSupportingMoreThan1    -- RNA-supported NCBI genes having at least 1 RNA accession that supports other NCBI gene
## those in 1) but not in 2) get processed
## 3) %accZFINsupportingOnly1                -- GenBank accessions/ZDB gene Id pairs


$ct1to1NCBItoZFIN = $ct1toNNCBItoZFIN = $ctProcessedNCBIgenes = $ctNCBIgenesSupported = $ctNCBIgenesWithAllAccsNotFoundAtZFIN = 0;

foreach $ncbiGene (keys %supportedGeneNCBI) {
   $ctNCBIgenesSupported++;

   ## those genes with even just 1 supporting RNA sequence that supports another gene won't be processed
   if (!exists($geneNCBIwithAccSupportingMoreThan1{$ncbiGene})) {

      $ctProcessedNCBIgenes++;

      ## refence to the array of supporting GenBank RNA accessions
      $ref_arrayOfAccs = $supportedGeneNCBI{$ncbiGene};

      $ctAccsForGene = 0;
      $mapped1to1 = 1;
      $firstMappedZFINgeneIdSaved = "None";

      $ct = 0;
      foreach $acc (@$ref_arrayOfAccs) {
        $ct++;

        ## only map NCBI genes to ZFIN genes that are with supporting RNA accessions supporting only 1 ZFIN gene
        ## may have supporting acc at NCBI that is not found at ZFIN yet or not associated with any gene at ZFIN yet; do nothing in such cases
        if (exists($accZFINsupportingOnly1{$acc}) && !exists($accZFINsupportingMoreThan1{$acc})) {

          $ctAccsForGene++;
          $ZDBgeneId = $accZFINsupportingOnly1{$acc};  ## this is the ZDB gene Id that is mapped to NCBI gene Id based on the common RNA acc

          if ($ctAccsForGene == 1) {   # first acc in the supporting acc list for NCBI gene which is also found at ZFIN
              $firstMappedZFINgeneIdSaved = $ZDBgeneId;
              $ref_mappedZDBgeneIds = {$firstMappedZFINgeneIdSaved => $acc};  ## anonymous hash to be expanded and saved
              %ZDBgeneIdsSaved = %$ref_mappedZDBgeneIds;                ## to be looked up to avoid redundant ZDB gene id
          } else {
              if (!exists($ZDBgeneIdsSaved{$ZDBgeneId})) {  ## if the gene is not found in the save hash, it means mapped to > 1 ZFIN genes
                                                              ## do nothing if it is found in the save hash
                  $mapped1to1 = 0;
                  $ZDBgeneIdsSaved{$ZDBgeneId} = $acc;         ## add it to the save hash
                  $ref_mappedZDBgeneIds->{$ZDBgeneId} = $acc;   ## add it to the hash for mapped ZFIN gene ids
              }

          }

        }

      }  # of foreach $acc (@$ref_arrayOfAccs)

      if ($mapped1to1 == 1 && $firstMappedZFINgeneIdSaved ne "None") {
        $oneToOneNCBItoZFIN{$ncbiGene} = $firstMappedZFINgeneIdSaved;
        $ct1to1NCBItoZFIN++;
      }

      if ($mapped1to1 == 0) {
        $ct1toNNCBItoZFIN++;
        $oneToNNCBItoZFIN{$ncbiGene} = $ref_mappedZDBgeneIds;
      }

      if ($mapped1to1 == 1 && $firstMappedZFINgeneIdSaved eq "None") {
         $ctNCBIgenesWithAllAccsNotFoundAtZFIN++;
         $genesNCBIwithAllAccsNotFoundAtZFIN{$ncbiGene} = $ref_arrayOfAccs;
      }
   }

}    # end of foreach $ncbiGene (keys %supportedGeneNCBI)

if ($debug) {
  open (DBG12, ">debug12") ||  die "Cannot open debug12 : $!\n";

  foreach $geneZDBId (sort keys %oneToOneNCBItoZFIN) {
    $geneZDBId = $oneToOneNCBItoZFIN{$ncbiGeneId};
    print DBG12 "$ncbiGeneId \t $geneZDBId\n";
  }

  close DBG12;

  open (DBG13, ">debug13") ||  die "Cannot open debug13 : $!\n";

  foreach $ncbiId (sort keys %oneToNNCBItoZFIN) {
    $ref_hashZDBgenes = $oneToNNCBItoZFIN{$ncbiId};
    print DBG13 "$ncbiId\t";

    foreach $zdbId (sort keys %$ref_hashZDBgenes) {
      print DBG13 "$zdbId ";
    }

    print DBG13 "\n";
  }

  close DBG13;
}

open (DBG14, ">debug14") ||  die "Cannot open debug14 : $!\n" if $debug;

$ctzeroToOne = 0;
foreach $geneAtNCBI (sort keys %genesNCBIwithAllAccsNotFoundAtZFIN) {
   $ref_arrayAccsNotFoundAtZFIN = $genesNCBIwithAllAccsNotFoundAtZFIN{$geneAtNCBI} if $debug;
   print DBG14 "$geneAtNCBI\t@$ref_arrayAccsNotFoundAtZFIN \n" if $debug;
   $ctzeroToOne++;
}

close DBG14 if $debug;

print LOG "\nctzeroToOne = $ctzeroToOne\n\n";

print LOG "\nctNCBIgenesSupported = $ctNCBIgenesSupported \nctProcessedNCBIgenes = $ctProcessedNCBIgenes\n\n";
print LOG "\nct1to1NCBItoZFIN = $ct1to1NCBItoZFIN \n ct1toNNCBItoZFIN = $ct1toNNCBItoZFIN\n\n";

print LOG "\nThe following should add up \nct1to1NCBItoZFIN + ct1toNNCBItoZFIN + ctNCBIgenesWithAllAccsNotFoundAtZFIN = ctProcessedNCBIgenes \nOtherwise there is bug.\n";
print LOG "$ct1to1NCBItoZFIN + $ct1toNNCBItoZFIN + $ctNCBIgenesWithAllAccsNotFoundAtZFIN = $ctProcessedNCBIgenes\n\n";

## if the numbers don't add up, stop the whole process
if ($ct1to1NCBItoZFIN + $ct1toNNCBItoZFIN + $ctNCBIgenesWithAllAccsNotFoundAtZFIN != $ctProcessedNCBIgenes) {
   close STATS;
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: some numbers don't add up";
   &reportErrAndExit($subjectLine);
}

print STATS "\nMapping result statistics: number of 0:1 (ZFIN to NCBI) - $ctzeroToOne\n\n";

#------------------------------------------------------------------------------------------------------------------------
# Step 5-6: compare the 2-way mapping results and get the final 1:1, 1:N, N:1, and N:N lists
#
# pass 3 of the mapping: compare the results of both of the one-way mappings and make the final 1:1, 1:N, N:1, N:N lists
#------------------------------------------------------------------------------------------------------------------------

%mapped = ();  ## the list of 1:1; key: ZDB gene Id; value: NCBI gene Id
%mappedReversed = ();  ## the list of 1:1; key: NCBI gene Id; value: ZDB gene Id

$ctAllpotentialOneToOneZFIN = $ctOneToOneZFIN = 0;

foreach $zdbid (keys %oneToOneZFINtoNCBI) {
   $ctAllpotentialOneToOneZFIN++;
   $ncbiId = $oneToOneZFINtoNCBI{$zdbid};
   if (exists($oneToOneNCBItoZFIN{$ncbiId})) {
         $ctOneToOneZFIN++;
         $mapped{$zdbid} = $ncbiId;
   }
}

print LOG "\n ctAllpotentialOneToOneZFIN = $ctAllpotentialOneToOneZFIN \n ctOneToOneZFIN = $ctOneToOneZFIN\n\n";

$ctAllpotentialOneToOneNCBI = $ctOneToOneNCBI = 0;
foreach $ncbiId (keys %oneToOneNCBItoZFIN) {
   $ctAllpotentialOneToOneNCBI++;
   $zdbId = $oneToOneNCBItoZFIN{$ncbiId};
   if (exists($oneToOneZFINtoNCBI{$zdbId})) {
         $ctOneToOneNCBI++;    ## this number should be the same as $ctOneToOneZFIN
         $mappedReversed{$ncbiId} = $zdbId;
   }
}

print LOG "\n ctAllpotentialOneToOneNCBI = $ctAllpotentialOneToOneNCBI \n ctOneToOneNCBI = $ctOneToOneNCBI\n\n";

print STATS "\nMapping result statistics: number of 1:1 based on GenBank RNA - $ctOneToOneNCBI\n\n";

#---------------- open a .unl file as the add list -----------------

open (TOLOAD, ">toLoad.unl") ||  die "Cannot open toLoad.unl : $!\n";

# -------- write the NCBI gene Ids mapped based on GenBank RNA accessions on toLoad.unl ------------
$ctToLoad = 0;

foreach $zdbId (sort keys %mapped) {
  $mappedNCBIgeneId = $mapped{$zdbId};
  print TOLOAD "$zdbId|$mappedNCBIgeneId|||$fdcontNCBIgeneId|$pubMappedbasedOnRNA|\n";
  $ctToLoad++;
}

#------------------------ get 1:N list and N:N from ZFIN to NCBI -----------------------------

# %nToOne is a hash storing NCBI gene Ids in 1 to N mapping results mapped from NCBI to ZFIN
# 1 to N from NCBI to ZFIN is equivalent to N to 1 from ZFIN to NCBI
# key: NCBI gene Id
# value: reference to hash of mapped ZDB gene Ids

%nToOne = ();

# %oneToN is a hash storing ZDB gene Ids in 1 to N mapping results mapped from ZFIN to NCBI
# key: ZDB gene Id
# value: reference to hash of mapped NCBI gene Ids

%oneToN = ();

$ctOneToN = $ctNtoNfromZFIN = 0;

# report N:N
open (NTON, ">reportNtoN") ||  die "Cannot open reportNtoN : $!\n";

foreach $geneZFINtoMultiNCBI (sort keys %oneToNZFINtoNCBI) {

   # %zdbIdsOfNtoN is a hash storing ZDB gene Ids in N to N mapping results mapped from ZFIN to NCBI and back to ZFIN
   # key: ZDB gene Id
   # value: reference to hash of associated NCBI gene Id(s)

   %zdbIdsOfNtoN = ();

   ## set on the flag of 1 to N (ZFIN to NCBI)
   $oneToNflag = 1;

   # get the reference to the hash of mapped NCBI genes for this ZFIN gene
   $ref_hashNCBIids = $oneToNZFINtoNCBI{$geneZFINtoMultiNCBI};

   ## for each 1 to N (ZFIN to NCBI), examine if there is 1 to N mapping the other way (NCBI to ZFIN)
   foreach $ncbiId (sort keys %$ref_hashNCBIids) {

     ## if existing 1 to N the other way (NCBI to ZFIN), indicating N to N
     if (exists($oneToNNCBItoZFIN{$ncbiId})) {

       ## set off flag 1 to N (ZFIN to NCBI)
       $oneToNflag = 0;

       $ref_hashZdbIds = $oneToNNCBItoZFIN{$ncbiId};
       foreach $zdbId (keys %$ref_hashZdbIds) {
         if (exists($oneToNZFINtoNCBI{$zdbId})) {
             $zdbIdsOfNtoN{$zdbId} = $oneToNZFINtoNCBI{$zdbId};
         } elsif (exists($oneToOneZFINtoNCBI{$zdbId})) {
             $mappedNCBIgene = $oneToOneZFINtoNCBI{$zdbId};
             $zdbIdsOfNtoN{$zdbId} = {$mappedNCBIgene => 1};
         } else {                              ## impossible
             print LOG "\n\nThere is a bug: $zdbId is one of the mapped ZDB Ids of $ncbiId but could not find a mapped NCBI Id?\n\n";
         }
       }
     }
   }

   # print N to N if it is the case, otherwise, populate the 1 to N (ZFIN to NCBI) list
   if ($oneToNflag == 0) {  ## 1 to N flag off means N to N
       $ctNtoNfromZFIN++;

       print NTON "$ctNtoNfromZFIN) -------------------------------------------------------------------------------------------------\n";
       foreach $zdbIdNtoN (sort keys %zdbIdsOfNtoN) {
         $refArrayAccs = $supportedGeneZFIN{$zdbIdNtoN};
         print NTON "$zdbIdNtoN ($geneZDBidsSymbols{$zdbIdNtoN}) [@$refArrayAccs]\n";

         $refAssociatedNCBIgenes = $zdbIdsOfNtoN{$zdbIdNtoN};
         # for each mapped NCBI gene
         foreach $ncbiId (sort keys %$refAssociatedNCBIgenes) {
           $refArrayAccs = $supportedGeneNCBI{$ncbiId};
           print NTON "	$ncbiId ($NCBIidsGeneSymbols{$ncbiId}) [@$refArrayAccs]\n";
         }
       }

       print NTON "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
       foreach $ncbiGene (sort keys %$ref_hashNCBIids) {
         $refArrayAccs = $supportedGeneNCBI{$ncbiGene};
         print NTON "$ncbiGene ($NCBIidsGeneSymbols{$ncbiGene}) [@$refArrayAccs]\n";

         # the following print the associated ZFIN records for each of the mapped NCBI gene

         if (exists($oneToNNCBItoZFIN{$ncbiGene})) {
             $refAssociatedZFINgenes = $oneToNNCBItoZFIN{$ncbiGene};

             # for each of the associated ZFIN gene
             foreach $zdbId (sort keys %$refAssociatedZFINgenes) {
               $refArrayAccs = $supportedGeneZFIN{$zdbId};
               print NTON "	$zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n";
             }
         } elsif (exists($oneToOneNCBItoZFIN{$ncbiGene})) {
             $mappedZFINgene = $oneToOneNCBItoZFIN{$ncbiGene};
             $refArrayAccs = $supportedGeneZFIN{$mappedZFINgene};
             print NTON "	$mappedZFINgene ($geneZDBidsSymbols{$mappedZFINgene}) [@$refArrayAccs]\n";
         } else {                              ## impossible
             print NTON "There is a bug: $ncbiGene is one of the mapped NCBI gene Ids of $geneZFINtoMultiNCBI but could not find a mapped ZDB Id?\n\n";
         }
       }

       print NTON "\n";

   } else {                 ## 1 to N (ZFIN to NCBI)
       $ctOneToN++;
       $oneToN{$geneZFINtoMultiNCBI} = $ref_hashNCBIids;
   }

}

print NTON "\n**** the above N to N are derived from mapping ZFIN records to NCBI records and then back to ZFIN records *****\n";
print NTON "\n**** the following N to N are derived from mapping NCBI records to ZFIN record and then back to NCBI records ****\n";
print NTON "\n******** redundancy between the two parts of reporting N to N is expected ***********\n\n";

print LOG "\nctOneToN = $ctOneToN\nctNtoNfromZFIN = $ctNtoNfromZFIN\n\n";

print STATS "\nMapping result statistics: number of 1:N (ZFIN to NCBI) - $ctOneToN\n\n";
print STATS "\nMapping result statistics: number of N:N (ZFIN to NCBI) - $ctNtoNfromZFIN\n\n";

#------------------------ get N:1 list and N:N from ZFIN to NCBI -----------------------------

$ctNtoOne = $ctNtoNfromNCBI = 0;

# the following hash stores those zdb gene ids that are involved in N:1 and N:N (ZFIN to NCBI)
%zdbGeneIdsNtoOneAndNtoN = ();

foreach $geneNCBItoMultiZFIN (sort keys %oneToNNCBItoZFIN) {
   # %ncbiIdsOfNtoN is a hash storing NCBI gene Ids in N to N mapping results mapped from NCBI to ZFIN and back to NCBI
   # key: NCBI gene Id
   # value: reference to hash of associated ZDB gene Id(s)

   %ncbiIdsOfNtoN = ();

   ## set on the flag of 1 to N (NCBI to ZFIN)
   $oneToNflag = 1;

   # get the reference to the hash of mapped ZFIN genes for this NCBI gene
   $ref_hashZFINids = $oneToNNCBItoZFIN{$geneNCBItoMultiZFIN};

   ## for each 1 to N (NCBI to ZFIN), examine if there is 1 to N mapping the other way (ZFIN to NCBI)
   foreach $zfinId (sort keys %$ref_hashZFINids) {

     $zdbGeneIdsNtoOneAndNtoN{$zfinId} = $geneNCBItoMultiZFIN;

     ## if existing 1 to N the other way (ZFIN to NCBI), indicating N to N
     if (exists($oneToNZFINtoNCBI{$zfinId})) {

       ## set off flag 1 to N (NCBI to ZFIN)
       $oneToNflag = 0;

       $ref_hashNcbiIds = $oneToNZFINtoNCBI{$zfinId};
       foreach $ncbiId (keys %$ref_hashNcbiIds) {
         if (exists($oneToNNCBItoZFIN{$ncbiId})) {
             $ncbiIdsOfNtoN{$ncbiId} = $oneToNNCBItoZFIN{$ncbiId};
         } elsif (exists($oneToOneNCBItoZFIN{$ncbiId})) {
             $mappedZFINgene = $oneToOneNCBItoZFIN{$ncbiId};
             $ncbiIdsOfNtoN{$ncbiId} = {$mappedZFINgene => 1};
         } else {                              ## impossible
             print LOG "\n\nThere is a bug: $ncbiId is one of the mapped NCBI Ids of $zfinId but could not find a mapped ZDB Id?\n\n";
         }
       }
     }
   }

   # print N to N if it is the case, otherwise, populate the 1 to N (ZFIN to NCBI) list, i.e. the N to 1 (ZFIN to NCBI) list
   if ($oneToNflag == 0) {  ## 1 to N flag off means N to N
       $ctNtoNfromNCBI++;

       print NTON "$ctNtoNfromNCBI -------------------------------------------------------------------------------------------------\n";
       foreach $ncbiIdNtoN (sort keys %ncbiIdsOfNtoN) {
         $refArrayAccs = $supportedGeneNCBI{$ncbiIdNtoN};
         print NTON "$ncbiIdNtoN ($NCBIidsGeneSymbols{$ncbiIdNtoN}) [@$refArrayAccs]\n";

         $refAssociatedZFINgenes = $ncbiIdsOfNtoN{$ncbiIdNtoN};
         # for each mapped ZFIN gene
         foreach $zdbId (sort keys %$refAssociatedZFINgenes) {
           $refArrayAccs = $supportedGeneZFIN{$zdbId};
           print NTON "	$zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n";
         }
       }

       print NTON "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
       foreach $zdbId (sort keys %$ref_hashZFINids) {
         $refArrayAccs = $supportedGeneZFIN{$zdbId};
         print NTON "$zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n";

         # the following print the associated NCBI records for each of the mapped ZDB gene

         if (exists($oneToNZFINtoNCBI{$zdbId})) {
             $refAssociatedNCBIgenes = $oneToNZFINtoNCBI{$zdbId};

             # for each of the associated ZFIN gene
             foreach $ncbiGene (sort keys %$refAssociatedNCBIgenes) {
               $refArrayAccs = $supportedGeneNCBI{$ncbiGene};
               print NTON "	$ncbiGene ($NCBIidsGeneSymbols{$ncbiGene}) [@$refArrayAccs]\n";
             }
         } elsif (exists($oneToOneZFINtoNCBI{$zdbId})) {
             $mappedNCBIgene = $oneToOneZFINtoNCBI{$zdbId};
             $refArrayAccs = $supportedGeneNCBI{$mappedNCBIgene};
             print NTON "	$mappedNCBIgene ($NCBIidsGeneSymbols{$mappedNCBIgene}) [@$refArrayAccs]\n";
         } else {                              ## impossible
             print NTON "There is a bug: $zdbId is one of the mapped ZFIN gene Ids of $geneNCBItoMultiZFIN but could not find a mapped NCBI Id?\n\n";
         }
       }

       print NTON "\n";

   } else {                 ## 1 to N (NCBI to ZFIN) i.e. N to 1 from ZFIN to NCBI
       $ctNtoOne++;
       $nToOne{$geneNCBItoMultiZFIN} = $ref_hashZFINids;
   }
}

close NTON;

print LOG "\nctNtoOne = $ctNtoOne\nctNtoNfromNCBI = $ctNtoNfromNCBI\n\n";

print STATS "\nMapping result statistics: number of N:1 (ZFIN to NCBI) - $ctNtoOne\n\n";
print STATS "\nMapping result statistics: number of N:N (NCBI to ZFIN) - $ctNtoNfromNCBI\n\n";

$subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: List of N to N";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_REPORT|-->","$subject","reportNtoN");

#--------------------- report 1:N ---------------------------------------------

open (ONETON, ">reportOneToN") ||  die "Cannot open reportOneToN : $!\n";

$ct = 0;
foreach $zdbId (sort keys %oneToN) {
  $ct++;
  print ONETON "$ct) ---------------------------------------------\n";
  $refArrayAccs = $supportedGeneZFIN{$zdbId};
  print ONETON "$zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n\n";

  $refHashMultiNCBIgenes = $oneToN{$zdbId};
  foreach $ncbiId (sort keys %$refHashMultiNCBIgenes) {
    $refArrayAccs = $supportedGeneNCBI{$ncbiId};
    print ONETON "   $ncbiId ($NCBIidsGeneSymbols{$ncbiId}) [@$refArrayAccs]\n\n";
  }
}

close ONETON;

$subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: List of 1 to N";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_REPORT|-->","$subject","reportOneToN");

#------------------- report N:1 -------------------------------------------------

open (NTOONE, ">reportNtoOne") ||  die "Cannot open reportNtoOne : $!\n";

$ct = 0;
foreach $ncbiId (sort keys %nToOne) {
  $ct++;
  print NTOONE "$ct) ---------------------------------------------\n";
  $refArrayAccs = $supportedGeneNCBI{$ncbiId};
  print NTOONE "$ncbiId ($NCBIidsGeneSymbols{$ncbiId}) [@$refArrayAccs]\n\n";

  $refHashMultiZFINgenes = $nToOne{$ncbiId};
  foreach $zdbId (sort keys %$refHashMultiZFINgenes) {
    $refArrayAccs = $supportedGeneZFIN{$zdbId};
    print NTOONE "   $zdbId ($geneZDBidsSymbols{$zdbId}) [@$refArrayAccs]\n\n";
  }
}

close NTOONE;

$subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: List of N to 1";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_REPORT|-->","$subject","reportNtoOne");

##-----------------------------------------------------------------------------------
## Step 6: map ZFIN gene records to NCBI gene Ids based on common Vega Gene Id
##-----------------------------------------------------------------------------------

#---------------------------------------------------------------------------
# prepare the list of ZFIN gene with Vega Ids to be mapped to NCBI records
#---------------------------------------------------------------------------

$sqlGetVEGAidAndGeneZDBId = 'select mrel_mrkr_1_zdb_id, dblink_acc_num
                               from marker_relationship, db_link
                              where mrel_mrkr_2_zdb_id = dblink_linked_recid
                                and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-14"
                                and (mrel_mrkr_1_zdb_id like "ZDB-GENE%" or mrel_mrkr_1_zdb_id like "%RNAG%")
                                and dblink_acc_num like "OTTDARG%"
                                and mrel_type = "gene produces transcript";';

$curGetZDBgeneIdVegaGeneId = $handle->prepare($sqlGetVEGAidAndGeneZDBId);
$curGetZDBgeneIdVegaGeneId->execute();
$curGetZDBgeneIdVegaGeneId->bind_columns(\$geneZdbId,\$VegaGeneId);

## store the ZDB Gene ID/VEGA Gene Id and VEGA Gene Id/ZDB Gene ID pairs in hashes
## also store those ZDB gene Ids with multiple corresponding VEGA Gene Ids

%ZDBgeneAndVegaGeneIds = %VegaGeneAndZDBgeneIds = %ZDBgeneWithMultipleVegaGeneIds = %vegaGeneIdWithMultipleZFINgenes = ();

$ctTotalZDBgeneIdVegaGeneIds = 0;

while ($curGetZDBgeneIdVegaGeneId->fetch()) {
   $ctTotalZDBgeneIdVegaGeneIds++;

   if (exists($ZDBgeneWithMultipleVegaGeneIds{$geneZdbId}) || (exists($ZDBgeneAndVegaGeneIds{$geneZdbId}) && $ZDBgeneAndVegaGeneIds{$geneZdbId} ne $VegaGeneId)) {
     if (!exists($ZDBgeneWithMultipleVegaGeneIds{$geneZdbId})) {
         $firstVegaGeneIdFound = $ZDBgeneAndVegaGeneIds{$geneZdbId};
         $ref_arrayVegaGeneIds = [$firstVegaGeneIdFound,$VegaGeneId];
         $ZDBgeneWithMultipleVegaGeneIds{$geneZdbId} = $ref_arrayVegaGeneIds;
     } else {
         $ref_arrayVegaGeneIds = $ZDBgeneWithMultipleVegaGeneIds{$geneZdbId};
         push(@$ref_arrayVegaGeneIds, $VegaGeneId);
     }
   }

   if (exists($vegaGeneIdWithMultipleZFINgenes{$VegaGeneId})
       || (exists($VegaGeneAndZDBgeneIds{$VegaGeneId}) && $VegaGeneAndZDBgeneIds{$VegaGeneId} ne $geneZdbId)) {
     if (!exists($vegaGeneIdWithMultipleZFINgenes{$VegaGeneId})) {
         $firstGeneZDBidFound = $VegaGeneAndZDBgeneIds{$VegaGeneId};
         $ref_arrayZDBgenes = [$firstGeneZDBidFound,$geneZdbId];
         $vegaGeneIdWithMultipleZFINgenes{$VegaGeneId} = $ref_arrayZDBgenes;
     } else {
         $ref_arrayZDBgenes = $vegaGeneIdWithMultipleZFINgenes{$VegaGeneId};
         push(@$ref_arrayZDBgenes, $geneZdbId);
     }
   }

   $ZDBgeneAndVegaGeneIds{$geneZdbId} = $VegaGeneId;
   $VegaGeneAndZDBgeneIds{$VegaGeneId} = $geneZdbId;
}

print LOG "\nctTotalZDBgeneIdVegaGeneIds = $ctTotalZDBgeneIdVegaGeneIds\n\n";

print STATS "\nThe total number of ZFIN genes with Vega Gene Id: $ctTotalZDBgeneIdVegaGeneIds\n\n";

$curGetZDBgeneIdVegaGeneId->finish();

$ctVegaIdWithMultipleZDBgene = 0;
print LOG "\nThe following Vega Gene Ids at ZFIN correspond to multiple ZDB Gene Ids\n";
foreach $vega (sort keys %vegaGeneIdWithMultipleZFINgenes) {
  $ctVegaIdWithMultipleZDBgene++;
  $ref_arrayZDBgenes = $vegaGeneIdWithMultipleZFINgenes{$vega};
  print LOG "$vega @$ref_arrayZDBgenes\n";
}
print LOG "\nctVegaIdWithMultipleZDBgene = $ctVegaIdWithMultipleZDBgene\n\n";

open (ZDBGENENVEGA, ">reportZDBgeneIdWithMultipleVegaIds") ||  die "Cannot open reportZDBgeneIdWithMultipleVegaIds : $!\n";
$ctZDBgeneIdWithMultipleVegaId = 0;
foreach $zdbGene (sort keys %ZDBgeneWithMultipleVegaGeneIds) {
  $ctZDBgeneIdWithMultipleVegaId++;
  $ref_arrayVegaIds = $ZDBgeneWithMultipleVegaGeneIds{$zdbGene};
  print ZDBGENENVEGA "$zdbGene @$ref_arrayVegaIds\n";
}
print LOG "\nctZDBgeneIdWithMultipleVegaId = $ctZDBgeneIdWithMultipleVegaId\n\n";
close ZDBGENENVEGA;

%oneToOneViaVega = ();

$ctMappedViaVega = 0;

## ---------------------------------------------------------------------------------------------------------------------
## doing the mapping based on common Vega Gene Id
## ---------------------------------------------------------------------------------------------------------------------

foreach $zdbId (sort keys %geneZDBidsSymbols) {
  ## exclude those in the final 1:1, 1:N, N:1, N:N lists and those in the list of those
  ## with at least one GenBank RNA accession supporting more than 1 genes

  if (!exists($mapped{$zdbId}) && !exists($oneToNZFINtoNCBI{$zdbId}) && !exists($zdbGeneIdsNtoOneAndNtoN{$zdbId})
       && !exists($geneZFINwithAccSupportingMoreThan1{$zdbId})) {

    $ctProcessedZDBidForMappingViaVegaId++;
    if (exists($ZDBgeneAndVegaGeneIds{$zdbId}) && !exists($ZDBgeneWithMultipleVegaGeneIds{$zdbId})) {
      $vegaGeneId = $ZDBgeneAndVegaGeneIds{$zdbId};

      ## exclude those NCBI gene Ids that are in the final 1:1, 1:N, N:1, N:N lists and
      ## those in the list of those with at least 1 GenBank RNA accession supporting more than 1 genes

      if (exists($vegaIdsNCBIids{$vegaGeneId})
           && !exists($vegaIdwithMultipleNCBIids{$vegaGeneId})
           && !exists($vegaGeneIdWithMultipleZFINgenes{$vegaGeneId})
         ) {

          $NCBIgeneIdMappedViaVega = $vegaIdsNCBIids{$vegaGeneId};

          ## exclude those NCBI gene Ids with multiple Vega Gene Ids, and those with multiple GenBank RNAs,
          ## and those already mapped

          if (!exists($NCBIgeneWithMultipleVega{$NCBIgeneIdMappedViaVega})
            && !exists($geneNCBIwithAccSupportingMoreThan1{$NCBIgeneIdMappedViaVega})
            && !exists($mappedReversed{$NCBIgeneIdMappedViaVega})
             ) {

            ## ----- write the NCBI gene Ids mapped via Vega gene Id on toLoad.unl ---------------
            print TOLOAD "$zdbId|$NCBIgeneIdMappedViaVega|||$fdcontNCBIgeneId|$pubMappedbasedOnVega|\n";
            $ctToLoad++;

            $oneToOneViaVega{$NCBIgeneIdMappedViaVega} = $zdbId;
            $ctMappedViaVega++;
          }
      }
    }
  }
}

$ctTotalMapped = $ctMappedViaVega + $ctOneToOneNCBI;
print LOG "\nctMappedViaVega = $ctMappedViaVega\n\nTotal number of the gene records mapped: $ctMappedViaVega + $ctOneToOneNCBI = $ctTotalMapped\n\n";
print STATS "\nMapping result via Vega Gene Id: $ctMappedViaVega additional gene records are mapped\n\n";
print STATS "Total number of the gene records mapped: $ctMappedViaVega + $ctOneToOneNCBI = $ctTotalMapped\n\n";

#--------------------------------------------------------------------------------------------------------------
# This section CONTINUES to deal with dblink_length field
# There are 3 sources for length:
# 1) the existing dblink_length for GenBank including GenPept records
# 2) the length value of RefSeq sequences on NCBI's RefSeq-release#.catalog file
# 3) calculated length
# The first two have been done before parsing the gene2accession file.
# During parsing gene2accession file, accessions still missing length are stored in a hash named %noLength
#---------------------------------------------------------------------------------------------------------------

#----------------------- 3) calculate the length for the those still with no length ---------------

open (NOLENGTH, ">noLength.unl") ||  die "Cannot open noLength.unl : $!\n";

foreach $accWithNoLength (keys %noLength) {
  $NCBIgeneId = $noLength{$accWithNoLength};

  # print to the noLength.unl only for those with mapped gene Id
  print NOLENGTH "$accWithNoLength|\n" if exists($mappedReversed{$NCBIgeneId}) || exists($oneToOneViaVega{$NCBIgeneId});
}

close NOLENGTH;

system("/bin/date");

if (!-e "noLength.unl") {
   print LOG "\nCannot find noLength.unl as input file for efetch.r.\n\n";
   close STATS;
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: no input file for efetch.r";
   &reportErrAndExit($subjectLine);
}

print LOG "\nStart efetching ... \n\n";

# Using the above noLength.unl as input, call efetch.r to get the fasta sequences
# and output to seq.fasta file. This step is time-consuming.

$cmdEfetch = "/private/bin/efetch.r -t fasta -i noLength.unl -o seq.fasta";
&doSystemCommand($cmdEfetch);

system("/bin/date");

if (!-e "seq.fasta") {
   print LOG "\nCannot execute /commons/bin/efetch.r -t fasta -i noLength.unl -o seq.fasta: $! \n\n";
   close STATS;
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: ERROR with efetch.r";
   &reportErrAndExit($subjectLine);
}

print LOG "\nDone with efetching.\n\n";

# fasta_len.awk is the script that does the calculation based on fasta sequence

$cmdCalLength = "/private/bin/fasta_len.awk seq.fasta >length.unl";
&doSystemCommand($cmdCalLength);

if (!-e "length.unl") {
   print LOG "\nError happened when execute fasta_len.awk seq.fasta >length.unl: $! \n\n";
   close STATS;
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: ERROR with fasta_len.awk";
   &reportErrAndExit($subjectLine);
}

$ctSeqLengthCalculated = 0;
open (LENGTH, "length.unl") ||  die "Cannot open length.unl : $!\n";
while (<LENGTH>) {
  chomp;

  if ($_ =~ m/^(\w+)\|(\d+)\|$/) {
    $acc = $1;
    $length = $2;

    $sequenceLength{$acc} = $length;

    $ctSeqLengthCalculated++;
  }

}

close LENGTH;

print LOG "\nctSeqLengthCalculated = $ctSeqLengthCalculated\n\n";

#---------------------------------------------------------------------------------------------
# Step 7: prepare the final add-list for RefSeq and GenBank records
#---------------------------------------------------------------------------------------------

## the following SQL is used to get all the existing GenBank and RefSeq records with gene and pseudogene at ZFIN
## these records should not and could not be loaded

$sqlGetGenBankAndRefSeqAccs = 'select dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id, dblink_zdb_id
                                 from db_link
                                where (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
                                  and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-37","ZDB-FDBCONT-040412-42",
                                                                "ZDB-FDBCONT-040412-36","ZDB-FDBCONT-040412-38",
                                                                "ZDB-FDBCONT-040412-39","ZDB-FDBCONT-040527-1");';

$curGetGenBankAndRefSeqAccs = $handle->prepare($sqlGetGenBankAndRefSeqAccs);

$curGetGenBankAndRefSeqAccs->execute();
$curGetGenBankAndRefSeqAccs->bind_columns(\$gene,\$acc,\$fdbcont,\$dblinkId);

# in order for the inserting not to violate the constraint unique (dblink_linked_recid,dblink_acc_num,dblink_fdbcont_zdb_id)
# key: concatenated string of gene zdb id,accession number and zdb if of fdbcont
# value: zdb id of the db_link record

%geneAccFdbcont = ();

$ctGeneAccFdbcont = 0;
while ($curGetGenBankAndRefSeqAccs->fetch()) {
  if (!exists($toDelete{$dblinkId})) {              # exclude those to be deleted first
    $geneAccFdbcont{$gene . $acc . $fdbcont} = $dblinkId;
    $ctGeneAccFdbcont++;
  }
}

print LOG "\nctGeneAccFdbcont = $ctGeneAccFdbcont\n\n";

$curGetGenBankAndRefSeqAccs->finish();

#---------------------------------------------------------------------------
#  write GenBank RNA accessions with mapped genes onto toLoad.unl
#---------------------------------------------------------------------------

foreach $GenBankRNA (sort keys %accNCBIsupportingOnly1) {
  $NCBIgeneId = $accNCBIsupportingOnly1{$GenBankRNA};
  if (exists($mappedReversed{$NCBIgeneId})) {
      $zdbGeneId = $mappedReversed{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $GenBankRNA . $fdcontGenBankRNA})) {
        print TOLOAD "$zdbGeneId|$GenBankRNA||$sequenceLength{$GenBankRNA}|$fdcontGenBankRNA|$pubMappedbasedOnRNA|\n";
        $geneAccFdbcont{$zdbGeneId . $GenBankRNA . $fdcontGenBankRNA} = 1;
        $ctToLoad++;
      }
  } elsif (exists($oneToOneViaVega{$NCBIgeneId})) {
      $zdbGeneId = $oneToOneViaVega{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $GenBankRNA . $fdcontGenBankRNA})) {
        print TOLOAD "$zdbGeneId|$GenBankRNA||$sequenceLength{$GenBankRNA}|$fdcontGenBankRNA|$pubMappedbasedOnVega|\n";
        $geneAccFdbcont{$zdbGeneId . $GenBankRNA . $fdcontGenBankRNA} = 1;
        $ctToLoad++;
      }
  }
}

#---------------------------------------------------------------------------------------
#  write GenPept accessions with mapped genes onto toLoad.unl
#---------------------------------------------------------------------------------------

# get the Genpept accessions and the attribututed pulications that are not the load publications

$sqlGenPeptAttributedToNonLoadPub = 'select dblink_acc_num, dblink_zdb_id, recattrib_source_zdb_id
                                       from record_attribution, db_link
                                      where recattrib_data_zdb_id = dblink_zdb_id
                                        and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-42"
                                        and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
                                        and recattrib_source_zdb_id not in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2");';

$curGenPeptAttributedToNonLoadPub = $handle->prepare($sqlGenPeptAttributedToNonLoadPub);

$curGenPeptAttributedToNonLoadPub->execute;

$curGenPeptAttributedToNonLoadPub->bind_columns(\$GenPept,\$dbLinkId,\$nonLoadPub);

# use the following hash to store GenPept accessions and the attributed pulications that are not one of the load publications
# key: GenPept accession
# value: publication zdb id

%GenPeptAttributedToNonLoadPub = ();

# use the following hash to store GenPept acc and db_link zdb Id that are attributed pulications that are not one of the load publications
# key: GenPept accession
# value: db_link zdb id

%GenPeptDbLinkIdAttributedToNonLoadPub = ();

$ctGenPeptNonLoadPub = 0;
while ($curGenPeptAttributedToNonLoadPub->fetch) {
  $GenPeptAttributedToNonLoadPub{$GenPept} = $nonLoadPub;
  $GenPeptDbLinkIdAttributedToNonLoadPub{$GenPept} = $dbLinkId;
  $ctGenPeptNonLoadPub++;
}

print LOG "\nNumber of GenPept accessions attributed to non-load pub: $ctGenPeptNonLoadPub\n\n";

$curGenPeptAttributedToNonLoadPub->finish();

$ctGenPeptAttributedToNonLoadPub = scalar(keys %GenPeptAttributedToNonLoadPub);

print LOG "\nctGenPeptAttributedToNonLoadPub = $ctGenPeptAttributedToNonLoadPub\n\n";

#-------- deal with those GenBank accessions attributed to non-load publication -------

# use the following hash to store GenPept accessions to be loaded
# key: GenPept accession number
# value: zdb gene id

%GenPeptsToLoad = ();

open (MORETODELETE, ">>toDelete.unl") ||  die "Cannot open toDelete.unl : $!\n";

$ctToAttribute = 0;

print LOG "\nThe GenPept accessions used to attribute to non-load publication now attribute to load pub:\n\n";
print LOG "GenPept\tZFIN gene Id\tnon-load pub\tload pub\n";
print LOG "-------\t------------\t------------\t--------\n";

foreach $GenPept (sort keys %GenPeptNCBIgeneIds) {
  $NCBIgeneId = $GenPeptNCBIgeneIds{$GenPept};
  if (exists($mappedReversed{$NCBIgeneId})) {
      $zdbGeneId = $mappedReversed{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $GenPept . $fdcontGenPept})) {
          print TOLOAD "$zdbGeneId|$GenPept||$sequenceLength{$GenPept}|$fdcontGenPept|$pubMappedbasedOnRNA|\n";
          $geneAccFdbcont{$zdbGeneId . $GenPept . $fdcontGenPept} = 1;
          $ctToLoad++;
          $GenPeptsToLoad{$GenPept} = $zdbGeneId;
      } else {
          if (exists($GenPeptAttributedToNonLoadPub{$GenPept}) && exists($GenPeptDbLinkIdAttributedToNonLoadPub{$GenPept})) {
            print MORETODELETE "$GenPeptDbLinkIdAttributedToNonLoadPub{$GenPept}|\n";
            print TOLOAD "$zdbGeneId|$GenPept||$sequenceLength{$GenPept}|$fdcontGenPept|$pubMappedbasedOnRNA|\n";
            $geneAccFdbcont{$zdbGeneId . $GenPept . $fdcontGenPept} = 1;
            $ctToLoad++;
            print LOG "$GenPept\t$zdbGeneId\t$GenPeptAttributedToNonLoadPub{$GenPept}\t$pubMappedbasedOnRNA\n";
            $ctToAttribute++;
          }
      }
  } elsif (exists($oneToOneViaVega{$NCBIgeneId})) {
      $zdbGeneId = $oneToOneViaVega{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $GenPept . $fdcontGenPept})) {
          print TOLOAD "$zdbGeneId|$GenPept||$sequenceLength{$GenPept}|$fdcontGenPept|$pubMappedbasedOnVega|\n";
          $geneAccFdbcont{$zdbGeneId . $GenPept . $fdcontGenPept} = 1;
          $ctToLoad++;
          $GenPeptsToLoad{$GenPept} = $zdbGeneId;
      } else {
          if (exists($GenPeptAttributedToNonLoadPub{$GenPept}) && exists($GenPeptDbLinkIdAttributedToNonLoadPub{$GenPept})) {
            print MORETODELETE "$GenPeptDbLinkIdAttributedToNonLoadPub{$GenPept}|\n";
            print TOLOAD "$zdbGeneId|$GenPept||$sequenceLength{$GenPept}|$fdcontGenPept|$pubMappedbasedOnVega|\n";
            $geneAccFdbcont{$zdbGeneId . $GenPept . $fdcontGenPept} = 1;
            $ctToLoad++;
            print LOG "$GenPept\t$zdbGeneId\t$GenPeptAttributedToNonLoadPub{$GenPept}\t$pubMappedbasedOnVega\n";
            $ctToAttribute++;
          }
      }
  }
}

close MORETODELETE;

print LOG "---------------------------------------------------------------\nTotal: $ctToAttribute\n\n";

print STATS "\nNon-load attribution for the $ctToAttribute manually curated GenPept db_link records get replaced by;\n 1 of the 2 load pubs (depending on mapping type).\n\n";

# ----- get all the Genpept accessions associated with gene at ZFIN, and those with multiple ZFIN genes ----------------------------

$sqlAllGenPeptWithGeneZFIN = 'select dblink_acc_num, dblink_linked_recid
                                from db_link
                               where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-42"
                                 and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$curAllGenPeptWithGeneZFIN = $handle->prepare($sqlAllGenPeptWithGeneZFIN);

$curAllGenPeptWithGeneZFIN->execute;

$curAllGenPeptWithGeneZFIN->bind_columns(\$GenPept,\$geneZdbId);

# use the following hash to store all the GenPept accession stored at ZFIN that are assoctied with gene
# key: GenPept accession
# value: gene zdb id

%AllGenPeptWithGeneZFIN = ();

# a hash to store GenPept accessions and the multiple related ZFIN gene Ids
# key: GenPept accession
# value: reference to an array of gene zdb id

%GenPeptWithMultipleZDBgene = ();

while ($curAllGenPeptWithGeneZFIN->fetch) {

  if (exists($GenPeptWithMultipleZDBgene{$GenPept}) ||
       (exists($AllGenPeptWithGeneZFIN{$GenPept}) && $AllGenPeptWithGeneZFIN{$GenPept} ne $geneZdbId)) {

        if (!exists($GenPeptWithMultipleZDBgene{$GenPept})) {
            $firstGenPept = $AllGenPeptWithGeneZFIN{$GenPept};
            $ref_arrayZDBgeneIds = [$firstGenPept,$geneZdbId];
            $GenPeptWithMultipleZDBgene{$GenPept} = $ref_arrayZDBgeneIds;
        } else {
            $ref_arrayZDBgeneIds = $GenPeptWithMultipleZDBgene{$GenPept};
            push(@$ref_arrayZDBgeneIds, $geneZdbId);
        }
   }

  $AllGenPeptWithGeneZFIN{$GenPept} = $geneZdbId;
}

$curAllGenPeptWithGeneZFIN->finish();

$ctAllGenPeptWithGeneZFIN = scalar(keys %AllGenPeptWithGeneZFIN);

$ctGenPeptWithMultipleZDBgene = scalar(keys %GenPeptWithMultipleZDBgene);

print LOG "\nctAllGenPeptWithGeneZFIN = $ctAllGenPeptWithGeneZFIN\n\n";

print LOG "\nctGenPeptWithMultipleZDBgene = $ctGenPeptWithMultipleZDBgene\n\n";

print LOG "-----The GenBank accessions to be loaded but also associated with multiple ZFIN genes----\n\n";
print LOG "GenPept \t mapped gene \tall associated genes\n";
print LOG "--------\t-------------\t-------------\n";

$ctGenPeptWithMultipleZDBgeneToLoad = 0;
foreach $GenPept (sort keys %GenPeptWithMultipleZDBgene) {
  if (exists($GenPeptsToLoad{$GenPept})) {
    $ref_arrayZDBgeneIds = $GenPeptWithMultipleZDBgene{$GenPept};
    print LOG "$GenPept\t$GenPeptsToLoad{$GenPept}\t@$ref_arrayZDBgeneIds\n";
    $ctGenPeptWithMultipleZDBgeneToLoad++;
  }
}
print LOG "-----------------------------------------\nTotal: $ctGenPeptWithMultipleZDBgeneToLoad\n\n\n";

print STATS "\nBefore the load, the total number of GenPept accessions associated with multiple ZFIN genes: $ctGenPeptWithMultipleZDBgeneToLoad\n\n";

#---------------------------------------------------------------------------
#  write GenBank DNA accessions with mapped genes onto toLoad.unl
#---------------------------------------------------------------------------

foreach $GenBankDNA (sort keys %GenBankDNAncbiGeneIds) {
  $NCBIgeneId = $GenBankDNAncbiGeneIds{$GenBankDNA};
  if (exists($mappedReversed{$NCBIgeneId})) {
      $zdbGeneId = $mappedReversed{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $GenBankDNA . $fdcontGenBankDNA})) {
        print TOLOAD "$zdbGeneId|$GenBankDNA||$sequenceLength{$GenBankDNA}|$fdcontGenBankDNA|$pubMappedbasedOnRNA|\n";
        $geneAccFdbcont{$zdbGeneId . $GenBankDNA . $fdcontGenBankDNA} = 1;
        $ctToLoad++;
      }
  } elsif (exists($oneToOneViaVega{$NCBIgeneId})) {
      $zdbGeneId = $oneToOneViaVega{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $GenBankDNA . $fdcontGenBankDNA})) {
        print TOLOAD "$zdbGeneId|$GenBankDNA||$sequenceLength{$GenBankDNA}|$fdcontGenBankDNA|$pubMappedbasedOnVega|\n";
        $geneAccFdbcont{$zdbGeneId . $GenBankDNA . $fdcontGenBankDNA} = 1;
        $ctToLoad++;
      }
  }
}

#---------------------------------------------------------------------------
#  write RefSeq RNA accessions with mapped genes onto toLoad.unl
#---------------------------------------------------------------------------

foreach $RefSeqRNA (sort keys %RefSeqRNAncbiGeneIds) {
  $NCBIgeneId = $RefSeqRNAncbiGeneIds{$RefSeqRNA};
  if (exists($mappedReversed{$NCBIgeneId})) {
      $zdbGeneId = $mappedReversed{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $RefSeqRNA . $fdcontRefSeqRNA})) {
        print TOLOAD "$zdbGeneId|$RefSeqRNA||$sequenceLength{$RefSeqRNA}|$fdcontRefSeqRNA|$pubMappedbasedOnRNA|\n";
        $geneAccFdbcont{$zdbGeneId . $RefSeqRNA . $fdcontRefSeqRNA} = 1;
        $ctToLoad++;
      }
  } elsif (exists($oneToOneViaVega{$NCBIgeneId})) {
      $zdbGeneId = $oneToOneViaVega{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $RefSeqRNA . $fdcontRefSeqRNA})) {
        print TOLOAD "$zdbGeneId|$RefSeqRNA||$sequenceLength{$RefSeqRNA}|$fdcontRefSeqRNA|$pubMappedbasedOnVega|\n";
        $geneAccFdbcont{$zdbGeneId . $RefSeqRNA . $fdcontRefSeqRNA} = 1;
        $ctToLoad++;
      }
  }
}

#---------------------------------------------------------------------------
#  write RefPept accessions with mapped genes onto toLoad.unl
#---------------------------------------------------------------------------

foreach $RefPept (sort keys %RefPeptNCBIgeneIds) {
  $NCBIgeneId = $RefPeptNCBIgeneIds{$RefPept};
  if (exists($mappedReversed{$NCBIgeneId})) {
      $zdbGeneId = $mappedReversed{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $RefPept . $fdcontRefPept})) {
        print TOLOAD "$zdbGeneId|$RefPept||$sequenceLength{$RefPept}|$fdcontRefPept|$pubMappedbasedOnRNA|\n";
        $geneAccFdbcont{$zdbGeneId . $RefPept . $fdcontRefPept} = 1;
        $ctToLoad++;
      }
  } elsif (exists($oneToOneViaVega{$NCBIgeneId})) {
      $zdbGeneId = $oneToOneViaVega{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $RefPept . $fdcontRefPept})) {
        print TOLOAD "$zdbGeneId|$RefPept||$sequenceLength{$RefPept}|$fdcontRefPept|$pubMappedbasedOnVega|\n";
        $geneAccFdbcont{$zdbGeneId . $RefPept . $fdcontRefPept} = 1;
        $ctToLoad++;
      }
  }
}

#---------------------------------------------------------------------------
#  write RefSeq DNA accessions with mapped genes onto toLoad.unl
#---------------------------------------------------------------------------

foreach $RefSeqDNA (sort keys %RefSeqDNAncbiGeneIds) {
  $NCBIgeneId = $RefSeqDNAncbiGeneIds{$RefSeqDNA};
  if (exists($mappedReversed{$NCBIgeneId})) {
      $zdbGeneId = $mappedReversed{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $RefSeqDNA . $fdcontRefSeqDNA})) {
        print TOLOAD "$zdbGeneId|$RefSeqDNA||$sequenceLength{$RefSeqDNA}|$fdcontRefSeqDNA|$pubMappedbasedOnRNA|\n";
        $geneAccFdbcont{$zdbGeneId . $RefSeqDNA . $fdcontRefSeqDNA} = 1;
        $ctToLoad++;
      }
  } elsif (exists($oneToOneViaVega{$NCBIgeneId})) {
      $zdbGeneId = $oneToOneViaVega{$NCBIgeneId};
      if (!exists($geneAccFdbcont{$zdbGeneId . $RefSeqDNA . $fdcontRefSeqDNA})) {
        print TOLOAD "$zdbGeneId|$RefSeqDNA||$sequenceLength{$RefSeqDNA}|$fdcontRefSeqDNA|$pubMappedbasedOnVega|\n";
        $geneAccFdbcont{$zdbGeneId . $RefSeqDNA . $fdcontRefSeqDNA} = 1;
        $ctToLoad++;
      }
  }
}

#---------------------------------------------------------------------------
#  write UniGene Ids with mapped genes onto toLoad.unl
#---------------------------------------------------------------------------

open (GENECLUSTER, "gene2unigene") ||  die "Cannot open gene2unigene : $!\n";

## sample records:
## 324615  Dr.743
## 445030  Dr.743

$ctUniGenes = 0;
while (<GENECLUSTER>) {
  chomp;

  if ($_ =~ m/([0-9]+)\s+Dr.([0-9]+)/) {

    $ctUniGenes++;

    $ncbiGeneId = $1;
    $UniGeneId = $2;

    # print the UniGene accessions to be loaded
    if (exists($mappedReversed{$ncbiGeneId})) {
        $zdbGeneId = $mappedReversed{$ncbiGeneId};
        print TOLOAD "$zdbGeneId|$UniGeneId|||$fdcontUniGeneId|$pubMappedbasedOnRNA|\n";
        $ctToLoad++;
    } elsif (exists($oneToOneViaVega{$ncbiGeneId})) {
        $zdbGeneId = $oneToOneViaVega{$ncbiGeneId};
        print TOLOAD "$zdbGeneId|$UniGeneId|||$fdcontUniGeneId|$pubMappedbasedOnVega|\n";
        $ctToLoad++;
    }
  }
}

print LOG "\nctUniGenes = $ctUniGenes\n\n\n";

close GENECLUSTER;

close TOLOAD;

system("/bin/date");
print LOG "Done everything before doing the deleting and inserting\n";
print LOG "\n$ctToDelete total number of db_link records are dropped.\n$ctToLoad total number of new records are added.\n\n";
print STATS "\n$ctToDelete total number of db_link records are dropped.\n$ctToLoad total number of new records are added.\n\n";

#-----------------------------------------------------------------------------------------------------------------------
# Step 8: execute the SQL file to do the deletion according to delete list, and do the loading according to te add list
#-----------------------------------------------------------------------------------------------------------------------

if (!-e "toLoad.unl" || $ctToLoad == 0) {
   print LOG "\nMissing the add list, toLoad.unl, or it is empty. Something is wrong!\n\n";
   close STATS;
   $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: missing or empty add list, toLoad.unl";
   &reportErrAndExit($subjectLine);
}

$cmd = "$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> loadNCBIgeneAccs.sql >loadLog1 2> loadLog2";
&doSystemCommand($cmd);

print LOG "\nDone with the deltion and loading!\n\n";

&sendLoadLogs;

#-------------------------------------------------------------------------------------------------
# Step 9: Report the GenPept accessions associated with multiple ZFIN genes after the load.
# Report GenPept accessions associated with ZFIN genes still attributed to a non-load pub.
# And do the record counts after the load, and report statistics.
#-------------------------------------------------------------------------------------------------

# ----- AFTER THE LOAD, get all the Genpept accessions associated with gene at ZFIN, and those with multiple ZFIN genes ---------

$sqlAllGenPeptWithGeneAfterLoad = 'select dblink_acc_num, dblink_linked_recid
                                     from db_link
                                    where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-42"
                                      and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$curAllGenPeptWithGeneAfterLoad = $handle->prepare($sqlAllGenPeptWithGeneAfterLoad);

$curAllGenPeptWithGeneAfterLoad->execute;

$curAllGenPeptWithGeneAfterLoad->bind_columns(\$GenPept,\$geneZdbId);

# use the following hash to store all the GenPept accession stored at ZFIN that are assoctied with gene after the load
# key: GenPept accession
# value: gene zdb id

%allGenPeptWithGeneAfterLoad = ();

# a hash to store GenPept accessions and the multiple related ZFIN gene Ids after the load
# key: GenPept accession
# value: reference to an array of gene zdb id

%GenPeptWithMultipleZDBgeneAfterLoad = ();

while ($curAllGenPeptWithGeneAfterLoad->fetch) {

  if (exists($GenPeptWithMultipleZDBgeneAfterLoad{$GenPept}) ||
       (exists($allGenPeptWithGeneAfterLoad{$GenPept}) && $allGenPeptWithGeneAfterLoad{$GenPept} ne $geneZdbId)) {

        if (!exists($GenPeptWithMultipleZDBgeneAfterLoad{$GenPept})) {
            $firstGenPept = $allGenPeptWithGeneAfterLoad{$GenPept};
            $ref_arrayZDBgeneIds = [$firstGenPept,$geneZdbId];
            $GenPeptWithMultipleZDBgeneAfterLoad{$GenPept} = $ref_arrayZDBgeneIds;
        } else {
            $ref_arrayZDBgeneIds = $GenPeptWithMultipleZDBgeneAfterLoad{$GenPept};
            push(@$ref_arrayZDBgeneIds, $geneZdbId);
        }
   }

  $allGenPeptWithGeneAfterLoad{$GenPept} = $geneZdbId;
}

$curAllGenPeptWithGeneAfterLoad->finish();

$ctAllGenPeptWithGeneZFINafterLoad = scalar(keys %allGenPeptWithGeneAfterLoad);

$ctGenPeptWithMultipleZDBgeneAfterLoad = scalar(keys %GenPeptWithMultipleZDBgeneAfterLoad);

print LOG "\nctAllGenPeptWithGeneZFINafterLoad = $ctAllGenPeptWithGeneZFINafterLoad\n\n";

print LOG "\nctGenPeptWithMultipleZDBgeneAfterLoad = $ctGenPeptWithMultipleZDBgeneAfterLoad\n\n";

print STATS "----- After the load, the GenBank accessions associated with multiple ZFIN genes----\n\n";
print STATS "GenPept \t mapped gene \tall associated genes\n";
print STATS "--------\t-------------\t-------------\n";

$ctGenPeptWithMultipleZDBgeneAfterLoad = 0;
foreach $GenPept (sort keys %GenPeptWithMultipleZDBgeneAfterLoad) {
    $ref_arrayZDBgeneIds = $GenPeptWithMultipleZDBgeneAfterLoad{$GenPept};
    print STATS "$GenPept\t$GenPeptsToLoad{$GenPept}\t@$ref_arrayZDBgeneIds\n";
    $ctGenPeptWithMultipleZDBgeneAfterLoad++;
}
print STATS "-----------------------------------------\nTotal: $ctGenPeptWithMultipleZDBgeneAfterLoad\n\n\n";

print LOG "\nctGenPeptWithMultipleZDBgeneAfterLoad = $ctGenPeptWithMultipleZDBgeneAfterLoad\n\n";

#-------------------------------------------------------------------------------------------------
# Report GenPept accessions associated with ZFIN genes still attributed to a non-load pub.
#-------------------------------------------------------------------------------------------------
print STATS "\n------GenPept accessions with ZFIN genes still attributed to non-load publication ----------\n\n";

open (NONLOADPUBGENPPEPT, "reportNonLoadPubGenPept") ||  die "Cannot open reportNonLoadPubGenPept : $!\n";

@lines = <NONLOADPUBGENPPEPT>;
$ctGenPeptNonLoadPub = 0;
foreach $line (@lines) {
   $ctGenPeptNonLoadPub++;
   chop($line);
   @fields = split(/\|/, $line);
   print STATS "$fields[0]\t$fields[1]\t$fields[2]\n";
}

close NONLOADPUBGENPPEPT;

print STATS "--------------------------\nTotal: $ctGenPeptNonLoadPub\n\n\n";

#-------------------------------------------------------------------------------------------------
# Do the record counts after the load, and report statistics.
#-------------------------------------------------------------------------------------------------

$sql = 'select mrkr_zdb_id, mrkr_abbrev from marker
         where (mrkr_zdb_id like "ZDB-GENE%" or mrkr_zdb_id like "%RNAG%")
           and exists (select "x" from db_link
         where dblink_linked_recid = mrkr_zdb_id
           and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-38","ZDB-FDBCONT-040412-39","ZDB-FDBCONT-040527-1"));';

$curGenesWithRefSeqAfter = $handle->prepare($sql);

$curGenesWithRefSeqAfter->execute;

$curGenesWithRefSeqAfter->bind_columns(\$geneId,\$geneSymbol);

%genesWithRefSeqAfterLoad = ();

while ($curGenesWithRefSeqAfter->fetch) {
  $genesWithRefSeqAfterLoad{$geneId} = $geneSymbol;
}

$curGenesWithRefSeq->finish();

$ctGenesWithRefSeqAfter = scalar(keys %genesWithRefSeqAfterLoad);

$handle->disconnect();

# NCBI Gene Id
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numNCBIgeneIdAfter = ZFINPerlModules->countData($sql);

# UniGene
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-44"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numUniGeneAfter = ZFINPerlModules->countData($sql);

#RefSeq RNA
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-38"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numRefSeqRNAAfter = ZFINPerlModules->countData($sql);

# RefPept
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-39"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numRefPeptAfter = ZFINPerlModules->countData($sql);

#RefSeq DNA
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040527-1"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numRefSeqDNAAfter = ZFINPerlModules->countData($sql);

# GenBank RNA (only those loaded - excluding curated ones)
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-37"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
           and exists(select "x" from record_attribution
                       where recattrib_data_zdb_id = dblink_zdb_id
                         and recattrib_source_zdb_id in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"));';

$numGenBankRNAAfter = ZFINPerlModules->countData($sql);

# GenPept (only those loaded - excluding curated ones)
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-42"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
           and exists(select "x" from record_attribution
                       where recattrib_data_zdb_id = dblink_zdb_id
                         and recattrib_source_zdb_id in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"));';

$numGenPeptAfter = ZFINPerlModules->countData($sql);

# GenBank DNA (only those loaded - excluding curated ones)
$sql = 'select distinct dblink_acc_num
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-36"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%")
           and exists(select "x" from record_attribution
                       where recattrib_data_zdb_id = dblink_zdb_id
                         and recattrib_source_zdb_id in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"));';

$numGenBankDNAAfter = ZFINPerlModules->countData($sql);

# number of genes with RefSeq RNA
$sql = 'select distinct dblink_linked_recid
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-38"
           and dblink_acc_num like "NM_%"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numGenesRefSeqRNAAfter = ZFINPerlModules->countData($sql);

# number of genes with RefPept
$sql = 'select distinct dblink_linked_recid
          from db_link
         where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-39"
           and dblink_acc_num like "NP_%"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numGenesRefSeqPeptAfter = ZFINPerlModules->countData($sql);

# number of genes with GenBank
$sql = 'select distinct dblink_linked_recid
          from db_link, foreign_db_contains, foreign_db
         where dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = fdb_db_pk_id
           and fdb_db_name = "GenBank"
           and (dblink_linked_recid like "ZDB-GENE%" or dblink_linked_recid like "%RNAG%");';

$numGenesGenBankAfter = ZFINPerlModules->countData($sql);

print STATS "\n********* Percentage change of various categories of records *************\n\n";

print STATS "number of db_link records with gene     \t";
print STATS "before load\t";
print STATS "after load\t";
print STATS "percentage change\n";
print STATS "----------------------------------------\t-----------\t-----------\t-------------------------\n";

print STATS "NCBI gene Id                                  \t";
print STATS "$numNCBIgeneIdBefore   \t";
print STATS "$numNCBIgeneIdAfter   \t";
printf STATS "%.2f\n", ($numNCBIgeneIdAfter - $numNCBIgeneIdBefore) / $numNCBIgeneIdBefore * 100 if ($numNCBIgeneIdBefore > 0);

print STATS "UniGene Id                                    \t";
print STATS "$numUniGeneBefore        \t";
print STATS "$numUniGeneAfter       \t";
printf STATS "%.2f\n", ($numUniGeneAfter - $numUniGeneBefore) / $numUniGeneBefore * 100 if ($numUniGeneBefore > 0);


print STATS "RefSeq RNA                                 \t";
print STATS "$numRefSeqRNABefore        \t";
print STATS "$numRefSeqRNAAfter       \t";
printf STATS "%.2f\n", ($numRefSeqRNAAfter - $numRefSeqRNABefore) / $numRefSeqRNABefore * 100 if ($numRefSeqRNABefore > 0);

print STATS "RefPept                                 \t";
print STATS "$numRefPeptBefore   \t";
print STATS "$numRefPeptAfter   \t";
printf STATS "%.2f\n", ($numRefPeptAfter - $numRefPeptBefore) / $numRefPeptBefore * 100 if ($numRefPeptBefore > 0);

print STATS "RefSeq DNA                                 \t";
print STATS "$numRefSeqDNABefore      \t";
print STATS "$numRefSeqDNAAfter        \t";
if ($numRefSeqDNABefore > 0) {
    printf STATS "%.2f\n", ($numRefSeqDNAAfter - $numRefSeqDNABefore) / $numRefSeqDNABefore * 100;
} else {
    printf STATS "\n";
}

print STATS "GenBank RNA                                 \t";
print STATS "$numGenBankRNABefore        \t";
print STATS "$numGenBankRNAAfter       \t";
printf STATS "%.2f\n", ($numGenBankRNAAfter - $numGenBankRNABefore) / $numGenBankRNABefore * 100 if ($numGenBankRNABefore > 0);

print STATS "GenPept                                 \t";
print STATS "$numGenPeptBefore   \t";
print STATS "$numGenPeptAfter   \t";
printf STATS "%.2f\n", ($numGenPeptAfter - $numGenPeptBefore) / $numGenPeptBefore * 100 if ($numGenPeptBefore > 0);

print STATS "GenBank DNA                                 \t";
print STATS "$numGenBankDNABefore       \t";
print STATS "$numGenBankDNAAfter        \t";
printf STATS "%.2f\n", ($numGenBankDNAAfter - $numGenBankDNABefore) / $numGenBankDNABefore * 100 if ($numGenBankDNABefore > 0);

print STATS "\n\n";

print STATS "number of genes                              \t";
print STATS "before load\t";
print STATS "after load\t";
print STATS "percentage change\n";
print STATS "----------------------------------------\t-----------\t-----------\t-------------------------\n";

print STATS "with RefSeq                             \t";
print STATS "$ctGenesWithRefSeqBefore   \t";
print STATS "$ctGenesWithRefSeqAfter   \t";
printf STATS "%.2f\n", ($ctGenesWithRefSeqAfter - $ctGenesWithRefSeqBefore) / $ctGenesWithRefSeqBefore * 100 if ($ctGenesWithRefSeqBefore > 0);

print STATS "with RefSeq NM                          \t";
print STATS "$numGenesRefSeqRNABefore   \t";
print STATS "$numGenesRefSeqRNAAfter   \t";
printf STATS "%.2f\n", ($numGenesRefSeqRNAAfter - $numGenesRefSeqRNABefore) / $numGenesRefSeqRNABefore * 100 if ($numGenesRefSeqRNABefore > 0);

print STATS "with RefSeq NP                          \t";
print STATS "$numGenesRefSeqPeptBefore   \t";
print STATS "$numGenesRefSeqPeptAfter   \t";
printf STATS "%.2f\n", ($numGenesRefSeqPeptAfter - $numGenesRefSeqPeptBefore) / $numGenesRefSeqPeptBefore * 100 if ($numGenesRefSeqPeptBefore > 0);

print STATS "with GenBank                            \t";
print STATS "$numGenesGenBankBefore        \t";
print STATS "$numGenesGenBankAfter       \t";
printf STATS "%.2f\n", ($numGenesGenBankAfter - $numGenesGenBankBefore) / $numGenesGenBankBefore * 100 if ($numGenesGenBankBefore > 0);

@keysSortedByValues = sort { lc($geneZDBidsSymbols{$a}) cmp lc($geneZDBidsSymbols{$b}) } keys %geneZDBidsSymbols;

print STATS "\n\nList of genes used to have RefSeq acc but no longer having any:\n";
print STATS "-------------------------------------------------------------------\n";

$ctGenesLostRefSeq = 0;
foreach $zdbGeneId (@keysSortedByValues) {
  $symbol = $geneZDBidsSymbols{$zdbGeneId};
  if (exists($genesWithRefSeqBeforeLoad{$zdbGeneId})
    && !exists($genesWithRefSeqAfterLoad{$zdbGeneId})) {
        $ctGenesLostRefSeq++;
        print STATS "$symbol\t$zdbGeneId\n";

  }
}

print STATS "\ntotal: $ctGenesLostRefSeq\n\n";

print STATS "\n\nList of genes now having RefSeq acc but used to have none ReSeq:\n";
print STATS "-------------------------------------------------------------------\n";

$ctGenesGainRefSeq = 0;
foreach $zdbGeneId (@keysSortedByValues) {
  $symbol = $geneZDBidsSymbols{$zdbGeneId};
  if (exists($genesWithRefSeqAfterLoad{$zdbGeneId})
      && !exists($genesWithRefSeqBeforeLoad{$zdbGeneId})) {
        $ctGenesGainRefSeq++;
        print STATS "$symbol\t$zdbGeneId\n";

  }
}

print STATS "\ntotal: $ctGenesGainRefSeq\n\n\n";

close STATS;

$subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: Statistics";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_REPORT|-->","$subject","reportStatistics");

print LOG "\n\nAll done! \n\n\n";

close LOG;

$subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: log file";
ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","logNCBIgeneLoad");

system("/bin/date");

exit;

#---------------------- subroutines  -------------------------------------------------------

# The return code from "system" isn't reliable when used in syntax of "system(...) or die ..."
# Use this subroutine to a better handling.

sub doSystemCommand {

  $systemCommand = $_[0];

  print LOG "$0: Executing [$systemCommand] \n";

  $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) {
     $subjectLine = "Auto from $dbname: " . "NCBI_gene_load.pl :: failed at: $systemCommand . $! ";
     print LOG "\nFailed to execute system command, $systemCommand\nExit.\n\n";

     if ($systemCommand =~ m/loadNCBIgeneAccs\.sql/) {
       &sendLoadLogs;
     }
     &reportErrAndExit($subjectLine);
  }
}

sub reportErrAndExit {
  $subjectError = $_[0];
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subjectError","logNCBIgeneLoad");
  close LOG;
  exit -1;
}

sub sendLoadLogs {
  $subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: loadLog1 file";
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","loadLog1");

  $subject = "Auto from $dbname: " . "NCBI_gene_load.pl :: loadLog2 file";
  ZFINPerlModules->sendMailWithAttachedReport("<!--|SWISSPROT_EMAIL_ERR|-->","$subject","loadLog2");
}
