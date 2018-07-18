--liquibase formatted sql
--changeset xshao:PUB-469

delete from int_person_pub where target_id = 'ZDB-PUB-140325-6' and source_id = 'ZDB-PERS-060815-1';

update curation 
                                set cur_pub_zdb_id = 'ZDB-PUB-140606-4'
                              where cur_pub_zdb_id = 'ZDB-PUB-140325-6';

update int_person_pub 
                                set target_id = 'ZDB-PUB-140606-4'
                              where target_id = 'ZDB-PUB-140325-6';

update publication_file 
                                set pf_pub_zdb_id = 'ZDB-PUB-140606-4'
                              where pf_pub_zdb_id = 'ZDB-PUB-140325-6';

update publication_note 
                                set pnote_pub_zdb_id = 'ZDB-PUB-140606-4'
                              where pnote_pub_zdb_id = 'ZDB-PUB-140325-6';

delete from pub_tracking_history where pth_pub_zdb_id = 'ZDB-PUB-140325-6';

delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PUB-140325-6';

update withdrawn_data set wd_new_zdb_id = 'ZDB-PUB-140606-4' where wd_new_zdb_id = 'ZDB-PUB-140325-6';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-140325-6';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-140325-6', 'ZDB-PUB-140606-4', 'merged');

