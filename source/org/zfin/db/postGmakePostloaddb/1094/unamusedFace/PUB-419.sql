--liquibase formatted sql
--changeset xshao:PUB-419

update curation
                                set cur_pub_zdb_id = 'ZDB-PUB-050216-43'
                              where cur_pub_zdb_id = 'ZDB-PUB-150909-9';

update pub_tracking_history
                                set pth_pub_zdb_id = 'ZDB-PUB-050216-43'
                              where pth_pub_zdb_id = 'ZDB-PUB-150909-9';

delete from publication_file where pf_pub_zdb_id = 'ZDB-PUB-150909-9'; 

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-150909-9';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-150909-9', 'ZDB-PUB-050216-43', 'merged');

