--liquibase formatted sql
--changeset cmpich:ZFIN-8158

delete
from marker_relationship
where mrel_mrkr_1_zdb_id = 'ZDB-BAC-050218-1950'
  and mrel_mrkr_2_zdb_id = 'ZDB-MIRNAG-190403-2';
