#!/private/bin/perl -w
#------------------------------------------------------------------------
# 
# Script to to unload an app page from a ZFIN database.
#
# Usage:
#  
#  unloadapppage.pl [-d dest_dir] dbname app_page_name [app_page_name2 ... ]
#
#     -d dest_dir     Directory to write the app pages to.  If this is not 
#                     provided then the app pages will be written in the 
#                     current working directory.
#                     This directory must already exist.
#
#     dbname          Name of the database to get the app page form
#
#     app_page_name   Name of app-page without the leading "aa-" prefix,
#                     but with the ".apg" suffix.  A file with this
#                     this name will be created in the destination directory.
#                     If a file already exists, and is writeable,
#                     it will be overwritten
#
#   The informix environment variables are assumed to be set before calling
#   this script.
#
# Returns:
#  0   No errors were detected, app page was unloaded.
#  >0  Errors were encountered.  Check error messages.
#

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
    
    system("echo '$sql' | dbaccess $globalDbName - > $stdoutFile 2> $stderrFile");

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
        # ignore "Database selected.", "Database closed.",  and blank lines
        if (! ($line =~ /^Database selected\.\s*$|^Database closed\.\s*$|^\s*$/)) {
            # An interesting line was found.  If its a row retrieved message
            # than get the value.

            if ($line =~ /^No rows (found|unloaded)\.\s*$/) {
                $nRowsFound = 0;
            }
            elsif ($line =~ /^\d+ row\(s\) (retrieved|unloaded)\.\s*$/) {
                my @tokens = split(/\s+/, $line);
                $nRowsFound = $tokens[0];
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
# Get an app page from the database, and put it in a file.
#
# Params
#  $   $appPage  Name of app page to get.  This is without the "aa-"
#                prefix, but with the ".apg" suffix.
#  $   $destFile Name of file to put app page into in the working directory.
#
# Returns ()

sub getAppPage($$) {

    my $appPage = $_[0];
    my $destFile = $_[1];
    my $fileName = "$globalTmpDir/$appPage";
    my $unloadFile = "$fileName.unload";

    my $sql = "unload to \"$unloadFile\" "
	    . "  select object from webpages where id = \"aa-$appPage\";";
    execSql($sql, $fileName);

    my $nRows = getNRowsReturned($fileName);
    if ($nRows == 0) {
	# no rows unloaded.  App page does not exist.
	logError("App page \"$appPage\" does not exist.");
    }
    elsif ($nRows == 1) {
	# app page unloaded.  Reformat app page from unload file.

	open(UNLOADFILE, $unloadFile) 
	    or die "Unable to open unload file \"$unloadFile\"";

	open(OUTFILE, ">$destFile")
	    or die "Unable to open \"$destFile\" for writing.";

	my $line;
	my $eofComing = 0;
	
	while ($line = <UNLOADFILE>) {

	    if ($eofComing) {
		# next line should have been EOF and wasn't.
		die "Line in unload file contains an unexpected "|" character" .
		    " at end of line.  Script needs to be rewritten to deal" .
			" with this.";
	    }
	    $line =~ s/\\\n/\n/g;  # replace escaped newlines with newline
	    $line =~ s/\\\\/\\/g;  # replace escaped backslashes with backslash
	    $line =~ s/\\\|/\|/g;  # replace escaped bars with bars

	    if ($line =~ /\|$/) {
		# line ends in a |. Remove it.  Next line had 
		# damn well better be EOF.
		$line =~ s/\|$//;
		chomp($line);
		$eofComing = 1;
	    }
	    print(OUTFILE $line);
	}
	close(UNLOADFILE);
	close(OUTFILE);
    }
    return ();
}





#------------------------------------------------------------------------
# Main.
#

use Cwd;


#
# Define GLOBALS
#

$globalErrorCount = 0;
$globalDbName = "";
$globalTmpDir = "/tmp/unloadapppage.$PROCESS_ID";
$globalStderrExt = "err";
$globalStdoutExt = "out";
$globalDestDir = cwd();


# process args

if ($ARGV[0] eq "-d") {
    $globalDestDir = $ARGV[1];
    shift(@ARGV); 
    shift(@ARGV);
}

$globalDbName = shift(@ARGV);

my $dirPerms = oct(770);
mkdir($globalTmpDir, $dirPerms);

my $appPage;
foreach $appPage (@ARGV) {
    getAppPage($appPage, "$globalDestDir/$appPage");
}
system("rm -r $globalTmpDir");

exit ($globalErrorCount);
