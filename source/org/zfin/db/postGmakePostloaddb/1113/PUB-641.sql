--liquibase formatted sql
--changeset pm:PUB-641

--followin 2 lines are changes for PUB-642 adding Jon to list of "student curators"
update zdb_submitters set is_curator='t' where zdb_id='ZDB-PERS-051031-1';
update zdb_submitters set is_curator='f' where zdb_id='ZDB-PERS-190501-1';
drop table if exists tmp_pub1;


select pth_pub_zdb_id,pth_claimed_by, pth_status_set_by, pth_location_id, pth_status_id,pth_status_is_current into tmp_pub1 from pub_tracking_history where pth_claimed_by='ZDB-PERS-190501-1' and pth_status_is_current='t';

insert into pub_tracking_history(pth_pub_zdb_id, pth_status_id, pth_status_set_by,pth_location_id,pth_claimed_by,pth_status_is_current)
select pth_pub_zdb_id,pth_status_id,pth_status_set_by,pth_location_id,  'ZDB-PERS-190501-2','t' from tmp_pub1;
drop table tmp_pub1;
