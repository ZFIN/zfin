#! /bin/tcsh

setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->
setenv LD_LIBRARY_PATH ${INFORMIXDIR}/lib:${INFORMIXDIR}/lib/esql
setenv PATH <!--|INFORMIX_DIR|-->/bin:/research/zfin/central/Commons/bin:$PATH

set pth=/research/zfin/central/data/unloads/<!--|MACHINE_NAME|-->
set dirname=`date +"%Y.%m.%d.1"`

# increment untill we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end

#send out a warning
mailx  -s"production unloaded as $dirname" clements@cs.uoregon.edu << END 
#mailx  -s"production unloaded as $dirname" tomc@cs.uoregon.edu << END 
production is being being unloaded to 
$pth/$dirname 
at `date`.
END

/research/zfin/central/Commons/bin/unloaddb.pl <!--|DB_NAME|--> $pth/$dirname

chgrp -R fishadmin $pth/$dirname
chmod -R g+rw $pth/$dirname
