-- this file loads the xrefs in the obo file
-- uses one temp table: tmp_dbxrefs


!echo "load term_dbxrefs";
create temp table tmp_dbxrefs (
term_id varchar(30),
xref_db varchar(50),
xref_accession varchar(400),
type varchar(30));

load from term_xref.unl
  insert into tmp_dbxrefs;



create temp table tmp_dbxrefs_with_ids (
tmp_term_zdb_id varchar(30),
tmp_xref_db varchar(50),
tmp_xref_db_id bigint,
tmp_xref_accession varchar(200));

insert into tmp_dbxrefs_with_ids
select term_zdb_id, xref_db,0, xref_accession
from tmp_dbxrefs,term
where term_id=term_ont_id;

update tmp_dbxrefs_with_ids
set tmp_xref_db_id =
(select fdb_db_pk_id from foreign_db where
 lower(fdb_db_name) = lower(tmp_xref_db)
 ) where exists (
 select 'x' from foreign_db where
 lower(fdb_db_name) = lower(tmp_xref_db) );

update  tmp_dbxrefs_with_ids
set tmp_xref_db_id=null where tmp_xref_db_id = 0 or not exists (
select 'x' from foreign_db where
tmp_xref_db_id = fdb_db_pk_id
);

--for statistics dump the xrefs that will be deleted from this load
unload to removed_xrefs
SELECT tx_term_zdb_id,
       tx_full_accession,
       tx_fdb_db_id
FROM   term_xref
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   tmp_dbxrefs_with_ids
                   WHERE  tx_term_zdb_id = tmp_term_zdb_id
                          AND nvl(tx_fdb_db_id, 0) = nvl(tmp_xref_db_id, 0)
                          AND tx_full_accession = tmp_xref_db
                                                  || ':'
                                                  || tmp_xref_accession)
       AND EXISTS (SELECT 'x'
                   FROM   term,
                          ontology,
                          tmp_header
                   WHERE  ont_ontology_name = default_namespace
                          AND ont_pk_id = term_ontology_id
                          AND term_zdb_id = tx_term_zdb_id);
                          
-- delete those records from the base table that are not found in the temp table
DELETE FROM term_xref
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   tmp_dbxrefs_with_ids
                   WHERE  tx_term_zdb_id = tmp_term_zdb_id
                          AND nvl(tx_fdb_db_id, 0) = nvl(tmp_xref_db_id,0)
                          AND tx_full_accession = tmp_xref_db
                                                  || ':'
                                                  || tmp_xref_accession)
       AND EXISTS (SELECT 'x'
                   FROM   term,
                          ontology,
                          tmp_header
                   WHERE  ont_ontology_name = default_namespace
                          AND ont_pk_id = term_ontology_id
                          AND term_zdb_id = tx_term_zdb_id);

-- delete those records from the temp table that are already in base table
DELETE FROM tmp_dbxrefs_with_ids
WHERE  EXISTS (SELECT 'x'
               FROM   term_xref
               WHERE  tx_term_zdb_id = tmp_term_zdb_id
                      AND tx_full_accession = tmp_xref_db
                                              || ':'
                                              || tmp_xref_accession
                      AND nvl(tx_fdb_db_id, 0) = nvl(tmp_xref_db_id, 0));

--for statistics load the new xrefs that will be added

unload to new_xrefs
SELECT tmp_term_zdb_id,
       term_ont_id,
       tmp_xref_db || ':' || tmp_xref_accession
FROM   tmp_dbxrefs_with_ids
       join term on  term_zdb_Id = tmp_term_zdb_id
       left outer join foreign_db on fdb_db_pk_id = tmp_xref_db_id;

INSERT INTO term_xref
            (tx_term_zdb_id,
             tx_full_accession,
             tx_prefix,
             tx_accession,
             tx_fdb_db_id)
SELECT DISTINCT tmp_term_zdb_id,
                tmp_xref_db
                || ':'
                || tmp_xref_accession,
                tmp_xref_db,
                tmp_xref_accession,
                tmp_xref_db_id
FROM   tmp_dbxrefs_with_ids
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   term_xref
                   WHERE  tmp_term_zdb_id = tx_term_zdb_id
                          AND tmp_xref_db
                              || ':'
                              || tmp_xref_accession = tx_full_accession
                          AND tx_prefix = tmp_xref_db
                          AND tx_accession = tmp_xref_accession
                          AND tx_fdb_db_id = tmp_xref_db_id);





