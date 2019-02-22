--liquibase formatted sql
--changeset sierra:ZFIN-6134.sql

update publication
 set pub_is_curatable = 't' 
 where zdb_id = 'ZDB-PUB-180131-10';

update publication
 set jtype = 'Unpublished'
 where zdb_id = 'ZDB-PUB-180131-10';

update publication
 set pub_jrnl_zdb_id = 'ZDB-JRNL-181119-2'
 where zdb_id = 'ZDB-PUB-180131-10';
