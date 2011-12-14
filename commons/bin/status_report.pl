#!/private/bin/perl -w
#------------------------------------------------------------------------
# Script that is meant to be run from cron to send out regularly scheduled
# reminder e-mails.  Initially, this is only the "beginning of the month,
# tell Dala your vacation and sick leave e-mail

my $zcs = "technical\@zfin.org";

open MAIL, "|/usr/lib/sendmail -t";

print MAIL "To: dhowe\@zfin.org\n";
print MAIL "Cc: $zcs \n";
print MAIL "Subject: Status report to Doug\n";
print MAIL
    "Please send your weekly status report to Doug\n";
close MAIL;
