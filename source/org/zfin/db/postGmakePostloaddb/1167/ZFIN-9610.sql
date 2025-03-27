--liquibase formatted sql
--changeset rtaylor:ZFIN-9610.sql

UPDATE
    blast_database
SET
    blastdb_path = 'https://genome.ucsc.edu/cgi-bin/hgTracks?org=Zebrafish&db=danRer11&position='
WHERE
    blastdb_zdb_id = 'ZDB-BLASTDB-090929-17'
AND
    blastdb_path = 'http://genome.ucsc.edu/cgi-bin/hgTracks?org=Zebrafish&db=current&position=';


UPDATE
    blast_database
SET
    blastdb_path = 'https://genome.ucsc.edu/cgi-bin/hgBlat?db=danRer11'
WHERE
    blastdb_zdb_id = 'ZDB-BLASTDB-090929-18'
AND
    blastdb_path = 'http://genome.ucsc.edu/cgi-bin/hgBlat?db=danRer10';