#!/bin/tcsh

set pth=/opt/postgres/postgres_wal/base_backups

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

# -wal-method='stream' -> Stream the write-ahead log while the backup is created. This will open a second connection to the server and start streaming 
#     the write-ahead log in parallel while running the backup. Therefore, it will use up two connections configured by the max_wal_senders parameter. 
#     As long as the client can keep up with write-ahead log received, using this mode requires no extra write-ahead logs to be saved on the master. 
#     The write-ahead log files are written to a separate file named pg_wal.tar (if the server is a version earlier than 10, the file will be named 
#     pg_xlog.tar).  This value is the default.

# -format=t  -> Write the output as tar files in the target directory. The main data directory will be written to a file named base.tar, and all other tablespaces will be named after the tablespace OID.

# -D output directory for the base.tar file
# -v verbose
# -P progress is posted to standard out
# -z gzip output

/opt/postgres/postgresql/bin/pg_basebackup --wal-method='stream' --format=t -D $pth/$dirname/ -v -P -z

# compress wal archives, add to data directory.  the method above also captures wal files, this command is to help secure a 2 day supply of
# wal files so we can comfortably remove anything older than a day.

cd /opt/postgres/postgres_wal/

tar -jcf archives.tar.gz wal_archive
mv archives.tar.gz $pth/$dirname

# take a filesystem backup as well, which does backup /opt/postgres/data
tar -jcf $pth/$dirname/$dirname.filesytem.tar.gz -h /opt/postgres/data

# delete WAL log archive files older than 3 days (assumes the base backup and filesystem backups above, happen nightly)          
cd /opt/postgres/postgres_wal/wal_archive/
find -mtime +2 -exec rm {} \;

find /opt/postgres/postgres_wal/base_backups/* -type d -ctime +5 -exec rm -rf {} \;
