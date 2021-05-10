--liquibase formatted sql
--changeset pkalita:PUB-438

insert into curation_topic (curtopic_name) values ('Sequence Variant');
