begin work;

create temp table external_reference_temp_pre
(term_ont_id_temp varchar(50), database_id_temp varchar(20), reference_temp varchar(80))
with no log;

-- load all references into temp table
load from term_references.unl
  insert into external_reference_temp_pre;

select * from external_reference_temp_pre;

-- copy into new table
create temp table external_reference_temp
(tdr_foreign_db_id_temp int8, tdr_term_zdb_id_temp varchar(50), tdr_term_ont_id_temp varchar(50), tdr_database_id_temp varchar(20), tdr_reference_temp varchar(80))
with no log;

insert into external_reference_temp
select 0, (SELECT NULL::INTEGER FROM single), term_ont_id_temp, database_id_temp, reference_temp from external_reference_temp_pre;

create index exreftemp_reference_id_index
  on external_reference_temp (tdr_reference_temp)
 using btree in idxdbs3;

create index exreftemp_database_id_index
  on external_reference_temp (tdr_database_id_temp)
 using btree in idxdbs1;

create index exreftemp_term_id_index
  on external_reference_temp (tdr_term_zdb_id_temp)
 using btree in idxdbs1;

create index exreftemp_term_ont_id_index
  on external_reference_temp (tdr_term_ont_id_temp)
 using btree in idxdbs2;

!echo "Number of records";

select count(*) from external_reference_temp;

-- add the foreign_db ids
-- populate the tdr_foreign_db_id_temp with fdb_db_pk_id
update external_reference_temp
set tdr_foreign_db_id_temp =
(select fdb_db_pk_id from foreign_db where
 lower(fdb_db_name) = lower(tdr_database_id_temp)
 ) where exists (
 select 'x' from foreign_db where
 lower(fdb_db_name) = lower(tdr_database_id_temp) );

-- update http references:
-- [http://zfin.org] turns into '//zfin.org' link as it is split at the colon
-- so we have to add the 'http:' part again.
!echo "Number of HTTP references";

update external_reference_temp
 set tdr_reference_temp = 'http:'||tdr_reference_temp
 where exists (
  select 'x' from foreign_db where
  tdr_foreign_db_id_temp = fdb_db_pk_id AND
  upper(fdb_db_name) = 'HTTP');

-- put the ZFA: in front of the reference back as it was parsed out due to the semicolon
update external_reference_temp
 set tdr_reference_temp = 'ZFA:'||tdr_reference_temp
 where exists (
  select 'x' from foreign_db where
  tdr_foreign_db_id_temp = fdb_db_pk_id AND
  upper(fdb_db_name) = 'ZFA');

-- put the UBERON_ in front of the reference back as it was parsed out
update external_reference_temp
 set tdr_reference_temp = 'UBERON_'||tdr_reference_temp
 where exists (
  select 'x' from foreign_db where
  tdr_foreign_db_id_temp = fdb_db_pk_id AND
  upper(fdb_db_name) = 'UBERON');

-- populate the term_zdb_ids from the obo ids
update external_reference_temp
 set tdr_term_zdb_id_temp =
   (select term_zdb_id from term where
   term_ont_id = tdr_term_ont_id_temp);

select * From external_reference_temp;

-- remove the records that do not have a foreign_db (non-null query) associated with
delete from external_reference_temp
where tdr_foreign_db_id_temp = 0 or not exists (
select 'x' from foreign_db where
tdr_foreign_db_id_temp = fdb_db_pk_id
);

select * From external_reference_temp;

-- remove the records pointing to a TAO foreign db as they should be suppressed
delete from external_reference_temp
where tdr_foreign_db_id_temp = 0 or exists (
select 'x' from foreign_db where
tdr_foreign_db_id_temp = fdb_db_pk_id
AND fdb_db_name in ('TAO')
);


 -- delete those records from the base table that are not found in the temp table
 delete from external_reference
 where not exists (
  select 'x' from external_reference_temp
  where exref_data_zdb_id = tdr_term_zdb_id_temp AND
        exref_foreign_db_id = tdr_foreign_db_id_temp AND
        exref_reference = tdr_reference_temp
  ) AND
  exists (
   select 'x' from term, ontology, tmp_header
   	where ont_ontology_name = default_namespace AND
          ont_pk_Id = term_ontology_id AND
          term_zdb_id = exref_data_zdb_id
  );


-- select * from external_reference_temp;

-- delete those records from the temp table that are already in base table
delete from external_reference_temp
where exists (
  select 'x' from external_reference
  where exref_data_zdb_id = tdr_term_zdb_id_temp AND
        exref_foreign_db_id = tdr_foreign_db_id_temp AND
        exref_reference = tdr_reference_temp
);


--select * From external_reference_temp;

--select * From external_reference_temp where
--tdr_term_zdb_id_temp is null;

-- add the missing records into base table
insert into external_reference (exref_data_zdb_id, exref_foreign_db_id, exref_reference,exref_attype_type)
select tdr_term_zdb_id_temp,  tdr_foreign_db_id_temp, tdr_reference_temp, 'anatomy definition'
from external_reference_temp
where tdr_term_zdb_id_temp is not null;


-- remove temp tables
drop table external_reference_temp_pre;
drop table external_reference_temp;

--rollback work;
commit work;
