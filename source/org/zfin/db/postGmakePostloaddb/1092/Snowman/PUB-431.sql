--liquibase formatted sql
--changeset xshao:PUB-431

DELETE FROM mesh_heading WHERE mh_pk_id = 319184;
 
UPDATE mesh_heading_qualifier
   SET mhq_mesh_heading_id = 260599
 WHERE mhq_mesh_heading_id = 319186;
 
DELETE FROM mesh_heading WHERE mh_pk_id = 319186;
   
DELETE FROM mesh_heading_qualifier
 WHERE mhq_mesh_heading_id = 319192
   AND mhq_mesht_mesh_qualifier_id = 'Q000378';
      
UPDATE mesh_heading_qualifier
   SET mhq_mesh_heading_id = 260607
 WHERE mhq_mesh_heading_id = 319192;
 
DELETE FROM mesh_heading WHERE mh_pk_id = 319192;   

UPDATE mesh_heading 
   SET mh_pub_zdb_id = 'ZDB-PUB-980313-2'
 WHERE mh_pub_zdb_id = 'ZDB-PUB-170217-1';

UPDATE pub_db_xref
   SET pdx_pub_zdb_id = 'ZDB-PUB-980313-2'
 WHERE pdx_pub_zdb_id = 'ZDB-PUB-170217-1';

UPDATE pub_tracking_history
   SET pth_pub_zdb_id = 'ZDB-PUB-980313-2'
 WHERE pth_pub_zdb_id = 'ZDB-PUB-170217-1';

INSERT INTO withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) 
VALUES ('ZDB-PUB-170217-1', 'ZDB-PUB-980313-2',  "publication merged");

