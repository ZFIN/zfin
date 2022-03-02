--liquibase formatted sql
--changeset rtaylor:ZFIN-7774
--update construct to change its association from rbm15b to rbm15

UPDATE construct SET construct_name = 'Tg(hsp70l:rbm15-FLAG)'
WHERE construct_zdb_id = 'ZDB-TGCONSTRCT-210325-4'
  AND construct_name = 'Tg(hsp70l:rbm15b-FLAG)';

UPDATE marker SET
    mrkr_name = 'Tg(hsp70l:rbm15-FLAG)',
    mrkr_abbrev = 'tg(hsp70l:rbm15-flag)'
WHERE mrkr_zdb_id = 'ZDB-TGCONSTRCT-210325-4'
  AND mrkr_name = 'Tg(hsp70l:rbm15b-FLAG)';

UPDATE construct_marker_relationship SET conmrkrrel_mrkr_zdb_id = 'ZDB-GENE-041008-192'
WHERE conmrkrrel_mrkr_zdb_id = 'ZDB-GENE-080204-91'
  AND conmrkrrel_construct_zdb_id = 'ZDB-TGCONSTRCT-210325-4';

UPDATE feature_marker_relationship SET fmrel_mrkr_zdb_id = 'ZDB-GENE-041008-192'
WHERE fmrel_mrkr_zdb_id = 'ZDB-GENE-080204-91'
  AND fmrel_ftr_zdb_id = 'ZDB-ALT-210325-7';

UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-GENE-041008-192'
WHERE mrel_mrkr_2_zdb_id = 'ZDB-GENE-080204-91'
  AND mrel_mrkr_1_zdb_id = 'ZDB-TGCONSTRCT-210325-4';
