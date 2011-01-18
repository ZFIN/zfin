#!/private/bin/perl -w
#
# Script to reboot apache on development.  Necessary because the webdriver
# aware apache establishes and holds connections to databases.  This is
# a problem when you want to delete and reload a database.  Informix 
# won't let you delete a database when there is an open connection to it.
# This will sever all connections.
#

BEGIN {
    $ENV{PATH}="/local/bin:/usr/bin";
    $ENV{SHELL}="/usr/bin/sh";
    delete $ENV{LD_LIBRARY_PATH};
};

use English;


# set all the user and group ids we can figure out how to.
my @userData = getpwnam("root");
$REAL_USER_ID = $userData[2];
$EFFECTIVE_USER_ID = $userData[2];
$REAL_GROUP_ID = $userData[3];
$EFFECTIVE_GROUP_ID = $userData[3];
system("/private/apps/apache/bin/apachectl restart");
