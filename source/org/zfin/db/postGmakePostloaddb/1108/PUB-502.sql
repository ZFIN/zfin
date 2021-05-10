--liquibase formatted sql
--changeset pkalita:PUB-502

insert into processing_checklist_task (pct_task) values ('ADD_PDF');
insert into processing_checklist_task (pct_task) values ('ADD_FIGURES');
insert into processing_checklist_task (pct_task) values ('LINK_AUTHORS');

