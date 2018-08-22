--liquibase formatted sql
--changeset xshao:CUR-833

delete from pub_tracking_history where pth_pub_zdb_id = 'ZDB-PUB-180731-2';

delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PUB-180731-2';

update withdrawn_data set wd_new_zdb_id = 'ZDB-PUB-180726-7' where wd_new_zdb_id = 'ZDB-PUB-180731-2';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-180731-2';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-180731-2', 'ZDB-PUB-180726-7', 'merged');

