--liquibase formatted sql
--changeset rtaylor:ZFIN-8620-post.sql comment:REVIEW_temp_8620_log_TABLE_FOR_DETAILS

select mrel_retain_only_single_attribution(pub,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) from temp_8620;

drop table temp_8620;
drop function mrel_retain_only_single_attribution;

