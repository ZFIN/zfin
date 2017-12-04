#! /private/bin/perl -w
#
# This monthly-run script queries the statistics on
# ZFIN nomenclature and expression pattern related
# to the ZGC project. The result is mailed to curators.
#
#-----------------------------------------------------------------------

chdir "<!--|TARGETROOT|-->/server_apps/Reports/Vega";

# Run vega_thisse_report.sql before VegaCount.sql.
# A file is created by vega_thisse_report.sql that is read by VegaCount.sql.

system("<!--|TARGETROOT|-->/server_apps/data_transfer/runSqlFiles.groovy vega_thisse_report_PG.sql");

exit;
