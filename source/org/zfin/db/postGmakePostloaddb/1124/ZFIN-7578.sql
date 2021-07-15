--liquibase formatted sql
--changeset christian:PUB-469

update curation
                                set cur_pub_zdb_id = 'ZDB-PUB-060906-3'
                              where cur_pub_zdb_id = 'ZDB-PUB-090225-18';

-- record already exists, so removing this but in general this needs to be done.
--update int_person_pub
--                                set target_id = 'ZDB-PUB-060906-3'
--                              where target_id = 'ZDB-PUB-090225-18';

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

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-090225-18', 'ZDB-PUB-060906-3', 'merged');

delete from expression_result where xpatres_fig_zdb_id = 'ZDB-FIG-061009-5';

delete from expression_pattern_figure where xpatfig_fig_zdb_id = 'ZDB-FIG-061009-5';
-- delete expressions from merged into pub
delete from expression_experiment where xpatex_source_zdb_id = 'ZDB-PUB-060906-3';
delete from expression_experiment2 where xpatex_source_zdb_id = 'ZDB-PUB-060906-3';

update expression_experiment set xpatex_source_zdb_id = 'ZDB-PUB-060906-3' where xpatex_source_zdb_id = 'ZDB-PUB-090225-18';
update expression_experiment2 set xpatex_source_zdb_id = 'ZDB-PUB-060906-3' where xpatex_source_zdb_id = 'ZDB-PUB-090225-18';

--update record_attribution as r set recattrib_source_zdb_id = 'ZDB-PUB-060906-3' where recattrib_source_zdb_id = 'ZDB-PUB-090225-18'
--and not exists (
--    select 'c' from record_attribution as ra where ra.recattrib_source_zdb_id = 'ZDB-PUB-060906-3'
--    and ra.recattrib_data_zdb_id = r.recattrib_data_zdb_id
--        );

update figure set fig_source_zdb_id = 'ZDB-PUB-060906-3' where fig_zdb_id = 'ZDB-FIG-090309-22';

update experiment set exp_source_zdb_id = 'ZDB-PUB-060906-3' where exp_source_zdb_id = 'ZDB-PUB-090225-18';

--delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-090225-18';
delete from publication where zdb_id = 'ZDB-PUB-090225-18';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-FIG-061009-5';

delete from figure where fig_zdb_id = 'ZDB-FIG-061009-5';
