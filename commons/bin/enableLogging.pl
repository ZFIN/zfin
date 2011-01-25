#!/private/bin/perl -wT
#------------------------------------------------------------------------
# 
# Script to enable logging in an informix database.  The only 
# reason this script exists is because 
#  1. The loaddb.pl script loads databases with logging turned off (the load
#     goes much faster), and
#  2. You have to be informix to enable logging and this script runs as 
#     informix, and
#  3. We want to allow any user on the test machine to be able to load a 
#     database.
#
# Usage:
#  
#  enableLogging.pl dbname 
#
#     dbname          Name of database to enable logging in.
#
#   The informix environment variables are assumed to be set before calling
#   this script.
#
# Returns:
#  0   No errors were detected, logging was enabled
#  >0  Errors were encountered. logging may or may not have been enabled.
#
# $Id: enableLogging.pl,v 1.2 2005-01-10 17:19:44 informix Exp $
# $Source: /research/zusers/ndunn/CVSROOT/Commons/bin/enableLogging.pl,v $

BEGIN {
    $ENV{PATH}="/local/bin:/usr/bin:/private/apps/Informix/informix/bin";
    $ENV{SHELL}="/usr/bin/sh";
};

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
# Main.
#


#
# Define GLOBALS
#

$globalErrorCount = 0;

# process args; dbname needs to be untainted
my $dbNameTainted = shift(@ARGV);
$dbNameTainted =~ /^([\w.]*)$/;
my $dbName = $1;

# set all the user and group ids we can figure out how to.
my @userData = getpwnam("informix");
$REAL_USER_ID = $userData[2];
$EFFECTIVE_USER_ID = $userData[2];
$REAL_GROUP_ID = $userData[3];
$EFFECTIVE_GROUP_ID = $userData[3];

if (system("ontape", "-s", "-B", $dbName, "-L", "0")) {
	logError("Unable to turn on logging for $dbName",
		"  Try running ontape -s -B $dbName as user informix.");
}

exit ($globalErrorCount);
