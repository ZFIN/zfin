#!/usr/bin/tcsh

# set Informix environment for PRODUCTION to be used by cron

setenv PATH $PATH':<!--|INFORMIX_DIR|-->/bin/'
setenv INFORMIXDIR <!--|INFORMIX_DIR|-->
setenv INFORMIXSERVER <!--|INFORMIX_SERVER|-->
setenv ONCONFIG <!--|ONCONFIG_FILE|-->
setenv INFORMIXSQLHOSTS ${INFORMIXDIR}/etc/<!--|SQLHOSTS_FILE|-->

# onstat - > /dev/null return codes
# 0 Initalisation
# 1 Quiescent
# 2 Recovery
# 3 Backup
# 4 Shutdown
# 5 Online
# 6 Abort

###############################################################################

# the onstat command is used by DBAs to check online status of the informix
# server.  There are 6 onstat states, (type 'onstat' on the command line to 
# see output) as listed above--this script 
# chops the output, and returns 'On' (short for Online) if the server
# is up and running. 

set modeon=`onstat -| tr -d '\12' | cut -f2- -d\-|cut -c3,4`

# find the ontape process in the process list--grep -c does a count
# of the number of times the string occurs.  The grep -c always counts
# as '1' so if the process is running just once, then the grep -c will return
# '2'

if ($HOST != "embryonix" && $HOST != "zygotix") then 

  set logon=`ps -ef | grep -c "$INFORMIXDIR/bin/ontape"`

# find the process that signifies a backup is taking place

  set backupon=`ps -ef | grep -c "onback.pl"`

  if ($modeon != "On") then
     set mode="`onstat -` | $modeon"

# if the server is in a state other than Online ('On') then we should
# be notified--send an email to informix@cs.uoregon.edu

  echo $mode | /bin/mailx -s "<!--|INFORMIX_SERVER|-->  ABNORMAL!"  <!--|VALIDATION_EMAIL_OTHER|-->

  endif

# check first if the onback.pl script is running.

  if ($backupon < 2) then

# if it is not running, then check if ontape is running.

    if ($logon < 2) then

# if ontape is not in the process list, wait and check again. 

     sleep 6
     set logon=`ps -ef | grep -c "$INFORMIXDIR/bin/ontape"`

# if ontape is still not in the process list, email informix@cs.uoregon.edu

     if ($logon < 2) then
         set logmode="check ontape"
         echo $logmode | /bin/mailx -s "<!--|INFORMIX_SERVER|-->  ABNORMAL!" <!--|VALIDATION_EMAIL_OTHER|-->
     endif

   endif

  endif

endif
