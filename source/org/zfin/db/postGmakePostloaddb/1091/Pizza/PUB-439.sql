--liquibase formatted sql
--changeset prita:PUB-439
update  journal set  jrnl_print_issn = '1674-7232' where jrnl_zdb_id='ZDB-JRNL-170810-3';
update journal set  jrnl_online_issn = '2095-9443' where jrnl_zdb_id='ZDB-JRNL-170810-3';



