--liquibase formatted sql
--changeset pm:PUB-544

update journal set jrnl_print_issn='0093-7355' where jrnl_zdb_id='ZDB-JRNL-050621-893';
update journal set jrnl_online_issn='1548-4475' where jrnl_zdb_id='ZDB-JRNL-050621-893';
update journal set jrnl_name='Lab Anim.' where jrnl_zdb_id='ZDB-JRNL-050621-893';
update journal set jrnl_abbrev='Lab Anim.' where jrnl_zdb_id='ZDB-JRNL-050621-893';
delete from source_alias where salias_source_zdb_id='ZDB-JRNL-050621-893';