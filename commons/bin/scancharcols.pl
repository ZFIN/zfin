#!/private/bin/perl -w
#------------------------------------------------------------------------
#
# Script to scan all occurrences of character columns in all tables 
# for a particular value.
#
# Usage:
#   scancharcols.pl [-like] [-lower] "dbname" "value1" ["value2" ...]
#
# The first argument is the name of the database to connect to.
# The remaining arguments are values to be searched for.
#
# The -like option will do a like comparison with the value.  The value
# should contain like wildcard characters, otherwise you'll just be doing
# the default equality comparison.
#
# The -lower option will wrap a lower() call around every column before 
# comparing it to value.  value is assumed to already be in lower case and is
# not converted.
#
# The Informix environment variables are assumed to be set when the script 
# is called.
#
# The program searches the entire DB for each value before moving on to 
# the next value.
#




#------------------------------------------------------------------------
# Main.
#

use English;			# enable longer special variable names

#
# Define GLOBALS
#

# ain't got none, everything in main. (put another way - their all globals!)


# Process like and lower options.

my $comparison = "=";
my $lowerOn = 0;

while ($ARGV[0] =~ /^-/) {
    if ($ARGV[0] =~ /^-like\s*$/) {
	$comparison = "like";
    }
    elsif ($ARGV[0] =~ /^-lower\s*$/) {
	$lowerOn = 1;
    }
    else {
	print("Unrecognized option: \"$ARGV[0]\".  Ignoring it.\n");
    }
    shift(@ARGV);
}

my $database = shift(@ARGV);
my $tmpDir = "/tmp/scancharcols.$PROCESS_ID";
my $tablesFile  = "$tmpDir/tables";
my $columnsBase = "$tmpDir/columns";
my $resultsBase = "$tmpDir/results";

my $dirPerms = oct(770);
mkdir($tmpDir, $dirPerms);


foreach $searchString (@ARGV) {

    print("Search string: \"$searchString\"\n");
    
    # get the names of all the tables in the database. Ignore views and
    # synonyms.

    my $sql = "select tabname from systables where tabtype = \"T\" order by tabname;";

    system("echo '$sql' | dbaccess $database - > $tablesFile 2>/dev/null");

    open(TABLESFILE, $tablesFile) 
	or die "Unable to open $tablesFile";

    while (my $line = <TABLESFILE>) {
	# not at EOF.  Pay attention only to lines that start with "tabname"
	if ($line =~ /^tabname/) {
	    my @tokens = split(/\s+/, $line);
	    my $tableName = $tokens[1];
	    # get columns from this table, that are char or varchar
	    # column types are for pg 1-24 of SQL Reference.

	    $sql = "select colname from syscolumns sc, systables st "
		 . " where st.tabname = \"$tableName\" "
		 . "   and st.tabid = sc.tabid "
		 . "   and (sc.coltype in (0, 13, 256, 269) or"
		 . "        (sc.coltype = 40 and exists "
		 . "          (select name "
		 . "             from sysxtdtypes sx "
                 . "            where sx.extended_id = sc.extended_id "
		 . "              and sx.name = \"lvarchar\")))";
	    my $columnsFile = "$columnsBase.$tableName";
	    system("echo '$sql' | dbaccess $database - > $columnsFile 2>/dev/null");

	    # open columns file and read in character columns.

	    open(COLUMNSFILE, $columnsFile) 
		or die "Unable to open $columnsFile";
	    my @cols;
	    while (my $colLine = <COLUMNSFILE>) {
		# not at EOF.  Use only lines that start with "colname"
		if ($colLine =~ /^colname/) {
		    my $colName = (split(/\s+/, $colLine))[1];
		    if ($lowerOn) {
			push(@cols, "lower($colName) $comparison \"$searchString\"");
		    }
		    else {
			push(@cols, "$colName $comparison \"$searchString\"");
		    }
		}
	    }
	    close(COLUMNSFILE);

	    if ($#cols >= 0) {
		$condList = join(" OR ", @cols);
		$sql = "select * from $tableName where $condList";
		my $resultsFile = "$resultsBase.$tableName";
		my $msgFile = "$resultsBase.$tableName.messages";
		system("echo '$sql' | dbaccess $database - > $resultsFile 2>$msgFile ");
		# open message file.  We care about 2 things:
		#  The number of rows selected, and
		#  error messages.
		my $nRowsFound =0;
		
		open(MSGFILE, $msgFile)
		    or die "Unable to open $msgFile";
		my $msgLine;
		my $tableError = 0;

		while($msgLine = <MSGFILE>) {
		    # ignore "Database selected.", "Database closed.",
		    # "No rows found", and blank lines
		    if (! ($msgLine =~ /^Database selected\.\s*$|^Database closed\.\s*$|^No rows found\.\s*$|^\s*$/)) {
			# an interesting line was found. If it's a 
			# rows retrieved message than read results file.
			# Otherwise print out line.
			if ($msgLine =~ /^\d+ row\(s\) retrieved\.\s*$/) {
			    @msgTokens = split(/\s+/, $msgLine);
			    $nRowsFound = $msgTokens[0];
			}
			else {
			    if (! $tableError) {
				print("\n\n===========================================\n");
				print("\nError with $tableName\n\n");
				$tableError = 1;
			    }
			    print($msgLine);
			}
		    }
		}
		
		if ($nRowsFound) {
		    # write tablename and rest of results to output.
		    print("\n\n===========================================\n");
		    print("\nTable: $tableName\n$nRowsFound row(s) matched.");

		    open(RESULTSFILE, $resultsFile) 
			or die "Unable to open $resultsFile";
		
		    my $resLine;
		    while($resLine = <RESULTSFILE>) {
			print("$resLine");
		    }
		    close(RESULTSFILE);
		}
	    }
	} # end if found a table name
    } # end while still more input from tables file
    close (TABLESFILE);

} # end for each search string

system("rm -r $tmpDir");

exit 0;
