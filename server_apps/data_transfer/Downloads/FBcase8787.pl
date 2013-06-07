#!/private/bin/perl

# FILE: FBcase8787.pl
# 

use MIME::Lite;

#set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

system("rm -f FB8787log1");
system("rm -f FB8787log2");

$dir = "<!--|ROOT_PATH|-->";

@dirPieces = split(/www_homes/,$dir);

$dbname = $dirPieces[1];
$dbname =~ s/\///;

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> runNumbersFBcase8787.sql >FB8787log1 2> FB8787log2");

$SUBJECT="Auto: FB8787log1 from ".$dbname;
$MAILTO="informix\@cs.uoregon.edu";
$TXTFILE="./FB8787log1";

# Create a new multipart message:
$msg1 = new MIME::Lite
From    => "$ENV{LOGNAME}",
To      => "$MAILTO",
Subject => "$SUBJECT",
Type    => 'multipart/mixed';

attach $msg1
Type     => 'text/plain',
Path     => "$TXTFILE";

# Output the message to sendmail

open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
$msg1->print(\*SENDMAIL);

$SUBJECT="Auto: FB8787log2 from ".$dbname;
$TXTFILE="./FB8787log2";

# Create another multipart message:
$msg2 = new MIME::Lite
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

open (XPAT8787PIPIE, "<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpatGenesPipe.txt") || die "Cannot open <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/xpatGenesPipe.txt : $!\n";
@lines=<XPAT8787PIPIE>;
close(XPAT8787PIPIE);

open (XPAT8787, "><!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/xpatGenes.txt") || die "Cannot open <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpatGenes.txt : $!\n";


foreach $line (@lines) {
  if ($line) {
    $line =~ s/\\//g; 
    print XPAT8787 $line;
  }
}

# remove temporary file
system("rm <!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpatGenesPipe.txt");

exit;





