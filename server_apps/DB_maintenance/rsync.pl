#!/private/bin/perl -w

# FILE: rysnc.pl
# DESCRIPTION: This file syncs production and development image/pdf
# file systems. syncing means adding files to development that are missing
# when compared to the production file system, and deleting files
# from the development file system when they are not available on production.

# INPUT VARS: none
# OUTPUT: adds or deletes files from embryonix/development machine

# see man rsync for additional details!
#
# -u : update only (don't overwrite newer files)
# -p : preserve permissions on files that get copied
# --delete: delete files that don't exist on sender.
# --ignore-existing: ignore files that already exist on receiver
#
# /research/zprod/loadUp/imageLoadUp is production 
# /research/zcentral/loadUp/imageLoadUp is development
#
# we do not need to provide server names in this case because
# our file systems are nfs mounted on each other.

system ("cd /research/zprod/loadUp/imageLoadUp/") ;

system("/local/bin/rsync -up --delete --ignore-existing /research/zprod/loadUp/imageLoadUp/ /research/zcentral/loadUp/imageLoadUp") ;

system ("cd /research/zprod/loadUp/PDFLoadUp/") ;

system("/local/bin/rsync -up --delete --ignore-existing /research/zprod/loadUp/PDFLoadUp/ /research/zcentral/loadUp/PDFLoadUp") ;

exit;
