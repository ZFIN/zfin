#! /bin/tcsh

#$1 db name
setenv INFORMIXSERVER waldo
setenv INFROMIXDIR /private/apps/Informix/informix/
/private/apps/Informix/informix/bin/dbaccess $1 /research/zunloads/projects/GO/schema.sql;

exit
