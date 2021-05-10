--liquibase formatted sql
--changeset xshao:CUR-942

update marker set mrkr_comments = (select mrkr_comments from marker m where m.mrkr_zdb_id = 'ZDB-NCCR-191112-21')
  where mrkr_zdb_id = 'ZDB-ENHANCER-190910-1';

update construct_marker_relationship 
                                set conmrkrrel_mrkr_zdb_id = 'ZDB-NCCR-191112-21'
                              where conmrkrrel_mrkr_zdb_id = 'ZDB-ENHANCER-190910-1';

update marker_history_audit 
                                set mha_mrkr_zdb_id = 'ZDB-NCCR-191112-21'
                              where mha_mrkr_zdb_id = 'ZDB-ENHANCER-190910-1';

update marker_relationship 
                                set mrel_mrkr_2_zdb_id = 'ZDB-NCCR-191112-21'
                              where mrel_mrkr_2_zdb_id = 'ZDB-ENHANCER-190910-1';

update construct_component 
                                set cc_component_zdb_id = 'ZDB-NCCR-191112-21'
                              where cc_component_zdb_id = 'ZDB-ENHANCER-190910-1';

update data_alias 
                                set dalias_data_zdb_id = 'ZDB-NCCR-191112-21'
                              where dalias_data_zdb_id = 'ZDB-ENHANCER-190910-1';

update record_attribution set recattrib_data_zdb_id = 'ZDB-NCCR-191112-21' where recattrib_data_zdb_id = 'ZDB-ENHANCER-190910-1';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-ENHANCER-190910-1';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-NCCR-191112-21' where zrepld_new_zdb_id = 'ZDB-ENHANCER-190910-1';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ENHANCER-190910-1';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ENHANCER-190910-1', 'ZDB-NCCR-191112-21');
