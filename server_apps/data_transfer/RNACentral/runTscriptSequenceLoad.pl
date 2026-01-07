#!/opt/zfin/bin/perl

use DBI;

chdir "$ENV{'ROOT_PATH'}/server_apps/data_transfer/RNACentral/";

use lib "$ENV{'ROOT_PATH'}/server_apps/perl_lib/";
use ZFINPerlModules;
use Try::Tiny;

## set environment variables

$dbname = "$ENV{'DB_NAME'}";

print "load transcripts that need sequences.\n";
try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $ENV{'DB_NAME'} -a -f preLoadTranscriptSequence.sql");
} catch {
  warn "Failed to execute preLoadTranscriptSequence.sql - $_";
  exit -1;
};
try {
  ZFINPerlModules->doSystemCommand("./loadTscriptSeq.pl");
} catch {
  warn "Failed at loadTscriptSeq.pl - $_";
  exit -1;
};
try {
  ZFINPerlModules->doSystemCommand("psql -v ON_ERROR_STOP=1 -d $ENV{'DB_NAME'} -a -f loadTranscriptSequences.sql");
} catch {
  warn "Failed at loadTranscriptSequences - $_";
  exit -1;
};



exit;



