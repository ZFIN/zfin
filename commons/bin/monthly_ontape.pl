#!/private/bin/perl -w
#------------------------------------------------------------------------
# Script that is meant to be run from cron to send out regularly scheduled
# reminder e-mails.  This is sent on the 20th of the month to remind informix 
# user to restart ontape.


my $zifx = "informix\@cs.uoregon.edu";

open MAIL, "|/usr/lib/sendmail -t";
#open MAIL, ">mailfile.txt";

print MAIL "To: staylor\@cs.uoregon.edu\n";
print MAIL "Cc: $zifx\n";
print MAIL "Subject: Auto: restart ontape\n";
print MAIL 
    "Its time to restart ontape so that logs do not fill up /research/zprod.\n";
print MAIL 
    "as informix on helix:
       % srchelix 
 
     find the process id of ontape
       % ps -ef | grep ontape 
     
     issue a kill command for the ontape process 
       % kill [found_process_id_from_above]
       % cdhelix 
       % cd server_apps/DB_maintenance/ 
       % gmake dumpLogsContinuous 
      
    dump the informix server 
       % cd /research/zprod/www_homes/zfin.org/server_apps/DB_maintenance/
       % ./dumpServer.pl 

    If this routine completes successfully, delete the old log file with
    last month's datetime stamp.

       % cd /research/zprod/www_homes/zfin.org/server_apps/DB_maintenance/
   
    Confirm that the logs symlink is pointing to a logfile with 
    today's date.
       % ls -al logs
      
    Confirm that the data symlink is pointing to a data file with today's
    date.  Will be created with the dumpServer.pl script
       % ls -al data

    Remove the old log file that the logs symlink is NOT pointing to.

       % rm log[the_date_to_delete.0]

    Else, wait untill tomorrow, and make sure we have a dump from tonight's
    cron job and delete the historical log file with last month's
    datetime stamp then.";

close MAIL;
