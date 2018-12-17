--liquibase formatted sql
--changeset pm:ZFIN-6070


update foreign_db set fdb_db_query='https://www.genenames.org/data/gene-symbol-report/#!/symbol/' where fdb_db_name='HGNC';