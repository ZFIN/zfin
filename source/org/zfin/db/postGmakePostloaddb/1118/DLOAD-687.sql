--liquibase formatted sql
--changeset pm:DLOAD-687.sql

update foreign_db set fdb_db_query='https://zmp.buschlab.org/allele/'  where fdb_db_pk_id=67;





