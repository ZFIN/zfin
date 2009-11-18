#!/private/bin/perl

# FILE: validateUniprotIDsZFIN.pl

# DESCRIPTION: This script is run weekly before go.pl is run, to validate all UniProt IDs at ZFIN 
#              against those IDs in AC field of uniprot_sprot.dat and uniprot_trembl.dat. 
#              The validation result is sent to GO curator. 
# EFFECT: Up to 3 emails with the result are sent to GO curator. First, there is a summary report email, 
#         with numbers of total primary and secondary UniProt IDs from EBI's data files, and those numbers
#         at ZFIN and the number of invalid IDs found. Then, if any secondary and invalid IDs are found, 
#         a second and third emails are sent out as well to list them (for secondary IDs, the corresponding 
#         primary IDs are listed, too). I expect only a small of number of the incorrect IDs (secondary and 
#         invalid), if any, will be found weekly.      

use MIME::Lite;


# ----------------- Send Error Report -------------
# Parameter
#   $    Error message 
sub sendErrorReport ($) {
    open (SENDMAIL, "| /usr/lib/sendmail -t -oi") || die "Cannot open mailprog!";
    print SENDMAIL "To: <!--|GO_EMAIL_ERR|-->\n";
    print SENDMAIL "Subject: UniProt IDs validation error\n";

    print SENDMAIL "$_[0]\n"; 
    close(SENDMAIL);
    exit;
}

#------------------ Send Validation Summary ----------------
# No parameter
#
sub sendValidationSummary {
		
 #----- One mail send out the validation summary report----

  my $SUBJECT="Auto: summary report of validating Uniprot IDs at ZFIN";
  my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";
  my $TXTFILE="./validationSummaryReport.txt";
 
  # Create a new multipart message:
  my $msg2 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg2 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg2->print(\*SENDMAIL);

  close(SENDMAIL);
}

#------------------ Send secondary IDs found and their corresponding primary IDs  ----------------
# No parameter
#
sub sendSecondaryIDs {
	
 #----- One mail send out the secondary UniProt IDs found at ZFIN, together with their corresponding primary IDs ----

  my $SUBJECT="Auto: secondary UniProt IDs found at ZFIN and the corresponding primary IDs";
  my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";
  my $TXTFILE="./validSecondaryIDs";
 
  # Create a new multipart message:
  my $msg3 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg3 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg3->print(\*SENDMAIL);

  close(SENDMAIL);
}


#------------------ Send invalid IDs found ----------------
# No parameter
#
sub sendInvalidIDs {
	
 #----- One mail send out the invalid UniProt IDs found at ZFIN ----

  my $SUBJECT="Auto: invalid UniProt IDs found at ZFIN";
  my $MAILTO="<!--|GO_EMAIL_CURATOR|-->";
  my $TXTFILE="./invalidIDs";
 
  # Create a new multipart message:
  my $msg4 = new MIME::Lite 
    From    => "$ENV{LOGNAME}",
    To      => "$MAILTO",
    Subject => "$SUBJECT",
    Type    => 'multipart/mixed';
 
  attach $msg4 
   Type     => 'text/plain',   
   Path     => "$TXTFILE";

  # Output the message to sendmail

  open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
  $msg4->print(\*SENDMAIL);

  close(SENDMAIL);
}


#--------------- Main --------------------------------

print "\nRunning validation script ...\n\n";

# set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";
$ENV{"DATABASE"}="<!--|DB_NAME|-->";

# remove old files 
system("/bin/rm -f *.plain") and die "can not rm old plain data file";
system("/bin/rm -f *.unl") and die "can not rm old unl data file";

sendErrorReport ("unloadZFINuniprotIDs.sql failed") if 
  system( "$ENV{'INFORMIXDIR'}/bin/dbaccess zfindb unloadZFINuniprotIDs.sql" );

$url = "ftp://ftp.ebi.ac.uk/pub/databases/uniprot/knowledgebase/uniprot_sprot.dat.gz";
print "\nDownloading uniprot_sprot.dat.gz ...\n\n";
sendErrorReport ("downloading uniprot_sprot.dat.gz failed") if 
  system("/local/bin/wget -q $url -O uniprot_sprot.plain.gz");
sendErrorReport ("unzipping uniprot_sprot.dat.gz failed") if 
  system("/local/bin/gunzip uniprot_sprot.plain.gz");

$url = "ftp://ftp.ebi.ac.uk/pub/databases/uniprot/knowledgebase/uniprot_trembl.dat.gz";
print "\nDownloading uniprot_trembl.dat.gz ...\n\n";
sendErrorReport ("downloading uniprot_trembl.dat.gz failed") if 
  system("/local/bin/wget -q $url -O uniprot_trembl.plain.gz");
sendErrorReport ("unzipping uniprot_trembl.dat.gz failed") if 
  system("/local/bin/gunzip uniprot_trembl.plain.gz");

print "\nSuccessfully downloaded and decompressed \n\n";

# open the text file containing valid and secondary UniProt IDs
open (UNIPROT, "uniprot_sprot.plain") || die "Can't open uniprot_sprot.plain : $!\n";

print "\nProcessing ...  \n\n";

%uniprotPrimaryIDs = (); 
%uniprotSecondaryIDs = (); 
$ct1 = $ct2 = 0;
while (<UNIPROT>) {
  $line = $_;
  if ($line && $line =~ /^AC/) {
    $ct1++;
    @fields = split(/AC\s+/, $line);
    $ids = $fields[1];
    @categoriezedIDs = split(/;\s+/, $ids);
    $primary = $categoriezedIDs[0];
    $uniprotPrimaryIDs{$primary} = 1;
    for ($i = 1; $i <= $#categoriezedIDs; $i++) {
      $ct2++;
      $uniprotSecondaryIDs{$categoriezedIDs[$i]} = $primary;
    }
  }
}

close(UNIPROT);

# open another text file containing valid and secondary UniProt IDs
open (UNIPROT2, "uniprot_trembl.plain") || die "Can't open uniprot_trembl.plain : $!\n";

print "\nProcessing ...  \n\n";

while (<UNIPROT2>) {
  $line = $_;
  if ($line && $line =~ /^AC/) {
    $ct1++;
    @fields = split(/AC\s+/, $line);
    $ids = $fields[1];
    @categoriezedIDs = split(/;\s+/, $ids);
    $primary = $categoriezedIDs[0];
    $uniprotPrimaryIDs{$primary} = 1;
    for ($i = 1; $i <= $#categoriezedIDs; $i++) {
      $ct2++;
      $uniprotSecondaryIDs{$categoriezedIDs[$i]} = $primary;
    }
  }
}

close(UNIPROT2);

open (SUM, ">validationSummaryReport.txt") || die "Cannot open validationSummaryReport.txt : $!\n";  

print "Total number of primary UniProt IDs in the two updated external files, uniprot_sprot.dat and uniprot_trembl.dat: $ct1\n";
print "Total number of secondary UniProt IDs in the two updated external files, uniprot_sprot.dat and uniprot_trembl.dat: $ct2\n";

print SUM "Total number of primary UniProt IDs in the two updated external files, uniprot_sprot.dat and uniprot_trembl.dat: $ct1\n";
print SUM "Total number of secondary UniProt IDs in the two updated external files, uniprot_sprot.dat and uniprot_trembl.dat: $ct2\n";

open (INP, "allZFINuniprotIDs.unl") || die "Can't open allZFINuniprotIDs.unl : $!\n";
@lines=<INP>;
close(INP);

# get all UniProt IDs at ZFIN
%zfinUniProtIDs = ();
$ctZfinUniProtIDs = 0;
foreach $line (@lines) {
  $ctZfinUniProtIDs++;
  @fields = split(/\|/, $line);
  $zfinUniProtIDs{$fields[0]} = 1;
}

print "Total number of UniProt IDs at ZFIN: $ctZfinUniProtIDs\n";
print SUM "Total number of UniProt IDs at ZFIN: $ctZfinUniProtIDs\n";

open (OUTPUT, ">ZFINuniprotIDs") || die "Cannot open ZFINuniprotIDs : $!\n";

%validPrimaryIDs = %validSecondaryIDs = %invalidIDs = ();
$ctValidPrimaryIDs = $ctValidSecondaryIDs = $ctInvalidUniProIDs = 0;

# validation process
foreach $zfinUniProtID (keys %zfinUniProtIDs) {
  print OUTPUT "$zfinUniProtID\n";
  if (exists($uniprotPrimaryIDs{$zfinUniProtID})) {
    $ctValidPrimaryIDs++;
    $validPrimaryIDs{$zfinUniProtID} = 1;
  } elsif (exists($uniprotSecondaryIDs{$zfinUniProtID})) {
    $ctValidSecondaryIDs++;
    $validSecondaryIDs{$zfinUniProtID} = $uniprotSecondaryIDs{$zfinUniProtID};  
  } else {
    $ctInvalidUniProIDs++;
    $invalidIDs{$zfinUniProtID} = 1;    
  }  
}

close(OUTPUT);

open (PRI, ">validPrimaryIDs") || die "Cannot open validPrimaryIDs : $!\n";
foreach $validPrimaryID (keys %validPrimaryIDs) {
  print PRI "$validPrimaryID\n";
}

close(PRI);

print "Total number of valid primary UniProt IDs at ZFIN: $ctValidPrimaryIDs\n";

print SUM "Total number of valid primary UniProt IDs at ZFIN: $ctValidPrimaryIDs\n";

open (SEC, ">validSecondaryIDs") || die "Cannot open validSecondaryIDs : $!\n";
print SEC "SecondaryID\tPrimaryID\n";
foreach $validSecondaryID (keys %validSecondaryIDs) {
  $value = $validSecondaryIDs{$validSecondaryID};
  print SEC "$validSecondaryID\t$value\n";
}

close(SEC);

print "Total number of valid secondary UniProt IDs at ZFIN: $ctValidSecondaryIDs\n";
print SUM "Total number of valid secondary UniProt IDs at ZFIN: $ctValidSecondaryIDs\n";

open (INV, ">invalidIDs") || die "Cannot open invalidIDs : $!\n";
foreach $invalidID (keys %invalidIDs) {
  print INV "$invalidID\n";
}

close(INV);

print "Total number of invalid UniProt IDs at ZFIN: $ctInvalidUniProIDs\n";
print SUM "Total number of invalid UniProt IDs at ZFIN: $ctInvalidUniProIDs\n";

close(SUM);

sendValidationSummary();

sendSecondaryIDs() if ($ctValidSecondaryIDs > 0);
sendInvalidIDs() if ($ctInvalidUniProIDs > 0);

exit;
