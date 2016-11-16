--liquibase formatted sql
--changeset sierra:addOrder

alter table pub_tracking_status
 add (pts_pipeline_pull_down_order int8);

update pub_tracking_status
  set pts_pipeline_pull_down_order = pts_pk_id
;

update pub_tracking_status
  modify (pts_pipeline_pull_down int8 not null constraint pts_pipeline_pull_down_not_null);

alter table pub_tracking_status
 add (pts_hidden_status boolean default 'f' not null constraint pts_hidden_status_not_null);


update pub_tracking_status
  set pts_hidden_status = 't'
 where pts_status = 'INDEXED';

update pub_tracking_status
  set pts_hidden_status = 't'
 where pts_status = 'CURATED';

update pub_tracking_status
  set pts_hidden_status = 't'
 where pts_status = 'CLOSED';
