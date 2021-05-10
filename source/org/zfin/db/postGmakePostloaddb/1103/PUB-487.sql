--liquibase formatted sql
--changeset xiang:PUB-487

update journal 
   set jrnl_abbrev = 'Aging (Albany NY)',
       jrnl_medabbrev = 'Aging (Albany NY)',
       jrnl_isoabbrev = 'Aging (Albany NY)',
       jrnl_nlmid = '101508617'
 where jrnl_zdb_id = 'ZDB-JRNL-110706-2';

