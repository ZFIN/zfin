--liquibase formatted sql
--changeset xshao:PUB-444

UPDATE curation
SET cur_pub_zdb_id = 'ZDB-PUB-020102-1'
WHERE cur_pub_zdb_id = 'ZDB-PUB-170217-9';


UPDATE publication_note
SET pnote_pub_zdb_id = 'ZDB-PUB-020102-1'
WHERE pnote_pub_zdb_id = 'ZDB-PUB-170217-9';


UPDATE pub_tracking_history
SET pth_pub_zdb_id = 'ZDB-PUB-020102-1'
WHERE pth_pub_zdb_id = 'ZDB-PUB-170217-9';

DELETE FROM publication_file
WHERE pf_pub_zdb_id = 'ZDB-PUB-170217-9';

DELETE FROM zdb_active_source 
WHERE zactvs_zdb_id = 'ZDB-PUB-170217-9';

INSERT INTO withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) 
VALUES ('ZDB-PUB-170217-9', 'ZDB-PUB-020102-1',  "publication merged");
