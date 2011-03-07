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
chmod 550 restartapache.pl starttomcat.pl stoptomcat.pl restarttomcat.pl 
chmod 550 setContextDescriptor.pl makemediumimages.bash 
chmod 550 starthappytomcat.pl stophappytomcat.pl restarthappytomcat.pl 
chmod 550 starttrunktomcat.pl stoptrunktomcat.pl restarttrunktomcat.pl 
chmod 550 starttesttomcat.pl stoptesttomcat.pl restarttesttomcat.pl

chown root:fishadmin restartapache.pl setContextDescriptor.pl
chown root:fishadmin starttomcat.pl stoptomcat.pl restarttomcat.pl
chown root:fishadmin starthappytomcat.pl stophappytomcat.pl restarthappytomcat.pl
chown root:fishadmin starttrunktomcat.pl stoptrunktomcat.pl restarttrunktomcat.pl
chown root:fishadmin starttesttomcat.pl stoptesttomcat.pl restarttesttomcat.pl

chmod u+s restartapache.pl setContextDescriptor.pl 
chmod u+s starttomcat.pl stoptomcat.pl restarttomcat.pl
chmod u+s starthappytomcat.pl stophappytomcat.pl restarthappytomcat.pl
chmod u+s starttrunktomcat.pl stoptrunktomcat.pl restarttrunktomcat.pl
chmod u+s starttesttomcat.pl stoptesttomcat.pl restarttesttomcat.pl

chown informix:fishadmin ../env/bionix.env ../env/helix.env ../env/embryonix.env ../env/frost.env ../env/mirror.env
chmod u+s enableLogging.pl
