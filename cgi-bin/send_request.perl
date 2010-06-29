#!/private/bin/perl 
# -wT # changed tec 01-18-01
#
# submit_problem.cgi
#
# # submits request to ZFIN admn to register a locus/abbrev/allele.
#
# Modified some code from Jeff Hobbs Jeffrey Hobbs 
# Jan 15 1997

# Script deals with forms that are to be mailed to locusreg. Locusreg is an alias which points to ZFIN members of the nomenclature committee.  It grabs
# the relevant vars that are passed in, and composes into a nice email.
# Required Variables:
# Sender: The name of the person sending the request.
# Email: The email of the person
# ZFIN_id: Person ZFIN id.
# Locus, abbrev, and allele: The name of the locus,abbrev,allele they are dealing with.
# Action: new_locus or new_allele, depending on whether they want to start whole new locus, or just add
#  and allele to existing locus.


$mailprog = '/usr/lib/sendmail -t -oi -oem';

sub print_confirmation {
print <<EOA;
Content-type: text/HTML\n\n
<HTML>
<BODY bgcolor="#FFFFFF">
<TITLE>ZFIN Locus Registration Confirmation</TITLE>
<h1 align=center> Confirmation</h1>
<b> Your request has been emailed to ZFIN nomenclature committee.</b> If you have given a contact email, you should receive a copy of this email.  <p>
The nomenclature committee will review the proposed name.  Assuming there are no problems, you should receive confirmation of registration within the next week or two. If you do not receive a confirmation, please <A HREF="mailto:<!--|LOCUS_REGISTRATION_EMAIL|-->">Contact the ZFIN staff</A>.
<p>
<form>
<input type=button value="Go to ZFIN HOME Page" onClick="window.location.replace('/cgi-bin/webdriver?MIval=aa-ZDB_home.apg')">
</form>
</BODY></HTML>
EOA
}

read(STDIN, $raw_data, $ENV{CONTENT_LENGTH});

$items = split('&', $raw_data);
for ($i = 0; $i < $items; $i++) {
  ($key,$value) = split('=', $_[$i]);
  $value =~ tr/+/ /;
  $value =~ s/%(..)/pack("C", hex($1))/eg;
  $data{"$key"} = $value;
}

$email=$data{email};
if ($email eq 'Unknown') {$email=''};

open(MAIL, "| $mailprog") || die "Content-type: text/plain\n\nCan't open mailprog $mailprog, stopped";
print MAIL <<"STOP";
To: <!--|LOCUS_REGISTRATION_EMAIL|-->
Cc: $email
From: <!--|LOCUS_REGISTRATION_EMAIL|--> 
Subject: Request to Register a new locus or Allele

REQUEST TO REGISTER A NEW LOCUS AND/OR ALLELE.

Name: $data{sender}
Contact Email: $data{email}
ZFIN_ID: $data{ZFIN_id}
Connected From: $ENV{REMOTE_HOST}

Action Requested: $data{action}

Locus: $data{locus}
Abbreviation: $data{abbrev}
Allele: $data{allele}
Description: $data{descrip}

STOP
close(MAIL) || die "pipe exited $?";

&print_confirmation;

exit;














