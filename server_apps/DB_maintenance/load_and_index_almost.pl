#!/private/bin/perl -w
#-----------------------------------------------------------------------
# 
# This script is meant to be run from cron.  As it is such a resource hog,
# it is not meant to be run during the day from the shell prompt.
#
# Every time this script is run it does these 2 steps:
#
# 1. Loads almdb from the latest dump it can find.  This loads every
#    table in the database.
#    Approx run time: < 45 minutes
# 2. Runs the gmake postloaddb target.  This will get the corresponding
#    web site in sync with the database backing it.
#    Approx run time: < 3 minutes
#
# If the needed option is specified then it will also
#
# 3. Runs the uniquery indexer.  It runs the indexer with how every many
#    threads are specified in the index script.  It will consume that many
#    CPUs for the whole time it is running.
#    Approx run time as of 2004/07: 
#     2 threads:  > 10 HOURS.
#     3 threads:  >  9 HOURS.
#
# Usage:
#
#   load_and_index_almost.pl [-i]
#
#      The script will always load almdb and gmake postloaddb.
#   
#   Options
#   -i In addition to loading and running gmake postloaddb, the script
#      will also index the web site.  See above.
#
#   There are no arguments 
#

use English;


#------------------------------
# Abort the program with an error message to STDERR
#
# Params 
#  @      List of lines to print out before dying.
#
# Globals
#  $logFile

sub abort(@) {

    my $line;
    print (STDERR "Error:   ");
    foreach $line (@_) {
        print (STDERR "$line\n");
    }
    print (STDERR "\n");
    print (STDERR "$PROGRAM_NAME aborted.\n");
    print (STDERR "See $logFile for details.\n");
    exit (-1);
}



#------------------------------
# Writes a warning to STDERR and continues execution.
#
# Params 
#  @      List of lines to print to STDERR
#
# Globals
#  $logFile

sub warning(@) {

    my $line;
    print (STDERR "Warning: ");
    foreach $line (@_) {
        print (STDERR "$line\n");
    }
    print (STDERR "\n");
    print (STDERR "$PROGRAM_NAME warning.\n");
    print (STDERR "See $logFile for details.\n");
    return;
}



#------------------------------
# Copy indexes from where this script puts them to another directory.
# This also moves the existing index directory (if there is one) to 
# old_indexes.
#
# Params 
#  $     Index directory to copy from.
#  $     Parent directory to put index directory into.  The 
#        new_indexes, old_indexes, and indexes directories are all 
#        affected by this routine.
#
# Globals
#  $logFile

sub copyIndexes($$) {

    my $status;
    my $srcDir = $_[0];
    my $destDir = $_[1];
    my $destNewIndexDir = "$destDir/new_indexes";
    my $destOldIndexDir = "$destDir/old_indexes";
    my $destIndexDir    = "$destDir/indexes";

    # This routine does
    #   1. remove $destNewIndexDir if it exists
    #   2. copy   $srcDir          to $destNewIndexDir
    #   3. remove $destOldIndexDir if it exists
    #   4. move   $destIndexDir    to $destOldIndexDir
    #   5. move   $destNewIndexDir to $destIndexDir
    # We copy the index to new_indexes first and then move it to indexes because
    # the copy is likely to be slow and the renames are likely to be fast.
    # Once we rename indexes, and until we have a new indexes, the uniquery
    # will not work.  Therefore we try to minimize that window.

    # 1. remove new_indexes dir if it exists
    $status = system("/bin/rm -rf $destNewIndexDir >> $logFile 2>&1");
    if ($status) {
	warning("rm -rf $destNewIndexDir failed." .
		" Terminating this copy.");
	return $status;		# !!!!!!!!! EARLY RETURN !!!!!!!!!
    }

    # 2. copy index to new_indexes.
    $status = system("/bin/cp -pr $srcDir $destNewIndexDir >> $logFile 2>&1");
    if ($status) {
	warning("cp -pr $srcDir $destNewIndexDir failed." .
		" Terminating this copy.\n");
	return $status;		# !!!!!!!!! EARLY RETURN !!!!!!!!!
    }

    # 3. if old_indexes exists, remove it first.
    if (-d "$destOldIndexDir") {
	$status = system("/bin/rm -rf $destOldIndexDir >> $logFile 2>&1");
	if ($status) {
	    warning("rm -rf $destOldIndexDir failed." .
		    " Terminating this copy.\n");
	    return $status;	# !!!!!!!!! EARLY RETURN !!!!!!!!!
	}
    }
 
    # 4. if indexes exists, move it to old_indexes.
    if (-d "$destIndexDir") {
	$status = system("/bin/mv $destIndexDir $destOldIndexDir >> $logFile 2>&1");
	if ($status) {
	    warning("mv $destIndexDir $destOldIndexDir failed." .
		    " Terminating this copy.\n");
	    return $status;	# !!!!!!!!! EARLY RETURN !!!!!!!!!
	}
    } 

    # 5. Move new_indexes to indexes
    $status = system("/bin/mv $destNewIndexDir $destIndexDir >> $logFile 2>&1");
    if ($status) {
	warning("mv $destNewIndexDir $destIndexDir failed." .
		" Attempting to mv $destOldIndexDir back to $destIndexDir.\n");
	$status = system("/bin/mv $destOldIndexDir $destIndexDir >> $logFile 2>&1");
	if ($status) { # restore failed.  big trouble
	    warning("mv $destOldIndexDir $destIndexDir failed." .
		    " Uniquery is no longer working.");
	}
	else { # restore worked, little trouble
	    warning("mv $destOldIndexDir $destIndexDir succeeded." .
		    " Uniquery is working, but with the old index.");
	}
    } 
    return $status;
}



#------------------------------
# MAIN
#
# See top of the file for comments

$logFile = "/tmp/load_and_index_almost.log";
open(LOG, ">$logFile") or abort("Cannot open log file $logFile.\n");
print(LOG "Log file created by $PROGRAM_NAME\n");

$dbName = "almdb";
$zfinWwwDir = "/research/zusers/almost/ZFIN_WWW";
$makeEnvFile = "/research/zcentral/Commons/env/almost.env";

# Use your own values while testing/modifying this script.
#$dbName      = "clemdb";
#$zfinWwwDir  = "/research/zusers/clements/Zfin/ZFIN_WWW";
#$makeEnvFile = "/research/zcentral/Commons/env/albino.env";


#set environment variables
$ENV{"INFORMIXDIR"}      = "<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}   = "<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}         = "<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"} = "<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

# Get argument
my $indexing = 0;
my $nArgs = @ARGV;
if ($nArgs > 1) {
    abort("Too many arguments.  See $PROGRAM_NAME for usage.");
}
elsif (1 == $nArgs && "-i" eq $ARGV[0]) {
    $indexing = 1;
}

# Find latest DB dump
$dumpDir = "/research/zunloads/databases/production";
$latestDump = `/bin/ls -1t $dumpDir | head -1`;
chop($latestDump);
print(LOG "Using dump $dumpDir/$latestDump\n");

# load it
close(LOG) or abort("Cannot close log file $logFile.\n");
$status = system("/research/zcentral/Commons/bin/loaddb.pl -ee $dbName $dumpDir/$latestDump >> $logFile 2>&1");
if ($status) {
    abort("loadddb.pl $dbName $dumpDir/$latestDump failed.\n");
}

# gmake postloaddb it
$status = system("<!--|ROOT_PATH|-->/server_apps/DB_maintenance/postloaddb_almost.csh $zfinWwwDir $makeEnvFile >> $logFile 2>&1");
if ($status) {
    abort("gmake postloaddb failed.\n");
}

# Index the web site, if requested.  This takes many hours.
if ($indexing) {
    $status = system("<!--|ROOT_PATH|-->/j2ee/uniquery/IndexApp/regenerateindex.pl >> $logFile 2>&1");
    if ($status) {
	abort("regenerateindex.pl failed.\n");
    }
    # Need to copy indexes to production.  The problem is this script can't
    # tell if production is running on chromix or embryonix.  Copy to both.
    # If either copy gets an error, report it, but keep running.
    copyIndexes("<!--|ROOT_PATH|-->/j2ee/uniquery/indexes",
		"/research/zfin/chromix/www/j2ee/uniquery");
    copyIndexes("<!--|ROOT_PATH|-->/j2ee/uniquery/indexes",
		"/research/zcentral/www_homes/embryonix/j2ee/uniquery");
}

exit 0;
