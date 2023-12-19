--liquibase formatted sql
--changeset rtaylor:ZFIN-8797.sql

-- COMMENTED OUT: handling loading of notes may no longer be necessary
-- fix null values for external notes loaded in past uniprot loads
-- INSERT INTO external_note_type (extntype_name)
-- VALUES ('dblink');
--
-- UPDATE external_note
--  SET extnote_note_type = 'dblink'
--  WHERE extnote_source_zdb_id = 'ZDB-PUB-230615-71'
--   AND extnote_note_type IS NULL;
-- end fix for external notes
-- END COMMENTED OUT SECTION

-- DELETE external notes instead
-- DELETE FROM external_note
--  WHERE extnote_source_zdb_id = 'ZDB-PUB-230615-71'
--    AND extnote_note_type IS NULL;

-- clean up ncbi loaded refseqs for uniprot release:
-- these refseqs were moved to ZDB-GENE-100922-192
DELETE FROM db_link WHERE "dblink_linked_recid" = 'ZDB-GENE-060503-858' and "dblink_acc_num" = 'XM_003200579' and "dblink_info" = 'uncurated: NCBI gene load 2023-07-10 18:14:22.673585-07' and "dblink_acc_num_display" = 'XM_003200579' and "dblink_length" is NULL and "dblink_fdbcont_zdb_id" = 'ZDB-FDBCONT-040412-38' and "dblink_zdb_id" = 'ZDB-DBLINK-230710-145250';
DELETE FROM db_link WHERE "dblink_linked_recid" = 'ZDB-GENE-060503-858' and "dblink_acc_num" = 'XR_002456119' and "dblink_info" = 'uncurated: NCBI gene load 2023-07-10 18:14:22.673585-07' and "dblink_acc_num_display" = 'XR_002456119' and "dblink_length" is NULL and "dblink_fdbcont_zdb_id" = 'ZDB-FDBCONT-040412-38' and "dblink_zdb_id" = 'ZDB-DBLINK-230710-172883';
DELETE FROM db_link WHERE "dblink_linked_recid" = 'ZDB-GENE-060503-858' and "dblink_acc_num" = 'XR_659022' and "dblink_info" = 'uncurated: NCBI gene load 2023-07-10 18:14:22.673585-07' and "dblink_acc_num_display" = 'XR_659022' and "dblink_length" is NULL and "dblink_fdbcont_zdb_id" = 'ZDB-FDBCONT-040412-38' and "dblink_zdb_id" = 'ZDB-DBLINK-230710-173788';

-- add column to uniprot_release table: upr_secondary_load_date
ALTER TABLE uniprot_release ADD COLUMN upr_secondary_load_date timestamp without time zone;

-- add a unique constraint to uniprot_release table: upr_path
ALTER TABLE uniprot_release ADD CONSTRAINT upr_path_unique UNIQUE (upr_path);

-- populate uniprot_release table:
INSERT INTO uniprot_release
("upr_date", "upr_size", "upr_md5", "upr_path", "upr_download_date")
VALUES
('2023-06-28', '102650055', '93b8561ec57603f4df80270d03b6f018', '2023-06-28/pre_zfin.dat', '2023-06-28'),
('2023-09-13', '102916208', 'c14187a949c9995945d23618e2436fd6', '2023-09-13/pre_zfin.dat', '2023-09-13'),
('2023-11-08', '102714638', 'fe4d82492dc7bd9a67d1fcd943f41b63', '2023-11-08/pre_zfin.dat', '2023-11-08')
ON CONFLICT DO NOTHING;
