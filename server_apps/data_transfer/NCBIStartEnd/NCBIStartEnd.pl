#!/opt/zfin/bin/perl

# NCBIStartEnd.pl
# parses GCF_000002035.6_GRCz11_feature_table.txt(.gz), supplied by upload or local path (see below)
# prepare the list to update related sequence_feature_chromosome_location_generated records 

use DBI;
use Try::Tiny;

$dbname = "<!--|DB_NAME|-->";
$instance = "<!--|INSTANCE|-->";
$username = "";
$password = "";

if (-e "updateList") {
  &doSystemCommand("/bin/rm -f updateList");
}

if (-e "addList") {
  &doSystemCommand("/bin/rm -f addList");
}

# The feature table is no longer downloaded (NCBI removed it from the FTP site).
# It is supplied either as a Jenkins file upload (FEATURE_TABLE_UPLOAD) or as a
# local path (FEATURE_TABLE_PATH). An upload wins if both are given.
# The file may be gzipped or uncompressed.
$defaultPath = "/research/zarchive/load_files/Load-NCBIStartEndPositions/GCF_000002035.6_GRCz11_feature_table.txt.gz";
$inputFile = $defaultPath;
if ($ENV{FEATURE_TABLE_PATH}) {
  $inputFile = $ENV{FEATURE_TABLE_PATH};
}
if ($ENV{FEATURE_TABLE_UPLOAD} && $ENV{WORKSPACE}) {
  $inputFile = "$ENV{WORKSPACE}/FEATURE_TABLE_UPLOAD";
}

die "Feature table file not found: $inputFile\n" unless -e $inputFile;

print "Reading feature table from $inputFile\n";
if ($inputFile =~ /\.gz$/ || (-B $inputFile)) {
  open (INFO, "-|", "/bin/gunzip", "-c", $inputFile) || die "Cannot gunzip $inputFile : $!\n";
} else {
  open (INFO, "<", $inputFile) || die "Cannot open $inputFile : $!\n";
}

@lines = <INFO>;

close(INFO);

die "No lines read from $inputFile\n" unless @lines;

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
      print "ID: $ID $symbol";
    if ($feature eq "gene" && $assmblUnit eq 'Primary Assembly' && $start && $end && $start ne "" && $end ne "" && $start =~ /^\d+$/ && $end =~ /^\d+$/) {
      $ctGenes++;
      print "ID: $ID $symbol";
      $chrsGeneIDs{$ID} = $chromosome;
      $startsGeneIDs{$ID} = $start;
      $endsGeneIDs{$ID} = $end;
    }
  }
}

print "\nNumber of Genes On NCBI file = $ctGenes\n";

### open a handle on the db
my $dbhost = "<!--|PGHOST|-->";
$handle = DBI->connect ("DBI:Pg:dbname=$dbname;host=$dbhost", $username, $password)
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
  if (exists($chrsGeneIDs{$zdbID}) && exists($startsGeneIDs{$zdbID}) && exists($endsGeneIDs{$zdbID})) {
     print ADDLIST "$ncbiID|$zdbID|$chrsGeneIDs{$zdbID}|$startsGeneIDs{$zdbID}|$endsGeneIDs{$zdbID}\n";
  }
}

close ADDLIST;

try {
  &doSystemCommand("psql -v ON_ERROR_STOP=1 -d <!--|DB_NAME|--> -a -f NCBIStartEnd.sql");
} catch {
  die "Failed to execute NCBIStartEnd.sql - $_";
};

exit;

sub doSystemCommand {

  $systemCommand = $_[0];

  print "$0: Executing [$systemCommand] \n";

  $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) {
     die "NCBIStartEnd.pl :: failed at: $systemCommand . $!\n";
  }
}
