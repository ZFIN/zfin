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

$uniqueryDir = "<!--|ROOT_PATH|-->/j2ee/uniquery";
$newIndexDir = "$uniqueryDir/new_indexes";
$oldIndexDir = "$uniqueryDir/old_indexes";
$indexDir    = "$uniqueryDir/indexes";
$indexAppDir = "$uniqueryDir/IndexApp";
$logsDir     = "$uniqueryDir/IndexApp/logs";

$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Regenerating index.\n");

# create directory for new indexes
$status = system("/bin/rm -rf $newIndexDir");
if ($status) {
    abort($status, "rm $newIndexDir failed.");
}

$status = system("/bin/mkdir $newIndexDir");
if ($status) {
    abort($status, "mkdir $newIndexDir failed.");
}

# generate the new indexes
$status = 
    system("/private/apps/java1.4/bin/java" .
	   " -server" .
	   " -classpath " .
	      "$indexAppDir/classes:" .
	      "$indexAppDir/lib/UniquerySupport.jar:" .
	      "$indexAppDir/lib/commons-lang-2.0.jar:" .
	      "$indexAppDir/lib/cvu.jar:" .
	      "$indexAppDir/lib/lucene-1.3.jar" .
	   " org.zfin.uniquery.index.Spider " .
	   " -d $newIndexDir " .
	   " -u $indexAppDir/etc/searchurls.txt " .
	   " -e $indexAppDir/etc/excludeurls.txt " .
	   " -c $indexAppDir/etc/crawlonlyurls.txt " .
	   " -t 2 " .
	   " -l $logsDir " .
	   " -v");
if ($status) {
    abort($status, "Spider failed.");
}

# rename current index to old index and move new to current

if (-d "$oldIndexDir") {
    $status = system("/bin/rm -rf $oldIndexDir");
    if ($status) {
	abort($status, "rm $oldIndexDir failed.");
    }
}

if (-d "$indexDir") {
    $status = system("/bin/mv $indexDir $oldIndexDir");
    if ($status) {
	abort($status, "mv $indexDir $oldIndexDir failed.");
    }
}

$status = system("/bin/mv $newIndexDir $indexDir");
if ($status) {
    abort($status, "mv $newIndexDir $indexDir failed.");
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
print ("$dateTime: Counting crawled pages ...\n");
$status = system("$logsDir/count_pages.pl $logsDir/crawledUrls.log");
if ($status) {
    abort($status, "count_pages.pl $logsDir/crawledUrls.log failed.");
}

$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Counting indexed pages ...\n");
$status = system("$logsDir/count_pages.pl $logsDir/indexedUrls.log");
if ($status) {
    abort($status, "count_pages.pl $logsDir/indexedUrls.log failed.");
}

$dateTime = `/bin/date`;
chop($dateTime);
print ("$dateTime: Log analysis finished. Done indexing.\n");

# call it a day
exit 0
