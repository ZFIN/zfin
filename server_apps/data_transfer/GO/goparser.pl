#!/local/bin/perl

#
#  fmparser.pl
#


open (INDEXFILE, "go.zfin") or die "open failed";
open (UNL, ">gene_association.zfin") or die "Cannot open exppat.unl";

# set count to 0 before processing, increment it with each row processed.
$gene='';
$spid='';
while ($line = <INDEXFILE>) {
      chomp $line;
      @fields = split /\t/, $line;
      $db='ZFIN';
      $mrkrid=$fields[0];
      $mrkrabb=$fields[1];
      $mrkrname=$fields[2];
      $qualifier=goqual($fields[8]);
      $goid=$fields[3];
      $pubid=goPub($fields[4],$fields[5]);
      $evidence=$fields[6];
      $inf=goInf($fields[7]);
      $go_o=goAspect($fields[9]);
      $ev_date=$fields[10];
      $mod_by=goMod($fields[11]);
      print UNL "$db\t$mrkrid\t$mrkrabb\t$qualifier\tGO:$goid\tZFIN:$pubid\t$evidence\t$inf\t$go_o\t$mrkrname\t\tgene\ttaxon:7955\t$ev_date\t$mod_by\n";
}

close (UNL);
close (INDEXFILE);
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
    $pub =$_[0];
    $pmid='|PMID:';
    $pub = $pub.$pmid.$accession if (length($accession)!=0 && ($accession ne 'none'));
    $pub = $pub if (length($accession)==0);
    return $pub;
  }
sub goMod()
  {
    $mod_by =$_[0];
    $source = 'ZFIN' if ($mod_by ne 'S-P Curators');
    $source = 'UniProt' if ($mod_by eq 'S-P Curators');

    return $source;
  }


sub goInf()
  {
    $inf =$_[0];
    # $inf = substr($inf,3,length($inf)) if (index($inf,'GO:')==0);
    $inf =~ s/Genbank:/EMBL:/g;
    if (index($inf,'SWISS-PROT:')==0) {
       $inf=~s/SWISS-PROT:/SPTR:/;
     }

    if (index($inf,'GenPept:')==0) {
       $inf=~s/GenPept:/NCBI_NP:/;
     }
    if (index($inf,'RefSeq:')==0) {
       $inf=~s/RefSeq:/NCBI_NM:/;
     }

    if (index($inf,'\ ')==0) {
       $inf=~s/\\ //;
     }
    return $inf;
  }

sub goqual()
 {
   $qual=$_[0];
   if (index($qual,'\ ')==0) {
       $qual=~s/\\ //;
     }
    return $qual;
  }
exit;
