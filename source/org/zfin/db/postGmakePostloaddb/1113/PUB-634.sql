--liquibase formatted sql
--changeset xshao:PUB-634

update journal 
   set jrnl_print_issn = '0023-6772',
       jrnl_online_issn = '1758-1117',
       jrnl_nlmid = '0112725'
 where jrnl_zdb_id = 'ZDB-JRNL-050621-893';

