--liquibase formatted sql
--changeset sierra:pushPubsToHistory

delete from pub_tracking_history;

insert into pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_insert_date, pth_status_set_by)
 select zdb_id, (select pts_pk_id from pub_tracking_status
 			where pts_status= 'NEW'), extend (entry_time, year to second), 'ZDB-PERS-030520-1'
  from publication
 where entry_time is not null;

insert into pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_insert_date, pth_status_set_by)
 select distinct zdb_id, (select pts_pk_id from pub_tracking_status
 			where pts_status= 'READY_FOR_CURATION'),pub_indexed_date, 'ZDB-PERS-030520-1'
  from publication
  where pub_indexed_date is not null
 and pub_is_indexed = 't';

insert into pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_set_by)
 select distinct zdb_id, (select pts_pk_id from pub_tracking_status
 			where pts_status= 'READY_FOR_INDEXING'), 'ZDB-PERS-030520-1'
  from publication
  where pub_completion_date is null
  and pub_indexed_date is null
 and status in ('active','epublish','in press','Epub ahead of print');

insert into pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_insert_date, pth_status_set_by)
 select distinct zdb_id, (select pts_pk_id from pub_tracking_status
 			where pts_status= 'CLOSED'),extend (pub_completion_date, year to second), 'ZDB-PERS-030520-1'
  from publication
  where pub_completion_date is not null;

update pub_tracking_history a
 set a.pth_status_is_current = 'f'
 where a.pth_status_id < 11
and exists (select 'x' 
                        from publication
               where pub_completion_date is not null
        and pth_pub_zdb_id = zdb_id)
and pth_status_is_current = 't';

update pub_tracking_status
 set pts_status_display = 'New'
where pts_status_display = 'new';
