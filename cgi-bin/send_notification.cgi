#!/private/bin/perl 
# 
#  Process data of a notification letter submitted from publication 
#  curation page, and send out properly formatted letter. 
#  
# Parameter:
#    STDIN form data: contacts, sender_email, letter
#


sub print_confirmation {
    print <<EOA;
Content-type: text/HTML\n\n
<HTML>
<p>
The Notification has been sent.
<p>
<input type=button value="Close Window" onClick="window.close()">
</form>
EOA
}

read(STDIN, $raw_data, $ENV{CONTENT_LENGTH});

@items = split(/&/, $raw_data);
foreach $item (@items) {
  ($key,$value) = split(/=/, $item);
  $value =~ tr/+/ /;
  $value =~ s/%(..)/pack("C", hex($1))/eg;
  $data{"$key"} = $value;
}

$mailprog = '/usr/lib/sendmail -t -oi -oem';
open(MAIL, "| $mailprog") || die "Content-type: text/plain\n\nCan't open mailprog $mailprog, stopped";
print MAIL<<STOP;
From: $data{sender_email}
To: $data{contacts}
Cc: $data{sender_email}
Subject: $data{ltitle}
STOP

#To be able to support italic and bold, we decided to use HTML format rather than plain text format. 
#Since it is sent over in HTML format, there isn't really much to be done here. 
print MAIL "Content-type: text/HTML\n\n";    

foreach $line (split(/\n/, $data{letter})) {

    # trim off leading and tailing space 
    $line =~ s/\s*(\S.*\S)\s*/$1/;

    # skip blank lines
    if ($line =~ /^\s*$/) {
	next;
    }
    # Restore coded single quote. 
    $line =~ s/%27/'/g;

    print MAIL "$line";
    print MAIL "\n";
}

close(MAIL) || die "pipe exited $?";

&print_confirmation;
exit;
