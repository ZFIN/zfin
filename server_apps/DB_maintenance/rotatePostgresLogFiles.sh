#!/bin/tcsh -e                                                                           

set pth=/opt/zfin/postgres_wal/base_backups
set dirname=`date +"%Y.%m.%d.1"`

# increment until we get name which has not been taken                                               
while ( -d $pth/$dirname )
        set z=$dirname:e
        set y=$dirname:r
@ x = $z + 1
        set dirname=$y.$x
end

mkdir $pth/$dirname

# make a base backup                                                                                 
/opt/postgres/postgresql/bin/pg_basebackup --wal-method='fetch' --format=t -D $pth/$dirname

# compress wal archives, add to data directory.                                                      
cd /opt/zfin/postgres_wal/
tar -cf $dirname.archives.tar wal_archive
mv $dirname.archives.tar $pth/$dirname

# delete WAL log archive files older than 3 days (assumes the base backup happens nightly)           

cd /opt/zfin/postgres_wal/wal_archive
find -mtime +2 -exec rm {} \;

cd /opt/postgres/data $pth/$dirname/
tar -cf /opt/postgres/data $pth/$dirname/$dirname.filesytem.tar
gzip $dirname.filesytem.tar
