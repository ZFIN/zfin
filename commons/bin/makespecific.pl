#!/private/bin/perl -w
#
# This is a wrapper program that goes around calls to the makespecific 
# script that actually does the work.
#
# Usage:
#   makespecific.pl _infile_ _translatefile_ _outfile
#
# The wrapper provides a number of functions:
# 1. Rebol, the language that makespecific is written in, always returns
#    a 0 status, if it was successful or not.  However, when the makespecific
#    script fails (in the sense we care about) it produces output.
#    This script detects that output and returns a 1 and prints the output
#    if any is produced.
# 2. Rebol, does not have a good handle on Unix file permissions.  Therefore,
#    the permissions of the input file aren't necessarily related to the
#    permissions of the output file.  This script changes the permissions
#    of the output file to agree with those of the input file.
#
# $Id: makespecific.pl,v 1.3 2004-11-23 17:27:31 clements Exp $
# $Source: /research/zusers/ndunn/CVSROOT/Commons/bin/makespecific.pl,v $

use English;

# The stat command produces many columns of output.  Identify the columns 
# we care about.

$STAT_MODE_IDX = 2;
$STAT_SIZE_IDX = 7;

$inFile = $ARGV[0];
$translateFile = $ARGV[1];
$outFile = $ARGV[2];

$makeSpecificWorker = "/private/ZfinLinks/Commons/bin/makespecificworker";

$statusFile = "/tmp/makespecifc.$PROCESS_ID";

# Error if returnStatus non-0, statusFile not created, or statusFile has non-0
# length

system("rm -rf $outFile");
$returnStatus = system("$makeSpecificWorker $inFile $translateFile $outFile > $statusFile 2>&1");

$statusFileExists = -e $statusFile;
$statusFileSize = 0;

if ($statusFileExists) {
    @statusFileStats = stat($statusFile);
    $statusFileSize = $statusFileStats[$STAT_SIZE_IDX];
}

if ($returnStatus || ! $statusFileExists || $statusFileSize) {
    # Call failed.
    $returnStatus = 1;
    print(STDERR "$PROGRAM_NAME call failed.\n");
    if ($statusFileSize) {
	# Status file has non-0 length.  Print it out.
	open(STATUSFILE, $statusFile);
	while ($line = <STATUSFILE>) {
	    print(STDERR $line);
	}
	close(STATUSFILE);
    }
}
else {
    # call appears to have worked.  Change permissions of outfile to match
    # those of infile.
    @inFileStats = stat($inFile);
    chmod ($inFileStats[$STAT_MODE_IDX], $outFile);
}

system("rm $statusFile");

exit $returnStatus;
