--liquibase formatted sql
--changeset pm:CUR-1002
--sql generated from merge marker script



update marker set mrkr_comments = 'tdEosFP is a pseudomonomeric variant in which two copies of an engineered EosFP variant are fused to form a tandem dimer

Eos is a photo-convertible coral fluorescent protein. tdEosFP/tdEos is a pseudomonomeric variant in which two copies of an engineered EosFP variant are fused to form a tandem dimer' where mrkr_zdb_id = 'ZDB-EFG-121024-2';

delete from record_attribution where recattrib_pk_id = '101868390';

delete from marker_relationship where mrel_zdb_id = 'ZDB-MREL-140430-2';

delete from marker_relationship where mrel_zdb_id = 'ZDB-MREL-140430-3';

update construct_marker_relationship
                                set conmrkrrel_mrkr_zdb_id = 'ZDB-EFG-121024-2'
                              where conmrkrrel_mrkr_zdb_id = 'ZDB-EFG-110721-1';

update expression_experiment2
                                set xpatex_gene_zdb_id = 'ZDB-EFG-121024-2'
                              where xpatex_gene_zdb_id = 'ZDB-EFG-110721-1';

update marker_relationship
                                set mrel_mrkr_2_zdb_id = 'ZDB-EFG-121024-2'
                              where mrel_mrkr_2_zdb_id = 'ZDB-EFG-110721-1';

delete from data_alias where dalias_zdb_id = 'ZDB-DALIAS-140430-5';

update construct_component
                                set cc_component_zdb_id = 'ZDB-EFG-121024-2'
                              where cc_component_zdb_id = 'ZDB-EFG-110721-1';

update data_alias
                                set dalias_data_zdb_id = 'ZDB-EFG-121024-2'
                              where dalias_data_zdb_id = 'ZDB-EFG-110721-1';











