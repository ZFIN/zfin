--liquibase formatted sql
--changeset xshao:ONT-677

delete from term_subset where termsub_term_zdb_id = 'ZDB-TERM-150506-1040' and termsub_subset_id = '59';

update all_term_contains 
                                set alltermcon_contained_zdb_id = 'ZDB-TERM-190222-5'
                              where alltermcon_contained_zdb_id = 'ZDB-TERM-150506-1040';

update disease_annotation 
                                set dat_term_zdb_id = 'ZDB-TERM-190222-5'
                              where dat_term_zdb_id = 'ZDB-TERM-150506-1040';

update term_subset 
                                set termsub_term_zdb_id = 'ZDB-TERM-190222-5'
                              where termsub_term_zdb_id = 'ZDB-TERM-150506-1040';

update db_link 
                                set dblink_linked_recid = 'ZDB-TERM-190222-5'
                              where dblink_linked_recid = 'ZDB-TERM-150506-1040';




update record_attribution set recattrib_data_zdb_id = 'ZDB-TERM-190222-5' where recattrib_data_zdb_id = 'ZDB-TERM-150506-1040';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-TERM-150506-1040';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-TERM-190222-5' where zrepld_new_zdb_id = 'ZDB-TERM-150506-1040';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-TERM-150506-1040';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-TERM-150506-1040', 'ZDB-TERM-190222-5');

