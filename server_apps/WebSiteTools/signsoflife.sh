#!/usr/bin/tcsh
# set Informix environment for PRODUCTION to be used by cron

setenv PATH $PATH':<!--|INFORMIX_DIR|-->/bin/'
setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->
# define Makefile environment variables.

# onstat - > /dev/null return codes
# 0 Initalisation
# 1 Quiescent
# 2 Recovery
# 3 Backup
# 4 Shutdown
# 5 Online
# 6 Abort

############################################################################################################
set modeon=`onstat -|cut -f2- -d\-|cut -c3,4`

if ($modeon != "On") then
    set mode="`onstat -`|$modeon"
    echo $mode | /local/bin/mail -s "ZFIN ABNORMAL!" tomc@cs.uoregon.edu
    echo $mode | /local/bin/mail -s "ZFIN ABNORMAL!" judys@cs.uoregon.edu
    echo $mode | /local/bin/mail -s "ZFIN ABNORMAL!" clements@cs.uoregon.edu	
#    cd /research/zfin/users/bionixprod/ZFIN_WWW/;onmode -ky;oninit;echo ""|/private/bin/onlog.pl	
endif 

############################################################################################################
set ifx_ps="`ps -u informix -o args`"

if("`ps -u informix -o args|cut -f2- -d ' '|grep ^Server\ <!--|DB_NAME|-->\ <!--|JAVA_SERVER_PORT|-->`" != "Server <!--|DB_NAME|--> <!--|JAVA_SERVER_PORT|-->") then
	<!--|ROOT_PATH|-->/server_apps/javaserver stop >& /dev/null
	<!--|ROOT_PATH|-->/server_apps/javaserver start >& /dev/null

	echo $ifx_ps | /local/bin/mail -s "javaserver nonexistant on $HOST" tomc@cs.uoregon.edu 
	echo $ifx_ps | /local/bin/mail -s "javaserver nonexistant on $HOST" judys@cs.uoregon.edu

	#tail <!--|ROOT_PATH|-->/server_apps/javaserver/Server.out |\
endif
