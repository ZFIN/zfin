#!/private/bin/perl
#------------------------------------------------------------------------
# 
# Script to load a ZFIN database from a files that were created by unloaddb.pl
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# If a database already exists in the Informix server being loaded into, 
# then that database will be removed and replaced with the one being loaded.
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#
# Usage:
#  
#  loaddb.pl [-b ] dbname inputdir  
#
#                     This option can only be used on non-production databases.
#     dbname          Name to load the database as.  If a database with this
#                     name already exists in the target Informix server, THEN
#                     IT WILL BE DELETED AND REPLACED WITH THE DATABASE 
#                     DEFINED IN inputdir
#
#     inputdir        Directory containing the files created by 
#                     unloaddb.pl.  
#
#   The informix environment variables are assumed to be set before calling
#   this script.
#
# Returns:
#  0   No errors were detected, database was loaded.
#  >0  Errors were encountered.  Check error messages.
#
#
# Note also, that this script will only run on kinetix if running as user
#      informix.
#
# After running this script you must also make the postloaddb target.
#
# $Id: loaddb.pl,v 1.26 2007-12-31 23:37:59 staylor Exp $
# $Source: /research/zusers/ndunn/CVSROOT/Commons/bin/loaddb.pl,v $


use English;
use Getopt::Long;

#------------------------------------------------------------------------
# Log message.  Writes a message to output.
#
# Params 
#  @      List of lines to print out, in order.
#
# Returns ()

sub logMsg(@) {

    my $line;
    my $date = `/bin/date`;
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
    
    system("echo '$sql' | $ENV{INFORMIXDIR}/bin/dbaccess $dbName - > $stdoutFile 2> $stderrFile");

    return ();
}




#------------------------------------------------------------------------
# Execute SQL statements from a file and save the results in other files.
#
# Params 
#  $   $dbName   Name of database to execute SQL in.
#  $   $sqlFile  File containing SQL statements to execute.
#  $   $fileBase Base name of files to put results in in the working 
#                directory.
#
# Returns ()

sub execSqlFile($$$) {

    my $dbName   = $_[0];
    my $sqlFile  = $_[1];
    my $fileBase = $_[2];
    my $stderrFile = "$fileBase.$globalStderrExt";
    my $stdoutFile = "$fileBase.$globalStdoutExt";
 
    system("$ENV{INFORMIXDIR}/bin/dbaccess $dbName $sqlFile > $stdoutFile 2> $stderrFile");

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
# Check the return status from an SQL that just does a connect.
#
# Standalone connect statements are just meant to test the existense 
# of a database.
#
# Params
#  $   $fileBase Base name of stdout and stderr files that the results of 
#                the connect were put in.
#
# Returns 
#   0  Connected.  Database exists
#  !=0 Did not connect.  Database probably does not exist.  This is not
#      considered an error condition.
#  Does not return if an unexpected condition is encountered.

sub checkConnect($) {

    my $fileBase = $_[0];
    my $fileName = "$fileBase.$globalStderrExt";

    # open file.  Look for strings that indicate connection status:
    #  "Database selected.", or
    #  "  329: Database not found or no system permission."

    open(CONNECTFILE, $fileName)
        or die "Unable to open $fileName";
    my $line;
    my $connected = 0;
    my $unknown = 1;

    while($unknown and $line = <CONNECTFILE>) {
        # Ignore blank lines
        if (! ($line =~ /^\s*$/)) {

	    if ($line =~ /^Database selected\.\s*$/) {
		$connected = 1;
		$unknown = 0;
	    }
	    elsif ($line =~ /^\s*329: Database not found or no system permission\.\s*$/) {
		$connected = 0;
		$unknown = 0;
	    }
	    else {
		# Serious problem.  If one of the above lines isn't first
		# in the file then we got something unexpected.  Send rest of
		# file as an error message
		do {
		    logError("Unexpected line encountered:", $line);
		} while ($line = <CONNECTFILE>);
		die("Terminating load.");
            }
        }
    }
    if ($unknown) {
	# Shouldn't be able to happen.
	die("Connect status file $fileName was unexpectedly empty.  " .
	    "Terminating.");
    }

    close(CONNECTFILE);
    return (! $connected)
}




#------------------------------------------------------------------------
# Check the return status from a drop DB SQL statement
#
# Params
#  $   $fileBase Base name of stdout and stderr files that the results of 
#                the drop db statement were put in.
#
# Returns 
#   0  Database dropped
#  !=0 Error encountered and logged.  Database was probably not dropped.

sub checkDrop($) {

    my $fileBase = $_[0];
    my $fileName = "$fileBase.$globalStderrExt";

    # open file.  It should contain only blank lines and "Database dropped."

    open(DROPFILE, $fileName)
        or die "Unable to open $fileName";
    my $line;
    my $dropped = 0;
    my $dropError = 0;

    while((! $dropped) and $line = <DROPFILE>) {
        # Ignore blank lines
        if (! ($line =~ /^\s*$/)) {

	    if ($line =~ /^Database dropped\.\s*$/) {
		$dropped = 1;
	    }
	    else {
		$dropError = 1;   # Serious problem.
		logError("Unexpected line encountered:", $line);
            }
        }
    }
    close(DROPFILE);

    if (! $dropped) {
	logError("Database not dropped.");
	return 1;
    }
    else {
	return $dropError;
    }
}




#------------------------------------------------------------------------
# Check the return status from a create DB SQL statement
#
# Params
#  $   $fileBase Base name of stdout and stderr files that the results of 
#                the create db statement were put in.
#
# Returns 
#   0  Database created
#  !=0 Error encountered and logged.  Database was probably not created

sub checkCreateDb($) {

    my $fileBase = $_[0];
    my $fileName = "$fileBase.$globalStderrExt";

    # open file.  It should contain only blank lines and "Database created.",
    # and "Database closed.".

    open(CREATEDBFILE, $fileName)
        or die "Unable to open $fileName";
    my $line;
    my $created = 0;
    my $createError = 0;

    while((! $created) and $line = <CREATEDBFILE>) {
        # Ignore blank lines & database closed
        if (! ($line =~ /^\s*$|^Database closed\.\s*$/)) {

	    if ($line =~ /^Database created\.\s*$/) {
		$created = 1;
	    }
	    else {
		$createError = 1;   # Serious problem.
		logError("Unexpected line encountered:", $line);
            }
        }
    }
    close(CREATEDBFILE);

    if (! $created) {
	logError("Database not created.");
	return 1;
    }
    else {
	return $createError;
    }
}


#------------------------------------------------------------------------
# Check the return status from loading the schema
#
# Params
#  $   $fileBase Base name of stdout and stderr files that the results of 
#                the schema load were put in.
#
# Returns 
#   0  Schema loaded successfully.
#  !=0 Error encountered and logged.  Not all of schema loaded successfully.

sub checkLoadSchema($) {

    my $fileBase = $_[0];
    my $fileName = "$fileBase.$globalStderrExt";

    open(LOADSCHEMAFILE, $fileName)
        or die "Unable to open $fileName";
    my $line;
    my $loadError = 0;

    while($line = <LOADSCHEMAFILE>) {

	# There are so many valid strings that can occur.  Scan only for
	# lines that look like errors.  Errors are usually of the form
	#   nnn: Text
	# where nnn is an integer.  nnn may be preceded by whitespace.

        if ($line =~ /^\s*\d+: /) {
	    # Looks like an error
	    $loadError = 1;   # Serious problem.
	    logError("Unexpected line encountered:", $line);
        }
    }
    close(LOADSCHEMAFILE);

    if ($loadError) {
	logError("Schema load failed. See $fileBase.* for details.");
    }
    return $loadError;
}




#------------------------------------------------------------------------
# Check the return status of executing pre or post load file.
#
# Params
#  $   $fileBase Base name of stdout and stderr files that the results of 
#                the pre or post load statements were put in.
#
# Returns 
#   0  Database created
#  !=0 Error encountered and logged.  Database is in a mixed state.

sub checkPreOrPostLoad($) {

    my $fileBase = $_[0];
    my $fileName = "$fileBase.$globalStderrExt";

    open(PLOADFILE, $fileName)
        or die "Unable to open $fileName";
    my $line;
    my $ploadError = 0;

    while($line = <PLOADFILE>) {
        # Ignore blank lines, database selected, mode set, and database closed
	# There is also an update statistics high at the end of the post load
	#  script, so handle that too.
        if (! ($line =~ /^\s*$|^Mode set\.\s*$|^Database selected\.\s*$|^Database closed\.\s*$|^Statistics updated\.\s*$/)) {
	    $ploadError = 1;   # Serious problem.
	    logError("Unexpected line encountered:", $line);
	}
    }
    close(PLOADFILE);

    return $ploadError;
}




#------------------------------------------------------------------------
# Check the return status of load statements
#
# Params
#  $   $fileBase Base name of stdout and stderr files that the results of 
#                the load statements were put in.
#
# Returns 
#   0  All tables loaded successfully
#  !=0 Error encountered and logged.  Some or all tables did not load 
#      successfully.

sub checkLoad($) {

    my $fileBase = $_[0];
    my $fileName = "$fileBase.$globalStderrExt";

    open(LOADFILE, $fileName)
        or die "Unable to open $fileName";
    my $line;
    my $loadError = 0;

    while($line = <LOADFILE>) {
        # Ignore blank lines, database selected, rows loaded, lock mode set,
	#  and database closed lines
        if (! ($line =~ /^\s*$|^\d+ row\(s\) loaded\.\s*$|^Database selected\.\s*$|^Lockmode set\.\s*$|^Database closed\.\s*$/)) {
	    $loadError = 1;   # Serious problem.
	    logError("Unexpected line encountered:", $line);
	}
    }
    close(LOADFILE);

    return $loadError;
}

#------------------------------------------------------------------------
# Check the return status from a drop exclude tables SQL statement
#
# Params
#  $   $fileBase Base name of stdout and stderr files that the results of 
#                the drop db statement were put in.
#
# Returns 
#   0  Tables dropped
#  !=0 Error encountered and logged. Tables were probably not dropped.

sub checkDropExclude($) {

    my $fileBase = $_[0];
    my $fileName = "$fileBase.$globalStderrExt";

    # open file.  It should contain only blank lines, "Table created (recreated
    # excluded tables for use with fxns and procedures) and "Table dropped."

    open(DROPFILE, $fileName)
        or die "Unable to open $fileName";
    my $line;
    my $dropError = 0;
  
  
    while($line = <DROPFILE>) {
      # Ignore blank lines, database selected, rows loaded, table dropped,
      # table created,and database closed lines
      if (! ($line =~ /^\s*$|^Table dropped\.\s*$|^Table created\.\s*$|^Database selected\.\s*$|^Database closed\.\s*$/)) {
	$dropError = 1;   # Serious problem.
	logError("Unexpected line encountered:", $line);
      }
    }
    close(DROPFILE);

    return $dropError;
}
 

#------------------------------------------------------------------------
# Restarts apache.  This is not called on kinetix.
#
# Params
#  none
#
# Returns
#   0  Returns 0 no matter what.  restartApache.pl does not have a 
#      return status.

sub restartApache() {

    system("$globalBinDir/restartapache.pl > /dev/null");

    return 0;			# success, guaranteed
}



#------------------------------------------------------------------------
# Drops the old database as a precursor to loading the new one.
#
# Params
#  $   $dbName       Name of database.  If it exists, it will be dropped by
#                    this routine.
#
# Returns
#   0  Database successfully dropped, or database did not exist.
#  !=0 Problem encountered.  Error has been logged.

sub dropDb($) {

    my $dbName = $_[0];

    # first determine if database already exists.

    my $sql = "";		# empty string, just test connect
    my $connectLog = "$globalTmpDir/connectLog";

    execSql($dbName, $sql, $connectLog);
    if (! checkConnect($connectLog)) {
	# connect worked, database exists, need to drop it
	$sql = "drop database $dbName";
	my $dropLog = "$globalTmpDir/dropLog";
	execSql("", $sql, $dropLog);
	if (checkDrop($dropLog)) {
	    return -1;		# !!!! MULTIPLE RETURNS !!!!
	}
    }
    return 0;			# success
}



#------------------------------------------------------------------------
# Create DB in destination Informix server.
#
# Params
#  $   $dbName       Name of database to create.
#  $   $schemaFile   File containing schema defininition.
# 
# Returns
#   0  Database successfully created
#  !=0 Problem encountered.  Error has been logged.

sub createDb($$) {

    my $dbName = $_[0];
    my $schemaFile = $_[1];

    # Create database without logging (it will get turned on later).
    # Create it in defaultdbs.  This will cause tables, implicit indexes
    # (FKs and PKs without prior explicit indexes) and explicit indexes
    # without specified dbspaces to be placed in defaultdbs.  Of course
    # we should never be creating tables or indexes without specifying
    # where they go.  However, people do.

    my $createDbLog = "$globalTmpDir/createDbLog";
    my $sql = "create database $dbName in defaultdbs;";
    execSql("", $sql, $createDbLog);
    if (checkCreateDb($createDbLog)) {
	return -1;		# !!! MULTIPLE RETURNS !!!!
    }

    # database created, now populate it with tables, indexes, etc.

    my $loadSchemaLog = "$globalTmpDir/loadSchemaLog";
    
    execSqlFile($dbName, $schemaFile, $loadSchemaLog);
    return checkLoadSchema($loadSchemaLog);
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
    my $tableListLog = "$globalTmpDir/tableListLog";
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
    # NOTE: there is equivalent SQL in unloaddb.pl.  If the query is modified
    #       here it should also be modified there.  There is also another
    #       area of this file where these 3 tables are listed again.

    my $sql = 
	"select tabname, ncols "
	. "from systables "
	. "where tabtype = \"T\"  " # filter out views & synonyms
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
# Generates either 
#  a list of FOREIGN-KEY constraint names for the given table, or 
#  a list of NON-FOREIGN-KEY constraint names for the given table. 
#
# Params 
#  $   $dbName      Name of database to get table info for.
#  $   $tableName   Name of table to get non-foreign key constraints for.
#  $   $fkRequest   True if requesting a list of foreign key constraints, 
#                   False if requesting a list of non-foreign key constraints
#
#
# Returns
#  List of names of non-foreign key constraints for the table
#  -1   An error was hit.

sub createConstraintList($$$) {

    my $dbName = $_[0];
    my $tableName = $_[1];
    my $fkRequest = $_[2];
    my $fkRelop;
    if ($fkRequest) {
	$fkRelop = "=";
    }
    else {
	$fkRelop = "<>";
    }
    my $constrListLog = "$globalTmpDir/constraintListLog";
    my $retStatus = 0;
    my @constrList = ();

    my $sql = 
	"select constrname "
	. "from sysconstraints "
	. "where constrtype $fkRelop \"R\" "
	. "  and tabid = (select tabid "
	. "                 from systables "
        . "                 where tabname = \"$tableName\");";

    execSql($dbName, $sql, $constrListLog);
   
    if (getNRowsReturned($constrListLog) > 0) {
	# query successful.  Get constraint names

	open(CONSTRLOG, "$constrListLog.$globalStdoutExt")
	    or die "Unable to open $constrListLog.$globalStdoutExt";

	# 2 types of lines: Blanks, constraint names
	my $line;

	while ($line = <CONSTRLOG>) {
	    if (! ($line =~ /^\s*$/)) {
		# line is non-blank
		if ($line =~ /^constrname/) {
		    my @tokens = split(/\s+/, $line);
		    push(@constrList, $tokens[1]);
		}
		else {
		    logError("Unexpected line in $constrListLog: $line");
		    return -1;
		}
	    }
	}
	close(CONSTRLOG);
    }
    return @constrList;
}




#------------------------------------------------------------------------
# Create the preload and postload SQL files.
#
# These can't be created on at the time of the unload because the names of 
# many constraints will be different in the source and destination databases.
#
# Params 
#  $   $dbName       Name of database to get table info for.
#  $   $preLoadFile  Full path of file to put the preload SQL command s in.
#  $   $postLoadFile Full path of file to put the postload SQL commands in.
#
# Returns
#  0     if successful
#  non-0 if unsuccessful

sub createPreAndPostLoadFiles($$$) {

    my $dbName       = $_[0];
    my $preLoadFile  = $_[1];
    my $postLoadFile = $_[2];
    my $tableName;

    open(PRELOAD, ">$preLoadFile")
	or die "Unable to open $preLoadFile";
    open(POSTLOAD, ">$postLoadFile")
	or die "Unable to open $postLoadFile";
    
    # Have to handle foreign key constraints in a special way.  Before 
    # loading they have to be disabled before the primary key constraints 
    # they are dependent on are disabled.  After loading they have to 
    # be enabled after the primary key constraints they are dependent on.

    # disable foreign key constraints first

    foreach $tableName (@globalTableNames) {
	# Get all FK constraints for this table.
	my @fkConstraintList = createConstraintList($dbName, $tableName, 1);
	if (0 == $#fkConstraintList && $fkConstraintList[0] eq "-1") {
	    return -1;		# error encountered   RETURN EARLY
	}
	elsif ($#fkConstraintList > -1) {
	    print(PRELOAD
		  "set constraints " . join(", ",@fkConstraintList) . 
		  " disabled;\n");
	}
    }

    foreach $tablename (@globalTableNames) {

	# exclude the data dictionary tables that are loaded.  We can't turn
	# off their constraints, triggers and indexes.

	if ($tablename ne "syserrors" &&
	    $tablename ne "systraceclasses" &&
	    $tablename ne "systracemsgs") {

	    # Disable everything remaining with blanket disable statements.
	    print(PRELOAD 
		  "set constraints, indexes, triggers for $tablename disabled;\n");
	    print(POSTLOAD 
		  "set indexes, triggers for $tablename enabled;\n");

	    # Turn on all non-FK constraints at this time, and then only 
	    # after all of them have been turned on, turn on the FK constraints

	    # Get all non-FK constraints for this table.
	    my @nonFkConstraintList = 
		                  createConstraintList($dbName, $tablename, 0);
	    if (0 == $#nonFkConstraintList && 
		$nonFkConstraintList[0] eq "-1") {
		return -1;		# error encountered   RETURN EARLY
	    }
	    elsif ($#nonFkConstraintList > -1) {
		print(POSTLOAD
		      "set constraints " . join(", ",@nonFkConstraintList) . 
		      " enabled;\n");
	    }
	}
    }

    # Now we need to turn on the foreign key constraints for each table that
    # has them.  Could scan the DB again, looking for foreign key contstraints
    # or we could just issue blanket enable statements.  Take the latter 
    # approach.

    foreach $tableName (@globalTableNames) {
	print(POSTLOAD 
	      "set constraints for $tableName enabled;\n");
    }

    # add an update statistics high to the postload for good measure.
    print(POSTLOAD "update statistics high;\n");

    close(PRELOAD);
    close(POSTLOAD);

    return 0;
}




#------------------------------------------------------------------------
# Execute preload SQL statements.  This consists of disabling constraints, 
# indexes, and triggers in the DB.
#
# Params
#  $   $dbName       Name of database.
#  $   $preLoadFile  File containing preload SQL statements
# 
# Returns
#   0  All preload statements successfuly executed.
#  !=0 Problem encountered.  Error has been logged.

sub preLoad($$) {

    my $dbName = $_[0];
    my $preLoadFile = $_[1];

    my $preLoadLog = "$globalTmpDir/preLoadLog";

    execSqlFile($dbName, $preLoadFile, $preLoadLog);
    return checkPreOrPostLoad($preLoadLog);
}





#------------------------------------------------------------------------
# Execute postload SQL statements.  This consists of enabling constraints, 
# indexes, and triggers in DB, and then updating statistics.
#
# Params
#  $   $dbName       Name of database.
#  $   $postLoadFile File containing postload statements.
# 
# Returns
#   0  All postload statements successfully executed.
#  !=0 Problem encountered.  Error has been logged.

sub postLoad($$) {

    my $dbName = $_[0];
    my $postLoadFile = $_[1];

    my $postLoadLog = "$globalTmpDir/postLoadLog";

    execSqlFile($dbName, $postLoadFile, $postLoadLog);
    return checkPreOrPostLoad($postLoadLog);
}




#------------------------------------------------------------------------
# Load the database
#
# Params
#  $   $dbName       Database will be loaded with this name.  This database
#                    must not exist at the time this routine is called.
#  $   $loadSqlFile  File containsing SQL commands to load data.
#
# Returns
#   0  All tables successfully loaded
#  !=0 Problem encountered.  Error has been logged.  Some tables have 
#      probably been loaded.

sub loadDb($$) {

    my $dbName = $_[0];
    my $loadSqlFile = $_[1];
    my $doLoadSqlFile = "$globalTmpDir/doload.sql";

    my $loadLog = "$globalTmpDir/loadLog";
    open DOLOAD, ">$doLoadSqlFile" or die "Cannot open the $doLoadSqlFile to write.";


    &execSqlFile($dbName, $loadSqlFile, $loadLog);
    
    return checkLoad($loadLog);
}




#------------------------------------------------------------------------
# Main.
#

$usage = "\nUsage: loaddb.pl dbname inputdir \n"
	  ."or give [-b] to create or refresh your copy of the blastdbs.\n";

if (@ARGV < 2) {
  print $usage and exit;
}

# process args

&GetOptions("b");

my $dbName = shift(@ARGV);
my $inputDir = shift(@ARGV);

#
# Define GLOBALS
#
# /tmp/loaddb/  needs to pre-exist which it does not after a reboot
if (-d "/tmp/loaddb"){
    print "/tmp/loaddb exists\n";
}
else {
    system ("mkdir -m 775 -p /tmp/loaddb");
    system ("chgrp fishadmin /tmp/loaddb");
    system ("chmod o-w /tmp/loaddb");
}
$globalErrorCount = 0;
$globalTmpDir = "/tmp/loaddb/loaddb.$PROCESS_ID";
$globalStderrExt = "err";
$globalStdoutExt = "out";
$globalBinDir = "/private/ZfinLinks/Commons/bin";

# unloaddb.pl expects these files to have these names.

my $schemaFile      = "schemaFile.sql";
my $loadSqlFile     = "load.sql";

my $preLoadSqlFile  = "$globalTmpDir/preLoad.sql";
my $postLoadSqlFile = "$globalTmpDir/postLoad.sql";

# Parallel arrays to contain Table names, and number of columns in tables.

@globalTableNames = ();
@globalTableNCols = ();

# Accept if on test, or if running on production as informix.

if ($ENV{HOST} =~ /kinetix/ && $ENV{USER} ne "informix") {
    die("Running on $ENV{HOST} not allowed unless logged in as informix.  " .
	"This script has the potential to obliterate existing databases " .
	"and this script will not risk it.");
}

if (! chdir($inputDir)) {
    die("Can't cd into $inputDir.");
}

system("/bin/rm -f doload.sql");
 
my $dirPerms = oct(770);
mkdir($globalTmpDir, $dirPerms);


# Execute a several step process:
# o If running on development restart apache
# o Drop the old Database
# o Create the new database.
# o Disable indexes, constraints, and triggers
# o Load the new Database
# o Enable indexes, constraints, and triggers.
# o Deal with the app pages in webpages table
# o If running on development, restart apache
# o Enable logging.

# Set PDQPRIORITY to HIGH.  This doesn't speed up the loading of the data, 
# but it really speeds up the enabling of the indexes and constraints.

$ENV{PDQPRIORITY} = "HIGH";    # Take as much as you can.


if ($ENV{HOST} =~ /kinetix/) {
    $ENV{PSORT_NPROCS} = 4;    # 4 CPUs, suck it all up
}
else {
    logMsg("Restarting Apache ...");
    restartApache();

    logMsg("stopping tomcat...");
    system("/private/ZfinLinks/Commons/bin/tomcat.sh stop");

    if  ($ENV{HOST} =~ /zygotix/) {
	$ENV{PSORT_NPROCS} = 3;    # 3 CPUs, leave a little for non-loaders
    }
    else {
	$ENV{PSORT_NPROCS} = 2;    # 2 CPUs, suck it all up
    }
}


logMsg("Dropping old database (if it exists)...");
dropDb($dbName);

logMsg("Defining new database...");
if (! createDb($dbName, $schemaFile)) {

    logMsg("Creating list of tables to load...");
    if (! createTableLists($dbName)) {   

	logMsg("Creating preload and postload scripts...");
	if (! createPreAndPostLoadFiles($dbName, $preLoadSqlFile,
					$postLoadSqlFile)) {

	    logMsg("Disabling indexes, constraints, and triggers...");
	    if (! preLoad($dbName, $preLoadSqlFile)) {

		logMsg("Loading data into database...");
		  
       		if (! loadDb($dbName, $loadSqlFile)) {

		    logMsg("Enabling indexes, constraints, and triggers...");
		    if (! postLoad($dbName, $postLoadSqlFile)) {
		
		      if ($ENV{HOST} !~ /kinetix/) {
			  # restart apache here because sometimes between
			  # the start of the load and here, people accidentally
			  # access thier web pages
			  logMsg("Restarting Apache ...");
			  restartApache();
		      }
		      logMsg("Enabling logging...");
		      if (system("$globalBinDir/enableLogging.pl $dbName")) {
			  logError("Failed to enable logging.");
		      }
		      if ($opt_b && $ENV{HOST} !~ /kinetix/) {
			  print "creating developer blastdb copy";
			  system("$ENV{TARGETROOT}/server_apps/DB_maintenance/makeDeveloperBlastDbs.sh");
		      }
		      
		      
		  }
		}
	    }
	}
    }
}

if (! $globalErrorCount) {
  print(STDOUT "WARNING!!!!  You MUST now cd to your ZFIN_WWW directory and type\n");
  print(STDOUT "WARNING!!!! \n");
  print(STDOUT "WARNING!!!!    % gmake postloaddb\n");
  print(STDOUT "WARNING!!!!\n");
  print(STDOUT "WARNING!!!!  Failure to do this results in very unpleasant\n");
  print(STDOUT "WARNING!!!!  behavior in your web site, and the ire of all\n");
  print(STDOUT "WARNING!!!!  your coworkers.\n\n");

  system("/bin/rm -r $globalTmpDir");
}

logMsg("Finished with $globalErrorCount errors.");

exit ($globalErrorCount);

