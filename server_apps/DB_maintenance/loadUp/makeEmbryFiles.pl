#!/opt/zfin/bin/perl -w

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

chdir ("/research/zcentral/loadUp/embryonixLoadUp/embryonixImageLoadUp");
system ("pwd") ;

system("/local/bin/rsync -aupv --delete --ignore-existing /research/zprod/loadUp/imageLoadUp/ /research/zcentral/loadUp/embryonixLoadUp/embryonixImageLoadUp/imageLoadUp/");

# our file systems are nfs mounted on each other.

chdir ("/research/zcentral/loadUp/embryonixLoadUp/embryonixPDFLoadUp");
system ("pwd") ;

system("/local/bin/rsync -aupv --delete --ignore-existing /research/zcentral/loadUp/PDFLoadUp/ /research/zcentral/loadUp/embryonixLoadUp/embryonixPDFLoadUp/PDFLoadUp/");

chdir ("/research/zcentral/loadUp/embryonixLoadUp/embryonixVideoLoadUp");
system ("pwd") ;

system("/local/bin/rsync -aupv --delete --ignore-existing /research/zcentral/loadUp/videoLoadUp/ /research/zcentral/loadUp/embryonixLoadUp/embryonixVideoLoadUp/videoLoadUp/");

exit; 
