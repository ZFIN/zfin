#!/opt/zfin/bin/perl -w
#-----------------------------------------------------------------------
# Runs script to create outgoing data file for S-P data exchange.


# define GLOBALS

# set environment variables

$ENV{"DBDATE"}="Y4MD-";

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT";

if (! -e "<!--|FTP_ROOT|-->/pub/transfer/Swiss-Prot") {
    system("/bin/mkdir -m 755 -p <!--|FTP_ROOT|-->/pub/transfer/Swiss-Prot");
}

system("psql -d <!--|DB_NAME|--> -a -f create_file_for_swiss_prot.sql");

system("/bin/chmod 644 <!--|FTP_ROOT|-->/pub/transfer/Swiss-Prot/swiss_prot.txt");

