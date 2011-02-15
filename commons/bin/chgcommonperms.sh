#!/bin/sh
#
# This file is run after the Commons project has been updated by CVS to
# restore the permissions of certain files to be what they should be.
# You must run this script with sudo.  If you don't have sudo privileges
# then this will not work for you.
#
# $Id: chgcommonperms.sh,v 1.12 2007-11-30 20:13:39 kschaper Exp $
# $Source: /research/zusers/ndunn/CVSROOT/Commons/bin/chgcommonperms.sh,v $

cd /private/ZfinLinks/Commons/bin
# chown informix:fishadmin enableLogging.pl pullfromfrost.pl 
chmod 550 restartapache.pl starttomcat.pl stoptomcat.pl restarttomcat.pl setContextDescriptor.pl makemediumimages.bash starthappytomcat.pl stophappytomcat.pl restarthappytomcat.pl
chown root:fishadmin restartapache.pl starttomcat.pl stoptomcat.pl restarttomcat.pl setContextDescriptor.pl
chown root:fishadmin starthappytomcat.pl stophappytomcat.pl restarthappytomcat.pl
chmod u+s restartapache.pl starttomcat.pl stoptomcat.pl restarttomcat.pl setContextDescriptor.pl starthappytomcat.pl stophappytomcat.pl restarthappytomcat.pl
chown informix:fishadmin ../env/bionix.env ../env/helix.env ../env/embryonix.env ../env/frost.env ../env/mirror.env
chmod u+s enableLogging.pl
