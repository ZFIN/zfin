#!/private/bin/perl -wT
#
# Script to stop and start tomcat on development.  We do this because once
# servlets are loaded into tomcat, they tend to stay there, even after the
# files defining the servlet have been changed.  There is supposedly a way
# to get Tomcat to recognize when the files change, but I haven't figured
# that out yet.

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
system("/private/ZfinLinks/Commons/bin/tomcat.sh stop happy");


