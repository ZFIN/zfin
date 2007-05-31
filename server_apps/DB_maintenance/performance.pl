#! /private/bin/perl -w 


##
# pnas_report.pl
# script runs once a month to generate a list of pubs
# from Proc. Nat'l Academ. of Sciences for Dave F. (or the current
# figure-permission curator. 
# Curator requested that the list is non-duplicative, so this 
# script also calls updatePNAS.sql which records the date this 
# script was executed and which figures it pulled out as needing
# permission.
##

use DBI;
use MIME::Lite;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

$mailprog = '/usr/lib/sendmail -t -oi -oem';

# subroutines to send email reports to curators.
sub sendReport()
  {
    open(MAIL, "| $mailprog") 
	|| die "cannot open mailprog $mailprog, stopped";
 
   open(REPORT, "reportPNAS.unl") 
	|| die "cannot open reportPermissons";


    print MAIL "Subject: performance stats\n";

    while($line = <REPORT>)
    {
	print MAIL $line;
    }
    close (REPORT);

    close (MAIL);
  }



sub openReport()
  {
    system("/bin/touch performance_sar.txt");
  }

## -------  MAIN -------- ##

# move into the appropriate directory

chdir "<!--|ROOT_PATH|-->/server_apps/DB_maintenance/";

$mailprog = '/usr/lib/sendmail -t -oi -oem';

&openReport();

system("/bin/sar -d 5 5 >> performance_sar.txt");

