--liquibase formatted sql
--changeset cmpich:ZFIN-9258.sql

insert into marker_history_reason
VALUES ('Renamed to conform with Ensembl transcript nomenclature');
insert into marker_history_reason
VALUES ('Same transcript');


select count(*)
from marker
where exists(select * from transcript where mrkr_zdb_id = tscript_mrkr_zdb_id and mrkr_abbrev != mrkr_name);

CREATE TEMP TABLE marker_name_temp as
select mrkr_zdb_id, mrkr_name, mrkr_abbrev
from marker
where exists(select * from transcript where mrkr_zdb_id = tscript_mrkr_zdb_id and mrkr_abbrev != mrkr_name);

-- still problem transcript as they have a conflict when setting abbrev = name
select *
from marker as m
where exists(
        select * from marker_name_temp as mnt where mnt.mrkr_zdb_id = m.mrkr_zdb_id
    )
  and exists(select * from marker as m2 where m2.mrkr_abbrev = m.mrkr_name);

-- remove conflicting records
delete from marker_name_temp where
        mrkr_zdb_id in ('ZDB-TSCRIPT-090929-18263', 'ZDB-TSCRIPT-090929-18262', 'ZDB-TSCRIPT-090929-3084', 'ZDB-TSCRIPT-090929-5042',
                              'ZDB-TSCRIPT-090929-14197', 'ZDB-TSCRIPT-110325-1463');

update marker as m
set mrkr_abbrev = lower(mrkr_name),
    mrkr_name   = lower(mrkr_name)
where exists(
        select * from marker_name_temp as mnt where mnt.mrkr_zdb_id = m.mrkr_zdb_id
    );

create temp table data_alias_temp as
select * from data_alias where exists (
                                       select * from marker_name_temp where mrkr_zdb_id = dalias_data_zdb_id and dalias_alias = mrkr_abbrev
                                   );


create temp table marker_alias_temp as
select get_id('DALIAS') as id, mrkr_abbrev, mrkr_zdb_id
from marker_name_temp;

delete from marker_alias_temp as mat where
    exists (
            select * from data_alias where exists (
                                                   select * from marker_name_temp as mn where mn.mrkr_zdb_id = dalias_data_zdb_id and dalias_alias = mrkr_abbrev
                                                                                          and mat.mrkr_zdb_id = mn.mrkr_zdb_id
                                               )
        );

insert into zdb_active_data
select id from marker_alias_temp;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_alias_lower, dalias_group_id)
select id, mrkr_zdb_id, mrkr_abbrev, lower(mrkr_abbrev), 1
from marker_alias_temp;

insert into marker_alias_temp
    select dalias_zdb_id, dalias_alias, dalias_data_zdb_id from data_alias_temp;

create temp table id_temp as
    select get_id('NOMEN') as id,
           mrkr_zdb_id
    from marker_name_temp;

insert into zdb_active_data
select id from id_temp;


insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date,
                            mhist_mrkr_prev_name, mhist_mrkr_abbrev_on_mhist_date, mhist_mrkr_name_on_mhist_date, mhist_dalias_zdb_id)
select it.id,
       mn.mrkr_zdb_id,
       'reassigned',
       'Renamed to conform with Ensembl transcript nomenclature',
       now(),
       mn.mrkr_abbrev,
       mn.mrkr_name,
       mn.mrkr_name,
       mat.id
from marker_name_temp as mn, id_temp as it, marker_alias_temp as mat
where mn.mrkr_zdb_id = it.mrkr_zdb_id
and mn.mrkr_zdb_id = mat.mrkr_zdb_id;

