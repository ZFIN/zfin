#!/opt/zfin/bin/perl

chdir "$ENV{'ROOT_PATH'}/server_apps/data_transfer/RNACentral/";

use DBI;
use lib "$ENV{'ROOT_PATH'}/server_apps/perl_lib/";
use ZFINPerlModules;

## set environment variables

$dbname = "$ENV{'DB_NAME'}";


system("rm -rf $ENV{'TARGETROOT'}/server_apps/data_transfer/RNACentral/loadedSeq.txt");

open LOADEDSEQ, ">$ENV{'TARGETROOT'}/server_apps/data_transfer/RNACentral/loadedSeq.txt" or die "Cannot open loadedseq file : $!\n" ;

open (TSCRIPTFILE, "getSequence") or die "open failed";

while ($line = <TSCRIPTFILE>) {
      chomp $line;
  @fields = split ',', $line;
  $tscriptid = $fields[0];
  $tscriptottdart = $fields[1];


  $string=`xdget -D 0 -n /research/zblastfiles/zmore/blastRegeneration/Current/vega_zfin $tscriptottdart`;
  chomp $string;
  @seqLines = split /\n/, $string;
  $sequence = join('',@seqLines); # Concatenates all elements of the @seqLines array into a single string.
  print LOADEDSEQ "$tscriptid\t$tscriptottdart\t$sequence\n";
  }
close(LOADEDSEQ);
close(TSCRIPTFILE);


