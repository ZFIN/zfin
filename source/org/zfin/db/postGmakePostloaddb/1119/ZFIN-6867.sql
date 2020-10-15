--liquibase formatted sql
--changeset pm:ZFIN-6867

update feature set ftr_chr_info_date= (select to_timestamp(ftrdate,'mm/dd/yy') from tmp_ftrchrdate where feature_zdb_id=zdbid) from tmp_ftrchrdate where feature_zdb_id=zdbid and ftr_chr_info_date is null;


