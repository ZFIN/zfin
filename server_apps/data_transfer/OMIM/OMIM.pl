#!/opt/zfin/bin/perl

# OMIM.pl
# parses OMIM data on mim2gene.txt and genemap files
# does some checking for the OMIM data (since at first I doubled why there were only 5K+ records inserted while there are 10K+ gene MIM numbers at ZFIN)
# calls loadOMIM.sql to do the loading with pre_load_input_omim.txt and reports what have been loaded with whatHaveBeenInsertedIntoOmimPhenotypeTable.txt
#

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

# set environment variables

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/";

system("/bin/rm -f *.txt");
system("/bin/rm -f genemap");

system("rm -f log*");

system("/bin/rm -f geneId_omim_no_pheno");

system("scp /research/zarchive/load_files/OMIM/alreadyReportedHumanOrthologyCandidate <!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/")  if (!-e "alreadyReportedHumanOrthologyCandidate");
system("scp /research/zarchive/load_files/OMIM/alreadyReportedHumanGenes <!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/")  if (!-e "alreadyReportedHumanGenes");

open (REPORTEDBEFORE, "alreadyReportedHumanOrthologyCandidate") ||  die "Cannot open alreadyReportedHumanOrthologyCandidate : $!\n";

@linesCandidatesAlreadyReported = <REPORTEDBEFORE>;

close(REPORTEDBEFORE);

# Use the following hash to store the human gene records that may have zf orthology and have already been examined

# key: human gene symbol
# value: OMIM ID

%humanGenePossibleOrthAlreadyReported = ();

foreach $line (@linesCandidatesAlreadyReported) {

  if ($line =~ m/(.+)\s+(\d+)/) {
      $humanGenePossibleOrthAlreadyReported{$1} = $2;
  }

}

open (ALREADY, "alreadyReportedHumanGenes") ||  die "Cannot open alreadyReportedHumanGenes : $!\n";

@linesAlreadyReported = <ALREADY>;

close(ALREADY);

# Use the following hash to store the human gene records that don't have zf orthology and have been already examined

# key: human gene symbol
# value: OMIM ID

%humanGeneAlreadyReported = ();

# value: human gene symbol
# key: OMIM ID

%humanGenesWithNoZfOrth = ();


foreach $line (@linesAlreadyReported) {

  if ($line =~ m/(.+)\s+(\d+)/) {
      $humanGeneAlreadyReported{$1} = $2;
      $humanGenesWithNoZfOrth{$2} = $1;
  }

}

print "\nDownloading OMIM files ... \n\n";
open (LOG, ">log1.log") || die "Cannot open log1.log : $!\n";

print LOG "\nDownloading OMIM files ... \n\n";

system("/local/bin/wget http://omim.org/static/omim/data/mim2gene.txt");
system("/local/bin/wget https://data.omim.org/downloads/TsbrMAQ1T76RqganEcUayA/genemap2.txt");

if (!-e "genemap2.txt" || !-e "mim2gene.txt") {
   print "One or more required file(s) not exisiting/downloaded. Exit.\n";
   exit -1;
}

print "\nDone with downloading OMIM files. \n\n";
print LOG "\nDone with downloading OMIM files. \n\n";


#######################################################################################################################################################


open (MIM2GENE, "mim2gene.txt") ||  die "Cannot open mim2gene.txt : $!\n";

@mim2genes = <MIM2GENE>;

close MIM2GENE;

$ctMim2genes = 0;

### key: all OMIM number on mim2gene.txt
%allMimNums = ();

### key: OMIM Gene number on mim2gene.txt
### value: human ortholog symbol on mim2gene.txt
%mimNumsHGNCsymbols = ();

### there should be 1 to 1 relationship between human ortholog symbol and NCBI gene Id on mim2gene.txt

### key: human ortholog symbol on mim2gene.txt
### value: NCBI gene Id on mim2gene.txt
%HGNCsymbolsNCBIgeneIds = ();

### key: NCBI gene Id on mim2gene.txt
### value: human ortholog symbol on mim2gene.txt
%ncbiGeneIdsHGNCsymbols = ();

### key: NCBI gene Id on mim2gene.txt
### value: OMIM number on mim2gene.txt
%ncbiGeneIdsMimNumbers = ();

### key: OMIM Gene number on mim2gene.txt
### value: NCBI gene Id on mim2gene.txt
%mimNumsNCBIids = ();

### key: discontinued OMIM Gene number on mim2gene.txt
### value: OMIM type
%allDiscontinuedMIMnums = ();

$ctMimHGNCsymbols = $ctHGNCsymbolsNCBIgeneIds = $ctAllDiscontinuedMIMnums = 0;


### parsing mim2gene.txt
# Copyright (c) 1966-2016 Johns Hopkins University. Use of this file adheres to the terms specified at http://omim.org/help/agreement.
# Generated: 2016-06-01
# This file provides links between the genes in OMIM and other gene identifiers.
# THIS IS NOT A TABLE OF GENE-PHENOTYPE RELATIONSHIPS.
# MIM Number    MIM Entry Type (see FAQ 1.3 at http://omim.org/help/faq)        Entrez Gene ID (NCBI)   Approved Gene Symbol (HGNC)     Ensembl Gene ID (Ensembl)

foreach $mim2gene (@mim2genes) {
   $ctMim2genes++;
   next if $ctMim2genes < 5;

   chop($mim2gene);
   @fieldsMim2gene = split(/\s+/, $mim2gene);

   $mimNum = $fieldsMim2gene[0];
   $mimNum =~ s/^\s+//;
   $mimNum =~ s/\s+$//;

   $allMimNums{$mimNum} = 1;

   $type = $fieldsMim2gene[1];
   $type =~ s/^\s+//;
   $type =~ s/\s+$//;

   $NCBIid = $fieldsMim2gene[2];
   $NCBIid =~ s/^\s+//;
   $NCBIid =~ s/\s+$//;

   $HGNCsymbol = $fieldsMim2gene[3];
   $HGNCsymbol =~ s/^\s+//;
   $HGNCsymbol =~ s/\s+$//;

   if ($type =~ m/removed/) {
     $ctAllDiscontinuedMIMnums++;
     $allDiscontinuedMIMnums{$mimNum} = $type;
   }

   next if $HGNCsymbol eq "";

   $ctMimHGNCsymbols++;
   $mimNumsHGNCsymbols{$mimNum} = $HGNCsymbol;

   if ($NCBIid ne "") {
     $ctHGNCsymbolsNCBIgeneIds++;
     $HGNCsymbolsNCBIgeneIds{$HGNCsymbol} = $NCBIid;
     $ncbiGeneIdsHGNCsymbols{$NCBIid} = $HGNCsymbol;
     $mimNumsNCBIids{$mimNum} = $NCBIid;
     $ncbiGeneIdsMimNumbers{$NCBIid} = $mimNum;
   }
}

$ctMim2genes = $ctMim2genes - 5;
print "total number of records on mim2gene.txt: $ctMim2genes \n\n\n";
print "total number of records on mim2gene.txt that have HGNC symbol::: $ctMimHGNCsymbols \n\n\n";
print "ctHGNCsymbolsNCBIgeneIds::: $ctHGNCsymbolsNCBIgeneIds \n\n\n";
print "ctAllDiscontinuedMIMnums::: $ctAllDiscontinuedMIMnums\n\n\n";

print LOG "total number of records on mim2gene.txt: $ctMim2genes \n\n\n";
print LOG "total number of records on mim2gene.txt that have HGNC symbol::: $ctMimHGNCsymbols \n\n\n";
print LOG "ctHGNCsymbolsNCBIgeneIds::: $ctHGNCsymbolsNCBIgeneIds \n\n\n";
print LOG "ctAllDiscontinuedMIMnums::: $ctAllDiscontinuedMIMnums\n\n\n";

##################################################################################################################################################################

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

$cur_orth_ncbi = $dbh->prepare("select ortho_zdb_id, ortho_other_species_ncbi_gene_id from ortholog where ortho_other_species_taxid = 9606;");
$cur_orth_ncbi->execute();
my ($orthID, $humanGeneNcbiID);
$cur_orth_ncbi->bind_columns(\$orthID, \$humanGeneNcbiID);

$ctHumanOrth = 0;
%humanOrthNcbiIDs = ();
while ($cur_orth_ncbi->fetch()) {
   $humanOrthNcbiIDs{$orthID} = $humanGeneNcbiID;
   $ctHumanOrth++;
}

print "total number of human orthologues stored at ZFIN: $ctHumanOrth\n";

$cur_orth_ncbi->finish();

open (ORTHMIM, ">ortholog_mim.txt") || die "Cannot open ortholog_mim.txt : $!\n";

$ctOrthMims = 0;
foreach $orthId (keys %humanOrthNcbiIDs) {
  $ncbiGeneIdOfHumanOrth = $humanOrthNcbiIDs{$orthId};
  if(exists($ncbiGeneIdsMimNumbers{$ncbiGeneIdOfHumanOrth})) {
    $ctOrthMims++;
    print ORTHMIM "$orthId|$ncbiGeneIdsMimNumbers{$ncbiGeneIdOfHumanOrth}\n";
    print "\n$orthId|$ncbiGeneIdsMimNumbers{$ncbiGeneIdOfHumanOrth}\n" if $ctOrthMims < 6;
  }
}

close ORTHMIM;

print "\n\nctOrthMims is $ctOrthMims\n\n";

$cur = $dbh->prepare('select distinct omimp_name, omimp_ortho_zdb_id from omim_phenotype;');
$cur->execute();
my ($omimPhenotypeName, $orthoId);
$cur->bind_columns(\$omimPhenotypeName,\$orthoId);

$ctOMIMphenotypeNamesAtZFIN = 0;
%OMIMphenotypeNamesAtZFIN = ();
while ($cur->fetch()) {
   ### if there is single or double quote in $omimPhenotypeName, the hash won't prevent duplication
   $OMIMphenotypeNamesAtZFIN{$omimPhenotypeName} = $orthoId;
   $ctOMIMphenotypeNamesAtZFIN++;
}

$cur->finish();

print "\ntotal number of OMIM phenotype names stored at ZFIN: $ctOMIMphenotypeNamesAtZFIN\n";

print LOG "total number of OMIM phenotype names stored at ZFIN: $ctOMIMphenotypeNamesAtZFIN";

open (GENEMAP, "genemap2.txt") || die "Cannot open genemap2.txt : $!\n";

open (OMIM, ">pre_load_input_omim.txt") || die "Cannot open pre_load_input_omim.txt : $!\n";

open (GENEDETAILS, ">human_gene_detail.txt") || die "Cannot open human_gene_detail.txt : $!\n";

open (OMIM2, ">pre_load_human_gene_omim.txt") || die "Cannot open pre_load_human_gene_omim.txt : $!\n";

open (CHECKNOPHENO, ">genemap_records_with_no_phenotype.txt") || die "Cannot open genemap_records_with_no_phenotype.txt : $!\n";

@lines = <GENEMAP>;

close GENEMAP;

$ctFoundMIMwithSymbolOnGenemap = $ctTotalOnGenmap = $ctInput = $ctMatchedOMIM = $ctNotEmptyPheno = $ctNoPhenotype = 0;

print "\nParsing and loading OMIM data ... \n\n";
print LOG "\nParsing and loading OMIM data ... \n\n";


%allMIMnumsZDBgeneIdsOnGeneMap = ();

$ctHumanGenePhenoNoOrth = 0;
$ctHumanGenePhenoPossibleOrth = 0;

%humanGeneMimNumPhenoNoOrth = ();
%humanGeneMimNumPhenoPossibleOrth = ();

# Chromosome    Genomic Position Start  Genomic Position End    Cyto Location   Computed Cyto Location  Mim Number      Gene Symbols    Gene Name       Approved Symbol Entrez Gene ID  Ensembl Gene ID Comments        Phenotypes      Mouse Gene Symbol/ID
# chr1    2404801 2412573 1p36.32 1p36.32 602859  PEX10, NALD, PBD6A, PBD6B       Peroxisome biogenesis factor 10 PEX10   5192    ENSG00000157911         Peroxisome biogenesis disorder 6A (Zellweger), 614870 (3), Autosomal recessive; Peroxisome biogenesis disorder 6B, 614871 (3), Autosomal
foreach $line (@lines) {
   $ctTotalOnGenmap++;
   next if $ctTotalOnGenmap < 5;
   %ZDBgeneIdOMIMnums = ();

   $matchedGeneOrGenesFound = 0;
   $matchedSymbolFound = 0;

   chop($line);
   @fields = split(/\t/, $line);


   $humanGeneName = $fields[7];

   ### not all filed[8] numbers are OMIM numbers for gene; many for phenotype
   $mimNumGene = $fields[5];


   ### only process those with Gene OMIM numbers having HGNC symbols
   if (exists($mimNumsHGNCsymbols{$mimNumGene})) {
   
     print GENEDETAILS "$mimNumGene|$humanGeneName|$mimNumsHGNCsymbols{$mimNumGene}\n";

     $ctFoundMIMwithSymbolOnGenemap++;

     $sqlOrth = "select distinct ortho_zebrafish_gene_zdb_id, oef_accession_number from ortholog, ortholog_external_reference where oef_ortho_zdb_id = ortho_zdb_id and oef_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-25' and oef_accession_number = ?;";
     $cur_orth = $dbh->prepare($sqlOrth);
     $cur_orth->execute($mimNumGene);
     my ($ZFINgeneId);
     my ($dblinkAcc);
     $cur_orth->bind_columns(\$ZFINgeneId,\$dblinkAcc);
     while ($cur_orth->fetch()) {
        $ZDBgeneIdOMIMnums{$ZFINgeneId} = $mimNumGene;

        $allMIMnumsZDBgeneIdsOnGeneMap{$mimNumGene} = $ZFINgeneId;

        $matchedGeneOrGenesFound = 1;
        $ctMatchedOMIM++;
     }

     $cur_orth->finish();

     $humanGene = $mimNumsHGNCsymbols{$mimNumGene};
     $humanGeneSymLowerCase = lc($humanGene);
     $humanGeneSymLowerCaseA = $humanGeneSymLowerCase . "a";
     $humanGeneSymLowerCaseB = $humanGeneSymLowerCase . "b";

     $sqlGene = "select mrkr_zdb_id from marker where mrkr_zdb_id like 'ZDB-GENE%' and (mrkr_abbrev = ? or mrkr_abbrev = ? or mrkr_abbrev = ?);";
     $cur_gene = $dbh->prepare($sqlGene);
     $cur_gene->execute($humanGeneSymLowerCase,$humanGeneSymLowerCaseA,$humanGeneSymLowerCaseB);
     my ($ZFINgeneIdSimilarSym);
     $cur_gene->bind_columns(\$ZFINgeneIdSimilarSym);
     while ($cur_gene->fetch()) {
        $matchedSymbolFound = 1;
     }
     $cur_gene->finish();

     $phenotypesIn3Fileds = $fields[12];

     if ($phenotypesIn3Fileds ne "") {
       $ctNotEmptyPheno++;

       @phenotypes = split(/;\s+/, $phenotypesIn3Fileds);

       $hasPhenotype = 0;
       foreach $phenotype (@phenotypes) {
         $phenotype =~ s/^\s+//;
         $phenotype =~ s/\s+$//;

         if($phenotype) {
           $hasPhenotype = 1 if ($phenotype ne "");
         }

         ### assuming all OMIM numbers for phenotype are 6 digits, which is the fact as have been confirmed
         ### but there are phenotypes (disorders) lacking OMIM number
         if ($phenotype =~ m/\s+([0-9]{6})\s+\([0-9]\)/) {
             $phenotypeOMIMnum = $1;
             @disordrTextPlus = split(/\s+([0-9]){6}\s+\([0-9]\)/, $phenotype);
             $disorder = $disordrTextPlus[0];
             $disorder =~ s/,$//;
             $disorder =~ s/^\s+//;
             $disorder =~ s/\s+$//;

             if ($matchedGeneOrGenesFound == 1) {
                 foreach $key (keys %ZDBgeneIdOMIMnums) {
                   ### if there is single or double quote in $omimPhenotypeName, the hash won't prevent duplication
                   ### if ($disorder ne "" && !exists($OMIMphenotypeNamesAtZFIN{$disorder})) {
                   if ($disorder ne "") {
                     print OMIM "$key|$mimNumGene|$disorder|$phenotypeOMIMnum\n";
                     $ctInput++;
                   }
                 }
             } else {
                 if ($matchedSymbolFound == 1) {
                     $humanGeneMimNumPhenoPossibleOrth{$humanGene} = $mimNumGene;
                     $ctHumanGenePhenoPossibleOrth++;
                 } else {
                     $humanGeneMimNumPhenoNoOrth{$humanGene} = $mimNumGene;
                     $ctHumanGenePhenoNoOrth++;
                 }
                 print OMIM2 "$mimNumGene|$disorder|$phenotypeOMIMnum\n";
             }
         } elsif ($phenotype =~ m/\s+\([0-9]\)$/) {   ## no phenotype OMIM number
             @disordrTextPlus = split(/\s+\([0-9]\)$/, $phenotype);
             $disorder = $disordrTextPlus[0];
             $disorder =~ s/^\s+//;
             $disorder =~ s/\s+$//;
             $disorder =~ s/\'/\"/g;
             if ($matchedGeneOrGenesFound == 1) {
                 foreach $key (keys %ZDBgeneIdOMIMnums) {
                   ### if there is single or double quote in $omimPhenotypeName, the hash won't prevent duplication
                   ### if ($disorder ne "" && !exists($OMIMphenotypeNamesAtZFIN{$disorder})) {
                   if ($disorder ne "") {
                     print OMIM "$key|$mimNumGene|$disorder|\n";
                     $ctInput++;
                   }
                 }
             } else {
                 if ($matchedSymbolFound == 1) {
                     $humanGeneMimNumPhenoPossibleOrth{$humanGene} = $mimNumGene;
                     $ctHumanGenePhenoPossibleOrth++;
                 } else {
                     $humanGeneMimNumPhenoNoOrth{$humanGene} = $mimNumGene;
                     $ctHumanGenePhenoNoOrth++;
                 }
             }
         } else {
             print CHECKNOPHENO "$line\n\t$phenotypesIn3Fileds\n";
             $ctNoPhenotype++;
         }

       }

     }

   }

   undef %ZDBgeneIdOMIMnums;
}

$ctTotalOnGenmap = $ctTotalOnGenmap - 4;

close OMIM;
close GENEDETAILS;
close OMIM2;
close CHECKNOPHENO;

##################################################################################################################################################################
$sqlOmim = "select distinct ortho_zebrafish_gene_zdb_id, oef_accession_number from ortholog, ortholog_external_reference where oef_ortho_zdb_id = ortho_zdb_id and oef_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-25';";
$cur_omim = $dbh->prepare($sqlOmim);
$cur_omim->execute();
my ($orthoId, $omimNum);
$cur_omim->bind_columns(\$geneId,\$omimNum);

print LOG "\nOMIM numbers on ZFIN not found on mim2gene.txt :\n\n";
while ($cur_omim->fetch()) {
   if (!exists($allMimNums{$omimNum})) {
      print LOG "$orthoId\t$omimNum\n";
   }
}

print LOG " \n";

$cur_omim->execute();
$cur_omim->bind_columns(\$geneId,\$omimNum);

print LOG "\nOMIM numbers on ZFIN found on mim2gene.txt but missing symbol on mim2gene.txt:\n\n";
while ($cur_omim->fetch()) {
   if (exists($allMimNums{$omimNum}) && !exists($mimNumsHGNCsymbols{$omimNum})) {
      print LOG "$geneId\t$omimNum\n";
   }
}

print LOG " \n";

$cur_omim->execute();
$cur_omim->bind_columns(\$geneId,\$omimNum);

print LOG "\nOMIM numbers on ZFIN found to be discontinued (removed) on mim2gene.txt:\n\n";
while ($cur_omim->fetch()) {
   if (exists($allDiscontinuedMIMnums{$omimNum})) {
      print LOG "$geneId\t$omimNum\n";
   }
}

print LOG " \n";

$cur_omim->execute();
$cur_omim->bind_columns(\$geneId,\$omimNum);

print LOG "\nOMIM numbers on ZFIN not found on genemap file: \n\n";
$ctNotFoundOnGeneMap = 0;
while ($cur_omim->fetch()) {
   if (!exists($allMIMnumsZDBgeneIdsOnGeneMap{$omimNum})) {
      print LOG "$geneId\t$omimNum\n";
      $ctNotFoundOnGeneMap++;
   }
}

print "ctNotFoundOnGeneMap : $ctNotFoundOnGeneMap\n";

print LOG "ctNotFoundOnGeneMap : $ctNotFoundOnGeneMap\n";

$cur_omim->finish();

$dbh->disconnect();

print "\nctFoundMIMwithSymbolOnGenemap:  $ctFoundMIMwithSymbolOnGenemap\ttotal number of records on genemap file:  $ctTotalOnGenmap \tctMatchedOMIM: $ctMatchedOMIM \tctInput: $ctInput\n\n";

print LOG "\nctFoundMIMwithSymbolOnGenemap:  $ctFoundMIMwithSymbolOnGenemap\ttotal number of records on genemap file:  $ctTotalOnGenmap \tctMatchedOMIM: $ctMatchedOMIM \tctInput: $ctInput\n\n";

print "For all $ctFoundMIMwithSymbolOnGenemap records that found with symbols and MIM numbers, the number of records that have non-empty pheno: $ctNotEmptyPheno\n the number of records that do not have phenotype: $ctNoPhenotype\n\n";

print LOG "For all $ctFoundMIMwithSymbolOnGenemap records that found with symbols and MIM numbers, the number of records that have non-empty pheno: $ctNotEmptyPheno\n the number of records that do not have phenotype: $ctNoPhenotype\n\n";

##################################################################################################################################################################

ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f loadOMIM.sql");
ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f update_omimp_termxref_mapping.sql");

print LOG "\nAll done!\n\n\n";

if ($ctHumanGenePhenoPossibleOrth > 0) {
   $ctPossibleMatchGeneWithPheno = 0;
   open (POSSIBLEORTHO, ">human_genes_with_pheno_possible_ortho.txt")  || die "Cannot open human_genes_with_pheno_possible_ortho.txt : $!\n";
   open (ALREADYREPORTED, ">>alreadyReportedHumanOrthologyCandidate") ||  die "Cannot open alreadyReportedHumanOrthologyCandidate : $!\n";
   foreach $symbol (sort keys %humanGeneMimNumPhenoPossibleOrth) {
      if($symbol !~ m/^\d+$/ && !exists($humanGenePossibleOrthAlreadyReported{$symbol})) {
        print POSSIBLEORTHO "$symbol\t$humanGeneMimNumPhenoPossibleOrth{$symbol}\n";
        print ALREADYREPORTED "$symbol\t$humanGeneMimNumPhenoPossibleOrth{$symbol}\n";
        $ctPossibleMatchGeneWithPheno++;
      }
   }
   close POSSIBLEORTHO;
   close ALREADYREPORTED;

   if ($ctPossibleMatchGeneWithPheno > 0) {
       $subject = "Auto from $dbname: " . "OMIM.pl :: $ctPossibleMatchGeneWithPheno human genes with phenotype, with no match to ZF gene via OMIM, but with similar symbol";
       ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_ERR|-->',"$subject","human_genes_with_pheno_possible_ortho.txt");
   } else {
       ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_ERR|-->',"no new human gene with phenotype, with no match to ZF gene via OMIM, but with similar symbol","alreadyReportedHumanOrthologyCandidate");
   }
}

if ($ctHumanGenePhenoNoOrth > 0) {
   $ctNoMatchGeneWithPheno = 0;
   open (NOORTHO, ">human_genes_with_pheno_but_not_ortho.txt")  || die "Cannot open human_genes_with_pheno_but_not_ortho.txt : $!\n";
   open (PREVIOUSLYREPORTED, ">>alreadyReportedHumanGenes") ||  die "Cannot open alreadyReportedHumanGenes : $!\n";
   foreach $symbol (sort keys %humanGeneMimNumPhenoNoOrth) {
      if($symbol !~ m/^\d+$/ && !exists($humanGeneAlreadyReported{$symbol})) {
        print NOORTHO "$symbol\t$humanGeneMimNumPhenoNoOrth{$symbol}\n";
        print PREVIOUSLYREPORTED "$symbol\t$humanGeneMimNumPhenoNoOrth{$symbol}\n";
        $ctNoMatchGeneWithPheno++;
      }
   }
   close NOORTHO;
   close PREVIOUSLYREPORTED;

   if ($ctNoMatchGeneWithPheno > 0) {
       $subject = "Auto from $dbname: " . "OMIM.pl :: $ctNoMatchGeneWithPheno human genes with phenotype, with no match to ZF gene via OMIM, and without similar symbol";
       ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_ERR|-->',"$subject","human_genes_with_pheno_but_not_ortho.txt");
   } else {
       ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_ERR|-->',"no new human gene with phenotype, with no match to ZF gene via OMIM, and without similar symbol","alreadyReportedHumanGenes");
   }
}

print "\nctHumanGenePhenoPossibleOrth = $ctHumanGenePhenoPossibleOrth\n\n";
print "\nctPossibleMatchGeneWithPheno = $ctPossibleMatchGeneWithPheno\n\n" ;
print "\nctNoMatchGeneWithPheno = $ctNoMatchGeneWithPheno\n\n" ;

system("scp <!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/alreadyReportedHumanOrthologyCandidate /research/zarchive/load_files/OMIM/");
system("scp <!--|ROOT_PATH|-->/server_apps/data_transfer/OMIM/alreadyReportedHumanGenes /research/zarchive/load_files/OMIM/");

exit;



