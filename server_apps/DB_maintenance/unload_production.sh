#! /bin/tcsh

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->
setenv LD_LIBRARY_PATH ${INFORMIXDIR}/lib:${INFORMIXDIR}/lib/esql
setenv PATH <!--|INFORMIX_DIR|-->/bin:/research/zcentral/Commons/bin:$PATH

set pth=/research/zunloads/databases/<!--|DB_NAME|-->
set dirname=`date +"%Y.%m.%d.1"`

# increment until we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end

#send out a warning
#mailx  -s"production unloaded as $dirname" clements@cs.uoregon.edu << END 
#mailx  -s"production unloaded as $dirname" tomc@cs.uoregon.edu << END 
#production is being being unloaded to 
#$pth/$dirname 
#at `date`.
#END

# A while back unloads from chromix to the NFS mounted development RAID
# started taking a really long time, as much as 8 times longer than 
# unloading the DB directly on to chromix disk and then copying it.
# Therefore, to speed up the process from over 4 hours to well under
# an hour, do the unload to chromix disk and then copy it.

if ($HOST == "chromix") then
  /research/zcentral/Commons/bin/unloaddb.pl <!--|DB_NAME|--> /research/zfin/chromix/backup/$dirname
  /bin/cp -pr /research/zfin/chromix/backup/$dirname $pth/$dirname
  /bin/rm -rf /research/zfin/chromix/backup/$dirname
else 
  /research/zcentral/Commons/bin/unloaddb.pl <!--|DB_NAME|--> $pth/$dirname
endif

chgrp -R fishadmin $pth/$dirname
chmod -R g+rw $pth/$dirname
