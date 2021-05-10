--liquibase formatted sql
--changeset xshao:DLOAD-605

update feature_marker_relationship
   set fmrel_mrkr_zdb_id = 'ZDB-GENE-170726-1'
 where fmrel_ftr_zdb_id in ('ZDB-ALT-130411-2840', 
                            'ZDB-ALT-131217-14565', 
                            'ZDB-ALT-131217-14791',
                            'ZDB-ALT-160601-1907',
                            'ZDB-ALT-160601-1908');

