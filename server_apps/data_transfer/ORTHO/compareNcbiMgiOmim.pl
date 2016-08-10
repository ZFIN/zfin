#!/private/bin/perl

# compareNcbiMgiOmim.pl

system("rm -f logCompareNcbiMgiOmim.rpt");
system("rm -f Mus_musculus.gene_info");
system("rm -f MGIgene.gene_info");
system("rm -f Homo_sapiens.gene_info");
system("rm -f mim2gene.gene_info");

system("rm -f ncbiIDsAtMGIdiffFromNCBI.rpt");

system("rm -f ncbiIDsAtNCBInotFoundAtMGI.rpt");

system("rm -f geneSymbolsMGIdiffFromNCBI.rpt");

system("rm -f geneNamesMGIdiffFromNCBI.rpt");

system("rm -f ncbiIDsWithSameMimDiffAtNCBIfromOMIM.rpt");


open LOG, '>', "logCompareNcbiMgiOmim.rpt" or die "can not open logCompareNcbiMgiOmim.rpt: $! \n";

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Mammalia/Mus_musculus.gene_info.gz");
&doSystemCommand("/local/bin/gunzip Mus_musculus.gene_info.gz");

&doSystemCommand("/local/bin/wget ftp://ftp.informatics.jax.org/pub/reports/HGNC_homologene.rpt -O MGIgene.gene_info");

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz");
&doSystemCommand("/local/bin/gunzip Homo_sapiens.gene_info.gz");

&doSystemCommand("/local/bin/wget http://omim.org/static/omim/data/mim2gene.txt -O mim2gene.gene_info");

open (NCBIMOUSEDATA, "Mus_musculus.gene_info") ||  die "Cannot open Mus_musculus.gene_info : $!\n";

%NCBIidsAndNamesMouse = (); 

%NCBImouseGeneSymbols = ();

%NCBIidsMGIids = ();

%MGIidsNCBIids = ();

$total = 0;
while (<NCBIMOUSEDATA>) {
  chomp;
  $total++;
  
  @fieldsNCBI = split("\t");

  $taxId = $fieldsNCBI[0];
  ## mouse gene info
  if ($taxId eq "10090") {
    $mouseNCBIgeneID = $fieldsNCBI[1];


    if ($fieldsNCBI[10] ne "-") {
      $mouseGeneSymbol = $fieldsNCBI[10];
      $NCBImouseGeneSymbols{$mouseNCBIgeneID} = $mouseGeneSymbol;
    }
    
    if ($fieldsNCBI[5] ne "-") {
      $mouseDBxrefs = $fieldsNCBI[5];
      if ($mouseDBxrefs =~ m/^MGI:(MGI:\d+)/) {
         $mgiIDatNCBI = $1;

         $NCBIidsMGIids{$mouseNCBIgeneID} = $mgiIDatNCBI;
         $MGIidsNCBIids{$mgiIDatNCBI} = $mouseNCBIgeneID;
      }
    }
         

    if ($fieldsNCBI[11] ne "-") {
        $NCBIidsAndNamesMouse{$mouseNCBIgeneID} = $fieldsNCBI[11];
    } 
  }
  
  undef @fieldsNCBI;
      
}
$total--;

print "\ntotal number of lines from NCBI mouse gene info file: $total \n\n";
print LOG "\ntotal number of lines from NCBI mouse gene info file: $total \n\n";


close(NCBIMOUSEDATA);

$ctMGIlines = 0;

open (MGI, "MGIgene.gene_info") ||  die "Cannot open MGIgene.gene_info : $!\n";

%mgiNCBIidsAndNamesMouse = ();
%mgiNCBIidsAndSymbols = ();

%mgiIDsAndNCBIids = ();
%ncbiIDsAndMGIids = ();

while (<MGI>) {
 chomp;
 
 $ctMGIlines++;
 next if $ctMGIlines < 2; 
 
 @fieldsMGI = split("\t");

 $mgiID = $fieldsMGI[0];

 if ($mgiID =~ m/^MGI:\d+/) {

   if ($fieldsMGI[4] =~ m/\d+/) {
     $ncbiGeneID = $fieldsMGI[4];
     $symbol = $fieldsMGI[1] if $fieldsMGI[1] ne "null";
     $geneName = $fieldsMGI[2] if $fieldsMGI[2] ne "null";

     $Chr = $fieldsMGI[5] if $fieldsMGI[5] ne "null";
 
     $mgiNCBIidsAndNamesMouse{$ncbiGeneID} = $geneName if $fieldsMGI[2] ne "null";
     $mgiNCBIidsAndSymbols{$ncbiGeneID} = $symbol if $fieldsMGI[1] ne "null";
 
     $mgiIDsAndNCBIids{$MGIid} = $ncbiGeneID;
     $ncbiIDsAndMGIids{$ncbiGeneID} = $MGIid;

   }
 }

 undef @fieldsMGI;
 
}
 
close (MGI);

$ctMGIlines--;

print "\ntotal number of lines from MGI_gene: $ctMGIlines \n\n";
print LOG "\ntotal number of lines from MGI_gene: $ctMGIlines \n\n";

open DIFFGENENAMES, '>', "geneNamesMGIdiffFromNCBI.rpt" or die "can not open geneNamesMGIdiffFromNCBI.rpt: $! \n";

print DIFFGENENAMES "NCBI Gene Id\n";
print DIFFGENENAMES "name at NCBI\n";
print DIFFGENENAMES "name at MGI\n\n";

$ctNCBIidAtMGI = 0;
foreach $ncbiGeneIDatMGI (sort keys %mgiNCBIidsAndNamesMouse) {
      $ctNCBIidAtMGI++;
      if(exists($NCBIidsAndNamesMouse{$ncbiGeneIDatMGI}) && $NCBIidsAndNamesMouse{$ncbiGeneIDatMGI} ne $mgiNCBIidsAndNamesMouse{$ncbiGeneIDatMGI}) {
        print DIFFGENENAMES "$ncbiGeneIDatMGI\n$mgiNCBIidsAndNamesMouse{$ncbiGeneIDatMGI}\n$NCBIidsAndNamesMouse{$ncbiGeneIDatMGI}\n\n";
      }
}

print "\nnumber of NCBI mouse gene IDs at MGI: $ctNCBIidAtMGI  \n\n";
print LOG "\nnumber of NCBI mouse gene IDs at MGI: $ctNCBIidAtMGI  \n\n";

close(DIFFGENENAMES);

open DIFFGENESYMBOLS, '>', "geneSymbolsMGIdiffFromNCBI.rpt" or die "can not open geneSymbolsMGIdiffFromNCBI.rpt: $! \n";
print DIFFGENESYMBOLS "NCBI Gene Id\tsymbol at NCBI\tsymbol at MGI\n";
print DIFFGENESYMBOLS "----------------------------------------------\n";
foreach $ncbiGeneIDatMGI (sort keys %mgiNCBIidsAndSymbols) {
      if(exists($NCBImouseGeneSymbols{$ncbiGeneIDatMGI}) && $NCBImouseGeneSymbols{$ncbiGeneIDatMGI} ne $mgiNCBIidsAndSymbols{$ncbiGeneIDatMGI}) {
        print DIFFGENESYMBOLS "$ncbiGeneIDatMGI\t$mgiNCBIidsAndSymbols{$ncbiGeneIDatMGI}\t$NCBImouseGeneSymbols{$ncbiGeneIDatMGI}\n";
      }
}

close(DIFFGENESYMBOLS);

open DIFFNCBIIDMGI, '>', "ncbiIDsAtNCBInotFoundAtMGI.rpt" or die "can not open ncbiIDsAtNCBInotFoundAtMGI.rpt: $! \n";
$ctNCBIidsAtNCBI = 0;
foreach $ncbi (sort keys %NCBIidsMGIids) {
  $ctNCBIidsAtNCBI++;
  if (exists($ncbiIDsAndMGIids{$ncbi}) && $NCBIidsMGIids{$ncbi} ne $ncbiIDsAndMGIids{$ncbi}) {
     print DIFFNCBIMGI "$ncbi\t$NCBIidsMGIids{$ncbi}\t$ncbiIDsAndMGIids{$ncbi}\n";
  }
}

print "\nnumber of NCBI mouse gene IDs at NCBI = $ctNCBIidsAtNCBI \n\n";
print LOG "\nnumber of NCBI mouse gene IDs at NCBI = $ctNCBIidsAtNCBI \n\n";

close(DIFFNCBIMGI);


open DIFFNCBIIDMGIFROMNCBI, '>', "ncbiIDsAtMGIdiffFromNCBI.rpt" or die "can not open ncbiIDsAtMGIdiffFromNCBI.rpt: $! \n";
print DIFFNCBIIDMGIFROMNCBI "NCBI Gene ID\tMGI Id at MGI\tMGI Id at NCBI\n";
print DIFFNCBIIDMGIFROMNCBI "---------------------------------------------------\n";
foreach $ncbi (sort keys %ncbiIDsAndMGIids) {
  if (exists($NCBIidsMGIids{$ncbi}) && $ncbiIDsAndMGIids{$ncbi} ne $NCBIidsMGIids{$ncbi}) {
     print DIFFNCBIIDMGIFROMNCBI "$ncbi\t$ncbiIDsAndMGIids{$ncbi}\t$NCBIidsMGIids{$ncbi}\n";

  }
}

close(DIFFNCBIIDMGIFROMNCBI);

open (NCBIHUMANDATA, "Homo_sapiens.gene_info") ||  die "Cannot open Homo_sapiens.gene_info : $!\n";

%mimNumbersNCBIidsAtNCBI = (); 

$total = 0;
while (<NCBIHUMANDATA>) {
  chomp;
  $total++;
  next if $total < 2;

  @fieldsNCBI = split("\t");

  $taxId = $fieldsNCBI[0];
  
  ## human gene info
  if ($taxId eq "9606") {
    $humanNCBIgeneID = $fieldsNCBI[1];

    if ($fieldsNCBI[5] ne "-") {
      $humanDBxrefs = $fieldsNCBI[5];
      if ($humanDBxrefs =~ m/^(MIM:\d+)/) {
         $mimAtNCBI = $1;

         $mimNumbersNCBIidsAtNCBI{$mimAtNCBI} = $humanNCBIgeneID;
      }
    }
  }
  
  undef @fieldsNCBI;
      
}

$total--;

print "\ntotal number of lines from NCBI human gene info file: $total \n\n";
print LOG "\ntotal number of lines from NCBI human gene info file: $total \n\n";


close(NCBIHUMANDATA);

### key: OMIM Gene number on mim2gene.txt
### value: NCBI gene Id on mim2gene.txt
%mimNumsNCBIids = ();

$ctMim2genes = 0;

open (MIM2GENE, "mim2gene.gene_info") ||  die "Cannot open mim2gene.gene_info : $!\n";
@mim2genes = <MIM2GENE>;

close MIM2GENE;

### parsing mim2gene.txt
## Mim Number    Type    Gene IDs        Approved Gene Symbols
## 100650  gene/phenotype  217     ALDH2
## 100680  moved/removed   -       -
## 100850  gene    50      ACO2
## 105800  phenotype       116833  ANIB1


foreach $mim2gene (@mim2genes) {
   $ctMim2genes++;
   next if $ctMim2genes < 2;

   chop($mim2gene);
   @fieldsMim2gene = split(/\s+/, $mim2gene);

   $mimNum = $fieldsMim2gene[0];
   $mimNum =~ s/^\s+//;
   $mimNum =~ s/\s+$//;

   $type = $fieldsMim2gene[1];
   $type =~ s/^\s+//;
   $type =~ s/\s+$//;

   $NCBIid = $fieldsMim2gene[2];
   $NCBIid =~ s/^\s+//;
   $NCBIid =~ s/\s+$//;

   $HGNCsymbol = $fieldsMim2gene[3];
   $HGNCsymbol =~ s/^\s+//;
   $HGNCsymbol =~ s/\s+$//;

   if ($type !~ m/removed/ && $NCBIid ne "-") {
     $mimNumsNCBIids{$mimNum} = $NCBIid;
   }

}

$ctMim2genes = $ctMim2genes - 1;

print "total number of records on mim2gene.txt: $ctMim2genes \n\n\n";

print LOG "total number of records on mim2gene.txt: $ctMim2genes \n\n\n";

open DIFFNCBIIDMIM, '>', "ncbiIDsWithSameMimDiffAtNCBIfromOMIM.rpt" or die "can not open ncbiIDsWithSameMimDiffAtNCBIfromOMIM.rpt: $! \n";
$ctNCBImim = 0;
##print DIFFNCBIIDMIM "OMIM number\tNCBI ID at NCBI\tNCBI ID at OMIM\n";
##print DIFFNCBIIDMIM "---------------------------------------------\n";
foreach $mim (sort keys %mimNumbersNCBIidsAtNCBI) {
  $ctNCBImim++;
  if (exists($mimNumsNCBIids{$mim}) && $mimNumbersNCBIidsAtNCBI{$mim} ne $mimNumsNCBIids{$mim}) {
     print DIFFNCBIIDMIM "$mim\t$mimNumbersNCBIidsAtNCBI{$mim}\t$mimNumsNCBIids{$mim}\n";
  }
}

print "\nnumber of OMIM numbers of human gene at NCBI = $ctNCBImim \n\n";
print LOG "\nnumber of OMIM numbers of human gene at NCBI = $ctNCBImim \n\n";

close(DIFFNCBIIDMIM);

close(LOG);

exit;

sub doSystemCommand {

  $systemCommand = $_[0];

  print LOG "$0: Executing [$systemCommand] \n";
    
  $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) { 
     $subjectLine = "Auto from $dbname: " . "NCBIorthology.pl :: failed at: $systemCommand . $! ";
     print LOG "\nFailed to execute system command, $systemCommand\nExit.\n\n";
     
     &reportErrAndExit($subjectLine);
  }
}





