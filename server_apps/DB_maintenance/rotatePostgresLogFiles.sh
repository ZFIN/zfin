#!/bin/bash

# make a base backup 
/opt/postgres/postgresql/bin/pg_basebackup --xlog --format=t -D /research/zunloads/databases/punkt/base_backups/`date +%Y%m%d`

# compress wal archives, add to data directory.
cd /research/zunloads/databases/punkt/
tar -cf archives.tar wal_log_archive
mv archives.tar /research/zunloads/databases/punkt/base_backups/`date +%Y%m%d`
cd /research/zunloads/databases/punkt/base_backups/`date +%Y%m%d`

# delete WAL log archive files older than 3 days (assumes the base backup happens nightly)

cd /research/zunloads/databases/punkt/wal_log_archive/
find -mtime +2 -exec rm {} \;
