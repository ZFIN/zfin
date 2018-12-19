--liquibase formatted sql
--changeset pm:ZFIN-6092

update marker set  mrkr_abbrev=mrkr_name where mrkr_zdb_id like '%CONSTRCT%' and mrkr_abbrev!=mrkr_name;









