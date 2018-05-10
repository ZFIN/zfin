#!/bin/bash


mkdir opt/zfin/postgres_wal/base_backups/`date +%Y%m%d`

# make a base backup 
/opt/postgres/postgresql/bin/pg_basebackup --wal-method='fetch' --format=t -D /opt/zfin/postgres_wal/base_backups/`date +%Y%m%d`

# compress wal archives, add to data directory.
cd /opt/zfin/postgres_wal/
tar -cf archives.tar wal_archive
mv archives.tar /opt/zfin/postgres_wal/base_backups/`date +%Y%m%d`

# delete WAL log archive files older than 3 days (assumes the base backup happens nightly)

cd /opt/zfin/postgres_wal/wal_archive
find -mtime +2 -exec rm {} \;
