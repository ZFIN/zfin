#!/bin/bash

set pth=/opt/postgres/postgres_wal/base_backups

set dirname=`date +"%Y.%m.%d.1"`

# increment until we get name which has not been taken
while [ -d $pth/$dirname ]
do
	z=$dirname:e
	y=$dirname:r
@ x = $z + 1
	dirname=$y.$x
done

# make a base backup 
/opt/postgres/postgresql/bin/pg_basebackup --wal-method='fetch' --format=t -D /opt/postgres/postgres_wal/base_backups/`date +%Y%m%d` -v -P

# compress wal archives, add to data directory.
cd /opt/postgres/postgres_wal/
tar -cf archives.tar wal_log_archive
mv archives.tar /opt/postgres/postgres_wal/base_backups/`date +%Y%m%d`

# delete WAL log archive files older than 3 days (assumes the base backup happens nightly)

cd /opt/postgres/postgres_wal/wal_log_archive/
find -mtime +2 -exec rm {} \;
