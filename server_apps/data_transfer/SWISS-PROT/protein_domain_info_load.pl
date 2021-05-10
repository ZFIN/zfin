#!/opt/zfin/bin/perl

# protein_domain_info_load.pl
# This script downloads some data file from Interpro and process it and some other file from UniProt Load,
# in order to load and update protein domain info stored in ZFIN database tables

use DBI;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;
use Try::Tiny;

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

### open a handle on the db
$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

## get the existing data so as to be able to compare with those in the load

$cur_domain = $dbh->prepare("select ip_interpro_id, ip_name, ip_type from interpro_protein;");
$cur_domain->execute();
$cur_domain->bind_columns(\$id, \$name, \$type);

$ctExistingInterpro = 0;
%interproIds = ();
while ($cur_domain->fetch()) {
   $interproIds{$id} = "$id|$name|$type";
   $ctExistingInterpro++;
}

$cur_domain->finish();

print "ctExistingInterpro = $ctExistingInterpro\n";

$cur_unipro = $dbh->prepare("select up_uniprot_id, up_length from protein;");
$cur_unipro->execute();
$cur_unipro->bind_columns(\$id, \$len);

$ctUnipro = 0;
%uniproIds = ();
%updatedUniproIds = ();
while ($cur_unipro->fetch()) {
   $uniproIds{$id} = $len;
   $updatedUniproIds{$id} = 1;
   $ctUnipro++;
}

$cur_unipro->finish();

print "ctUnipro = $ctUnipro\n";

$cur_mrkr = $dbh->prepare("select mtp_mrkr_zdb_id, mtp_uniprot_id from marker_to_protein;");
$cur_mrkr->execute();
$cur_mrkr->bind_columns(\$mrkrId, \$uniproId);

$ctMrkrUnipro = 0;
%mrkrUnipro = ();
%updatedMrkrUnipro = ();
while ($cur_mrkr->fetch()) {
   $mrkrUnipro{$mrkrId.$uniproId} = $mrkrId."|".$uniproId;   
   $updatedMrkrUnipro{$mrkrId.$uniproId} = 1; 
   $ctMrkrUnipro++;
}

$cur_mrkr->finish();

print "ctMrkrUnipro = $ctMrkrUnipro\n";

$cur_uniproInterpro = $dbh->prepare("select pti_uniprot_id, pti_interpro_id from protein_to_interpro;");
$cur_uniproInterpro->execute();
$cur_uniproInterpro->bind_columns(\$uniproId, \$interproId);

$ctUniproInterpro = 0;
%uniproInterpro = ();
%updatedUniproInterpro = ();
while ($cur_uniproInterpro->fetch()) {
   $uniproInterpro{$uniproId.$interproId} = $uniproId."|".$interproId;
   $updatedUniproInterpro{$uniproId.$interproId} = 1;
   $ctUniproInterpro++;
}

$cur_uniproInterpro->finish();

print "ctUniproPdb = $ctUniproPdb\n";

$cur_uniproPdb = $dbh->prepare("select ptp_uniprot_id, ptp_pdb_id from protein_to_pdb;");
$cur_uniproPdb->execute();
$cur_uniproPdb->bind_columns(\$uniproId, \$pdbId);

$ctUniproPdb = 0;
%uniproPdb = ();
%updatedUniproPdb = ();
while ($cur_uniproPdb->fetch()) {
   $uniproPdb{$uniproId.$pdbId} = $uniproId."|".$pdbId;
   $updatedUniproPdb{$uniproId.$pdbId} = 1;
   $ctUniproPdb++;
}

$cur_uniproPdb->finish();

print "ctUniproPdb = $ctUniproPdb\n";




$cur_gene = $dbh->prepare("select mrkr_zdb_id from marker where mrkr_zdb_id like 'ZDB-GENE%';");
$cur_gene->execute();
$cur_gene->bind_columns(\$zdbGeneId);

$ctGenes = 0;
%zfinGeneIds = ();
while ($cur_gene->fetch()) {
   $zfinGeneIds{$zdbGeneId} = 1;
   $ctGenes++;
}

$cur_gene->finish();

print "ctGenes = $ctGenes\n";

$cur_replaced = $dbh->prepare("select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data where zrepld_old_zdb_id like 'ZDB-GENE%' and zrepld_new_zdb_id like 'ZDB-GENE%';");
$cur_replaced->execute();
$cur_replaced->bind_columns(\$oldid, \$newid);

$ctReplacedGenes = 0;
%replacedGeneIds = ();
while ($cur_replaced->fetch()) {
   $replacedGeneIds{$oldid} = $newid;
   $ctReplacedGenes++;
}

$cur_replaced->finish();

print "ctReplacedGenes = $ctReplacedGenes\n";

system("/bin/rm -f entry.list");

system("/bin/rm -f domain.txt");

## download the Interpro file for protein domain summary
system("/local/bin/wget ftp://ftp.ebi.ac.uk/pub/databases/interpro/current/entry.list");

open (DOMAINS, "entry.list") || die "Cannot open entry.list : $!\n";
open (DOMAINOUT, ">domain.txt") || die "Cannot open domain.txt : $!\n";
@lines = <DOMAINS>;
close DOMAINS;

$ctInput = $ctLoadedDomains = 0;

# parsing the data file with protein domain info

# IPR022682	Domain	Peptidase C2, calpain, large subunit, domain III
%interproIdFromInput = ();
foreach $line (@lines) {
   $ctInput++;
   next if $ctInput < 2;

   chop($line);
   @fields = split(/\t/, $line);

   $ipr = $fields[0];
   $ipr =~ s/^\s+//;
   $ipr =~ s/\s+$//;
   
   $type = $fields[1];
   $type =~ s/^\s+//;
   $type =~ s/\s+$//;   
   
   $name = $fields[2];
   $name =~ s/^\s+//;
   $name =~ s/\s+$//;   
   
   $interproIdFromInput{$ipr} = "$ipr|$name|$type";

   # if the parsed data not existing at ZFIN, write to the file to be used by the loading process
   if (!exists($interproIds{$ipr}) || $interproIds{$ipr} ne "$ipr|$name|$type") {
     print DOMAINOUT "$ipr|$name|$type\n";
     $ctLoadedDomains++;
   }
   
}

close DOMAINOUT;

# delete the records in interpro_protein table that are not in this load or with different domain names and types
$ctDeletedDomain = 0;
$cur_delete_record = $dbh->prepare_cached("delete from zdb_active_data where zactvd_zdb_id = ?;");
for $existingIpr (keys %interproIds) {
  if(!exists($interproIdFromInput{$existingIpr}) || $interproIdFromInput{$existingIpr} ne $interproIds{$existingIpr}) {
     $cur_delete_record->execute($existingIpr);  
     $ctDeletedDomain++;
  } 
}

print "ctInput = $ctInput\nctLoadedDomains = $ctLoadedDomains\n";

print "ctDeletedDomain = $ctDeletedDomain\n\n";

undef %interproIds;
undef @lines;

system("/bin/rm -f protein.txt");
system("/bin/rm -f zfinprotein.txt");
system("/bin/rm -f unipro2interpro.txt");
system("/bin/rm -f unipro2pdb.txt");
system("/bin/rm -f postProteinDomainInfoLoadStatistics.txt");

$/ = "\/\/\n";

open (UNIPROT, "okfile") || die "Cannot open okfile : $!\n";
open (PROTEIN, ">protein.txt") || die "Cannot open protein.txt : $!\n";
open (ZFINPROT, ">zfinprotein.txt") || die "Cannot open zfinprotein.txt : $!\n";
open (UNIPROTINTERPRO, ">unipro2interpro.txt") || die "Cannot open unipro2interpro.txt : $!\n";
open (UNIPROTPDB, ">unipro2pdb.txt") || die "Cannot open unipro2pdb.txt : $!\n";

@uniproRecords = <UNIPROT>;

# parsing the UniProt file


$ctRecords = $ctUniProtIDs = $ctZfinUniProt = $ctUniProtInterpro = 0;
%uniproIdFromInput = ();
%mrkrUnipFromInput = ();
%unipIprFromInput = ();
foreach $record (@uniproRecords) {
  $ctRecords++;
  @zfinids = ();
  @iprs = ();
  @lines = split(/\n/, $record);
  foreach $line (@lines) {
    ## ID   A0A1L1QZT0_DANRE        Unreviewed;       337 AA.
    if ($line =~ m/ID\s+(\S+)_DANRE\s+\w+;\s+(\d+)\s+AA/) {

       ## do not process the redundant record
      $length = $2;

    }
    if ($line =~ m/^AC\s+(.*)/) {
              $sp_ac_list = $sp_ac_list.$1.' ';
              @sp_ac = split(' ',$sp_ac_list);
              $prm_ac = shift @sp_ac;
              chop $prm_ac;
              $uniproIdFromInput{$prm_ac} = $length;

          }
    ## DR   ZFIN; ZDB-GENE-131122-26; ikbip.
    ## could be multiple ZFIN ID
    if ($line =~ m/DR\s+ZFIN;\s+(ZDB-GENE\S+);/) {
        $zfinidFromInput = $1;
        if (!exists($zfinGeneIds{$zfinidFromInput})) {
          if (exists($replacedGeneIds{$zfinidFromInput}))  {
             $zfinid = $replacedGeneIds{$zfinidFromInput};
          } else {
             undef $zfinid;
          }
        } else {
          $zfinid = $zfinidFromInput;
        }
        if (defined $zfinid) {
          $mrkrUnipFromInput{$zfinid.$prm_ac} = 1;
          push @zfinids, $zfinid;
        }
    }

    ## DR   InterPro; IPR024152; Inh_kappa-B_kinase-int.
    ## could be multiple InterPro
    if ($line =~ m/DR\s+InterPro;\s+(IPR\S+);/) {
      $interproID = $1;
      if (exists($interproIdFromInput{$interproID})) {
        $unipIprFromInput{$prm_ac.$interproID} = 1;
        push @iprs, $interproID;
      }
    }
    if ($line =~ m/DR\s+PDB;/) {
    @values = split(';', $line);

        $pdbID=$values[1];
         push @pbids, $pdbID;

      }


  }

  # if the parsed data	not existing at	ZFIN, write to the file to be used by the loading process
  if (defined $prm_ac && scalar @zfinids > 0) {
    $length = 0 if (!defined $length || $length =~ m/\s+/ || $length eq '');
    if (!exists($updatedUniproIds{$prm_ac}) || ($length > 0 && $uniproIds{$prm_ac} ne $length)) {
      $updatedUniproIds{$prm_ac} = 1;
      print PROTEIN "$prm_ac|ZDB-FDBCONT-040412-47|$length\n";
      $ctUniProtIDs++;
    }
;
    foreach $zdbid (@zfinids) {
      if(!exists($updatedMrkrUnipro{$zdbid.$prm_ac})) {
         $updatedMrkrUnipro{$zdbid.$prm_ac} = 1;
         print ZFINPROT "$zdbid|$prm_ac\n";
         $ctZfinUniProt++;
      }
    }

    if (scalar @iprs > 0) {
      foreach $ipr (@iprs) {
        if(!exists($updatedUniproInterpro{$prm_ac.$ipr})) {
           $updatedUniproInterpro{$prm_ac.$ipr} = 1;
           print UNIPROTINTERPRO "$prm_ac|$ipr\n";
           $ctUniProtInterpro++;  
        } 
      }
    }

    if (scalar @pbids > 0) {
          foreach $pbid (@pbids) {
          if(!exists($updatedUniproPdb{$prm_ac.$pbid})) {
                     $updatedUniproPdb{$prm_ac.$pbid} = 1;
               print  UNIPROTPDB "$prm_ac|$pbid\n";
               $ctUniProtPdb++;

            }
          }
        }
  
  }
  
  undef $id;
  undef $length;
  undef $zfinidFromInput;
  undef $zfinid;
  undef $prm_ac;
  @zfinids = ();
  undef $interproID;
  @iprs = ();
  @pbids = ();
  @sp_ac=(); $sp_ac=''; $sp_ac_list='';
}

print "ctRecords = $ctRecords \t ctUniProtIDs = $ctUniProtIDs\t ctZfinUniProt = $ctZfinUniProt\tctUniProtInterpro = $ctUniProtInterpro\n";

# delete the records in protein table that are not in this load or having different length values
$ctDeletedUnip = 0;
for $existingUnipro (keys %uniproIds) {
  if(!exists($uniproIdFromInput{$existingUnipro}) or $uniproIdFromInput{$existingUnipro} ne $uniproIds{$existingUnipro}) {
     $cur_delete_record->execute($existingUnipro);  
     $ctDeletedUnip++;
  } 
}

$cur_delete_record->finish();

print "ctDeletedUnip = $ctDeletedUnip\n\n";

# delete the records in marker_to_protein table that are not in this load
$ctDeletedMrkrUnip = 0;
$cur_delete_mrkr_unipr = $dbh->prepare_cached("delete from marker_to_protein where mtp_mrkr_zdb_id = ? and mtp_uniprot_id = ?;");
for $existingMrkrUnipro (keys %mrkrUnipro) {
  if(!exists($mrkrUnipFromInput{$existingMrkrUnipro})) {
     @strings = split(/\|/, $mrkrUnipro{$existingMrkrUnipro}); 
     $mrkr = $strings[0];
     $unipro = $strings[1];
     $cur_delete_mrkr_unipr->execute($mrkr, $unipro);  
     $ctDeletedMrkrUnip++;
  }
}

$cur_delete_mrkr_unipr->finish();

print "ctDeletedMrkrUnip = $ctDeletedMrkrUnip\n\n";

# delete the records in protein_to_interpro table that are not in this load
$ctDeletedUnipInterpro = 0;
$cur_delete_unipr_ipr = $dbh->prepare_cached("delete from protein_to_interpro where pti_uniprot_id = ? and pti_interpro_id = ?;");
for $existingUniproIpr (keys %uniproInterpro) {
  if(!exists($unipIprFromInput{$existingUniproIpr})) {
     @strings = split(/\|/, $uniproInterpro{$existingUniproIpr}); 
     $unipro = $strings[0];
     $interpro = $strings[1];
     $cur_delete_unipr_ipr->execute($unipro, $interpro);  
     $ctDeletedUnipInterpro++;
  }
}

$cur_delete_unipr_ipr->finish();

print "ctDeletedUnipInterpro = $ctDeletedUnipInterpro\n\n";


# delete the records in protein_to_pdb table that are not in this load
$ctDeletedUnipPdb = 0;
$cur_delete_unipr_pdb = $dbh->prepare_cached("delete from protein_to_pdb where ptp_uniprot_id = ? and ptp_pdb_id = ?;");
for $existingUniproPdb (keys %uniproPdb) {
  if(!exists($unipIprFromInput{$existingUniproPdb})) {
     @strings = split(/\|/, $uniproPdb{$existingUniproPdb});
     $unipro = $strings[0];
     $pdb = $strings[1];
     $cur_delete_unipr_pdb->execute($unipro, $pdb);
     $ctDeletedUnipPdb++;
  }
}

$cur_delete_unipr_pdb->finish();

print "ctDeletedUnipPdb = $ctDeletedUnipPdb\n\n";


close UNIPROT;
close PROTEIN;
close ZFINPROT;
close UNIPROTINTERPRO;
close UNIPROTPDB;

$dbh->disconnect();

# execute the sql file to do the loading
try {
  system("psql -d <!--|DB_NAME|--> -a -f load_protein_domain_info.sql");
} catch {
  warn "Failed to execute load_protein_domain.sql - $_";
  exit -1;
};

undef @uniproRecords;
undef %uniproIds;
undef %updatedUniproIds;
undef %uniproIdFromInput;
undef %mrkrUnipro;
undef %updatedMrkrUnipro;
undef %mrkrUnipFromInput;
undef %uniproInterpro;
undef %updatedUniproInterpro;
undef %updatedUniproPdb;

undef %unipIprFromInput;

open (POSTLOADREPORT, '>postProteinDomainInfoLoadStatistics.txt') or die "Cannot open postProteinDomainInfoLoadStatistics.txt: $!";

print POSTLOADREPORT "count of records associated with protein domain info load\t";
print POSTLOADREPORT "before load\t";
print POSTLOADREPORT "after load\t";
print POSTLOADREPORT "percentage change\n";
print POSTLOADREPORT "---------------------------------------------------------\t-----------\t-----------\t-----------------\n";

$ctInterproAfter = countData("select * from interpro_protein;");

print POSTLOADREPORT "interpro_protein records                                       \t";
print POSTLOADREPORT "$ctExistingInterpro        \t";
print POSTLOADREPORT "$ctInterproAfter        \t";
print POSTLOADREPORT "\n" if ($ctExistingInterpro == 0);
printf POSTLOADREPORT "%.2f\n", ($ctInterproAfter - $ctExistingInterpro) / $ctExistingInterpro * 100 if ($ctExistingInterpro > 0);

$ctUniproAfter = countData("select * from protein;");

print POSTLOADREPORT "(uniprot) protein records                                      \t";
print POSTLOADREPORT "$ctUnipro        \t";
print POSTLOADREPORT "$ctUniproAfter        \t";
print POSTLOADREPORT "\n" if ($ctUnipro == 0);
printf POSTLOADREPORT "%.2f\n", ($ctUniproAfter - $ctUnipro) / $ctUnipro * 100 if ($ctUnipro > 0);

$ctMrkrUniproAfter = countData("select * from marker_to_protein;");

print POSTLOADREPORT "marker_to_protein records                                  \t";
print POSTLOADREPORT "$ctMrkrUnipro        \t";
print POSTLOADREPORT "$ctMrkrUniproAfter        \t";
print POSTLOADREPORT "\n" if ($ctMrkrUnipro == 0);
printf POSTLOADREPORT "%.2f\n", ($ctMrkrUniproAfter - $ctMrkrUnipro) / $ctMrkrUnipro * 100 if ($ctMrkrUnipro > 0);

$ctUniproInterproAfter = countData("select * from protein_to_interpro;");

print POSTLOADREPORT "protein_to_interpro records                                \t";
print POSTLOADREPORT "$ctUniproInterpro        \t";
print POSTLOADREPORT "$ctUniproInterproAfter        \t";
print POSTLOADREPORT "\n" if ($ctUniproInterpro == 0);
printf POSTLOADREPORT "%.2f\n", ($ctUniproInterproAfter - $ctUniproInterpro) / $ctUniproInterpro * 100 if ($ctUniproInterpro > 0);

close POSTLOADREPORT;

print "\nDone with protein domain info loading \n\n";
exit;


sub countData {
  my $ctsql = @_[0];
  my $nRecords = 0;

  my $dbname = "<!--|DB_NAME|-->";
  my $username = "";
  my $password = "";

  ### open a handle on the db
  my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";

  my $sth = $dbh->prepare($ctsql) or die "Prepare fails";

  $sth -> execute() or die "Could not execute $sql";

  while (my @row = $sth ->fetchrow_array()) {
    $nRecords++;
  }

  $dbh->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";

  return ($nRecords);
}


