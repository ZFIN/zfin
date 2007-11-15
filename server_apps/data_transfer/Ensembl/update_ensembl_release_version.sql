-- change Ensembl release version
begin work;

set constraints for foreign_db_contains.foreign_db_contains_fdb_db_name_foreign_key deferred;
set constraints for foreign_db.foreign_db_primary_key deferred;

UPDATE foreign_db
SET fdb_db_name = 'Ensembl(Zv6)'
WHERE fdb_db_name = 'Ensembl'
;

UPDATE foreign_db_contains
SET fdbcont_fdb_db_name = 'Ensembl(Zv6)'
WHERE fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
;

-- PreEnsembl not normally needed

UPDATE foreign_db_contains
SET fdbcont_fdb_db_name = 'PreEnsembl(Zv7)'
WHERE fdbcont_zdb_id = 'ZDB-FDBCONT-070718-1'
;

UPDATE foreign_db
SET fdb_db_name = 'PreEnsembl(Zv7)'
WHERE fdb_db_name = 'PreEnsembl'
;
--rollback work;

--
commit work;