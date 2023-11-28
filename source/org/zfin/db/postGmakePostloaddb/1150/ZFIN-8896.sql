--liquibase formatted sql
--changeset cmpich:ZFIN-8896.sql


create temp table IF NOT EXISTS pubs_temp
(
    zdb_id     text,
    cur_zdb_id text
);

-- match on title
insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id in (6,10)
  and (title ilike  '%xenograf%' OR title ilike  '%xenotranspl%')
;


update pub_tracking_history
set pth_location_id = 19
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id in (6,10)
                and (title ilike  '%xenograf%' OR title ilike  '%xenotranspl%')
          )
;


-- match on abstract
insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id in (6,10)
  and (pub_abstract ilike  '%xenograf%' OR pub_abstract ilike  '%xenotranspl%')
;

update pub_tracking_history
set pth_location_id = 19
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id in (6,10)
                and (pub_abstract ilike  '%xenograf%' OR pub_abstract ilike  '%xenotranspl%')
          )
;

-- match on keywords
insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id in (6,10)
  and (keywords ilike  '%xenograf%' OR keywords ilike  '%xenotranspl%')
;


update pub_tracking_history
set pth_location_id = 19
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id in (6,10)
                and (keywords ilike  '%xenograf%' OR keywords ilike  '%xenotranspl%')
          )
;

-- match on mesh terms
select count(*)
from pub_tracking_history,
     publication,
     mesh_heading,
     mesh_term
where zdb_id = pth_pub_zdb_id
  and mh_pub_zdb_id = zdb_id
  and mesht_mesh_id = mh_mesht_mesh_descriptor_id
  and pth_location_id in (6,10)
  and (mesht_term_name ilike  '%xenograf%' OR mesht_term_name ilike  '%xenotranspl%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication,
     mesh_heading,
     mesh_term
where zdb_id = pth_pub_zdb_id
  and mh_pub_zdb_id = zdb_id
  and mesht_mesh_id = mh_mesht_mesh_descriptor_id
  and pth_location_id in (6,10)
  and (mesht_term_name ilike  '%xenograf%' OR mesht_term_name ilike  '%xenotranspl%')
;


update pub_tracking_history
set pth_location_id = 19
where exists(
              select *
              from pubs_temp
              where zdb_id = pth_pub_zdb_id
                and pth_location_id in (6,10)
          )
;

-- create curation records that make this pub have toxicology topic assign

update pubs_temp
set cur_zdb_id =  get_id('CUR');

insert into zdb_active_data
select cur_zdb_id from pubs_temp;

insert into curation (cur_zdb_id, cur_pub_zdb_id, cur_curator_zdb_id, cur_data_found, cur_entry_date, cur_topic)
select cur_zdb_id, zdb_id, 'ZDB-PERS-030612-1', 't', now(), 'Disease Xenograft'
from pubs_temp;
