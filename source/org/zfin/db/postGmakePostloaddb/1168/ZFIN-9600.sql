--liquibase formatted sql
--changeset cmpich:ZFIN-9600.sql

delete
from int_fdbcont_analysis_tool
where  ifat_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'
AND ifat_blastdb_zdb_id in
    (select blastdb_zdb_id from blast_database where blastdb_tool_display_name != 'ZFIN BLAST' AND
    blastdb_zdb_id in (select ifat_blastdb_zdb_id from int_fdbcont_analysis_tool where ifat_fdbcont_zdb_id = 'ZDB-FDBCONT-060417-1'));
