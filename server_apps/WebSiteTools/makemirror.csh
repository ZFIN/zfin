#!/bin/csh
#----------------------------------------------------------------------
# This script updates the mirror.zfin.org web site with the latest updates
# from CVS.  It is meant to be run from cron.

cd /private/ZfinLinks/mirror_src/ZFIN_WWW
source /private/ZfinLinks/Commons/env/mirror.env

# get updates from CVS. Send stdout to /dev/null. Only care about errors.
/local/bin/svn up > /dev/null

# Push updates to web site.  Send stdout to dev/null.  Only care about errors
/local/bin/gmake mirror > /dev/null
