#!/opt/zfin/bin/perl

# NCBIStartEnd.pl
# parses ftp://ftp.ncbi.nlm.nih.gov/genomes/refseq/vertebrate_other/Danio_rerio/all_assembly_versions/GCF_000002035.6_GRCz11/GCF_000002035.6_GRCz11_feature_table.txt
# prepare the list to update related sequence_feature_chromosome_location_generated records 

use DBI;

$dbname = "<!--|DB_NAME|-->";
$username = "";
$password = "";

if (-e "GCF_000002035.6_GRCz11_feature_table.txt") {
  &doSystemCommand("/bin/rm -f GCF_000002035.6_GRCz11_feature_table.txt");
}

if (-e "updateList") {
  &doSystemCommand("/bin/rm -f updateList");
}

if (-e "addList") {
  &doSystemCommand("/bin/rm -f addList");
}

&doSystemCommand("/local/bin/wget ftp://ftp.ncbi.nlm.nih.gov/genomes/refseq/vertebrate_other/Danio_rerio/all_assembly_versions/GCF_000002035.6_GRCz11/GCF_000002035.6_GRCz11_feature_table.txt.gz");
&doSystemCommand("/local/bin/gunzip GCF_000002035.6_GRCz11_feature_table.txt.gz");

open (INFO, "GCF_000002035.6_GRCz11_feature_table.txt") ||  die "Cannot open GCF_000002035.6_GRCz11_feature_table.txt : $!\n";

@lines = <INFO>;

close(INFO);

$ct = 0;
%chrsGeneIDs = ();                 
%startsGeneIDs = ();
%endsGeneIDs = ();
$ctGenes = 0;
foreach $line (@lines) {
  $ct++;
  next if $ct == 1; 
  if ($line) {
    @fields = split(/\t/, $line);
    $feature = $fields[0];
    $assmblUnit = $fields[3];
    $chromosome = $fields[5];
    $start = $fields[7];
    $start =~ s/^\s+//;
    $end = $fields[8];
    $end =~ s/\s+$//; 
    $symbol = $fields[14];
    $ID = $fields[15];
    if ($feature eq "gene" && $assmblUnit eq 'Primary Assembly' && $start && $end && $start ne "" && $end ne "" && $start =~ /^\d+$/ && $end =~ /^\d+$/) {
      $ctGenes++;
      $chrsGeneIDs{$ID} = $chromosome;
      $startsGeneIDs{$ID} = $start;
      $endsGeneIDs{$ID} = $end;
    }
  }
}

print "\nNumber of Genes On NCBI file = $ctGenes\n";

### open a handle on the db
$handle = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
    or die "Cannot connect to database: $DBI::errstr\n";

$sql = "select dblink_linked_recid, dblink_acc_num, sfclg_chromosome, sfclg_start, sfclg_end 
          from db_link, sequence_feature_chromosome_location_generated, foreign_db_contains 
         where dblink_linked_recid = sfclg_data_zdb_id 
           and sfclg_location_source = 'NCBIStartEndLoader' 
           and sfclg_fdb_db_id = 10
           and dblink_fdbcont_zdb_id = fdbcont_zdb_id
           and fdbcont_fdb_db_id = 10;";

$cur = $handle->prepare($sql);

$cur->execute;

$cur->bind_columns(\$zdbGeneID,\$acc,\$chr,\$start,\$end);

%chrsAccs = ();
%startsAccs = ();
%endsAccs = ();
%zdbGeneIDs = ();
$ctExisting = 0;
while ($cur->fetch) {
  if ($start && $end && $start ne "" && $end ne "" && $start =~ /^\d+$/ && $end =~ /^\d+$/) {
    $chrsAccs{$acc} = $chr;
    $startsAccs{$acc} = $start;
    $endsAccs{$acc} = $end;
    $zdbGeneIDs{$acc} = $zdbGeneID;
    $ctExisting++;
  }
}

print "\nNumber of existing records = $ctExisting\n";

$cur->finish();

open (UPDATELIST, ">updateList") || die "Cannot open updateList : $!\n";

foreach $accNum (keys %zdbGeneIDs) {
  if (exists($chrsGeneIDs{$accNum}) && exists($startsGeneIDs{$accNum}) && exists($endsGeneIDs{$accNum}) && ($chrsAccs{$accNum} ne $chrsGeneIDs{$accNum} || $startsAccs{$accNum} != $startsGeneIDs{$accNum} || $endsAccs{$accNum} != $endsGeneIDs{$accNum})) {
     print UPDATELIST "$zdbGeneIDs{$accNum}|$accNum|$chrsGeneIDs{$accNum}|$startsGeneIDs{$accNum}|$endsGeneIDs{$accNum}\n"; 
  }       
}

close UPDATELIST;

$sqlMissingCor = "select dblink_linked_recid, dblink_acc_num 
                    from db_link, foreign_db_contains 
                   where dblink_fdbcont_zdb_id = fdbcont_zdb_id 
                     and fdbcont_fdb_db_id = 10
                     and not exists(select 1 from sequence_feature_chromosome_location_generated 
                                     where sfclg_data_zdb_id = dblink_linked_recid 
                                       and sfclg_location_source = 'NCBIStartEndLoader' 
                                       and sfclg_fdb_db_id = 10);";           
           
$curMissingCor = $handle->prepare($sqlMissingCor);

$curMissingCor->execute;

$curMissingCor->bind_columns(\$zdbID,\$ncbiID);

%zdbIDsMissingCor = ();
$ctMissing = 0;
while ($curMissingCor->fetch) {
  $zdbIDsMissingCor{$ncbiID} = $zdbID;
  $ctMissing++;
}

print "\nNumber of genes missing coordinate records = $ctMissing\n";

$curMissingCor->finish();


$handle->disconnect();

open (ADDLIST, ">addList") || die "Cannot open addList : $!\n";

foreach $zdbID (keys %zdbIDsMissingCor) {
  $ncbiID = $zdbIDsMissingCor{$zdbID};
  if (exists($chrsGeneIDs{$ncbiID}) && exists($startsGeneIDs{$ncbiID}) && exists($endsGeneIDs{$ncbiID})) {
     print ADDLIST "$zdbID|$ncbiID|$chrsGeneIDs{$ncbiID}|$startsGeneIDs{$ncbiID}|$endsGeneIDs{$ncbiID}\n"; 
  }       
}

close ADDLIST;

&doSystemCommand("psql -d <!--|DB_NAME|--> -a -f NCBIStartEnd.sql");

exit;

sub doSystemCommand {

  $systemCommand = $_[0];

  print LOG "$0: Executing [$systemCommand] \n";
    
  $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) { 
     $subjectLine = "Auto from $dbname: " . "NCBIStartEnd.pl :: failed at: $systemCommand . $! ";
     print LOG "\nFailed to execute system command, $systemCommand\nExit.\n\n";
     
     &reportErrAndExit($subjectLine);
  }
}

sub reportErrAndExit {
  $subjectError = $_[0];
  ZFINPerlModules->sendMailWithAttachedReport('<!--|SWISSPROT_EMAIL_ERR|-->',"$subjectError","logNCBIgeneLoad");
  close LOG;
  exit -1;
}


