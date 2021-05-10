#!/opt/zfin/bin/perl

use DBI;

use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;
use Try::Tiny;

## set environment variables

$dbname = "<!--|DB_NAME|-->";

print "load transcripts that need sequences.\n";
try {
  ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f preLoadTranscriptSequence.sql");
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
  ZFINPerlModules->doSystemCommand("psql -d <!--|DB_NAME|--> -a -f loadTranscriptSequences.sql");
} catch {
  warn "Failed at loadTranscriptSequences - $_";
  exit -1;
};



exit;



