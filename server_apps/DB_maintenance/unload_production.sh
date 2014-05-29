#!/bin/tcsh

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->
setenv LD_LIBRARY_PATH ${INFORMIXDIR}/lib:${INFORMIXDIR}/lib/esql
setenv PATH <!--|INFORMIX_DIR|-->/bin:/private/ZfinLinks/Commons/bin:$PATH
setenv INSTANCE 
set pth=/research/zunloads/databases/<!--|DB_NAME|-->
set pthLinux=/research/zunloads/databases/<!--|DB_NAME|-->/linux
set dirname=`date +"%Y.%m.%d.1"`

# increment until we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end

# A while back unloads from production to the NFS mounted development RAID
# started taking a really long time, as much as 8 times longer than 
# unloading the DB directly on to production disk and then copying it.
# Therefore, to speed up the process from over 4 hours to well under
# an hour, do the unload to production disk and then copy it.

# moved into Jenkins job: Time-Stamp-Database-Info_d
# This script should eventually go fully into the
# Jenkins Job.
#/private/bin/ant -f  <!--|ROOT_PATH|-->/server_apps/DB_maintenance/build.xml

if ($HOST != "zygotix") then
  /private/ZfinLinks/Commons/bin/unloaddb.pl <!--|DB_NAME|--> <!--|ROOT_PATH|-->/server_apps/DB_maintenance/$dirname
  #  $? is the return value of the last-executed command.  
  #  When it works correctly, unloaddb.pl returns 0. 
  if ($? != "0") then
    /bin/rm -rf $pth/$dirname
  else
    /bin/cp -pr <!--|ROOT_PATH|-->/server_apps/DB_maintenance/$dirname $pth/$dirname
    /bin/cp -pr <!--|ROOT_PATH|-->/server_apps/DB_maintenance/$dirname $pthLinux/$dirname
    # sed -i would be easier but does not work on solaris.
    /bin/sed 's/"bob"/"informix"/g' $pthLinux/$dirname/schemaFile.sql > $pthLinux/$dirname/schemaTempFile.sql
    /bin/rm $pthLinux/$dirname/schemaFile.sql
    /bin/mv $pthLinux/$dirname/schemaTempFile.sql $pthLinux/$dirname/schemaFile.sql
    /bin/sed 's@/research/zprod/www_homes/zfin.org/lib/DB_functions/@/private/lib/c_functions/@g' $pthLinux/$dirname/schemaFile.sql > $pthLinux/$dirname/schemaTempFile.sql
    /bin/rm $pthLinux/$dirname/schemaFile.sql
    /bin/mv $pthLinux/$dirname/schemaTempFile.sql $pthLinux/$dirname/schemaFile.sql
    # Same fix as above, but using the new paths [KLS]
    /bin/sed 's@/opt/zfin/www_homes/[a-z]*/lib/DB_functions/@/private/lib/c_functions/@g' $pthLinux/$dirname/schemaFile.sql > $pthLinux/$dirname/schemaTempFile.sql
    /bin/rm $pthLinux/$dirname/schemaFile.sql
    /bin/mv $pthLinux/$dirname/schemaTempFile.sql $pthLinux/$dirname/schemaFile.sql
    /bin/rm -rf <!--|ROOT_PATH|-->/server_apps/DB_maintenance/$dirname
    chgrp -R fishadmin $pth/$dirname
    chgrp -R fishadmin $pthLinux/$dirname
    chmod -R g+rw $pth/$dirname
    chmod -R g+rw $pthLinux/$dirname
  endif
else 
  /private/ZfinLinks/Commons/bin/unloaddb.pl <!--|DB_NAME|--> $pth/$dirname
  if ($? != "0") then
    /bin/rm -rf $pth/$dirname
  else 
    chgrp -R fishadmin $pth/$dirname
    chmod -R g+rw $pth/$dirname
    /bin/cp -pr $pth/$dirname $pthLinux/$dirname
    /bin/sed 's/"bob"/"informix"/g' $pthLinux/$dirname/schemaFile.sql > $pthLinux/$dirname/schemaTempFile.sql   
    /bin/rm $pthLinux/$dirname/schemaFile.sql
    /bin/mv $pthLinux/$dirname/schemaTempFile.sql $pthLinux/$dirname/schemaFile.sql
    /bin/sed 's@/research/zcentral/www_homes/<!--|INSTANCE|-->/lib/DB_functions/@/private/lib/c_functions/@g' $pthLinux/$dirname/schemaFile.sql > $pthLinux/$dirname/schemaTempFile.sql
    /bin/rm $pthLinux/$dirname/schemaFile.sql
    /bin/mv $pthLinux/$dirname/schemaTempFile.sql $pthLinux/$dirname/schemaFile.sql

    chgrp -R fishadmin $pth/$dirname
    chgrp -R fishadmin $pthLinux/$dirname
    chmod -R g+rw $pth/$dirname
    chmod -R g+rw $pthLinux/$dirname


    echo "unloaddb.pl completed successfully."
  endif
endif
