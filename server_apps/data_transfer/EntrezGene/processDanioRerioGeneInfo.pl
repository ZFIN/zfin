#!/private/bin/perl
# processDanioRerioGeneInfo.pl

use DBI;
use MIME::Lite;

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

system("rm -f entrezid_zdbid_lg_type.unl");

system("rm -f mismatchedGeneId");
system("rm -f mismatchedGeneIdViaVega");
system("rm -f mismatchedEvenWithAllSyn");
system("rm -f mismatchedZDBgeneIdSymbol");
system("rm -f parsedViaVega");
system("rm -f replaced");
system("rm -f noZDBgeneVegaId");

system("rm -f prob_Danio_rerio_gene_info");
system("rm -f report_Danio_rerio_gene_info");


system("wget --timestamping ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz");
system("gunzip Danio_rerio.gene_info.gz");

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

print "dbname::: $dbname\n\n\n";

### open a handle on the db
$dbh = DBI->connect ("DBI:Informix:$dbname", $username, $password) 
    or die "Cannot connect to Informix database: $DBI::errstr\n";

$cur = $dbh->prepare('select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data where zrepld_old_zdb_id like "ZDB-GENE%";');
$cur->execute();
my ($oldZDBId, $newZDBId);
$cur->bind_columns(\$oldZDBId,\$newZDBId);

$ctTotalReplacedZDBIds = 0;
%replacedZDBids = ();
while ($cur->fetch()) {
   $replacedZDBids{$oldZDBId} = $newZDBId;
   $ctTotalReplacedZDBIds++;
}


$cur = $dbh->prepare('select mrkr_zdb_id, mrkr_abbrev from marker where mrkr_zdb_id like "ZDB-GENE%";');
$cur->execute();
my ($geneZDBId, $geneAbbrev);
$cur->bind_columns(\$geneZDBId,\$geneAbbrev);

### store the Gene ZDB Id/symbol pairs in hashes
$ctGeneAbbrevZDBidAtZFIN = 0;
%geneAbbrevZDBidAtZFIN = ();
%ZDBidGeneAbbrevAtZFIN = ();
while ($cur->fetch()) {
   $geneAbbrevZDBidAtZFIN{$geneAbbrev} = $geneZDBId;
   $ZDBidGeneAbbrevAtZFIN{$geneZDBId} = $geneAbbrev;
   $ctGeneAbbrevZDBidAtZFIN++;
}

$cur = $dbh->prepare('select dblink_linked_recid, count(*) from db_link where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1" group by dblink_linked_recid having count(*) > 1;');
$cur->execute();
my ($geneZDBIdWithMoreThan1NCBIids, $NCBIidCt);
$cur->bind_columns(\$geneZDBIdWithMoreThan1NCBIids,\$NCBIidCt);

$ctZDBidWithMoreThan1NCBIids = 0;
### store all the ZDB Gene IDs that have more than 1 NCBI Gene Ids in a hash
%zdbIdsWithMoreThan1NCBIids = ();
while ($cur->fetch()) {
   $zdbIdsWithMoreThan1NCBIids{$geneZDBIdWithMoreThan1NCBIids} = $NCBIidCt;
   $ctZDBidWithMoreThan1NCBIids++;
}

$cur = $dbh->prepare('select dblink_acc_num, count(*) from db_link where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1" group by dblink_acc_num having count(*) > 1;');
$cur->execute();
my ($NCBIgeneIdWithMoreThan1ZDBids, $zdbIdCt);
$cur->bind_columns(\$NCBIgeneIdWithMoreThan1ZDBids,\$zdbIdCt);

$ctNCBIgeneIdWithMoreThan1zdbIds = 0;
### store all the NCBI Gene Ids stored at ZFIN that correspond to more than 1 ZDB Gene Ids in a hash
%NCBIgeneIdsWithMoreThan1zdbIds = ();
while ($cur->fetch()) {
   $NCBIgeneIdsWithMoreThan1zdbIds{$NCBIgeneIdWithMoreThan1ZDBids} = $zdbIdCt;
   $ctNCBIgeneIdWithMoreThan1zdbIds++;
}

### it has been confirmed that one VEGA Id corresponds to one gene ZDB Id
$sqlGetVEGAidAndGeneZDBId = 'select dblink_acc_num, mrel_mrkr_1_zdb_id 
                               from marker_relationship, db_link 
                              where mrel_mrkr_2_zdb_id = dblink_linked_recid 
                                and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-14" 
                                and mrel_mrkr_1_zdb_id like "ZDB-GENE%" 
                                and dblink_acc_num like "OTTDARG%" 
                                and mrel_type = "gene produces transcript";';
$cur = $dbh->prepare($sqlGetVEGAidAndGeneZDBId);
$cur->execute();
my ($VEGAidAtZFIN, $geneZdbIdWithVEGA);
$cur->bind_columns(\$VEGAidAtZFIN,\$geneZdbIdWithVEGA);

$ctVEGAidzdbGeneIds = 0;
### store the VEGA Id/ZDB Gene ID pairs in hashes
%VEGAandZDBgeneIds = %ZDBgeneAndVEGAids = ();
while ($cur->fetch()) {
   $VEGAandZDBgeneIds{$VEGAidAtZFIN} = $geneZdbIdWithVEGA;
   $ZDBgeneAndVEGAids{$geneZdbIdWithVEGA} = $VEGAidAtZFIN;
   $ctVEGAidzdbGeneIds++;
}

$sqlGetNCBIidAndGeneZDBId = 'select dblink_acc_num, dblink_linked_recid 
                               from db_link 
                              where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1" 
                                and dblink_linked_recid like "ZDB-GENE%";';                                

$cur = $dbh->prepare($sqlGetNCBIidAndGeneZDBId);
$cur->execute();
my ($NCBIidAtZFIN, $geneZdbIdWithNCBI);
$cur->bind_columns(\$NCBIidAtZFIN,\$geneZdbIdWithNCBI);

$ctNCBIzdbGeneIds = 0;
### store the NCBI Id/ZDB Gene ID pairs in hashes
%NCBIandZDBgeneIds = %ZDBandNCBIgeneIds = ();
while ($cur->fetch()) {
   if (!exists($NCBIgeneIdsWithMoreThan1zdbIds{$NCBIidAtZFIN}) && !exists($zdbIdsWithMoreThan1NCBIids{$geneZdbIdWithNCBI})) {
     $NCBIandZDBgeneIds{$NCBIidAtZFIN} = $geneZdbIdWithNCBI;
     $ZDBandNCBIgeneIds{$geneZdbIdWithNCBI} = $NCBIidAtZFIN;
     $ctNCBIzdbGeneIds++;
   }
}

open (ZFGENE, "Danio_rerio.gene_info") ||  die "Cannot open Danio_rerio.gene_info : $!\n";

open (PARSEDZFGENE,  ">entrezid_zdbid_lg_type.unl") || die "Can't open: entrezid_zdbid_lg_type.unl $!\n";

open (NOZDBGENEVEGAID,  ">noZDBgeneVegaId") || die "Can't open: noZDBgeneVegaId $!\n";
print NOZDBGENEVEGAID "\n\n\n*** Records with no ZDB Id or no Vega Id\n\n";

open (REPLACED,  ">replaced") || die "Can't open: replaced $!\n";

print REPLACED "\n\n\n*** records with ZDB Id that has been replaced\n\n";
print REPLACED "symbol NCBI\tGene Id\tZDB Id (old)  \tsynonyms NCBI      \tnew ZDB Id       \n";
print REPLACED "-----------\t-------\t--------------\t-------------------\t-----------------\n";


open (ZDBGENEIDSYMBOLMISMATCH,  ">mismatchedZDBgeneIdSymbol") || die "Can't open: mismatchedZDBgeneIdSymbol $!\n";

print ZDBGENEIDSYMBOLMISMATCH "\n\n\n*** symbol or ZDB Id on NCBI does not match that at ZFIN but one of the synonyms matches\n\n";
print ZDBGENEIDSYMBOLMISMATCH "symbol NCBI\tsymbol ZFIN\tGene Id\tZDB Id         \tsynonyms NCBI   \tZFIN previous names\n";
print ZDBGENEIDSYMBOLMISMATCH "-----------\t-----------\t-------\t---------------\t----------------\t-------------------\n";

open (MISMATCHSYN,  ">mismatchedEvenWithAllSyn") || die "Can't open: mismatchedEvenWithAllSyn $!\n";

print MISMATCHSYN "\n\n\n*** symbol or ZDB Id on NCBI does not match that at ZFIN and none of the synonyms matches\n\n";
print MISMATCHSYN "symbol NCBI\tsymbol ZFIN\tGene Id\tZDB Id         \tsynonyms NCBI   \tZFIN previous names\n";
print MISMATCHSYN "-----------\t-----------\t-------\t---------------\t----------------\t-------------------\n";

open (MISMATCHGENEID,  ">mismatchedGeneId") || die "Can't open: mismatchedGeneId $!\n";
print MISMATCHGENEID "*** NCBI Gene Ids stored at ZFIN that do not match those on NCBI file\n\n";
print MISMATCHGENEID "EntrezGene Id\tgene Id ZFIN\tZDB ID      \n";
print MISMATCHGENEID "-------------\t------------\t------------\n";

open (MISMATCHGENEIDVEGA,  ">mismatchedGeneIdViaVega") || die "Can't open: mismatchedGeneIdViaVega $!\n";
print MISMATCHGENEIDVEGA "\n\n*** via Vega Id, found NCBI Gene Ids stored at ZFIN that do not match those on NCBI file\n\n";
print MISMATCHGENEIDVEGA "EntrezGene Id\tgene Id ZFIN\tZDB ID      \tVega Id      \n";
print MISMATCHGENEIDVEGA "-------------\t------------\t------------\t-------------\n";

open (PARSEDZFGENEVIAVEGA, ">parsedViaVega") || die "Can't open: parsedViaVega $!\n";
print PARSEDZFGENEVIAVEGA "\n\n\n*** EntreGene Id found via VegaId (loaded to ZFIN this time)\n";
print PARSEDZFGENEVIAVEGA "EntreGene Id\tZDB Id         \ttype       \tVega Id\n";
print PARSEDZFGENEVIAVEGA "------------\t---------------\t-----------\t-----------\n";


$ctlines = $ctNoZDBGeneId = $ctParsed = $ctMisMatchSymbol = $ctMisMatchEvenWithPrevname = $ctMismatchGeneId = $ctMismatchGeneIdFoundThruVEGA = $ctReplacedZDBids = $ctZDBGeneIdFoundThruVEGA = $ctNCBIidThruVEGA = 0;

while (<ZFGENE>) {
 chomp;
 
 $ctlines++;
 next if $ctlines < 2; 
 
 undef @fieldsZFGENE;
 @fieldsZFGENE = split("\t");

 $taxId = $fieldsZFGENE[0];
 
 ## don't process if it is not zebrafish gene
 next if $taxId ne "7955";
 
 $NCBIgeneId = $fieldsZFGENE[1];
 $symbol = $fieldsZFGENE[2];
 $synonyms = $fieldsZFGENE[4]; 
 $dbXrefs = $fieldsZFGENE[5];
 $chr = $fieldsZFGENE[6];
 $typeOfGene = $fieldsZFGENE[9];
 
 ## don't process if the gene id corresponds to more than 1 ZDB gene Ids
 next if exists($NCBIgeneIdsWithMoreThan1zdbIds{$NCBIgeneId});
 
 ### print "\n$NCBIgeneId\t$symbol\t$synonyms\t$dbXrefs\t$chr\ttypeOfGene$\n\n" if $ctlines < 10;
 
 if ($dbXrefs =~ m/ZFIN:(ZDB\-GENE\-[0-9]{6}\-[0-9]+)/ || $dbXrefs =~ m/ZFIN:(ZDB\-GENEP\-[0-9]{6}\-[0-9]+)/) {
     $ZDBgeneId = $1;

     ## don't process if the ZDB Gene Id has been replaced; print the row of the NCBI file for such replaced ZDB Gene Id   
     if (exists($replacedZDBids{$ZDBgeneId})) {
        $ctReplacedZDBids++;
        print REPLACED "$symbol\t$NCBIgeneId\t$ZDBgeneId\t$synonyms\t$replacedZDBids{$ZDBgeneId}\n";
        next;
     }
     
     ## don't process if the ZDB Gene Id corresponds to more than 1 NCBI gene ids    
     next if exists($zdbIdsWithMoreThan1NCBIids{$ZDBgeneId});

     undef($ZFINsymbol);
     if (exists($ZDBidGeneAbbrevAtZFIN{$ZDBgeneId})) {
         $ZFINsymbol = $ZDBidGeneAbbrevAtZFIN{$ZDBgeneId};
     } 
     
     $cur = $dbh->prepare('select dalias_alias_lower from data_alias where dalias_group_id = "1" and dalias_data_zdb_id = ?;');
     $cur->execute($ZDBgeneId);
     undef @prevNames;
     undef $ZFINprev;
     $ZFINprev = "";
     $cur->bind_columns(\$prev);
     while ($cur->fetch()) {
        push(@prevNames, $prev);
        $ZFINprev = $ZFINprev.$prev."|";
     }

     $hasAtleastOneSynonym = 0;
     $lowerCaseSynonymString = lc($synonyms);
     
     ## set the flag for at least one match for synonyms/previous names
     foreach $p (@prevNames) {
        if(index($lowerCaseSynonymString, $p) != -1 || lc($symbol) eq $p) {
           $hasAtleastOneSynonym = 1;
           last;
        }
     }
     
     $hasAtleastOneSynonym = 1 if defined($ZFINsymbol) && index($lowerCaseSynonymString, $ZFINsymbol) != -1;
     $hasAtleastOneSynonym = 1 if index($ZFINprev, lc($symbol)) != -1;
       
     ## if the ZDB Id or symbol on NCBI files does not match that at ZFIN
     if ((exists($geneAbbrevZDBidAtZFIN{$symbol}) && $geneAbbrevZDBidAtZFIN{$symbol} ne $ZDBgeneId) || (defined($ZFINsymbol) && lc($ZFINsymbol) ne lc($symbol))) {
          $mismatchSymbolFound = 1;
     } else {
          $mismatchSymbolFound = 0;
     }
     
     if ($mismatchSymbolFound > 0 && $hasAtleastOneSynonym > 0) {
         $ctMisMatchSymbol++;
         print ZDBGENEIDSYMBOLMISMATCH "$symbol\t$ZFINsymbol\t$NCBIgeneId\t$ZDBgeneId\t$synonyms\t$ZFINprev\n";
     } elsif ($mismatchSymbolFound > 0 && $hasAtleastOneSynonym == 0) {
         $ctMisMatchEvenWithPrevname++;
         print MISMATCHSYN "$symbol\t$ZFINsymbol\t$NCBIgeneId\t$ZDBgeneId\t$synonyms\t$ZFINprev\n";
     } 
     
     $cur = $dbh->prepare('select dblink_acc_num from db_link where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-1" and dblink_linked_recid = ?;');
     $cur->execute($ZDBgeneId);
     $cur->bind_columns(\$NCBIgeneAtZFIN);
     $mismatchGeneId = 0;
     while ($cur->fetch()) {
        if ($NCBIgeneId ne $NCBIgeneAtZFIN) {
          $mismatchGeneId = 1;
          $ctMismatchGeneId++;
          print MISMATCHGENEID "$NCBIgeneId\t$NCBIgeneAtZFIN\t$ZDBgeneId\n";
        }
     }
     
     if ($mismatchSymbolFound == 0 && $mismatchGeneId == 0) {                      
         $ctParsed++; 
         print PARSEDZFGENE "$NCBIgeneId|$ZDBgeneId|$typeOfGene|\n";
     }
 } elsif ($dbXrefs =~ m/Vega:(OTTDARG[0-9]+)/) {  ### if no ZDB Gene Id found in the file, get the ZDB Id via VEGA Id (if VEGA Id is there)
     $VegaIdatNCBI = $1;
     $ctZDBGeneIdFoundThruVEGA++;
     
     if (exists($VEGAandZDBgeneIds{$VegaIdatNCBI})) {
        $ZDBgeneId = $VEGAandZDBgeneIds{$VegaIdatNCBI};
     
        ## don't process if the ZDB Gene Id corresponds to more than 1 NCBI gene ids    
        next if exists($zdbIdsWithMoreThan1NCBIids{$ZDBgeneId});   
     
        if (exists($ZDBandNCBIgeneIds{$ZDBgeneId})) {
           $EntrezGeneIdAtZFIN = $ZDBandNCBIgeneIds{$ZDBgeneId};
           if ($EntrezGeneIdAtZFIN ne $NCBIgeneAtZFIN) {
              $ctMismatchGeneIdFoundThruVEGA++;
              print MISMATCHGENEIDVEGA "$NCBIgeneId\t$EntrezGeneIdAtZFIN\t$ZDBgeneId\t$VegaIdatNCBI\n";
           }           
        } else {   ### if ZFIN doesn't have the EntrezGene Id (found thru Vegea Id), load it
           $ctParsed++;
           print PARSEDZFGENE "$NCBIgeneId|$ZDBgeneId|$typeOfGene|\n";
           
           $ctNCBIidThruVEGA++;
           print PARSEDZFGENEVIAVEGA "$NCBIgeneId\t$ZDBgeneId\t$typeOfGene\t$VegaIdatNCBI\n";
        }
     }
     
 } else {  ### no ZDB Id and no VEGA Id on NCBI file
     $ctNoZDBGeneId++;
     print NOZDBGENEVEGAID "$NCBIgeneId\t$symbol\t$synonyms\t$dbXrefs\t$chr\t$typeOfGene\n";
 }           
 
}     
 
$ctlines--;
 
$cur->finish(); 

$dbh->disconnect();  
 
close(ZFGENE);
close(PARSEDZFGENE);
close(NOZDBGENEVEGAID);
close(ZDBGENEIDSYMBOLMISMATCH);
close(MISMATCHSYN);
close(MISMATCHGENEID);
close(MISMATCHGENEIDVEGA);
close(REPLACED);
close(PARSEDZFGENEVIAVEGA);


print "\nctlines = $ctlines\nctParsed = $ctParsed\nctNoZDBGeneId = $ctNoZDBGeneId\tctMisMatchSymbol = $ctMisMatchSymbol\tctMisMatchEvenWithPrevname: $ctMisMatchEvenWithPrevname\tctMismatchGeneId = $ctMismatchGeneId \n\n";
print "\nctZDBidWithMoreThan1NCBIids: $ctZDBidWithMoreThan1NCBIids\tctNCBIgeneIdWithMoreThan1zdbIds: $ctNCBIgeneIdWithMoreThan1zdbIds\tctReplacedZDBids: $ctReplacedZDBids\n\n";

print "\nctMismatchGeneIdFoundThruVEGA: $ctMismatchGeneIdFoundThruVEGA\tctZDBGeneIdFoundThruVEGA: $ctZDBGeneIdFoundThruVEGA\tctNCBIzdbGeneIds: $ctNCBIzdbGeneIds\tctNCBIidThruVEGA: $ctNCBIidThruVEGA\n\n\n";

system("cat mismatchedGeneId mismatchedGeneIdViaVega mismatchedEvenWithAllSyn mismatchedZDBgeneIdSymbol parsedViaVega replaced noZDBgeneVegaId > prob_Danio_rerio_gene_info");

open (REPORT,  ">>report_Danio_rerio_gene_info") || die "Can't open: report_Danio_rerio_gene_info $!\n";

print REPORT "\nThere are total number of $ctlines lines on NCBI's Danio_rerio.gene_info file.\n\n";

print REPORT "$ctNoZDBGeneId of them have neither ZDB Id nor Vega Id (see section of Records with no ZDB Id and no Vega Id).\n\n";

print REPORT "$ctReplacedZDBids of them have replaced (old) ZDB Ids and the scripts does not process them. See section of those with ZDB Id that have been replaced.\n\n";

print REPORT "Mis-matches of symbols but with at least 1 match with synonyms/previous names: $ctMisMatchSymbol\n\n";

print REPORT "Mis-match even with all synonyms/previous names: $ctMisMatchEvenWithPrevname\n\n";

print REPORT "Mis-match of EntrezGene Id: $ctMismatchGeneId\n\n";

print REPORT "The number of possible mismatched EntrezGene Id found via VEGA Id: $ctMismatchGeneIdFoundThruVEGA\n\n";

print REPORT "The number of EntrezGene Ids (loaded to ZFIN by the scripts) found thru Vega Id: $ctNCBIidThruVEGA (see section of EntreGene Id found via VegaId)\n\n\n";

close(REPORT);

system("cat prob_Danio_rerio_gene_info >> report_Danio_rerio_gene_info");

&sendMail("Auto from $dbname: entrezGene.pl : ","<!--|SWISSPROT_EMAIL_REPORT|-->","report of processing NCBI data file, Danio_rerio_gene_info","report_Danio_rerio_gene_info");

exit;

sub sendMail($) {

    my $SUBJECT=$_[0] .": " .$_[2];
    my $MAILTO=$_[1];
    my $TXTFILE=$_[3]; 
    
    # Create a new multipart message:
    $msg1 = new MIME::Lite 
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
	Type    => 'multipart/mixed';

    attach $msg1 
	Type     => 'text/plain',   
	Path     => "$TXTFILE";

    # Output the message to sendmail
    
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg1->print(\*SENDMAIL);
    close (SENDMAIL);
    
}
