--liquibase formatted sql
--changeset cmpich:ZFIN-8521.sql


create temp table pubs_temp
(
    zdb_id     text,
    cur_zdb_id text
);

-- match on title
select count(*)
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(title) like '%nanomaterial%' or lower(title) like '%nanoparti%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(title) like '%nanomaterial%' or lower(title) like '%nanoparti%')
;


update pub_tracking_history
set pth_location_id = 17
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(title) like '%nanomaterial%' or lower(title) like '%nanoparti%')
          )
;


-- match on abstract
select count(*)
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(pub_abstract) like '%nanomaterial%' or lower(pub_abstract) like '%nanoparti%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(pub_abstract) like '%nanomaterial%' or lower(pub_abstract) like '%nanoparti%')
;

update pub_tracking_history
set pth_location_id = 17
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(pub_abstract) like '%nanomaterial%' or lower(pub_abstract) like '%nanoparti%')
          )
;

-- match on keywords
select count(*)
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(keywords) like '%nanomaterial%' or lower(keywords) like '%nanoparti%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(keywords) like '%nanomaterial%' or lower(keywords) like '%nanoparti%')
;


update pub_tracking_history
set pth_location_id = 17
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(keywords) like '%nanomaterial%' or lower(keywords) like '%nanoparti%')
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
  and pth_location_id = 6
  and (lower(mesht_term_name) like '%nanomaterial%' or lower(mesht_term_name) like '%nanoparti%')
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
  and pth_location_id = 6
  and (lower(mesht_term_name) like '%nanomaterial%' or lower(mesht_term_name) like '%nanoparti%')
;


update pub_tracking_history
set pth_location_id = 17
where exists(
              select *
              from publication,
                   mesh_heading,
                   mesh_term
              where zdb_id = pth_pub_zdb_id
                and mh_pub_zdb_id = zdb_id
                and mesht_mesh_id = mh_mesht_mesh_descriptor_id
                and pth_location_id = 6
                and (lower(mesht_term_name) like '%nanomaterial%' or lower(mesht_term_name) like '%nanoparti%')
          )
;

-- create curation records that make this pub have toxicology topic assign

update pubs_temp
set cur_zdb_id =  get_id('CUR');

insert into zdb_active_data
select cur_zdb_id from pubs_temp;

insert into curation (cur_zdb_id, cur_pub_zdb_id, cur_curator_zdb_id, cur_data_found, cur_entry_date, cur_topic)
select cur_zdb_id, zdb_id, 'ZDB-PERS-060413-1', 't', now(), 'Tox. Nanomaterials'
from pubs_temp;

