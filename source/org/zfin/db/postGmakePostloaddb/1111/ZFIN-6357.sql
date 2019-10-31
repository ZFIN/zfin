--liquibase formatted sql
--changeset pm:ZFIN-6357


update foreign_db set fdb_db_query='https://www.addgene.org/' where fdb_db_name='Addgene';

delete from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-141007-1';

