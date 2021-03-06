#!/bin/tcsh -e

set pth=/opt/postgres/postgres_wal/base_backups

set pth=/opt/zfin/postgres_wal/base_backups
set dirname=`date +"%Y.%m.%d.1"`


# increment until we get name which has not been taken
while ( -d $pth/$dirname )
	set z=$dirname:e
	set y=$dirname:r
@ x = $z + 1
	set dirname=$y.$x
end

# make a base backup 
/opt/postgres/postgresql/bin/pg_basebackup --wal-method='fetch' --format=t -D /opt/postgres/postgres_wal/base_backups/$dirname/`date +%Y%m%d` -v -P

# compress wal archives, add to data directory.
cd /opt/postgres/postgres_wal/
tar -cf archives.tar wal_archive
mv archives.tar /opt/postgres/postgres_wal/base_backups/$dirname

cd /opt/postgres/
tar -cf $pth/$dirname/$dirname.filesytem.tar -h /opt/postgres/data
cd /opt/postgres/postgres_wal/
gzip $pth/$dirname/$dirname.filesytem.tar
gzip $pth/$dirname/base.tar
# delete WAL log archive files older than 3 days (assumes the base backup happens nightly)                                                                                       

cd /opt/postgres/postgres_wal/wal_archive/
find -mtime +2 -exec rm {} \;
