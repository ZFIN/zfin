#!/bin/csh
#----------------------------------------------------------------------
# This script updates the mirror.zfin.org web site with the latest updates
# from CVS.  It is meant to be run from cron.

cd /research/zusers/mirror/ZFIN_WWW
source /research/zcentral/Commons/env/mirror.env
/local/bin/cvs -q update -dP

# Push updates to web site.  Send stdout to dev/null.  Only care about errors
/local/bin/gmake mirror > /dev/null
