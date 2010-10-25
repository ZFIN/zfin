--begin work;

unload to 'terms_missing_obo_id.txt'
select * from term
  where term_ont_id like 'ZDB-TERM-%' ;

 --      where not exists (select 'x' from tmp_xrefs_with_fdbcont_dblink

--rollback work;

