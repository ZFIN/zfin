#!/opt/zfin/bin/perl
package ZFINPerlModules;

use strict;
use MIME::Lite;
use DBI;
use Exporter 'import';

our @EXPORT_OK = qw(md5File assertEnvironment trim assertFileExists assertFileExistsAndNotEmpty getPropertyValue downloadOrUseLocalFile doSystemCommand doSystemCommandOrFailWithEmail);

my %monthDisplays = (
    "Jan" => "01",
    "Feb" => "02",
    "Mar" => "03",
    "Apr" => "04",
    "May" => "05",
    "Jun" => "06",
    "Jul" => "07",
    "Aug" => "08",
    "Sep" => "09",
    "Oct" => "10",
    "Nov" => "11",
    "Dec" => "12"
);

#------ Global variables for "whirleygig" to indicate busy working (https://www.perlmonks.org/?node_id=4943)
my $WHIRLEY_COUNT=-1;
# my @WHIRLEY=('-', '\\', '|', '/');
my @WHIRLEY=('-', '¯', '-', '_');
my $WHIRLEY_LAST_OUTPUT_TIME=0;

#rate limit whirley outputs in number of seconds
my $WHIRLEY_TIME_LIMIT = 10;

#------- Default sendmail path
my $SENDMAIL_FLAGS="-t -oi";
my $SENDMAIL_COMMAND="/usr/lib/sendmail";
if (exists($ENV{'SENDMAIL_COMMAND'})) {
    $SENDMAIL_COMMAND = $ENV{'SENDMAIL_COMMAND'};
}
$SENDMAIL_COMMAND .= " " . $SENDMAIL_FLAGS;

sub doSystemCommand {                  

  my $systemCommand = $_[1];

  print "Executing [$systemCommand] \n";

  my $returnCode = system( $systemCommand );

  if ( $returnCode != 0 ) {
    exit -1;
  }
}

sub doSystemCommandOrFailWithEmail {
    my $systemCommand = shift();
    my $email = shift();
    my $subject = shift();
    my $textFile = shift();

    print "Executing [$systemCommand] \n";
    my $returnCode = system( $systemCommand );

    if ( $returnCode != 0 ) {
        ZFINPerlModules->sendMailWithAttachedReport($email, $subject, $textFile);
        exit -1;
    }
}

sub sendMailWithAttachedReport {
    my ($self, $recipient, $subject, $attachment_path) = @_;

    # Log mail information
    print "\nPreparing mail:";
    print "\n  To: $recipient";
    print "\n  Subject: $subject";
    print "\n  Attachment: $attachment_path\n";

    # Create a new multipart message
    my $msg = MIME::Lite->new(
        From    => $ENV{LOGNAME},
        To      => $recipient,
        Subject => $subject,
        Type    => 'multipart/mixed'
    );

    # Attach the report file
    attach $msg
	Type     => 'text/plain',
	Path     => "$attachment_path";

    # Check if we should write to file instead of sending
    if ($ENV{EMAIL_TO_FILE} && $ENV{EMAIL_TO_FILE} eq "true") {
        # Generate a unique filename based on timestamp and recipient
        my $timestamp = time();
        my $safe_recipient = $recipient;
        $safe_recipient =~ s/[^\w\.\-]/_/g;  # Replace non-word chars with underscore
        my $output_file = "/tmp/email_${timestamp}_${safe_recipient}.eml";

        # Write the email to a file
        print "\n  Mode: Writing to file ($output_file)\n\n";

        open(my $outfile, ">", $output_file)
            or die "Failed to open output file $output_file: $!";
        $msg->print($outfile);
        close($outfile)
            or warn "Failed to close output file $output_file: $!";

        return { success => 1, mode => 'file', path => $output_file };
    }
    else {
        # Send through sendmail
        print "\n  Mode: Sending via sendmail\n\n";

        open(my $sendmail, "| $SENDMAIL_COMMAND")
            or die "Failed to open sendmail: $!";
        $msg->print($sendmail);
        close($sendmail)
            or warn "Failed to close sendmail: $!";

        return { success => 1, mode => 'sendmail' };
    }
}

sub countData() {

  my $ctsql = $_[1];
  my $nRecords = 0;

  ### open a handle on the db
    my $dbh = DBI->connect('DBI:Pg:dbname=' . $ENV{'DB_NAME'} . ';host=' . $ENV{"PGHOST"},
                       '',
                       '',
		       {AutoCommit => 1,RaiseError => 1}
		      )
    or die ("Failed while connecting to " . $ENV{'DB_NAME'});


  my $sth = $dbh->prepare($ctsql) or die "Prepare fails";
  
  $sth -> execute() or die "Could not execute $ctsql";
  
  while (my @row = $sth ->fetchrow_array()) {
    $nRecords++;
  }  

  $dbh->disconnect
    or warn "Disconnection failed: $DBI::errstr\n";

  return ($nRecords);
}

sub stringStartsWithNumber() {
  my $stringTested = $_[1];
  if ($stringTested =~ m/^[0-9]/) {
      return 1;
  } else {
      return 0;
  }
}

sub stringStartsWithLetter() {
  my $stringTested = $_[1];
  if ($stringTested =~ m/^[a-zA-Z]/) {
      return 1;
  } else {
      return 0;
  }
}

sub stringStartsWithLetterOrNumber() {
  my $stringTested = $_[1];
  if ($stringTested =~ m/^[a-zA-Z0-9]/) {
      return 1;
  } else {
      return 0;
  }
}


sub getYear() {
  my $dateString = $_[1];
  my $year = substr $dateString, 0, 4;
  return $year;
}

sub getMonth() {
  my $dateString = $_[1];
  my $month = substr $dateString, 4, 2;
  return $month;
}

sub getDay() {
  my $dateString = $_[1];
  my $day = substr $dateString, 6;
  return $day;
}

sub month3LettersToNumber() {
  my $month3Letters = $_[1];
  if (exists($monthDisplays{$month3Letters})) {
      return $monthDisplays{$month3Letters};
  } else {
      return "-1";
  }
}

# ====================================
#
# "Whirleygig" progress indicator
#
sub whirley {
  $WHIRLEY_COUNT = ($WHIRLEY_COUNT + 1) % @WHIRLEY;
  return $WHIRLEY[$WHIRLEY_COUNT];
}

sub printWhirleyToStderr {
    my $currentTime = time();
    if ($currentTime - $WHIRLEY_LAST_OUTPUT_TIME < $WHIRLEY_TIME_LIMIT) {
        return;
    }
    print STDERR whirley();
    $WHIRLEY_LAST_OUTPUT_TIME = time();
}


sub md5File {
    my $file = $_[0];
    my $hash = `md5sum '$file' | cut -d ' ' -f 1`;
    $hash =~ s/\s+$//;
    return $hash;
}

sub assertEnvironment {
    my @required_vars = @_;
    foreach my $var (@required_vars) {
        if (!$ENV{$var}) {
            print("No $var environment variable defined\n");
            exit(2);
        }
    }
}

sub assertFileExistsAndNotEmpty {
    my $filename = shift();
    my $error_message = shift();

    unless (-e $filename && -s $filename) {
        die($error_message);
    }
}

sub assertFileExists {
    my $filename = shift();
    my $error_message = shift();

    unless (-e $filename) {
        die($error_message);
    }
}

sub trim {
    my $s = shift();

    if (!defined($s)) {
        return undef;
    }

    $s =~ s/^\s*//u;
    $s =~ s/\s*$//u;

    return $s;
}

sub getPropertyValue {
    my $property_name = shift();
    my $property_file = $ENV{'TARGETROOT'} . "/home/WEB-INF/zfin.properties";

    open(PROPERTIES, $property_file) or die("Could not open $property_file");
    while (<PROPERTIES>) {
        chomp;
        if ($_ =~ m/^$property_name=(.*)$/) {
            close(PROPERTIES);
            return $1;
        }
    }
    close(PROPERTIES);
}

sub downloadOrUseLocalFile {
    my ($url, $outfile) = @_;
    if ($ENV{"SKIP_DOWNLOADS"}) {
        print("Skipping download '$url' to '$outfile'\n");
        my $outfileWithoutGz = $outfile  =~ s/\.gz$//r;
        if (!-e $outfile && !-e $outfileWithoutGz) {
            print "*************************************************\n";
            print "* ERROR: The $outfile file does not exist, but we are running with SKIP_DOWNLOADS flag\n";
            print "*************************************************\n";
            exit -1;
        } elsif (!-e $outfile && -e $outfileWithoutGz) {
            print "$outfile file missing, continuing with $outfileWithoutGz\n";
        }
    } else {
        print("Downloading '$url' to '$outfile'\n");

        #check if file exists
        if (-e $outfile) {
            print("File '$outfile' already exists, skipping download\n");
            exit(1);
        }

        #set the number of bytes that a dot represents in wget progress bar to 10M
        system("wget --progress=dot -e dotbytes=10M '$url' -O '$outfile'");
    }
}

1;
