--liquibase formatted sql
--changeset pkalita:PUB-424

insert into curation_topic (curtopic_name) values ('Regions');
