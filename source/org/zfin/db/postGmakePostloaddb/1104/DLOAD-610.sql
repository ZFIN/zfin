--liquibase formatted sql
--changeset xshao:DLOAD-610

update feature_marker_relationship
   set fmrel_mrkr_zdb_id = 'ZDB-GENE-121004-1'
 where fmrel_ftr_zdb_id in ('ZDB-ALT-130411-2117', 
                            'ZDB-ALT-130411-4235',
                            'ZDB-ALT-160601-3398');

