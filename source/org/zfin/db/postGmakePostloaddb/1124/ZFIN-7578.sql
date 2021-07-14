--liquibase formatted sql
--changeset christian:PUB-469

update curation
                                set cur_pub_zdb_id = 'ZDB-PUB-060906-3'
                              where cur_pub_zdb_id = 'ZDB-PUB-090225-18';

update int_person_pub 
                                set target_id = 'ZDB-PUB-060906-3'
                              where target_id = 'ZDB-PUB-090225-18';

update publication_file 
                                set pf_pub_zdb_id = 'ZDB-PUB-060906-3'
                              where pf_pub_zdb_id = 'ZDB-PUB-090225-18';

delete from publication_file where pf_original_file_name = 'ZDB-PUB-090225-18.pdf';

update publication_note 
                                set pnote_pub_zdb_id = 'ZDB-PUB-060906-3'
                              where pnote_pub_zdb_id = 'ZDB-PUB-090225-18';

delete from pub_tracking_history where pth_pub_zdb_id = 'ZDB-PUB-090225-18';

delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PUB-090225-18';

update withdrawn_data set wd_new_zdb_id = 'ZDB-PUB-060906-3' where wd_new_zdb_id = 'ZDB-PUB-090225-18';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-090225-18';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-090225-18', 'ZDB-PUB-060906-3', 'merged');

update expression_experiment set xpatex_source_zdb_id = 'ZDB-PUB-060906-3' where xpatex_source_zdb_id = 'ZDB-PUB-090225-18';

update record_attribution as r set recattrib_source_zdb_id = 'ZDB-PUB-060906-3' where recattrib_source_zdb_id = 'ZDB-PUB-090225-18'
and not exists (
    select 'c' from record_attribution as ra where ra.recattrib_source_zdb_id = 'ZDB-PUB-060906-3'
    and ra.recattrib_data_zdb_id = r.recattrib_data_zdb_id
        );

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-FIG-061009-5';

--delete from figure where fig_zdb_id = 'ZDB-FIG-061009-5';