#!/private/bin/perl -w

# FILE: rysnc.pl
# DESCRIPTION: This file syncs production and development image/pdf
# file systems.

#system ("cd /research/zprod/loadUp/imageLoadUp/") ;

#system("/local/bin/rsync -auv --temp-dir=/research/zcentral/loadUp/imageLoadUp --delete --ignore-existing --backup-dir=/tmp/staylor --suffix=.del /research/zprod/loadUp/imageLoadUp/ /research/zcentral/loadUp/imageLoadUp") ;

system ("cd /research/zprod/loadUp/PDFLoadUp/") ;

system("/local/bin/rsync -auv --temp-dir=/research/zcentral/loadUp/PDFLoadUp --delete --ignore-existing --backup-dir=/tmp/staylor --suffix=.del /research/zprod/loadUp/PDFLoadUp/ /research/zcentral/loadUp/PDFLoadUp") ;

exit;
