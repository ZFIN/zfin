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
use feature 'switch';

given ($ARGV[0]) {
    when (undef) {
        $command="/etc/init.d/tomcat stop; /etc/init.d/tomcat start";
    }
    when ("zfin.org") {
        $command="/etc/init.d/tomcat stop; /etc/init.d/tomcat start";
    }
    when ("almost") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart almost";
    }
    when ("trunk") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart trunk";
    }
    when ("test") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart test";
    }
    when ("mirror") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart mirror";
    }
    when ("watson_test") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart watson_test";
    }
    when ("crick_test") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart crick_test";
    }
    when ("watson") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart watson";
    }
    when ("crick") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart crick";
    }
    when ("watsondb") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart watson_test";
    }
    when ("crickdb") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart crick_test";
    }
    when ("darwindb") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart darwin";
    }
    when ("xraydb") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart xray";
    }
    when ("darwin") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart darwin";
    }
    when ("gorp") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart gorp";
    }
    when ("xray") {
        $command="/private/ZfinLinks/Commons/bin/tomcat.sh restart xray";
    }

    default {
	die "Incorrect argument '$ARGV[0]'"
    }
}

# set all the user and group ids we can figure out how to.
my @userData = getpwnam("root");
$REAL_USER_ID = $userData[2];
$EFFECTIVE_USER_ID = $userData[2];
$REAL_GROUP_ID = $userData[3];
$EFFECTIVE_GROUP_ID = $userData[3];
system($command);
