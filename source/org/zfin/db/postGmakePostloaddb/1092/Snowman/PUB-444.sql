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
