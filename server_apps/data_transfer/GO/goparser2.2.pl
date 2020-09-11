#!/opt/zfin/bin/perl
#
#  goparser2.2.pl
#

## system("/bin/rm -f gaf_from_go");

## my $url = "http://viewvc.geneontology.org/viewvc/GO-SVN/trunk/gene-associations/gene_association.zfin.gz";

## system("/local/bin/wget $url -O gaf_from_go.gz");

## system("/local/bin/gunzip gaf_from_go.gz");
## system("/local/bin/cp gene_association.zfin gene_association2.2.zfin");
## system("/local/bin/cp gene_association.zfin.gz gene_association2.2.zfin.gz");
## system("/local/bin/gunzip gene_association2.2.zfin.gz");

## open (OLDGAF1, "gene_association2.2.zfin") or die "Cannot open gene_association2.2.zfin : $!\n";
## while ($line = <OLDGAF1>) {
##    $gaf_version = $line if $line =~ m/!gaf-version/;
##    $versionNumber = $1 if $line =~ m/!Version:\s+([0123456789\.]+)/;
## }
## close OLDGAF1;

system("/bin/rm -f gene_association2.2.zfin");

$versionNumber += 0.001;

open (UNL1, ">gene_association2.2.zfin") or die "Cannot open exppat.UNL1";

print UNL1 "!gaf_version: 2.2\n";
printf UNL1 "!Version: %.3f\n", $versionNumber;
print UNL1 "!Date: ".`/bin/date +%Y/%m/%d`;
print UNL1 "!From: ZFIN (zfin.org) \n";
print UNL1 "! \n";

# set count to 0 before processing, increment it with each row processed.
$lastmrkrgoev = '';
$lastgrp=0;
@inf_array = ();
@rel_array= ();
$db='ZFIN';

open (INDEXFILE1, "go.zfin2") or die "open failed";
while ($line = <INDEXFILE1>) {

      chomp $line;
      @fields = split /\t/, $line;
      $mrkrgoev=$fields[0];

      if ($lastmrkrgoev ne '' && $mrkrgoev ne $lastmrkrgoev) {


          $lineToProduce = "$db\t$mrkrid\t$mrkrabb\t$qualifier\t$goid\t$pubid\t$evidence\t".
             join(',',@inf_array)."\t$go_o\t$mrkrname\t$aliases\t$gene_product\ttaxon:7955\t$ev_date\t$mod_by\t".
             "$relation\t$proteinid\n";

          ## DLOAD-480
          $find = 'GO Central';
          $replace = 'GO_Central';
          $lineToProduce =~ s/\Q$find\E/$replace/g;
          
          print UNL1 "$lineToProduce";

	  @inf_array = ();

      }


      @rel_array = ();
      $lastmrkrgoev = $mrkrgoev;
      $mrkrid=$fields[1];
      $mrkrabb=$fields[2];
      $mrkrname=$fields[3];
      $qualifier=goQlf($fields[9],$fields[10]);
      $goid=$fields[4];
      $pubid=goPub($fields[5],$fields[6],$fields[18],$fields[19]);
      $evidence=$fields[7];
      $inf=goInf($fields[8]);
      push(@inf_array, $inf);
      $go_o=goAspect($fields[11]);
      $ev_date=goDate($fields[12]);
      $mod_by=goMod($fields[13]);
      $aliases=$fields[14];
      $relation=$fields[15];
      $proteinid=$fields[17];
      $pubdoi=$fields[18];
      $pubgoref=$fields[19];




      if ($fields[16] eq "gene") {
	  $gene_product = 'protein';
      }
      elsif  ($fields[16] eq "lncrna_gene") {
	  $gene_product = 'lnc_RNA';
      }
      elsif  ($fields[16] eq "pseudogene") {
	  $gene_product = 'pseudogene';
      }
      elsif  ($fields[16] eq "lincrna_gene") {
	  $gene_product = 'lincRNA';
      }
      elsif  ($fields[16] eq "mirna_gene") {
	  $gene_product = 'miRNA';
      }
      elsif  ($fields[16] eq "pirna_gene") {
	  $gene_product = 'piRNA';
      }
      elsif  ($fields[16] eq "scrna_gene") {
	  $gene_product = 'scRNA';
      }
      elsif  ($fields[16] eq "snorna_gene") {
	  $gene_product = 'snoRNA';
      }
      elsif  ($fields[16] eq "trna_gene") {
	  $gene_product = 'tRNA';
      }
      elsif  ($fields[16] eq "rrna_gene") {
	  $gene_product = 'rRNA';
      }
      elsif  ($fields[16] eq "ncrna_gene") {
	  $gene_product = 'ncRNA';
      }
      elsif  ($fields[16] eq "srp_rna_gene") {
	  $gene_product = 'SRP_RNA';
      }
      else {
	  $gene_product=$fields[16];
      }
      $aliases=~s/,/|/g;
      $aliases=~s/Sierra/,/g;
      $relation=~s/,/|/g;
      $relation=~s/Prita/,/g;

}

close (UNL1);
close (INDEXFILE1);

sub goQlf()
 {
     $qualf = $_[0];
     $relation = $_[1];
     print $relation;
     $relation = 'contributes_to' if $relation eq 'contributes to';
     $relation = 'colocalizes_with' if $relation eq 'colocalizes with';
    if (length($qualf)!=0){
        if ($qualf eq 'not'){
            $qualf = 'NOT'.'|'. $relation
        }
     }
    else {
            $qualf =  $relation
          }

     return $qualf;
 }

sub goDate()
  {
    ($date, $time) = split(/ /, $_[0]);
    $date =~ s/-//g;
    return $date;
  }

sub goAspect()
  {
    $aspect = $_[0];
    $aspect = 'P' if ($aspect eq 'B');
    $aspect = 'F' if ($aspect eq 'M');

    return $aspect;
  }

sub goPub()
  {
    $accession = $_[1];
    $zfinpub =$_[0];
    $pubdoi=$_[2];
    $pubgoref=$_[3];
    $pmid='PMID:';
    $doiid='DOI:';
    $zfinid='ZFIN:';
    if (length($accession)!=0 && ($accession ne 'none')){
    $pub = $pmid.$accession.'|'.$zfinid.$zfinpub;
    }
    else{
    $pub = $zfinid.$zfinpub;
    }
    return $pub;
  }
sub goMod()
  {
    $mod_by =$_[0];
    $mod_by =~ s/UniProtKB/UniProt/;
#    $source = 'ZFIN' if ($mod_by ne 'S-P Curators');
#    $source = 'UniProt' if ($mod_by eq 'S-P Curators');
    $source = $mod_by ;
    return $source;
  }
sub goInf()
  {
    $inf =$_[0];

    $inf =~ s/GenBank:/EMBL:/g;
    $inf =~ s/GenPept:/protein_id:/;
    $inf =~ s/UniProt:/UniProtKB:/;

    if (index($inf,'\ ')==0) {
       $inf=~s/\\ //;
     }
    return $inf;
  }
 sub goRel()
    {
      $rel =$_[0];
      if (index($rel,'\ ')==0) {
         $rel=~s/\\ //;
       }
      return $rel;
    }

