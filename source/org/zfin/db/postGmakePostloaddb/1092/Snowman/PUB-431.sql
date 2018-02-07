--liquibase formatted sql
--changeset xshao:PUB-431

delete from int_person_pub where target_id = 'ZDB-PUB-170217-11' and source_id = 'ZDB-PERS-030306-1';

delete from int_person_pub where target_id = 'ZDB-PUB-170217-11' and source_id = 'ZDB-PERS-960805-87';

update curation
                                set cur_pub_zdb_id = 'ZDB-PUB-980313-2'
                              where cur_pub_zdb_id = 'ZDB-PUB-170217-11';

update int_person_pub
                                set target_id = 'ZDB-PUB-980313-2'
                              where target_id = 'ZDB-PUB-170217-11';

update pub_tracking_history
                                set pth_pub_zdb_id = 'ZDB-PUB-980313-2'
                              where pth_pub_zdb_id = 'ZDB-PUB-170217-11';

update publication_note
                                set pnote_pub_zdb_id = 'ZDB-PUB-980313-2'
                              where pnote_pub_zdb_id = 'ZDB-PUB-170217-11';

delete from publication_file where pf_pub_zdb_id = 'ZDB-PUB-170217-11';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-170217-11';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-170217-11', 'ZDB-PUB-980313-2', 'merged');
