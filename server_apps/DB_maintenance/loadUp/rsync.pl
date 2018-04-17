#!/private/bin/perl -w

# FILE: rysnc.pl
# DESCRIPTION: This file syncs production and development image/pdf
# file systems. syncing means adding files to development that are missing
# when compared to the production file system, and deleting files
# from the development file system when they are not available on production.

# INPUT VARS: none
# OUTPUT: adds or deletes files from embryonix/zygotix/development machine

# see man rsync for additional details!

# -a : archive, go through directories recursively
# -u : don't update the reciever if it is newer than the sender.
# /research/zprod/loadUp/imageLoadUp is production 
# /research/zcentral/loadUp/imageLoadUp is development
#
# we do not need to provide server names in this case because
# our file systems are nfs mounted on each other.

# --backup-dir=/tmp --suffix=uploadbkup --delete 

#system("/local/bin/rsync -vua  <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/ /research/zcentral/loadUp/imageLoadUp/");

# --backup-dir=/tmp --suffix=uploadbkup --delete 

#system("/local/bin/rsync -vua <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/ /research/zcentral/loadUp/PDFLoadUp/");

#system("/local/bin/rsync -vua <!--|LOADUP_FULL_PATH|--><!--|VIDEO_LOAD|-->/ /research/zcentral/loadUp/videoLoadUp/");

exit;
