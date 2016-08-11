-- this file loads the xrefs in the obo file
-- uses one temp table: tmp_dbxrefs


!echo "load term_dbxrefs";
create temp table tmp_dbxrefs (
term_id varchar(30),
xref_db varchar(50),
xref_accession varchar(80),
type varchar(30))
with no log;

load from term_xref.unl
  insert into tmp_dbxrefs;



create temp table tmp_dbxrefs_with_ids (
tmp_term_zdb_id varchar(30),
tmp_xref_db varchar(50),
tmp_xref_db_id int8,
tmp_xref_accession varchar(80))
with no log;

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
select tx_term_zdb_id,tx_full_accession,tx_fdb_db_id from term_xref
 where not exists (
  select 'x' from tmp_dbxrefs_with_ids
  where tx_term_zdb_id = tmp_term_zdb_id AND
        tx_fdb_db_id = tmp_xref_db_id AND
        tx_full_accession = tmp_xref_db||":"||tmp_xref_accession
  );
-- delete those records from the base table that are not found in the temp table
 delete from term_xref
 where not exists (
  select 'x' from tmp_dbxrefs_with_ids
  where tx_term_zdb_id = tmp_term_zdb_id AND
        tx_fdb_db_id = tmp_xref_db_id AND
        tx_full_accession = tmp_xref_db||":"||tmp_xref_accession
  )
  AND
  exists (
   select 'x' from term, ontology, tmp_header
   	where ont_ontology_name = default_namespace AND
          ont_pk_Id = term_ontology_id AND
          term_zdb_id = tx_term_zdb_id
  );


-- delete those records from the temp table that are already in base table
delete from tmp_dbxrefs_with_ids
where exists (
  select 'x' from term_xref
  where tx_term_zdb_id = tmp_term_zdb_id AND
        tx_full_accession = tmp_xref_db||":"||tmp_xref_accession AND
        tx_fdb_db_id = tmp_xref_db_id
)

;

--for statistics load the new xrefs that will be added

unload to new_xrefs
select tmp_term_zdb_id,tmp_xref_accession
from tmp_dbxrefs_with_ids,foreign_db
where fdb_db_pk_id=tmp_xref_db_id;



insert into term_xref (tx_term_zdb_id,tx_full_accession,tx_prefix,tx_accession,tx_fdb_db_id)
select distinct tmp_term_zdb_id,tmp_xref_db||":"||tmp_xref_accession,tmp_xref_db,tmp_xref_accession,tmp_xref_db_id from tmp_dbxrefs_with_ids
 where not exists (Select 'x' from term_xref
       	   	  	  where tmp_term_zdb_id = tx_term_zdb_id
			  and tmp_xref_db||":"||tmp_xref_accession = tx_full_accession
			  and tx_prefix = tmp_xref_db
			  and tx_accession = tmp_xref_accession
			  and tx_fdb_db_id = tmp_xref_db_id);




