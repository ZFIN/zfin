#! /private/bin/perl -w
#
# This monthly-run script queries the statistics on
# ZFIN nomenclature and expression pattern related
# to the ZGC project. The result is mailed to curators.
#
use strict;

# set environment variables



chdir "<!--|ROOT_PATH|-->/server_apps/Reports/ZGC";

system("psql -d <!--|DB_NAME|--> -f zgcCount_PG.sql > zgcStatistics.txt 2> err.txt");
exit;
