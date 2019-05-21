#!/private/bin/perl


use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

system("/bin/date");

system("/bin/rm -f *.transcripts.txt");
system("/bin/rm -f Danio_rerio.GRCz11.96.chr.gff3");

system("/local/bin/wget ftp://ftp.ensembl.org/pub/release-96/gff3/danio_rerio/Danio_rerio.GRCz11.96.chr.gff3.gz");
system("/local/bin/gunzip Danio_rerio.GRCz11.96.chr.gff3.gz");

open (GFF, "Danio_rerio.GRCz11.96.chr.gff3") ||  die "Cannot open Danio_rerio.GRCz11.96.chr.gff3 : $!\n";

@lines = <GFF>;

close(GFF);

$ctGenes = 0;
%genesEnsemble = ();
$ctTranscripts = 0;
%transcriptsEnsemble = ();
foreach $line (@lines) {

  if ($line =~ m/.+\s+gene\s+.+ID=gene:(ENSDARG\d+);Name=.+:(ZDB.+)];gene_id=.+/) {
      $ctGenes++;
      $genesEnsemble{$2} = $1; 
  }
  
  if ($line =~ m/.+ID=transcript:(ENSDART\d+);Parent=gene:(ENSDARG\d+);Name=.+/) {
      $ctTranscripts++;
      $ensdart = $1;
      $ensdarg = $2; 
      if (!exists($transcriptsEnsemble{$ensdarg})) {
         $ref_ensdarts = [$ensdart];
         $transcriptsEnsemble{$ensdarg} = $ref_ensdarts;
      } else {      
         $ref_ensdarts = $transcriptsEnsemble{$ensdarg};
         push(@$ref_ensdarts, $ensdart);
      }
  }

}


print "\nENSMBLE:  ctGenes = $ctGenes\n\nctTranscripts\t$ctTranscripts\n\n";


$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password) or die "Cannot connect to database: $DBI::errstr\n";

$sql = "select tscript_ensdart_id, tscript_mrkr_zdb_id
          from transcript 
         where tscript_ensdart_id is not null 
           and exists (select 1 from marker_relationship
                        where mrel_type  = 'gene produces transcript' 
                          and mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id);";

my $cur = $dbh->prepare($sql);
$cur ->execute();

$cur->bind_columns(\$ensdartZfin,\$transcriptZdbID);

%zfinEnsdarts = ();
%zfinGenesWithTranscript = ();

$ctZfinEnsdarts = 0;      
while ($cur->fetch()) {
   $ctZfinEnsdarts++;
   $zfinEnsdarts{$transcriptZdbID} = $ensdartZfin;
   $zfinGenesWithTranscript{$zdbGene} = 1;
}

$cur->finish(); 

print "ctZfinEnsdarts = $ctZfinEnsdarts\n\n";

$sqlZFIN = "select mrel_mrkr_2_zdb_id, mrel_mrkr_1_zdb_id
              from marker_relationship 
             where mrel_type  = 'gene produces transcript';";

my $curZFIN = $dbh->prepare($sqlZFIN);
$curZFIN ->execute();

$curZFIN->bind_columns(\$transcript,\$zdbGene);

%zfinTranscriptsAndGenes = ();

$ctRel = 0;      
while ($curZFIN->fetch()) {     
  $ctRel++;
  $zfinTranscriptsAndGenes{$transcript} = $zdbGene;
}

$curZFIN->finish(); 

$dbh->disconnect();

print "ctRel = $ctRel\n\n";


%transcriptsPerGeneZFIN = ();

foreach $transcript (keys %zfinTranscriptsAndGenes) {
  $gene = $zfinTranscriptsAndGenes{$transcript};  
  if($gene && exists($zfinEnsdarts{$transcript})) {
    $endsdartZfin = $zfinEnsdarts{$transcript};
    if (!exists($transcriptsPerGeneZFIN{$gene})) {
       $ref_transcripts = [$endsdartZfin];
       $transcriptsPerGeneZFIN{$gene} = $ref_transcripts;
    } else {      
       $ref_transcripts = $transcriptsPerGeneZFIN{$gene};
       push(@$ref_transcripts, $endsdartZfin);
    }
  } 
}

foreach $g (keys %zfinGenesWithTranscript) {
  if (!exists($transcriptsPerGeneZFIN{$g})) {
    $transcriptsPerGeneZFIN{$g} = 0;
  }
}

open (REP, ">report.transcripts.txt") || die "Cannot open report.transcripts.txt : $!\n";
open (NOG, ">genes_missing_ensdarg.transcripts.txt") || die "Cannot open genes_missing_ensdarg.transcripts.txt : $!\n";
open (DIFFT, ">genes_with_different_ensdarts.transcripts.txt") || die "Cannot open genes_with_different_ensdarts.transcripts.txt : $!\n";
$ctAll = 0;
$ctNoEnsdarg = $ctNoEnsdart = 0;
$totalNumMissingEnsdarts = 0;
foreach $zfinGene (sort keys %transcriptsPerGeneZFIN) {
  if (exists($genesEnsemble{$zfinGene})) {
    $ctAll++;
    $ensdarg = $genesEnsemble{$zfinGene};
    if (exists($transcriptsEnsemble{$ensdarg})) {
      $refEnsdarts = $transcriptsEnsemble{$ensdarg};
      $sizeEnsembl = 1 + $#$refEnsdarts;
      if($transcriptsPerGeneZFIN{$zfinGene} == 0) {
         print REP "$zfinGene\t$ensdarg\t0\t$sizeEnsembl\t$sizeEnsembl";
         foreach $ensdart (sort @$refEnsdarts) {
	   print REP "\t$ensdart";
         }
         print REP "\n";
      } else {
         $refTranscriptsZdbGene = $transcriptsPerGeneZFIN{$zfinGene};
         $sizeZFIN = 1 + $#$refTranscriptsZdbGene;
         $sizeDiff = $sizeEnsembl - $sizeZFIN;
         if ($sizeDiff != 0) {
           print REP "$zfinGene\t$ensdarg\t$sizeZFIN\t$sizeEnsembl\t$sizeDiff";
           foreach $ensdart (sort @$refEnsdarts) {
             $found = 0;
             foreach $transcriptAtZFIN (sort @$refTranscriptsZdbGene) {
               if ($transcriptAtZFIN eq $ensdart) {
                 $found = 1;
               }
             }
             if ($found == 0) {
               print REP "\t$ensdart";
               $totalNumMissingEnsdarts++;
             }         
           }
           print REP "\n";
         }
         if ($sizeDiff < 0) {
           print DIFFT "$zfinGene\t$ensdarg\t$sizeZFIN\t$sizeEnsembl\t$sizeDiff\nZFIN\t";
           foreach $tZ (sort @$refTranscriptsZdbGene) {
             print DIFFT "\t$tZ";
           }
           print DIFFT "\nENSEMBL\t";
           foreach $eT (sort @$refEnsdarts) {
             print DIFFT "\t$eT";
	   }
           print DIFFT "\n";
         }
      }      
    } else {
       print "No ENSDARTs for $zfinGene at ENSEMBL!!!\n";
       $ctNoEnsdart++;
    }
  } else {
     print NOG "$zfinGene\n";
     $ctNoEnsdarg++;
  }
}


print "ctAll = $ctAll \t ctNoEnsdarg = $ctNoEnsdarg \t ctNoEnsdart = $ctNoEnsdart \n\n";

print "\nTotal number of ENSDARTS missing from ZFIN: $totalNumMissingEnsdarts\n\n";

close REP;
close NOG;
close DIFFT;

system("/bin/date");

exit;


