--liquibase formatted sql
--changeset xshao:PUB-411

UPDATE curation
   SET cur_pub_zdb_id = 'ZDB-PUB-170218-14'
 WHERE cur_pub_zdb_id = 'ZDB-PUB-010220-6';

DELETE FROM mesh_heading
 WHERE mh_pub_zdb_id = 'ZDB-PUB-010220-6';

DELETE FROM pub_tracking_history
 WHERE pth_pub_zdb_id = 'ZDB-PUB-010220-6';

DELETE FROM publication_file 
WHERE pf_pub_zdb_id = 'ZDB-PUB-010220-6';

DELETE FROM zdb_active_source 
WHERE zactvs_zdb_id = 'ZDB-PUB-010220-6';

INSERT INTO withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) 
VALUES ('ZDB-PUB-010220-6', 'ZDB-PUB-170218-14',  "publication merged");
