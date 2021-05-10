--liquibase formatted sql
--changeset xshao:PUB-491

update journal 
   set jrnl_print_issn = '0199638098 9780199638093 019963808X 9780199638086' 
 where jrnl_zdb_id = 'ZDB-JRNL-050621-739';
