#!/bin/bash

set pth=/research/zunloads/databases/${HOSTNAME}

set dirname=`date +"%Y.%m.%d.1"`

# increment until we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end


# make a base backup 
/opt/postgres/postgresql/bin/pg_basebackup --wal-method='fetch' --format=t -D /research/zunloads/databases/${HOSTNAME}/base_backups/`date +%Y%m%d`

# compress wal archives, add to data directory.
cd /research/zunloads/databases/${HOSTNAME}/
tar -cf archives.tar wal_log_archive
mv archives.tar /research/zunloads/databases/${HOSTNAME}/base_backups/`date +%Y%m%d`

# delete WAL log archive files older than 3 days (assumes the base backup happens nightly)

cd /research/zunloads/databases/${HOSTNAME}/wal_log_archive/
find -mtime +2 -exec rm {} \;
