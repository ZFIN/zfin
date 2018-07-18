--liquibase formatted sql
--changeset xshao:PUB-471

delete from int_person_pub where target_id = 'ZDB-PUB-171210-4' and source_id = 'ZDB-PERS-021112-1';

delete from int_person_pub where target_id = 'ZDB-PUB-171210-4' and source_id = 'ZDB-PERS-160308-4';

delete from int_person_pub where target_id = 'ZDB-PUB-171210-4' and source_id = 'ZDB-PERS-960805-385';

delete from int_person_pub where target_id = 'ZDB-PUB-171210-4' and source_id = 'ZDB-PERS-970313-19';

update curation 
                                set cur_pub_zdb_id = 'ZDB-PUB-961219-23'
                              where cur_pub_zdb_id = 'ZDB-PUB-171210-4';

update int_person_pub 
                                set target_id = 'ZDB-PUB-961219-23'
                              where target_id = 'ZDB-PUB-171210-4';

update publication_file 
                                set pf_pub_zdb_id = 'ZDB-PUB-961219-23'
                              where pf_pub_zdb_id = 'ZDB-PUB-171210-4';

delete from pub_tracking_history where pth_pub_zdb_id = 'ZDB-PUB-171210-4';

delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PUB-171210-4';

update withdrawn_data set wd_new_zdb_id = 'ZDB-PUB-961219-23' where wd_new_zdb_id = 'ZDB-PUB-171210-4';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-171210-4';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-171210-4', 'ZDB-PUB-961219-23', 'merged');

