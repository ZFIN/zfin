--liquibase formatted sql

--changeset pm:case-13474

update  publication  set jtype='Thesis' where pub_jrnl_zdb_id='ZDB-JRNL-140407-2';
update  publication  set jtype='Thesis' where pub_jrnl_zdb_id='ZDB-JRNL-140407-3';
update  publication  set jtype='Thesis' where pub_jrnl_zdb_id='ZDB-JRNL-14527-1';
update  publication  set jtype='Thesis' where pub_jrnl_zdb_id='ZDB-JRNL-140422-1';