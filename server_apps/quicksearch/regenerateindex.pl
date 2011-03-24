#!/private/bin/perl -w
#------------------------------------------------------------------------
#
# Script that generates a new index for the web site, then moves the
# current index to the old_index holder, and moves the new_index into
# the current index.  In other words, script generates a new index
# for the site.
#
# Usage:
#
#   regenerateindex.pl
#
#   There are no arguments or options.
#

use English;

#------------------------------
# Abort the program with an error message to STDERR
#
# Params 
#  $ $abortStatus 
#  $ $errorText

sub abort($$) {

    my ($line, $abortStatus);
    $abortStatus = $_[0];
    $line = $_[1];
    print (STDERR "Error: $line\n");
    print (STDERR "$PROGRAM_NAME aborted.\n");
    print (STDERR "Exit status: $abortStatus.\n");
    exit ($abortStatus);
}


#------------------------------
# MAIN
#
# See top of the file for comments

$uniqueryDir = "<!--|ROOT_PATH|-->/server_apps/quicksearch";
$indexDir    = "$uniqueryDir/indexes";
$logsDir     = "$uniqueryDir/logs";

$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Launching Java indexer.\n");

# generate the new indexes
$status = 
    system("/usr/local/bin/ant -f $uniqueryDir/build.xml index");
if ($status) {
    abort($status, "Spider failed.");
}

$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Setting permissions on index files.\n");

$status = system("/bin/chmod o+rx $indexDir");
if ($status) {
    abort($status, "chmod o+rx $indexDir failed.");
}

$status = system("/bin/chmod o+r $indexDir/*");
if ($status) {
    abort($status, "chmod o+r $indexDir/* failed.");
}

$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Finished indexing.  Checking logs for crawled dups ...\n");
$status = system("$logsDir/check_for_duplicates.pl $logsDir/crawledUrls.log");
if ($status) {
    abort($status, "check_for_duplicates.pl $logsDir/crawledUrls.log failed.");
}

$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Checking logs for indexed dups ...\n");
$status = system("$logsDir/check_for_duplicates.pl $logsDir/indexedUrls.log");
if ($status) {
    abort($status, "check_for_duplicates.pl $logsDir/indexedUrls.log failed.");
}

$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Log analysis finished. Done indexing.\n");
print ("$dateTime: Check log file logs/spider.log for more detail.\n");

# call it a day
exit 0
