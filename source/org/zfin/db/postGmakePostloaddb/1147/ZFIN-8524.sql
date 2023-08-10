--liquibase formatted sql
--changeset cmpich:ZFIN-8524.sql


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
  and (lower(title) like '%therap%' or lower(title) like '%preclinical drug%' or lower(title) like '%efficacy%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(title) like '%therap%' or lower(title) like '%preclinical drug%' or lower(title) like '%efficacy%')
;


update pub_tracking_history
set pth_location_id = 15
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(title) like '%therap%' or lower(title) like '%preclinical drug%' or lower(title) like '%efficacy%')
          )
;


-- match on abstract
select count(*)
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(pub_abstract) like '%therap%' or lower(pub_abstract) like '%preclinical drug%' or lower(pub_abstract) like '%efficacy%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(pub_abstract) like '%therap%' or lower(pub_abstract) like '%preclinical drug%' or lower(pub_abstract) like '%efficacy%')
;

update pub_tracking_history
set pth_location_id = 15
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(pub_abstract) like '%therap%' or lower(pub_abstract) like '%preclinical drug%' or lower(pub_abstract) like '%efficacy%')
          )
;

-- match on keywords
select count(*)
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(keywords) like '%therap%' or lower(keywords) like '%preclinical drug%' or lower(keywords) like '%efficacy%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(keywords) like '%therap%' or lower(keywords) like '%preclinical drug%' or lower(keywords) like '%efficacy%'
  and (lower(keywords) like '%therap%' or lower(keywords) like '%preclinical drug%'))
;


update pub_tracking_history
set pth_location_id = 15
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(keywords) like '%therap%' or lower(keywords) like '%preclinical drug%' or lower(keywords) like '%efficacy%'
                and (lower(keywords) like '%therap%' or lower(keywords) like '%preclinical drug%')
          ))
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
  and (lower(mesht_term_name) like '%therap%' or lower(mesht_term_name) like '%preclinical drug%' or lower(mesht_term_name) like '%efficacy%')
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
  and (lower(mesht_term_name) like '%therap%' or lower(mesht_term_name) like '%preclinical drug%' or lower(mesht_term_name) like '%efficacy%')
;


update pub_tracking_history
set pth_location_id = 15
where exists(
              select *
              from publication,
                   mesh_heading,
                   mesh_term
              where zdb_id = pth_pub_zdb_id
                and mh_pub_zdb_id = zdb_id
                and mesht_mesh_id = mh_mesht_mesh_descriptor_id
                and pth_location_id = 6
                and (lower(mesht_term_name) like '%therap%' or lower(mesht_term_name) like '%preclinical drug%' or lower(mesht_term_name) like '%efficacy%' )
          )
;

-- create curation records that make this pub have toxicology topic assign

update pubs_temp
set cur_zdb_id =  get_id('CUR');

insert into zdb_active_data
select cur_zdb_id from pubs_temp;

insert into curation (cur_zdb_id, cur_pub_zdb_id, cur_curator_zdb_id, cur_data_found, cur_entry_date, cur_topic)
select cur_zdb_id, zdb_id, 'ZDB-PERS-030612-1', 't', now(), 'Tox. Drug Characterization/Treatment'
from pubs_temp;
