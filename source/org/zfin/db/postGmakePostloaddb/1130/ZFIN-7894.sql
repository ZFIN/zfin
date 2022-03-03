--liquibase formatted sql
--changeset christian:ZFIN-7894

-- mirBase Stem Loop
--no non-ZFIN blast
delete from int_fdbcont_analysis_tool
where ifat_fdbcont_zdb_id = 'ZDB-FDBCONT-090929-2'
AND ifat_blastdb_zdb_id in ('ZDB-BLASTDB-090929-16','ZDB-BLASTDB-090929-18','ZDB-BLASTDB-090929-19','ZDB-BLASTDB-090929-9');