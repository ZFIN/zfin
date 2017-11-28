#! /private/bin/perl -w
#
# This monthly-run script queries the statistics on
# ZFIN nomenclature and expression pattern related
# to the ZGC project. The result is mailed to curators.
#
#-----------------------------------------------------------------------

chdir "<!--|ROOT_PATH|-->/server_apps/Reports/Vega";

# Run vega_thisse_report.sql before VegaCount.sql.
# A file is created by vega_thisse_report.sql that is read by VegaCount.sql.

system("../data_transfer/runSqlFiles.groovy Vega/vega_thisse_report_PG.sql");

exit;
