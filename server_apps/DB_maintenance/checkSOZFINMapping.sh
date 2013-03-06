#!/bin/tcsh

# rm old reports

setenv INSTANCE <!--|INSTANCE|-->;

if ( -e /tmp/mismapSOZFIN.txt ) then
 /bin/rm /tmp/mismapSOZFIN.txt;
 /bin/touch /tmp/mismapSOZFIN.txt;

endif

echo "check if term_name matches szm_term_name. on <!--|DB_NAME|-->";

echo 'unload to /tmp/mismapSOZFIN.txt select * from so_zfin_mapping where not exists (Select "x" from term where term_ont_id = szm_term_ont_id and term_name != "szm_term_name");' | /private/apps/Informix/informix/bin/dbaccess <!--|DB_NAME|--> ;

if ( -s /tmp/mismapSOZFIN.txt ) then

    /local/bin/mutt -a /tmp/mismapSOZFIN.txt -s "so term updates created a mismatch in so_zfin_mapping table on <!--|DB_NAME|-->" -- <!--|DB_OWNER|-->@cs.uoregon.edu < <!--|ROOT_PATH|-->/server_apps/DB_maintenance/warehouse/char ; 

endif


exit
