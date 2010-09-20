#!/private/bin/perl
#------------------------------------------------------------------------
# 
# Script to unload a ZFIN Database, either just because, or in preparation
# to loading that database into another Informix server with the loaddb.pl
# script.
#
# Usage:
#  
#  unloaddb.pl dbname outputdir
#
#     dbname          Name of the database to unload.
#
#     outputdir       Name of the directory to put the output files in.
#                     A *lot* of output files will be placed in this directory.
#                     Any files in this directory may be overwritten by this 
#                     script.  If this directory does not exist, it will be
#                     created.
#
#   The informix environment variables are assumed to be set before calling
#   this script.
#
# Returns:
#  0   No errors were detected, app page/s was/were loaded.
#  >0  Errors were encountered.  Check error messages.
#
# NOTE: This was not the first attempt at this script.  I first tried to do
#  this with onunload/onload.  However, they don't support extended datatypes,
#  but they do appear to work, but actually onload silently corrupts the
#  destination server (in a very big way).  I then tried dbexport/dbimport.
#  This worked but had the side effect of exclusively locking the source 
#  database for several minutes while dbexport ran.  Finally, I looked at
#  the High-Performance Loader (HPL/ipload/onpload).  HPL actually works
#  similar to this script, but requires manual intervention whenever the
#  schema definition changes.
# 
#  Therefore this script uses the very low key UNLOAD SQL command and the
#  equally low key dbload utility.
#
# $Id: unloaddb.pl,v 1.20 2007-07-23 19:50:16 staylor Exp $

use English;
use DBI;

#------------------------------------------------------------------------
# Log message.  Writes a message to output.
#
# Params 
#  @      List of lines to print out, in order.
#
# Returns ()

sub logMsg(@) {

    my $line;
    my $date = `date`;
    chop($date);

    foreach $line (@_) {
        print(STDOUT "$date " . $line . "\n");
    }
    return ();
}




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
#  $   $dbName   Name of database to execute SQL in.
#  $   $sql      SQL statement to execute.
#  $   $fileBase Base name of files to put results in in the working 
#                directory.
#
# Returns ()

sub execSql($$$) {

    my $dbName   = $_[0];
    my $sql      = $_[1];
    my $fileBase = $_[2];

    my $stderrFile = "$fileBase.$globalStderrExt";
    my $stdoutFile = "$fileBase.$globalStdoutExt";
    
    system("echo '$sql' | dbaccess $dbName - > $stdoutFile 2> $stderrFile");

    return ();
}

#------------------------------------------------------------------------
# Execute update statistics on the database passed in.
#
# Params 
#  $   $dbName   Name of database to update stats in.
#
# Returns ()

sub execUpdateStats($) {

    my $dbName   = $_[0];
   
    system("echo 'update statistics high' | dbaccess $dbName");

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
# Create schema file by calling dbaccess
#
# Params 
#  $   $dbName      Name of database to generate schema for
#  $   $schemaFile  Name of file to put schema into.
#
# Returns
#  0     if successful
#  non-0 if unsuccessful

sub createSchema($$) {

    my $dbName     = $_[0];
    my $schemaFile = $_[1];
 
    my $retStatus = system("dbschema -d $dbName -ss $schemaFile");

    if ($retStatus) {
	logError("dbschema return status was $retStatus.  Failed to generate"
		 . " schema.");
    }
    return $retStatus;
}




#------------------------------------------------------------------------
# Populate the @globalTableNames and @globalTableNCols arrays with the 
# names of each table and the number of columns in each table in the DB.
#
# Note that @globalTableNCols is not currently (2000/09) used.  It would 
# be useful if the dbload utility were used to load data, but we currently
# use SQL LOAD commands to do that.
#
# Params 
#  $   $dbName      Name of database to get table info for.
#
# Returns
#  0     if successful
#  non-0 if unsuccessful

sub createTableLists($) {

    my $dbName = $_[0];
    my $tableListLog = "tableListLog";
    my $retStatus = 0;

    # filter out views and synonyms and all data dictionary table except for
    # these 3:
    #   syserrors
    #   systraceclasses
    #   systracemsgs
    # Records are inserted directly into these tables using SQL, and the web 
    # datablade installation process does just that.  However, when we load 
    # a DB that was dumped using this script, we don't install web datablade.
    # We mostly get it for free when we load the schema.  These 3 tables are
    # the parts we don't get for free.
    #
    # NOTE: there is equivalent SQL in loaddb.pl.  If the query is modified
    #       here it should also be modified there.

    my $sql = 
	"select tabname, ncols "
	. "from systables "
	. "where tabtype = \"T\" "  # filter out views & synonyms
	. "  and (tabid >= 100 or " # filter out data dictionary tables.
        . "       tabname in (\"syserrors\","
        . "                   \"systraceclasses\","
	. "                   \"systracemsgs\")) "
	. "order by tabname";
    
    execSql($dbName, $sql, $tableListLog);

    if (getNRowsReturned($tableListLog) > 0) {
	# query successful.  Get table names and columns counts.

	open(TABLELOG, "$tableListLog.$globalStdoutExt")
	    or die "Unable to open $tableListLog.$globalStdoutExt";

	# 3 types of lines: Blanks, tabnames, ncols
	my $line;

	while ($line = <TABLELOG>) {
	    if (! ($line =~ /^\s*$/)) {
		# line is non-blank
		if ($line =~ /^tabname/) {
		    my @tokens = split(/\s+/, $line);
		    push(@globalTableNames, $tokens[1]);
		}
		elsif ($line =~ /^ncols/) {
		    my @tokens = split(/\s+/, $line);
		    push(@globalTableNCols, $tokens[1]);
		}
		else {
		    logError("Unexpected line in $tableListLog: $line");
		    $retStatus = -1;
		}
	    }
	}
	close(TABLELOG);
    }
    else {
	$retStatus = -1;
    }
    return $retStatus;
}




#------------------------------------------------------------------------
# Create the load SQL files.
#
# Params 
#  $   $dbName       Name of database to get table info for.
#  $   $loadFile     Name of file to put the load SQL commands in.
#
# Returns
#  0     if successful
#  non-0 if unsuccessful

sub createLoadFile($$) {

    my $dbName       = $_[0];
    my $loadFile     = $_[1];
    my $tableName;

    open(LOAD, ">$loadFile")
	or die "Unable to open $loadFile";

    # try to avoid problems with lock contention by waiting it out.
    # wait for up to 60 seconds, under the assumption that we are willing
    # to wait 60 seconds to save a load that may have already run for
    # half an hour.

    print(LOAD "SET LOCK MODE TO WAIT 60;\n");

    foreach $tableName (@globalTableNames) {

	print(LOAD
	      "load from \"$tableName\" insert into $tableName;\n");
    }
    close(LOAD);

    return 0;
}




#------------------------------------------------------------------------
# Unload the data from the source database.
#
# Params 
#  $   $dbName       Name of database to get table info for.
#
# Returns
#  0     if successful
#  non-0 if unsuccessful

sub executeUnload($) {

    my $dbName = $_[0];
    my $username = "";
    my $password = "";

    foreach $tableName (@globalTableNames) {

	my $column_list = "";
	my $unloadFile = "$tableName";
	my $unloadTableLog = "unload_" . $tableName . "_log";

	my $dbh = DBI->connect ("DBI:Informix:$dbName", $username, $password) 
	    or die "Cannot connect to Informix database: $DBI::errstr\n";

	my $cur = $dbh->prepare ("select s.colname 
		from sysindexes i, sysconstraints c, syscolumns s, systables t
		where i.idxname = c.idxname
		and s.colno = i.part1
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
        union
	  select s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part2
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part3
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part4
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part5
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part6
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
  	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part7
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part8
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s, systables t
		where i.idxname = c.idxname
		and s.colno = i.part9
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part10
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	 select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part11
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part12
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part13
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s, systables t
		where i.idxname = c.idxname
		and s.colno = i.part14
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part15
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid
		and t.tabname = '$tableName'
	union
	  select  s.colname 
		from sysindexes i, sysconstraints c, syscolumns s,systables t
		where i.idxname = c.idxname
		and s.colno = i.part16
		and s.tabid = i.tabid
		and c.constrtype = 'P'
		and i.tabid = t.tabid 
		and t.tabname = '$tableName';") or die "DBI error";

	$cur->execute() or die "execute DBI error";
	my $pk_column;

	$cur->bind_columns(\$pk_column) or die "bind_columns DBI error";
	while ($cur->fetch){
	    $column_list = $column_list.$pk_column.',';
	}

	if ($column_list !~ m/\,/) { # if there are no PKs in the table, 
	    # then column_list will never get a comma, if there are no commas, 
	    # then there are no primary keys and so we don't want to have
	    # and order by clause in the unload statement.

	    my $sql = "unload to \"$unloadFile\" select * from $tableName" ;

	    execSql($dbName, $sql, $unloadTableLog);

	    if (getNRowsReturned($unloadTableLog) < 0) {
		# Serious error hit.
		logError("Aborting unload. $tableName");
		return -1;		# !!! EARLY RETURN !!!
	    }

	}

	else { # if there is a comma in the column_list variable, then there is at least 1 column
	       # in the PK for that table.

	    # remove trailing comma 
	    $column_list =~ s/\,\z// ;

	    my $sql = "unload to \"$unloadFile\" select * from $tableName order by $column_list;" ;

	    execSql($dbName, $sql, $unloadTableLog);

	    if (getNRowsReturned($unloadTableLog) < 0) {
		# Serious error hit.
		logError("Aborting unload. $tableName");
		return -1;		# !!! EARLY RETURN !!!
	    } 

	} # end else $column_list is not null

    } # end for each table in globalTables

    # BLOBs are unloaded into files that start with "blob".  For some
    # reason, those files are created with no group permissions.
    # change them

    system("chmod g+r blob*");

    return 0;
}




#------------------------------------------------------------------------
# Main.
#
# The unload process in a nutshell:
# 1. Run dbschema to capture schema definition.
# 2. Compile list of tablenames in database
# 3. Create SQL to load data into each table.
# 4. Execute unload statement for each table.
#

# Set PDQPRIORITY to HIGH in the hopes that this will speed up the unload.

$ENV{PDQPRIORITY} = "HIGH";    # Take as much as you can.


#
# Define GLOBALS
#

$globalErrorCount = 0;
$globalStderrExt = "err";
$globalStdoutExt = "out";


my $dbName = shift(@ARGV);
my $outputDir = shift(@ARGV);


# loaddb.pl expects these files to have these names.

my $schemaFile      = "schemaFile.sql";
my $loadSqlFile     = "load.sql";

# Parallel arrays to contain Table names, and number of columns in tables.

@globalTableNames = ();
@globalTableNCols = ();


# Verify output directory, and then cd into it.

if (-e $outputDir) {
    if (-d $outputDir) {
	if (! -w $outputDir) {
	    die("Output Directory $outputDir not writeable.");
	}
    }
    else {
	die("$outputDir is not a directory.");
    }
}
else {
    if (! mkdir($outputDir, oct(750))) {
	die("Unable to create $outputDir");
    }
}
if (! (chdir $outputDir)) {
    die("Unable to cd into $outputDir.");
}

logMsg("Creating schema file...");
if (! createSchema($dbName, $schemaFile)) {
    logMsg("Creating list of tables to unload...");
    if (! createTableLists($dbName)) {
	logMsg("Creating SQL load command file...");
	if (! createLoadFile($dbName, $loadSqlFile)) {
	    # Add code to disable updates.  This code is only uncommented on
	    # runs where we are creating a dump that will be used to move
	    # the database from one machine to another.
	    # my $sql =
	    # 	 "update zdb_flag " .
	    #	 "  set zflag_is_on = \"t\", " .
	    #	 "      zflag_last_modified = CURRENT " .
	    #	 "  where zflag_name = \"disable updates\"";
	    #  logMsg($sql);
	    #  execSql($dbName, $sql, "disableUpdatesLog"); # had better work
	     # keep the following two lines after you re-comment out the
	     # above disable updates code.
	     logMsg("Unloading data...");
	     executeUnload($dbName);
	}
    }
}

# Finally, create a file in the directory that indicates the unload is 
# finished.  This is used by other scripts to detect that the unload is
# not in progress.

system("touch done");

&execUpdateStats($dbName) ;

logMsg("Finished with $globalErrorCount errors.");

exit ($globalErrorCount);
