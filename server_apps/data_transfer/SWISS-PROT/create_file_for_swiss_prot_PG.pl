#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create outgoing data file for S-P data exchange.


# define GLOBALS

# set environment variables

$ENV{"DBDATE"}="Y4MD-";

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";

$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";

$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";

$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT";

if (! -e "<!--|FTP_ROOT|-->/pub/transfer/Swiss-Prot") {
    system("/bin/mkdir -m 755 -p <!--|FTP_ROOT|-->/pub/transfer/Swiss-Prot");
}

system("psql -d <!--|DB_NAME|--> -a -f create_file_for_swiss_prot_PG.sql");

system("/bin/chmod 644 <!--|FTP_ROOT|-->/pub/transfer/Swiss-Prot/swiss_prot.txt");

