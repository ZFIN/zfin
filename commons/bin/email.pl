#!/private/bin/perl -w
#------------------------------------------------------------------------
# Script that is meant to be run from cron to send out regularly scheduled
# reminder e-mails.  Initially, this is only the "beginning of the month,
# tell Dala your vacation and sick leave e-mail

my $zcs = "technical\@zfin.org";
my $zcur = "curators\@zfin.org";
my $zadmin = "knight\@uoneuro.uoregon.edu ";

open MAIL, "|/usr/lib/sendmail -t";

print MAIL "To: dhowe\@zfin.org\n";
print MAIL "Cc: $zcs $zcur $zadmin\n";
print MAIL "Subject: Send Monthly Leave or Time sheets to Manager\n";
print MAIL
    "Please prepare your monthly time sheet or vacation and sick leave balance sheet and give to your manager\n". "This should be done even if you took 0 hours leave. \n";
close MAIL;
