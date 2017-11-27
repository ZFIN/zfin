#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Script to pull information from ZIRC and EZRC.
#
# Usage:
#
#   pullFromResourceCenter.pl
#
#     There are no arguments to the script, and the script assumes
#     that no environment variables are set.
#
#
#     Returns
#       0    All data was successfully pulled from ZIRC and EZRC and loaded in the DB.
#      !0    None or only some of the data was successsfully pulled from
#            EZRC, CZRC, Baier and ZIRC and loaded in the DB.  See output for details.

use DBI;
use MIME::Lite;
#----------------------------------------------------------------------
# Write a line to the report 
#
# Params
#  @     Lines to write out.
#
# Returns ()

sub writeReport(@) {
    my $line;

    foreach $line (@_) {
	print(ZIRCREPORT "$line\n");
    }
    return ();
}

sub sendLoadReport ($$$) { # send email on error or completion

# . is concantenate
# $_[x] means to take from the array of values passed to the fxn, the
# number indicated: $_[0] takes the first member.

    my $SUBJECT="pullFromResourceCenter:".$_[0];
    my $MAILTO=$_[1];
    my $TXTFILE=$_[2];
    my $data=readpipe "/bin/cat $TXTFILE";

    # Create a new message:
    $msg1 = new MIME::Lite
	From    => "$ENV{LOGNAME}",
	To      => "$MAILTO",
	Subject => "$SUBJECT",
    Type    => 'text/plain',
    Data    => "$data"
    ;

    # Output the message to sendmail

    open (SENDMAIL, "| /usr/lib/sendmail -t -oi");
    $msg1->print(\*SENDMAIL);
    close (SENDMAIL);
}


#----------------------------------------------------------------------
# Writes out an error message and then aborts the program.
#
# Params
#  @     List of lines to write out.
#
# Returns
#  does not return.  Exits with an error status.

sub errorExit(@) {
    &writeReport(@_);
    &writeReport("All database changes are being rolled back.");
    exit 1;
}


#----------------------------------------------------------------------
# Download files from ZIRC
#
# Params
#  @      List of files to download.  They are downloaded into the current
#         directory with the same names they have at ZIRC.
#
# Returns ()

sub downloadFiles($$) {
    my $filename = $_[0];

    my $wgetStatusFile = "/tmp/pullFromResourceCenter.<!--|DB_NAME|-->.$filename";
    system("rm -f $wgetStatusFile");
   system("rm -f $wgetStatusFile*");
   system("rm -f $filename*");
   system("rm -f $filename");
    my $resourceCenter = $_[1];

    if ($resourceCenter eq "CZRC"){
	if (system("/local/bin/wget http://www.zfish.cn/$filename")) {
	    &errorExit("Failed to download $filename file from CZRC.","  See $wgetStatusFile for details.");
	}
	$labZdbId = "ZDB-LAB-130226-1";
    }
    if (-z $filename) {
	&errorExit("Downloaded file $filename is empty.  Aborting.",
		   "  See $wgetStatusFile for details.");
    }
    
    return ();
}


#----------------------------------------------------------------------
# All the common routines are defined above.  Anything defined above is
# either used in this file, or in 1 or more of the required files, or both.
#
# Now, get the subroutines for handling each type of data.

require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters/pullEstsFromZirc_PG.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters/pullGenoFromResourceCenter_PG.pl");
#require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ZIRC/pullAtbFromZirc.pl");

#----------------------------------------------------------------------
# Main
#
# See top of file for usage and params.


# define GLOBALS

# set environment variables
# Hard code the ZDB ID of ZIRC

my $czrcZdbId = "ZDB-LAB-130226-1";
my $labZdbId;
system("/bin/rm -f <!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters/loadReport.txt");

open(ZIRCREPORT, ">> <!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters/loadReport.txt") or die "can't open loadReport.txt";
system("/bin/chmod ug+w <!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters/loadReport.txt");
# Prepare to do some work.
#  CD into working directory
#  remove old downloaded files.
#  Open Database.
my $dbname = "<!--|DB_NAME|-->";
chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/ZIRC/";
my $dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
              or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";


# Now do the work.
# Each function below does more or less the same steps:
#  o remove old downloaded files
#  o download files
#  o parse and prepare the downloaded data into a format that can be used.
#  o Update the database, reporting as it goes

       # EST availability ZIRC


$dbh->commit();
$dbh->disconnect();

$dbh = DBI->connect ("DBI:Pg:dbname=$dbname;host=localhost", $username, $password)
           or die "Cannot connect to PostgreSQL database: $DBI::errstr\n";


&geno_main($dbh, $czrcZdbId,"CZRC");           # Genotype availability CZRC
#&est_main($dbh, $zircZdbId);	 

$dbh->commit();
$dbh->disconnect();

system("<!--|TARGETROOT|-->/server_apps/data_transfer/ResourceCenters/syncFishOrderThisLinks_PG.sh");
#&sendLoadReport("Data transfer report","<!--|VALIDATION_EMAIL_DBA|-->", "<!--|ROOT_PATH|-->/server_apps/data_transfer/ResourceCenters/loadReport.txt") ;

exit 0;
