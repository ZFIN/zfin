begin work ;

delete from ensdart_ottdart_mapping;

\copy ensdart_ottdart_mapping from ensdarT_dbacc.unl with DELIMITER ',';

create temp table tmp_ensdart_map (ensdarg_gene_stable_id text,
       ensdart_transcript_stable_id text,
       ensdart_transcript_stable_id_version text,
       ensdart_gene_id_stable_version text,
       transcript_name text,
       entrez_gene_transcript_name_id text,
       zfin_id text,
       vega_translational_id text)
;

\copy tmp_ensdart_map from mart_export.txt with  DELIMITER ',';

select * from tmp_ensdart_map
  where ensdart_transcript_stable_id = 'ENSDART00000137923';

create temp table tmp_counter (counter int, transcript_name text);

insert into tmp_counter
select count(*) as counter, transcript_name
 from tmp_ensdart_map
group by transcript_name having count(*) > 1;

\copy (select ensdart_transcript_stable_id, ensdart_transcript_stable_id_version, tmp_counter.transcript_name, zfin_id from tmp_counter, tmp_ensdart_map where tmp_counter.transcript_name = tmp_ensdart_map.transcript_name order by tmp_counter.transcript_name) to duplicate_names.txt;

--insert into ensdart_name_mapping(enm_ensdart_stable_id,
--       enm_ensdart_versioned_id,
--       enm_ensdarg_id, 
--       enm_tscript_zdb_id,
--       enm_ensembl_tscript_name,
--       enm_ottdart_id)
--  select distinct ensdart_transcript_stable_id, 
--                      ensdart_transcript_stable_id_version,
--                      ensdarg_gene_stable_id,
--                      tscript_mrkr_zdb_id,
--                      lower(transcript_name),
--                      ottdart_id
--     from transcript, ensdart_ottdart_mapping as eom, tmp_ensdart_map as em
--     where tscript_load_id = eom.ottdart_id
--     and eom.ensdart_id = ensdart_transcript_stable_id;


                      
--drop index if exists enm_ensdart_id_index;

--create index enm_ensdart_id_index
--  on ensdart_name_mapping(enm_tscript_zdb_id);

--\copy (select distinct tscript_mrkr_zdb_id, tscript_load_id, mrkr_abbrev from transcript, marker where tscript_mrkr_zdb_id = mrkr_zdb_id and not exists (Select 'x' from ensdart_name_mapping where ottdart_id = tscript_load_id) and tscript_status_id != '1' ) to 'missing_ottdart_ensdart_mapping.txt';

--\copy (select distinct tscript_mrkr_zdb_id, tscript_load_id, mrkr_abbrev, ensdart_stable_id from transcript, marker, ensdart_name_mapping where tscript_mrkr_zdb_id = mrkr_zdb_id and tscript_load_id = ottdart_id) to 'mapped_ottdart_ensdart.txt' ;

--create temp table tmp_dups (tscript_name text, counter int);

--insert into tmp_dups
--  select enm_ensembl_tscript_name, count(*)
--    from ensdart_name_mapping
--    where enm_ensembl_tscript_name is not null
--    group by enm_ensembl_tscript_name having count(*) > 1;

--create temp table tmp_name_exists (nmrkr_zdb_id text);

--insert into tmp_name_exists(nmrkr_zdb_id)
--select mrkr_zdb_id
--  from marker
-- where mrkr_abbrev in (select enm_ensembl_tscript_name from ensdart_name_mapping where mrkr_Zdb_id = enm_tscript_zdb_id);

--select * from ensdart_name_mapping where enm_ensembl_tscript_name = 'them6-201';

--select count(*) from tmp_dups;

--select * from ensdart_name_mapping 
-- where enm_ensdart_stable_id = 'ENSDART00000137923';

--delete from ensdart_name_mapping
--where exists (select 'x' from tmp_dups where enm_ensembl_tscript_name = tscript_name);

--create temp table tmp_alias (dalias_id text, alias text, mrkr_id text);

--insert into tmp_alias (alias,mrkr_id)
-- select distinct mrkr_abbrev, mrkr_zdb_id
--   from marker, ensdart_name_mapping 
--   where mrkr_zdb_id = enm_tscript_zdb_id;

--update tmp_alias
-- set dalias_id = get_id('DALIAS');

--insert into zdb_Active_Data (Zactvd_zdb_id)
--  select dalias_id 
--    from tmp_alias;

--insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
--  select dalias_id, mrkr_id, alias, 1
--   from tmp_alias
-- where not exists (Select 'x' from data_alias where dalias_data_zdb_id = mrkr_id
-- and dalias_alias = alias and dalias_group_id = 1);

--update marker
-- set (mrkr_abbrev,mrkr_name) = (select enm_ensembl_tscript_name, enm_ensembl_tscript_name from ensdart_name_mapping where mrkr_Zdb_id = enm_tscript_zdb_id)
-- where exists (Select 'x' from ensdart_name_mapping where mrkr_Zdb_id = enm_tscript_zdb_id)
-- and not exists (select 'x' from tmp_name_exists where nmrkr_zdb_id = mrkr_zdb_id);

--select count(*), mrkr_abbrev 
--  from marker
--group by mrkr_abbrev
-- having count(*) > 1;

--\copy (select ensdart_stable_id, ensdart_versioned_id, ensdarg_id, zfin_gene_zdb_id, ensembl_tscript_name, mrkr_name, ottdart_id from ensdart_name_mapping, marker where zfin_gene_zdb_id = mrkr_zdb_id) to name_updates.txt with delimiter ' ';

create temp table tmp_ottdart_dups (counter int, ottdart_id text);
insert into tmp_ottdart_dups (counter, ottdart_id)
select count(*), enm_ottdart_id from ensdart_name_mapping
 group by enm_ensdart_stable_id, enm_ottdart_id
having count(*) > 1;

--create index ottdart_index on tmp_ottdart_dups (ottdart_id);

update transcript
 set tscript_ensdart_id = (select enm_ensdart_stable_id from ensdart_name_mapping
 where tscript_load_id = enm_ottdart_id)
 where not exists (Select 'x' from tmp_ottdart_dups where ottdart_id = tscript_load_id);
                  
select count(*) From transcript where tscript_ensdart_id is not null;
select count(*) From transcript where tscript_ensdart_id is null;


--delete from db_link
--  where dblink_acc_num like 'ENSDART%';

--create temp table tscript_dblink (td_tscript_zdb_id text,
--                       td_dblink_id text);

--insert into tscript_dblink (td_tscript_zdb_id, td_dblink_id)
--  select tscript_mrkr_zdb_id, get_id('DBLINK')
--   from transcript
--   where tscript_ensdart_id is not null;

--insert into zdb_active_data (zactvd_zdb_id)
-- select td_dblink_id from tscript_dblink;

--insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_Acc_num, dblink_fdbcont_zdb_id)
--  select td_dblink_id, td_tscript_zdb_id, tscript_ensdart_id, 'ZDB-FDBCONT-110301-1'
--   from tscript_dblink, transcript
-- where td_tscript_zdb_id = tscript_mrkr_Zdb_id
--  ; 

--insert into record_attribution (recattrib_data_Zdb_id, recattrib_source_zdb_id)
--  select td_dblink_id, 'ZDB-PUB-190221-12'
--    from tscript_dblink, transcript
--  where td_tscript_zdb_id = tscript_mrkr_zdb_id;

commit work;
--rollback work;
