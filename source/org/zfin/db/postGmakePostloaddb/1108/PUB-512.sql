--liquibase formatted sql
--changeset xshao:PUB-512.sql

delete from int_person_pub where target_id = 'ZDB-PUB-151203-1' and source_id = 'ZDB-PERS-070820-1';

delete from int_person_pub where target_id = 'ZDB-PUB-151203-1' and source_id = 'ZDB-PERS-100120-4';

delete from int_person_pub where target_id = 'ZDB-PUB-151203-1' and source_id = 'ZDB-PERS-980505-6';

update curation 
                                set cur_pub_zdb_id = 'ZDB-PUB-111129-1'
                              where cur_pub_zdb_id = 'ZDB-PUB-151203-1';

update experiment_condition 
                                set expcond_exp_zdb_id = 'ZDB-EXP-120210-7'
                              where expcond_exp_zdb_id = 'ZDB-EXP-170829-1';

update expression_experiment2 
                                set xpatex_source_zdb_id = 'ZDB-PUB-111129-1'
                              where xpatex_source_zdb_id = 'ZDB-PUB-151203-1';

update int_person_pub 
                                set target_id = 'ZDB-PUB-111129-1'
                              where target_id = 'ZDB-PUB-151203-1';

update ortholog_evidence 
                                set oev_pub_zdb_id = 'ZDB-PUB-111129-1'
                              where oev_pub_zdb_id = 'ZDB-PUB-151203-1';

update pub_correspondence_received_email 
                                set pubcre_pub_zdb_id = 'ZDB-PUB-111129-1'
                              where pubcre_pub_zdb_id = 'ZDB-PUB-151203-1';

update pub_correspondence_sent_email 
                                set pubcse_pub_zdb_id = 'ZDB-PUB-111129-1'
                              where pubcse_pub_zdb_id = 'ZDB-PUB-151203-1';

update pub_correspondence_sent_tracker 
                                set pubcst_pub_zdb_id = 'ZDB-PUB-111129-1'
                              where pubcst_pub_zdb_id = 'ZDB-PUB-151203-1';

update publication_correspondence 
                                set pubcorr_pub_zdb_id = 'ZDB-PUB-111129-1'
                              where pubcorr_pub_zdb_id = 'ZDB-PUB-151203-1';

update publication_file 
                                set pf_pub_zdb_id = 'ZDB-PUB-111129-1'
                              where pf_pub_zdb_id = 'ZDB-PUB-151203-1';

update publication_note 
                                set pnote_pub_zdb_id = 'ZDB-PUB-111129-1'
                              where pnote_pub_zdb_id = 'ZDB-PUB-151203-1';

delete from experiment where exp_zdb_id = 'ZDB-EXP-170829-1';

delete from pub_tracking_history where pth_pub_zdb_id = 'ZDB-PUB-151203-1';

update experiment 
                                set exp_source_zdb_id = 'ZDB-PUB-111129-1'
                              where exp_source_zdb_id = 'ZDB-PUB-151203-1';

update figure 
                                set fig_source_zdb_id = 'ZDB-PUB-111129-1'
                              where fig_source_zdb_id = 'ZDB-PUB-151203-1';

update marker_go_term_evidence 
                                set mrkrgoev_source_zdb_id = 'ZDB-PUB-111129-1'
                              where mrkrgoev_source_zdb_id = 'ZDB-PUB-151203-1';

delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PUB-151203-1';

update withdrawn_data set wd_new_zdb_id = 'ZDB-PUB-111129-1' where wd_new_zdb_id = 'ZDB-PUB-151203-1';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-151203-1';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PUB-151203-1', 'ZDB-PUB-111129-1', 'merged');

