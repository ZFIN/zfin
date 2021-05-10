--liquibase formatted sql
--changeset sierra:remove_num_auths.sql

alter table publication
 drop num_auths;

update pub_tracking_location
  set ptl_location = 'PUB_INDEXER_1'
 where ptl_location = 'pub_indexer_1';

update pub_tracking_location
  set ptl_location = 'PUB_INDEXER_2'
 where ptl_location = 'pub_indexer_2';

update pub_tracking_location
  set ptl_location = 'PUB_INDEXER_3'
 where ptl_location = 'pub_indexer_3';


insert into pub_tracking_location (ptl_location,
       ptl_location_display,
       ptl_role,
       ptl_location_definition,
       ptl_display_order)
values ('ZEBRASHARE','zebraShare','curator','Papers from the zebraShare direct submission pipeline.',8);

