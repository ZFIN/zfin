#!/private/bin/perl 

##################################################################
#
# Search locus registrations for expired entries. For each expired
# registration send an email notification to the submitter.
#
##################################################################

use DBI;
use MIME::Lite;


# ----------------- Append Message -------------
# Parameters
#   @_ => {locus,abbrev,allele,person}

sub appendNotification (@_) {
  $message = $message."*  @_[0](@_[1])/(@_[2])\n";
}


# ----------------- Begin Email Message -------------
# Parameters
#   @_ => {locus,abbrev,allele,person}

sub beginNotification (@_) {
  $message = "";
  $message = "Hello @_[3],\n\n";
  $message = $message."This is an automated notification to keep you informmed about the status of your locus registration(s) with ZFIN. As you may or may not know, unpublished locus registrations expire after six months and are not publicly viewable on ZFIN. Listed are your locus registration(s) that have been stored as unpublishsed for six months.\n\n";
  $message = $message."Has a locus on this list been published?\n";
  $message = $message."*  If so, would you please send us the citation?\n"; 
  $message = $message."*  If not, would you like a 6-month extension for your registration?\n"; 
  $message = $message."(Please include a note telling us its status and when you expect to publish.)\n\n";

  $message = $message."Expired Locus Registration(s):\n";
  
  appendNotification(@_);
}


# ----------------- Content of Email Message -------------
# Parameters
#  none

sub messageContent () {

  $message = $message."\n";
  $message = $message."Thanks for your help.\n";
  $message = $message."Sherry Giglia\n";
  $message = $message."ZF Administrative Coordinator\n";
  $message = $message."ZFIN\n";
  $message = $message."5291 University of Oregon\n";
  $message = $message."Eugene, OR 97403-5291 USA\n";
  $message = $message."(541) 346-4979 / FAX (541) 346-0322\n";
  $message = $message."e-mail: zfinadmn\@zfin.org\n";

  return $message;
}


# ----------------- Send Email Notification -------------
# Parameters
#   @_ => {person,email}

sub sendNotification (@_) {
  
  my $SUBJECT="Your ZFIN Locus Registration";
  my $MAILTO="@_[4]";
  my $TXTFILE="report.txt";
 
  # Create a new multipart message:
  $msg1 = MIME::Lite->new(
    From    => "Sherry Giglia <giglias@uoneuro.uoregon.edu>",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'TEXT',
    Data    => messageContent())
  or die "Error creating MIME body: $!\n";

 
  $msg1->send;
}

sub emailError($)
  {
    &writeReport($_[0]);
    &sendReport("bsprunge\@cs.uoregon.edu");
    exit;
  }

sub writeReport($)
  {
    open (REPORT, ">>report.txt") or die "cannot open report.txt";
    print REPORT "$_[0] \n\n";
    close (REPORT);
  }

sub openReport()
  {
    system("/bin/rm -f report.txt");
    system("touch report.txt");
  }


# --------------- Last Name Last -------------
# Parameters
#   @_ => person full name - last name, first ? middle

sub sirNameLast($)
  {
    ($lName,$fName) = split (/,/,$_[0],2);
  
    return "$fName $lName";
  }


# --------------- fileToString -------------
# Parameters
#   $ => filename including path

sub fileToString ($) {
  open (FILE, "$_[0]") or die "failed to open $_[0]";
  
  while ($line = <FILE>) {
    $text.= $line;
  }
  close (FILE);

  return $text
}


#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

### open a handle on the db
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
                       '', 
                       '', 
		       {AutoCommit => 1,RaiseError => 1}
		      )
  || emailError("Failed while connecting to <!--|DB_NAME|--> "); 

chdir "<!--|ROOT_PATH|-->/<!--|CGI_BIN_DIR_NAME|-->/";

openReport();

expiredLocusReg();

sendReport("giglias\@uoneuro.uoregon.edu");

exit;


sub expiredLocusReg()
  {
    open (REPORT, ">>report.txt") or die "can not open report.txt";

    print REPORT "Expired Locus Registration\n\n";
    print REPORT "The following contacts have been notified that the associated locus registration has expired.\n\n";

    my $cur = $dbh->prepare('select lr.locus_name, 
                                    lr.abbrev, 
                                    lr.allele, 
                                    p.full_name, 
                                    p.email
                             from   locus_registration lr, 
                                    person p
                             where  lr.locusreg_expiration_deadline < TODAY
                               and  lr.owner = p.zdb_id
                          order by  p.full_name, lr.locus_name, lr.abbrev, lr.allele;'
			   );
    $cur->execute;
    my($locus, $abbrev, $allele, $person, $email);
    $cur->bind_columns(\$locus,\$abbrev,\$allele,\$person,\$email);
    $prev_person = "first row";
    while ($cur->fetch)
    {
      if( $prev_person eq $person)
	{
	  print REPORT "\t- $locus($abbrev)/($allele)\n";
          appendNotification($locus, $abbrev, $allele, sirNameLast($person), $email);
	}
      elsif ($prev_person eq "first row")
	{
	  print REPORT "$person <$email> - $locus($abbrev)/($allele)\n";
	  beginNotification($locus, $abbrev, $allele, sirNameLast($person), $email);
	}
      else
	{
	  print REPORT "\n$person <$email> - $locus($abbrev)/($allele)\n";
	  sendNotification(sirNameLast($prev_person), $prev_email);
	  beginNotification($locus, $abbrev, $allele, sirNameLast($person), $email);
	}
      $prev_person = $person;
      $prev_locus = $locus;
      $prev_abbrev = $abbrev;
      $prev_allele = $allele;
      $prev_email = $email;
    }
    print REPORT "\n";
    close(REPORT);
  }
  
  
  
  sub sendReport($)
    {
    
      my $SUBJECT="Expired Locus Registration";
      my $MAILTO=$_[0];
      my $data=fileToString("report.txt");

      # Create a new multipart message:
      $msg2 = MIME::Lite->new(
        From     => "<!--|SWISSPROT_EMAIL_ERR|-->",
        To       => "$MAILTO",
        Subject  => "$SUBJECT",
        Type     => 'text/plain',
        Encoding => 'base64',
        Data     => $data
        )
      or die "Error creating MIME body: $!\n";
      

      $msg2->send;
    
      $dbh->disconnect();
  }
