--liquibase formatted sql
--changeset pm:CUR-1002
--sql generated from merge marker script



update marker set mrkr_comments = 'tdEosFP is a pseudomonomeric variant in which two copies of an engineered EosFP variant are fused to form a tandem dimer

Eos is a photo-convertible coral fluorescent protein. tdEosFP/tdEos is a pseudomonomeric variant in which two copies of an engineered EosFP variant are fused to form a tandem dimer' where mrkr_zdb_id = 'ZDB-EFG-110721-1';




update construct_marker_relationship
                                set conmrkrrel_mrkr_zdb_id = 'ZDB-EFG-110721-1'
                              where conmrkrrel_mrkr_zdb_id = 'ZDB-EFG-121024-2';

                              update marker_relationship
                                set mrel_mrkr_2_zdb_id = 'ZDB-EFG-110721-1'
                              where mrel_mrkr_2_zdb_id = 'ZDB-EFG-121024-2';

update expression_experiment2
                                set xpatex_gene_zdb_id = 'ZDB-EFG-110721-1'
                              where xpatex_gene_zdb_id = 'ZDB-EFG-121024-2';

update marker_relationship
                                set mrel_mrkr_2_zdb_id = 'ZDB-EFG-110721-1'
                              where mrel_mrkr_2_zdb_id = 'ZDB-EFG-121024-2';



update construct_component
                                set cc_component_zdb_id = 'ZDB-EFG-110721-1'
                              where cc_component_zdb_id = 'ZDB-EFG-121024-2';

update data_alias
                                set dalias_data_zdb_id = 'ZDB-EFG-110721-1'
                              where dalias_data_zdb_id = 'ZDB-EFG-121024-2';





update record_attribution set recattrib_data_zdb_id = 'ZDB-EFG-110721-1' where recattrib_data_zdb_id = 'ZDB-EFG-121024-2';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-EFG-121024-2';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-EFG-110721-1' where zrepld_new_zdb_id = 'ZDB-EFG-121024-2';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-EFG-121024-2';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-EFG-121024-2', 'ZDB-EFG-110721-1');





