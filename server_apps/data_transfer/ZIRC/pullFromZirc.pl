#!/private/bin/perl -w
#-----------------------------------------------------------------------
# Script to pull information from ZIRC.
#
# We pull several different kinds of information:
#
#  o ESTs that are available from ZIRC.
#    This is just a list of EST ZDB IDs.  If an EST is in the list, then it
#    is available from ZIRC.  There is no additional status information.
#    The routines that are specific to this task are in the 
#    pullEstsFromZirc.pl file.
#
# Usage:
#
#   pullFromZirc.pl
#
#     There are no arguments to the script, and the script assumes 
#     that no environment variables are set.
#
#     This writes a report to STDOUT.
#
#     Returns
#       0    All data was successfully pulled from ZIRC and loaded in the DB.
#      !0    None or only some of the data was successsfully pulled from 
#            ZIRC and loaded in the DB.  See output for details.

use DBI;

#----------------------------------------------------------------------
# Write a line to the report (STDOUT)
#
# Params
#  @     Lines to write out.
#
# Returns ()

sub writeReport(@) {
    my $line;

    foreach $line (@_) {
	print(STDOUT "$line\n");
    }
    return ();
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
	
    my $wgetStatusFile = "/tmp/pullFromZirc.<!--|DB_NAME|-->.$filename";
    system("rm -f $wgetStatusFile");
    if (system("/local/bin/wget http://zebrafish.org/zirc/zfin/$filename >> $wgetStatusFile 2>&1")) {
	&errorExit("Failed to download $filename file from ZIRC.","  See $wgetStatusFile for details.");
    }
    if (-z $filename) {
	&errorExit("Downloaded file $filename is empty.  Aborting.",
		   "  See $wgetStatusFile for details.");
    }
#    else {
	
#    $wgetStatusFile = "/tmp/pullFromZirc.almdb.$filename";
#    system("rm -f $wgetStatusFile");
#    if (system("/local/bin/wget http://zirc.uoregon.edu/zfin/$filename >> $wgetStatusFile 2>&1")) {
#	&errorExit("Failed to download $filename file from ZIRC.",
#		   "  See $wgetStatusFile for details.");
#    }
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

require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ZIRC/pullEstsFromZirc.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ZIRC/pullGenoFromZirc.pl");
require ("<!--|ROOT_PATH|-->/server_apps/data_transfer/ZIRC/pullAtbFromZirc.pl");

#----------------------------------------------------------------------
# Main
#
# See top of file for usage and params.


# define GLOBALS

# set environment variables
$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

# Hard code the ZDB ID of ZIRC
my $zircZdbId = "ZDB-LAB-991005-53";

# Prepare to do some work.
#  CD into working directory
#  remove old downloaded files.
#  Open Database.

chdir "<!--|ROOT_PATH|-->/server_apps/data_transfer/ZIRC/";
my $dbh = DBI->connect('DBI:Informix:<!--|DB_NAME|-->',
		       '', 
		       '', 
		       {AutoCommit => 0, RaiseError => 1}
		       )
  || errorExit("Failed while connecting to <!--|DB_NAME|--> "); 


# Now do the work.
# Each function below does more or less the same steps:
#  o remove old downloaded files
#  o download files
#  o parse and prepare the downloaded data into a format that can be used.
#  o Update the database, reporting as it goes

&geno_main($dbh, $zircZdbId);           # Genotype availability
&est_main($dbh, $zircZdbId);	        # EST availability
&atb_main($dbh, $zircZdbId);	        # Antibody availability

$dbh->commit();
$dbh->disconnect();

exit 0;


