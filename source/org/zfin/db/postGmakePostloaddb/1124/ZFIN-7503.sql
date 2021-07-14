--liquibase formatted sql
--changeset christian:ZFIN-7503

update curation
                                set cur_pub_zdb_id = 'ZDB-PUB-190703-5'
                              where cur_pub_zdb_id = 'ZDB-PUB-200112-9';

update int_person_pub
                                set target_id = 'ZDB-PUB-190703-5'
                              where target_id = 'ZDB-PUB-200112-9';

update publication_file 
                                set pf_pub_zdb_id = 'ZDB-PUB-190703-5'
                              where pf_pub_zdb_id = 'ZDB-PUB-200112-9';

--delete from publication_file where pf_original_file_name = 'ZDB-PUB-200112-9.pdf';

update publication_note 
                                set pnote_pub_zdb_id = 'ZDB-PUB-190703-5'
                              where pnote_pub_zdb_id = 'ZDB-PUB-200112-9';

delete from pub_tracking_history where pth_pub_zdb_id = 'ZDB-PUB-200112-9';

--delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PUB-200112-9';

--update withdrawn_data set wd_new_zdb_id = 'ZDB-PUB-190703-5' where wd_new_zdb_id = 'ZDB-PUB-200112-9';

delete from publication_processing_checklist
where ppc_pub_zdb_id = 'ZDB-PUB-200112-9';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-200112-9';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-200112-9', 'ZDB-PUB-190703-5', 'merged');

-- update expression_experiment set xpatex_source_zdb_id = 'ZDB-PUB-190703-5' where xpatex_source_zdb_id = 'ZDB-PUB-200112-9';

update record_attribution as r set recattrib_source_zdb_id = 'ZDB-PUB-190703-5' where recattrib_source_zdb_id = 'ZDB-PUB-200112-9'
and not exists (
    select 'c' from record_attribution as ra where ra.recattrib_source_zdb_id = 'ZDB-PUB-190703-5'
    and ra.recattrib_data_zdb_id = r.recattrib_data_zdb_id
        );

