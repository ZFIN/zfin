--liquibase formatted sql
--changeset rtaylor:ZFIN-9081.sql

--
-- The changes in earlier pull request for ZFIN-9081 resulted in duplicate information
-- in the updates table.  The same information is already captured in the pub_tracking_history.
--

-- First create a temp table with the information from pub_tracking_history that we need to compare to the updates table.
drop table if exists pub_updates_summary;
create temp table pub_updates_summary as
select pth_pk_id, pth_pub_zdb_id, pth_status_set_by, pts.pts_status_display, pth_status_insert_date, ptl_location_display from pub_tracking_history pth
       left join pub_tracking_status pts on pth.pth_status_id = pts.pts_pk_id
       left join pub_tracking_location ptl on pth.pth_location_id = ptl.ptl_pk_id;

-- Next create a temp table that contains a side-by-side comparison of the updates table and the pub_updates_summary table.
-- We are matching rows that have the same user, pub, and status, and that were inserted within 3 seconds of each other.
drop table if exists duplicate_updates_details;
create temp table duplicate_updates_details as
select * from updates u
                  join pub_updates_summary pup
                       on u.rec_id = pup.pth_pub_zdb_id
                           and u.submitter_id = pup.pth_status_set_by
                           and (u.upd_when - '3 seconds'::interval, u.upd_when + '3 seconds'::interval) overlaps (pth_status_insert_date - '3 seconds'::interval, pth_status_insert_date + '3 seconds'::interval)
                           and new_value = pts_status_display
where rec_id like 'ZDB-PUB-%'
  and field_name = 'status'
order by upd_when desc ;

-- Now we can delete the rows from the updates table that are duplicates of the information in the pub_tracking_history table.
delete from updates where upd_pk_id in (select upd_pk_id from duplicate_updates_details);
