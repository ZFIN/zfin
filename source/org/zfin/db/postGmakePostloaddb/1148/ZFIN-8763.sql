--liquibase formatted sql
--changeset rtaylor:ZFIN-8763.sql

-- delete Vanzi Lab from lab table as it is a duplicate. The real one has a different ZDB ID (ZDB-LAB-180206-3)
insert into UPDATES (comments, field_name, new_value, old_value, rec_id, submitter_id, submitter_name, upd_when)
values ('Vanzi Lab deleted through liquibase', 'lab', '', 'ZDB-LAB-180206-2', 'ZDB-LAB-180206-2', 'ZDB-PERS-210917-1', 'Taylor, Ryan', '2023-08-28 17:06:22.61');
delete from zdb_active_source where zactvs_zdb_id= 'ZDB-LAB-180206-2';

-- add uniqueness constraint on lab table, column name
alter table lab add constraint lab_name_unique unique (name);
