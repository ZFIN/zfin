#!/private/bin/perl -w
#------------------------------------------------------------------------
# 
# Script runs the Informix oncheck utility against an informix server, and
# optionally against a particular database in that server.
#
# This script produces lots of output.  It is recommended you redirect the
# output of it.
# 
# WARNING: Some of the DB-Specific checks performed by this script acquire
#   shared locks on the tables they look at.  This means that updates to
#   those tables will fail or wait until the check is done. 
#
# Usage:
#  
#  checkifmx [dbname] [dbname] [...]
#
#     dbname          Optional database name.  If this is provided, then 
#                     additional checks will be run against this database.
#
#   The informix environment variables are assumed to be set before calling
#   this script.
#
# Returns:
#  Nothing really, but it does create a lot of output.

use English;

#------------------------------------------------------------------------
# Main.
#
# NOTE:  I tried to capture the return status of the oncheck calls, but 
#   everything I did resulted in the behavior of the oncheck call being
#   altered for the worse.  It would actually not run some tests.
#

$oncheck = "$ENV{INFORMIXDIR}/bin/oncheck";

# begin db server checks

print(STDOUT "\nChecking Extents ...\n");
system("$oncheck -ce ");

print(STDOUT "\nChecking root dbspace ...\n");
system("$oncheck -cR ");

print(STDOUT "\nChecking Smart Large Object Spaces ...\n");
system("$oncheck -cS ");

# Check each given database, if any, for db specific problems

foreach $dbName (@ARGV) {

    print(STDOUT "\nChecking System Catalog for $dbName ...\n");
    system("$oncheck -cc $dbName ");

    print(STDOUT "\nChecking Pages for $dbName ...\n");
    system("$oncheck -cD $dbName ");

    print(STDOUT "\nChecking Indexes for $dbName ...\n");
    system("$oncheck -cI $dbName ");

}

exit (0);
