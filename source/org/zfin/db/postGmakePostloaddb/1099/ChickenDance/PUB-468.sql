--liquibase formatted sql
--changeset xshao:PUB-468

delete from int_person_pub where target_id = 'ZDB-PUB-170217-10' and source_id = 'ZDB-PERS-960805-478';

update curation 
                                set cur_pub_zdb_id = 'ZDB-PUB-000831-4'
                              where cur_pub_zdb_id = 'ZDB-PUB-170217-10';

update int_person_pub 
                                set target_id = 'ZDB-PUB-000831-4'
                              where target_id = 'ZDB-PUB-170217-10';

update publication_file 
                                set pf_pub_zdb_id = 'ZDB-PUB-000831-4'
                              where pf_pub_zdb_id = 'ZDB-PUB-170217-10';

update publication_note 
                                set pnote_pub_zdb_id = 'ZDB-PUB-000831-4'
                              where pnote_pub_zdb_id = 'ZDB-PUB-170217-10';

delete from pub_tracking_history where pth_pub_zdb_id = 'ZDB-PUB-170217-10';

update marker_go_term_evidence 
                                set mrkrgoev_source_zdb_id = 'ZDB-PUB-000831-4'
                              where mrkrgoev_source_zdb_id = 'ZDB-PUB-170217-10';

delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PUB-170217-10';

update withdrawn_data set wd_new_zdb_id = 'ZDB-PUB-000831-4' where wd_new_zdb_id = 'ZDB-PUB-170217-10';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-170217-10';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-170217-10', 'ZDB-PUB-000831-4', 'merged');

