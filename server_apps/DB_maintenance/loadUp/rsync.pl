#!/private/bin/perl -w

# FILE: rysnc.pl
# DESCRIPTION: This file syncs production and development image/pdf
# file systems. syncing means adding files to development that are missing
# when compared to the production file system, and deleting files
# from the development file system when they are not available on production.

# INPUT VARS: none
# OUTPUT: adds or deletes files from embryonix/zygotix/development machine

# see man rsync for additional details!
#
# --backup-dir=/tmp : if rsync deletes, it'll put a copy of the deleted file
#   in /tmp
# -b : backup, preexisting destination files are renamed with specified suffix
#   in specified backup directory
# --suffix=uploadbkup : suffix appended to backup files in /tmp
# -a : archive, go through directories recursively
# -u : update only (don't overwrite newer files)
# -p : preserve permissions on files that get copied
# -copy-links: means rsync can go between symlink directories
# --delete: delete files that don't exist on sender.
# --ignore-existing: ignore files that already exist on receiver
#
# /research/zprod/loadUp/imageLoadUp is production 
# /research/zcentral/loadUp/imageLoadUp is development
#
# we do not need to provide server names in this case because
# our file systems are nfs mounted on each other.

system ("cd <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->");

# --backup-dir=/tmp --suffix=uploadbkup --delete 

system("/local/bin/rsync -upvab --copy-links --ignore-existing <!--|LOADUP_FULL_PATH|--><!--|IMAGE_LOAD|-->/ /research/zcentral/loadUp/imageLoadUp/");

system ("cd <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|--> ");

# --backup-dir=/tmp --suffix=uploadbkup --delete 

system("/local/bin/rsync -upvab  --copy-links --ignore-existing <!--|LOADUP_FULL_PATH|--><!--|PDF_LOAD|-->/ /research/zcentral/loadUp/PDFLoadUp/");

exit;
