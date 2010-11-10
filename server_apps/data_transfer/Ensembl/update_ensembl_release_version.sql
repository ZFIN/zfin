-- change Ensembl release version
begin work;

set constraints for foreign_db.foreign_db_primary_key deferred;

UPDATE foreign_db
SET fdb_db_name = 'Ensembl(Zv9)'
WHERE fdb_db_name = 'Ensembl(Zv8)'
;

UPDATE foreign_db_contains
SET fdbcont_fdb_db_id = (select fdb_db_pk_id from foreign_db where fdb_db_name = 'Ensembl(Zv9)')
WHERE fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
;

-- PreEnsembl not normally needed

--UPDATE foreign_db_contains
--SET fdbcont_fdb_db_id = (select fdb_db_pk_id from foreign_db where fdb_db_name ='PreEnsembl(Zv7)')
--WHERE fdbcont_zdb_id = 'ZDB-FDBCONT-070718-1'
--;

--UPDATE foreign_db
--SET fdb_db_name = 'PreEnsembl(Zv7)'
--WHERE fdb_db_name = 'PreEnsembl'
--;

-- change to commit and gmake on production as needed
--
rollback work;

--commit work;
