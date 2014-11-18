/local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/data_transfer/Ensembl/ensdargs_on_more_than_one_gene.txt -s "genes that might need merging, from: <!--|DB_NAME|-->" -- <!--|DEFAULT_EMAIL|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char;

/local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/data_transfer/Ensembl/changedSangerEnsdargs.txt -s "links that need to be updated from SangerMutantLoad, from: <!--|DB_NAME|-->" -- <!--|DEFAULT_EMAIL|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char;


#/local/bin/mutt -a <!--|ROOT_PATH|-->/server_apps/data_transfer/Ensembl/ -s "genes that might need merging, from: <!--|DB_NAME|-->" -- <!--|DEFAULT_EMAIL|--> < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char;