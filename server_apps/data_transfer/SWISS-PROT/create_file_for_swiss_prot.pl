#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create data file for S-P data exchange.


# define GLOBALS

# set environment variables

$ENV{"DBDATE"}="Y4MD-";

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";

$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";

$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";

$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess <!--|DB_NAME|--> create_file_for_swiss_prot.sql");


