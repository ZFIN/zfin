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
set modeon=`onstat -| tr -d '\12' | cut -f2- -d\-|cut -c3,4`
#set logon=`ps -ef | grep [0-9] /private/apps/Informix/informix_wildtype/bin/ontape -c | cut -f5 -d' '`

if ($modeon != "On") then
    set mode="`onstat -` | $modeon"
    echo $mode | /local/bin/mail -s "<!--|INFORMIX_SERVER|-->  ABNORMAL!" tomc@cs.uoregon.edu
    echo $mode | /local/bin/mail -s "<!--|INFORMIX_SERVER|-->  ABNORMAL!" staylor@cs.uoregon.edu
#elif ($logon != "roots") then
#    if ($logon != "informix") then
#     set logmode="check ontape"
#     echo $logmode | /local/bin/mail -s "<!--|INFORMIX_SERVER|-->  ABNORMAL!" tomc@cs.uoregon.edu
#     echo $logmode | /local/bin/mail -s "<!--|INFORMIX_SERVER|-->  ABNORMAL!" staylor@cs.uoregon.edu
#    endif
#    echo $mode | /local/bin/mail -s "<!--|INFORMIX_SERVER|-->  ABNORMAL!" judys@cs.uoregon.edu
#    echo $mode | /local/bin/mail -s "<!--|INFORMIX_SERVER|-->  ABNORMAL!" clements@cs.uoregon.edu	
#    cd /research/zfin/users/bionixprod/ZFIN_WWW/;onmode -ky;oninit;echo ""|/private/bin/onlog.pl	
endif 

