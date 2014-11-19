#!/private/bin/perl -w
#------------------------------------------------------------------------
#
# Script to load an app page into a ZFIN database.  The app page may or
# may not already exist in the database.
#
# Usage:
#
#  loadapppage.pl dbname app_page_name [app_page_name2 ... ]
#
#     dbname          Name of the database to put the app page into.
#
#     app_page_name   Name of file containing the text of the app page.
#                     This is also the name of the app page (without the
#                     leading "aa-" prefix, but with the ".apg" suffix).
#                     Optionally, there may be a path in front of the file
#                     name.
#
#   The informix environment variables are assumed to be set before calling
#   this script.
#
# Returns:
#  0   No errors were detected, app page/s was/were loaded.
#  >0  Errors were encountered.  Check error messages.


use English;



#------------------------------------------------------------------------
# Log an error message.
#
# Params
#  @       List of lines to print out.  List is assumed to be all part of
#          the same error.
#
# Returns ()

sub logError(@) {

    my $line;
    print(STDOUT "\n");
    foreach $line (@_) {
        print(STDOUT "ERROR: $line\n");
    }
    print(STDOUT "\n");
    $globalErrorCount++;
    return ();
}



#------------------------------------------------------------------------
# Execute an SQL statement and save its results in files.
#
# Params
#  $   $sql      SQL statement to execute.
#  $   $fileBase Base name of files to put results in in the working
#                directory.
#
# Returns ()

sub execSql($$) {

    my $sql = $_[0];
    my $fileBase = $_[1];
    my $stderrFile = "$fileBase.$globalStderrExt";
    my $stdoutFile = "$fileBase.$globalStdoutExt";

    system("echo '$sql' | $ENV{INFORMIXDIR}/bin/dbaccess $globalDbName - > $stdoutFile 2> $stderrFile");

    return ();
}




#------------------------------------------------------------------------
# Get the number of rows returned by an SQL statement.
#
# Params
#  $   $fileBase Base name of files to put results in in the working
#                directory.
#
# Returns
#  number of rows returned, or -1 if an error was encountered

sub getNRowsReturned($) {

    my $fileBase = $_[0];
    my $fileName = "$fileBase.$globalStderrExt";

    # open file.  We care about 2 things:
    #  The number of rows selected, and
    #  error messages.
    my $nRowsFound = -1;

    open(GETNROWSFILE, $fileName)
        or die "Unable to open $fileName";
    my $line;
    my $tableError = 0;

    while($line = <GETNROWSFILE>) {
        # ignore "Database selected.", "Database closed.", and blank lines
        if (! ($line =~ /^Database selected\.\s*$|^Database closed\.\s*$|^\s*$/)) {
            # An interesting line was found.  If its a row retrieved message
            # than get the value.

            if ($line =~ /^No rows (deleted|loaded|found|updated)\.\s*$/) {
                $nRowsFound = 0;
            }
            elsif ($line =~ /^\d+ row\(s\) (deleted|loaded|retrieved|updated)\.\s*$/) {
                my @tokens = split(/\s+/, $line);
                $nRowsFound = $tokens[0];
            }
	    elsif ($line =~ /^Temporary table created\.\s*$/) {
		$nRowsFound = 1;     # !!! HACK !!!
	    }
            else {
                # Unexpected line encountered.  Treat it as an error
                logError("Unexpected line encountered:",
                         $line);
            }
        }
    }
    close(GETNROWSFILE);
    return $nRowsFound;
}




#------------------------------------------------------------------------
# Converts the text in an app page file into a single character string
# suitable for the load command.
#
# Params
#  $   $appPageFile  Name of file containing app page text.
#
# Returns A single character string containing the text of the app page
#         in a format suitable for use with the load command.

sub convertToLoadText($) {

    my $appPageFile = $_[0];
    open(C2LT_APPPAGEFILE, "$appPageFile")
	or die "Unable to open source app page file \"$appPageFile\"";

    my $loadText;
    my $line;

    while($line = <C2LT_APPPAGEFILE>) {
	$line =~ s/\\/\\\\/g;	# escape backslashes
	$line =~ s/\|/\\\|/g;	# escape bars
	$line =~ s/\n/\\\n/g;	# escape newlines
	$loadText .= $line;
    }
    close(C2LT_APPPAGEFILE);

    return $loadText;
}




#------------------------------------------------------------------------
# Get the current date time in a load compatible format
#
# Params  none
#
# Returns Current date time in a load compatible format.

sub getLoadDateTime() {

    my $sec;
    my $min;
    my $hour;
    my $mday;
    my $month;
    my $year;
    my $wday;
    my $yday;
    my $isdst;

    ($sec, $min, $hour, $mday, $month, $year, $wday, $yday, $isdst) =
	localtime();
    return sprintf("%d-%02d-%02d %02d:%02d:%02d.000",
		   $year + 1900, $month + 1, $mday, $hour, $min, $sec);
}




#------------------------------------------------------------------------
# Sets the value of an app page in the database, to the contents of a file.
#
# Params
#  $   $appPageFile  Name of file containing app page text.  App page name
#                    and the file name are the same.  This is without the "aa-"
#                    prefix, but with the ".apg" suffix.  This is also the
#                    Optionally, the filename may be prefixed with a path.
#
# Returns ()

sub setAppPage($) {

    my $appPageFile = $_[0];
    my $appPage = $appPageFile;
    my $slashIdx = rindex($appPage, "/");
    if ($slashIdx >= 0) {
	$appPage = substr($appPage, $slashIdx + 1);
    }
    my $tempTable = "temp_webpages";

    my $appPageText = convertToLoadText($appPageFile);
    my $loadDate = getLoadDateTime();

    my $fileName = "$globalTmpDir/$appPage";
    my $loadFile = "$fileName.loaddata";

    open(LOADFILE, ">$loadFile")
	or die "Unable to open load file \"$loadFile\"";
    print(LOADFILE "aa-$appPage|zfish|AppPage|text/html|||1|$loadDate||0|0|$appPageText|\n");
    close(LOADFILE);

    # find out if web page already exists
    my $sqlSelect = "select * from webpages where id = \"aa-$appPage\";";
    my $fileNameSelect = "$fileName.select";
    execSql($sqlSelect, $fileNameSelect);
    my $nRowsSelect = getNRowsReturned($fileNameSelect);
    if ($nRowsSelect < 0 || $nRowsSelect > 1) {
	# something seriously wrong.
	die "Fatal error encountered while processing $appPageFile.";
    }

    # Paths now seriously diverge depending on if app page already exists
    # or not.
    if ($nRowsSelect == 1) {
	# app page exists: load new app page into staging table, then
	# update old app page with values from new app page.

	my $sqlLoad = "load from \'$loadFile\' insert into staging_webpages;";
	my $fileNameLoad = "$fileName.load";
	execSql($sqlLoad, $fileNameLoad);
	my $nRowsLoaded = getNRowsReturned($fileNameLoad);
	if ($nRowsLoaded != 1) {
	    # no rows loaded.  Something is very wrong.
	    die "App page \"$appPage\" failed to load.";
	}

	# CAN'T DIE AFTER THIS POINT UNLESS WE DELETE RECORD FROM STAGING TABLE

	my $fatalError = 0;
	# update old page with values from new page in temp table
	my $sqlUpdate =
	    "update webpages set (last_changed, object) = "
	  . "   ((select last_changed, object from staging_webpages "
	  . "     where id = \"aa-$appPage\")) "
	  . " where id = \"aa-$appPage\";";
	my $fileNameUpdate = "$fileName.update";
	execSql($sqlUpdate, $fileNameUpdate);
	my $nRowsUpdated = getNRowsReturned($fileNameUpdate);
	if ($nRowsUpdated != 1) {
	    # no rows updated.  Something went wrong.
	    logError("App page \"$appPage\" update failed");
	    $fatalError = 1;
	}

	# delete record from staging table.
	my $sqlDelete = "delete from staging_webpages where id = \"aa-$appPage\";";
	my $fileNameDelete = "$fileName.delete";
	execSql($sqlDelete, $fileNameDelete);
	my $nRowsDeleted = getNRowsReturned($fileNameDelete);
	if ($nRowsDeleted != 1) {
	    # no rows deleted.  Something went wrong.
	    logError("Delete of App page \"$appPage\" from staging table failed");
	    $fatalError = 1;
	}
	if ($fatalError) {
	    die "Fatal Error encountered.  Terminating.";
	}
    }
    else {
	# app page does not already exist.  Load new app page directly into
	# webpages table.
	my $sqlLoad = "load from \'$loadFile\' insert into webpages;";
	my $fileNameLoad = "$fileName.load";
	execSql($sqlLoad, $fileNameLoad);
	my $nRowsLoaded = getNRowsReturned($fileNameLoad);
	if ($nRowsLoaded != 1) {
	    # no rows loaded.  Something is very wrong.
	    die "App page \"$appPage\" failed to load.";
	}
    }
    return ();
}





#------------------------------------------------------------------------
# Main.
#



#
# Define GLOBALS
#

$globalErrorCount = 0;
$globalDbName = "";
$globalTmpDir = "/tmp/loadapppage.$PROCESS_ID";
$globalStderrExt = "err";
$globalStdoutExt = "out";

# process args

$globalDbName = shift(@ARGV);

my $dirPerms = oct(770);
mkdir($globalTmpDir, $dirPerms);

my $appPageFile;
foreach $appPageFile (@ARGV) {
    setAppPage($appPageFile);
}
system("/bin/rm -r $globalTmpDir");

#  if called from gmake and page load failed
#  run weblint verbosly (3) before exiting
#  WEBLINTIFMXBASE is exported from 'make.include'

if ($globalErrorCount  && $ENV{WEBLINTIFMXBASE}) {
    system("$ENV{WEBLINTIFMXBASE} 3 < @ARGV" );
}
exit ($globalErrorCount);
