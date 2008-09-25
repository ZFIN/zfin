#!/private/bin/perl -w
#------------------------------------------------------------------------
#
# Script to scan all occurrences of char and varchar columns in all tables 
# for condtions that we don't want to happen.  These conditions are those
# that can be fixed by the scrub_char() function.  Any columns reported by
# this script should be examined and either:
#  1. Added to this script's avoid list, in which case the script will no
#     longer check or report that column.
#  2. Identify how the bad data got in.
#     A. If it got in through a script then modify the script to prevent it
#        from happening again, and fix the data.
#     B. If it got in through the user interface then add update and insert
#        triggers on the column to always call scrub_char().
#
# Usage:
#   scrubscan.pl
#
# There are no arguments.
#
# NOTE: This really needs to be converted to use the PERL DBI at some point.

#------------------------------------------------------------------------
# Main.
#

use English;			# enable longer special variable names

#
# set environment variables
#

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

#
# Define GLOBALS
#

# ain't got none, everything in main. (put another way - they are all globals!)

my $database = "<!--|DB_NAME|-->";
my $tmpDir = "/tmp/scrubscan.$PROCESS_ID";
my $tablesFile  = "$tmpDir/tables";
my $columnsBase = "$tmpDir/columns";
my $resultsBase = "$tmpDir/results";
my $dbaccess = "<!--|INFORMIX_DIR|-->/bin/dbaccess";

my $dirPerms = oct(770);
mkdir($tmpDir, $dirPerms);

# Define tests:
# 3 items: Test Name:Apply to Char columns?:Condition
my @tests = 
  ("LeadSpace:Y:column like \" %\" and column <> \" \"",
   "DoublSpace:Y:column like \"_%  %_\" and column <> \" \"",
   "AllSpace:Y:column = \" \" and octet_length(column) > 0",
   "EmptyStrng:N:octet_length(column) = 0",
   # "Newlines:Y:column like \"%",
   # %\"", 
   # "Tabs:Y:column like \"%	%\"",
   "TrailSpace:N:length(column) <> octet_length(column) and column <> \" \"",
   "ScrubChar:Y:column <> scrub_char(column)");

# Define columns to avoid:
# 2 items: table name:column name

my %avoids = (
    "acc_ref_gid_eid:ref" => "avoid",	# causes syntax errors
    "db_link:dblink_info" => "avoid",   # not displayed anywhere
    "defline:dfln_abbrev" => "avoid",   # not really our data
    "defline:dfln_name"   => "avoid",     # not really our data
    "defline:dfln_defline" => "avoid",  # not really our data
    "fest_lib_inst:fli_lib" => "avoid",	# Tom C says leave it be.
    "probe_lib:pl_lib_name" => "avoid",	# Tom C says leave it be.
    "probe_lib:pl_develop_stage" => "avoid",	# Tom C says leave it be.
    "probe_library:probelib_name" => "avoid",	# Tom C says leave it be.
    "probe_library:probelib_restriction_sites" => "avoid", # Tom C says leave it be.
    "xpat_anatomy_capture:xac_anatitem_name" => "avoid" # bad user input 
);

# get the names of all the tables in the database. Ignore views and
# synonyms.

my $sql = "select tabname "
        . "  from systables "
        . "  where tabtype = \"T\" "
        . "    and tabid >= 100 "
        . "    and tabname not like \"sysbld%\" "
        . "  order by tabname;";

system("echo '$sql' | $dbaccess $database - > $tablesFile 2>/dev/null");

open(TABLESFILE, $tablesFile) 
    or die "Unable to open $tablesFile";

my $firstLineOut = 1;

while (my $line = <TABLESFILE>) {
    # not at EOF.  Pay attention only to lines that start with "tabname"
    if ($line =~ /^tabname/) {
	my @tokens = split(/\s+/, $line);
	my $tableName = $tokens[1];
	# get columns from this table, that are char or varchar
	# column types are for pg 1-29 of SQL Reference.

	$sql = "select colname, coltype from syscolumns sc, systables st "
	    . " where st.tabname = \"$tableName\" "
	    . "   and st.tabid = sc.tabid "
	    . "   and sc.coltype in (0, 13, 256, 269)";
	my $columnsFile = "$columnsBase.$tableName";
	system("echo '$sql' | $dbaccess $database - > $columnsFile 2>/dev/null");

	# open columns file and read in character columns.
	open(COLUMNSFILE, $columnsFile) 
	    or die "Unable to open $columnsFile";
	my @cols;
	while (my $colLine = <COLUMNSFILE>) {
	    # not at EOF.  Use only lines that start with "colname"
	    if ($colLine =~ /^colname/) {
		my $colName = (split(/\s+/, $colLine))[1];
		$colLine = <COLUMNSFILE>;
		my $colType = (split(/\s+/, $colLine))[1];
		if (! $avoids{"$tableName:$colName"}) {
		    foreach $test (@tests) {
			($testName,$applyToChar,$condition) = split(/:/,$test);
			if ($applyToChar eq "Y" || $colType == 13 || $colType == 269){
			    $condition =~ s/column/$colName/g;
			    $sql = "select $colName from $tableName where $condition";
			    my $resultsFile = "$resultsBase.$tableName";
			    my $msgFile = "$resultsBase.$tableName.messages";
			    system("echo '$sql' | $dbaccess $database - > $resultsFile 2>$msgFile ");
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
				if ($firstLineOut) {
				    $firstLineOut = 0;
				    print("                                                                      # Rows\n");
				    print("Table Name                  Column Name                    Test       Failed\n");
				    print("--------------------------- ------------------------------ ---------- ------\n");
				}
				printf("%27s %-30s %-10s %6d\n", $tableName, $colName, $testName, $nRowsFound);
			    } # end if rows found
			} # end if testing this column
		    } # end foreach test
		} # end if not avoiding this column
	    } # end if column name found
	} # end while more lines in column file.
	close(COLUMNSFILE);
    } # end if found a table name
} # end while still more input from tables file
close (TABLESFILE);

system("/bin/rm -r $tmpDir");

exit 0;
