--liquibase formatted sql
--changeset pm:ZFIN2950

UPDATE construct
SET construct_name='Tg(actb2:Hsa.BNC2)'
where construct_zdb_id='ZDB-TGCONSTRCT-160802-1';
UPDATE marker
SET mrkr_name='Tg(actb2:Hsa.BNC2)'
where mrkr_zdb_id='ZDB-TGCONSTRCT-160802-1';
UPDATE marker
SET mrkr_abbrev='Tg(actb2:Hsa.BNC2)'
where mrkr_zdb_id='ZDB-TGCONSTRCT-160802-1';
UPDATE construct_component
SET cc_component='BNC2'
where cc_construct_zdb_id='ZDB-TGCONSTRCT-160802-1'
and cc_component='BCN2';

