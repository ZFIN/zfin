--liquibase formatted sql
--changeset xshao:CUR-874


update marker
   set mrkr_name = 'Tg(myog:Hsa.HIST1H2BJ-mRFP)'
 where mrkr_zdb_id = 'ZDB-TGCONSTRCT-160803-2';

