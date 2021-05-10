--begin work;

unload to 'terms_missing_obo_id.txt'
select * from term
  where term_ont_id like 'ZDB-TERM-%' ;

 --      where not exists (select 'x' from tmp_xrefs_with_fdbcont_dblink

select * from term;

INSERT  INTO tmp_rels_zdb (ttermrel_ont_id_1, ttermrel_ont_id_2, ttermrel_type)
  select termrel_term_1_id, termrel_term_2_id, termrel_type
   from tmp_rels;

!echo "Delete from temp table";
delete from tmp_zfin_rels
  where termrel_term_2_zdb_id is null;

load from ontology_header.unl
  insert into tmp_header;

DELETE from tmp_zfin_rels
  where termrel_term_2_zdb_id is null;


UPDATE
     tmp_syndef
     set scoper = trim(scoper);

--rollback work;

