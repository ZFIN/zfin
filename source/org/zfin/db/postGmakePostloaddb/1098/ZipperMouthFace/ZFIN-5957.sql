--liquibase formatted sql
--changeset pm:ZFIN-5957


update foreign_db set fdb_db_query='http://flybase.org/reports/' where fdb_db_pk_id=11;





