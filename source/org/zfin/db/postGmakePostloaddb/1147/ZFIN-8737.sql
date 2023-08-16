--liquibase formatted sql
--changeset cmpich:ZFIN-8737.sql

-- remove all attributions except the oldest for marker-relationship IDs on type=contains region

select recattrib_data_zdb_id, count(*) as ct
into TEMP table temp_id
from record_attribution,
     marker_relationship
where recattrib_data_zdb_id = mrel_zdb_id
  AND mrel_type = 'contains region'
group by recattrib_data_zdb_id
having count(*) > 1;

-- total number of MREL records attributed
select count(*)
from temp_id;

-- total number of record_attributions
select sum(ct)
from temp_id;

-- the difference between them is the number of records to be delete
-- so that only one of the record_attributions is left
-- (the one with the lowest PK ID

delete
from record_attribution as r
where r.recattrib_pk_id not in (select min(ra.recattrib_pk_id)
                                from record_attribution as ra,
                                     temp_id as t
                                where t.recattrib_data_zdb_id = ra.recattrib_data_zdb_id
                                  and ra.recattrib_data_zdb_id = r.recattrib_data_zdb_id);
