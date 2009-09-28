#!/bin/bash

# This scripts pushes newly updated databases to genomix using rsync.
# change this to a env variable at some point: WEBHOST_BLASTDB_TO_COPY.
# Right now, this is running from cron, which gets no env variables; we could put this into 
# the ant build script, or make it a tt variable.  Both seem like overkill for one line of hard-coding.

FROM_DIRECTORY=/research/zprodmore/blastdb/Current

# make three identical copies to the development filesyste. 
#one for almost:
TO_ALMOST_DIRECTORY=/research/zblastfiles/zmore/almdb/Current/

# one copy for default developers that don't want their own
TO_DEFAULT_DIRECTORY=/research/zblastfiles/zmore/dev_blastdb/Current/

# on copy as a base to restore today from, in case either of the 
# two above are corrupted through testing, or a developer needs to
# make a new copy.
TO_PRISTINE_DIRECTORY=/research/zblastfiles/zmore/nightly/Current/

# rsync the almost directory and the unload directory nightly.
# the unload directory is the default directory

# rsync command:
# -c always checksum 
# -K follow symlinks 
# -v verbose 
# -u update only don't overwrite sooner timestamps

echo rsync the almost directory
/local/bin/rsync -rcvuK $FROM_DIRECTORY/*.x* $TO_ALMOST_DIRECTORY

echo rsync the default director
/local/bin/rsync -rcvuK $FROM_DIRECTORY/*.x* $TO_DEFAULT_DIRECTORY

echo rsync the pristine directory
/local/bin/rsync -rcvuK $FROM_DIRECTORY/*.x* $TO_PRISTINE_DIRECTORY

# if there are changes, then commit them as informix, most likely.
if [ <!--|DOMAIN_NAME|--> == almost.zfin.org ]; then

    cd $TO_ALMOST_DIRECTORY

    /local/bin/svn commit -m "updated the curated databases." published*
    /local/bin/svn commit -m "updated the curated databases." unreleased*
    /local/bin/svn commit -m "updated the curated databases." Curated*

fi

# establish the write permissions and group.
chgrp -R zfishweb $TO_ALMOST_DIRECTORY
chmod -R g+w $TO_ALMOST_DIRECTORY

chgrp -R fishadmin $TO_DEFAULT_DIRECTORY
chmod -R g+w $TO_DEFAULT_DIRECTORY

chgrp -R zfishweb $TO_PRISTINE_DIRECTORY
chmod -R g+w $TO_PRISTINE_DIRECTORY

