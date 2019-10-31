--liquibase formatted sql
--changeset sierra:add_goref_id.sql

alter table publication
 add pub_goref_id text ;

 update publication set pub_goref_id='GO_REF:0000104' where zdb_id='ZDB-PUB-170525-1';
update publication set pub_goref_id='GO_REF:0000040' where zdb_id='ZDB-PUB-120306-4';
update publication set pub_goref_id='GO_REF:0000041' where zdb_id='ZDB-PUB-130131-1';
update publication set pub_goref_id='GO_REF:0000039' where zdb_id='ZDB-PUB-120306-2';

