--liquibase formatted sql
--changeset sierra:PUB-417

delete from pub_tracking_history
 where pth_pub_zdb_id = 'ZDB-PUB-150903-30';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id)
 values ('ZDB-PUB-150903-30','ZDB-PUB-090520-32');

delete from zdb_active_source 
where zactvs_zdb_id = 'ZDB-PUB-150903-30';

