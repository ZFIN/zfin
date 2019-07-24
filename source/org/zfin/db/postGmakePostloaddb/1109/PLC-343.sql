--liquibase formatted sql
--changeset pkalita:PLC-343

update company
set url = replace(url, 'http://https://', 'https://')
where url like 'http://https://%';

update lab
set url = replace(url, 'http://https://', 'https://')
where url like 'http://https://%';

update person
set url = replace(url, 'http://https://', 'https://')
where url like 'http://https://%';

