#!/bin/sh
#------------------------------------------------------------------------
#
# Shell script that generates a new index for the web site, then moves the
# current index to the old_index holder, and moves the new_index into
# the current index.  In other words, shell script generates a new index
# for the site.
#

UNIQUERY_DIR=<!--|ROOT_PATH|-->/j2ee/uniquery

# create directory for new indexes
/bin/rm -rf $UNIQUERY_DIR/new_indexes
/bin/mkdir $UNIQUERY_DIR/new_indexes

# generate the new indexes
/private/apps/java1.4/bin/java \
  -server \
  -classpath $UNIQUERY_DIR/IndexApp/classes:$UNIQUERY_DIR/IndexApp/lib/UniquerySupport.jar:$UNIQUERY_DIR/IndexApp/lib/commons-lang-2.0.jar:$UNIQUERY_DIR/IndexApp/lib/cvu.jar:$UNIQUERY_DIR/IndexApp/lib/lucene-1.3.jar \
  org.zfin.uniquery.index.Spider \
  -d $UNIQUERY_DIR/new_indexes \
  -u $UNIQUERY_DIR/IndexApp/etc/searchurls.txt \
  -e $UNIQUERY_DIR/IndexApp/etc/excludeurls.txt \
  -c $UNIQUERY_DIR/IndexApp/etc/crawlonlyurls.txt \
  -t 2 \
  -l $UNIQUERY_DIR/IndexApp/logs \
  -v

# NEED TO CHECK RETURN STATUS

# rename current index to old index and move new to current
/bin/rm -rf $UNIQUERY_DIR/old_indexes
/bin/mv $UNIQUERY_DIR/indexes $UNIQUERY_DIR/old_indexes
/bin/mv $UNIQUERY_DIR/new_indexes $UNIQUERY_DIR/indexes

# call it a day
exit 0
