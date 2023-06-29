--liquibase formatted sql
--changeset cmpich:ZFIN-8523.sql


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
  and (lower(title) like '%plants%' or lower(title) like '%extract%' or lower(title) like '%ethnopharmacolog%' or lower(title) like '%traditional medicine%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(title) like '%plants%' or lower(title) like '%extract%' or lower(title) like '%ethnopharmacolog%' or lower(title) like '%traditional medicine%')
;


update pub_tracking_history
set pth_location_id = 18
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(title) like '%plants%' or lower(title) like '%extract%' or lower(title) like '%ethnopharmacolog%' or lower(title) like '%traditional medicine%')
          )
;


-- match on abstract
select count(*)
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(pub_abstract) like '%plants%' or lower(pub_abstract) like '%extract%' or lower(pub_abstract) like '%ethnopharmacolog%' or lower(pub_abstract) like '%traditional medicine%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(pub_abstract) like '%plants%' or lower(pub_abstract) like '%extract%' or lower(pub_abstract) like '%ethnopharmacolog%' or lower(pub_abstract) like '%traditional medicine%')
;

update pub_tracking_history
set pth_location_id = 18
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(pub_abstract) like '%plants%' or lower(pub_abstract) like '%extract%' or lower(pub_abstract) like '%ethnopharmacolog%' or lower(pub_abstract) like '%traditional medicine%')
          )
;

-- match on keywords
select count(*)
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(keywords) like '%plants%' or lower(keywords) like '%extract%' or lower(keywords) like '%ethnopharmacolog%' or lower(keywords) like '%traditional medicine%')
;

insert into pubs_temp (zdb_id)
select zdb_id
from pub_tracking_history,
     publication
where zdb_id = pth_pub_zdb_id
  and pth_location_id = 6
  and (lower(keywords) like '%plants%' or lower(keywords) like '%extract%' or lower(keywords) like '%ethnopharmacolog%' or lower(keywords) like '%traditional medicine%')
  and (lower(keywords) like '%plants%' or lower(keywords) like '%extract%')
;


update pub_tracking_history
set pth_location_id = 18
where exists(
              select *
              from publication
              where zdb_id = pth_pub_zdb_id
                and pth_location_id = 6
                and (lower(keywords) like '%plants%' or lower(keywords) like '%extract%' or lower(keywords) like '%ethnopharmacolog%' or lower(keywords) like '%traditional medicine%')
                and (lower(keywords) like '%plants%' or lower(keywords) like '%extract%')
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
  and (lower(mesht_term_name) like '%plants%' or lower(mesht_term_name) like '%extract%' or lower(mesht_term_name) like '%ethnopharmacolog%' or lower(mesht_term_name) like '%traditional medicine%')
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
  and (lower(mesht_term_name) like '%plants%' or lower(mesht_term_name) like '%extract%' or lower(mesht_term_name) like '%ethnopharmacolog%' or lower(mesht_term_name) like '%traditional medicine%')
;


update pub_tracking_history
set pth_location_id = 18
where exists(
              select *
              from publication,
                   mesh_heading,
                   mesh_term
              where zdb_id = pth_pub_zdb_id
                and mh_pub_zdb_id = zdb_id
                and mesht_mesh_id = mh_mesht_mesh_descriptor_id
                and pth_location_id = 6
                and (lower(mesht_term_name) like '%plants%' or lower(mesht_term_name) like '%extract%' or lower(mesht_term_name) like '%ethnopharmacolog%' or lower(mesht_term_name) like '%traditional medicine%')
          )
;

-- create curation records that make this pub have toxicology topic assign

update pubs_temp
set cur_zdb_id =  get_id('CUR');

insert into zdb_active_data
select cur_zdb_id from pubs_temp;

insert into curation (cur_zdb_id, cur_pub_zdb_id, cur_curator_zdb_id, cur_data_found, cur_entry_date, cur_topic)
select cur_zdb_id, zdb_id, 'ZDB-PERS-030612-1', 't', now(), 'Tox. Natural Product Characterization'
from pubs_temp;

drop table pubs_temp;