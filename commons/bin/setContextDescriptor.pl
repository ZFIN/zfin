#!/private/bin/perl -wT
#------------------------------------------------------------------------
# 
# Changes ownership and permissions on context descriptors in Tomcat.
# Tomcat 5.5 will delete any context descriptors that are not owned by
# the same user as the tomcat process.  
#
# Usage:
#  
#   setContextDescriptor.pl domainName contextDescriptorFile
#
#     domainName            Domain name of the web site the context descriptor
#                           is for.  For example, zfin.org or almost.zfin.org
#     contextDescriptorFile Web App context descriptor file to copy into 
#                           Tomcat.  This is just a file name, it is not a 
#                           path.  This file should have already been copied
#                           into Tomcat.
#
# Returns:
#  0   No errors were detected, file ownership/permissions were changed.
#  >0  Errors were encountered, file ownership/permissions may or may not have
#        been changed.
#
# $Id: setContextDescriptor.pl,v 1.2 2006-03-06 21:26:53 peirans Exp $
# $Source: /research/zusers/ndunn/CVSROOT/Commons/bin/setContextDescriptor.pl,v $

BEGIN {
    $ENV{PATH}="/local/bin:/usr/bin:";
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

# process args; everthing needs to be untainted.

my $dnTainted = shift(@ARGV);
$dnTainted =~ /^([\w.]*)$/;
my $domainName = $1;

my $cdTainted = shift(@ARGV);
$cdTainted =~ /^([\w.]*)$/;
my $contextDescriptor = $1;

my $destContextDescriptor = "/private/apps/tomcat/conf/Catalina/$domainName/$contextDescriptor";

if (system("/bin/chown zfishweb:fishadmin $destContextDescriptor")) {
    logError("Chown failed.");
}

if (system("/bin/chmod 660 $destContextDescriptor")) {
    logError("Chmod failed.");
}

exit ($globalErrorCount);
