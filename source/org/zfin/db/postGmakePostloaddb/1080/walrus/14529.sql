--liquibase formatted sql
--changeset sierra:14529

insert into curation_topic (curtopic_name)
 values ('Anatomy');

insert into curation_topic (curtopic_name)
 values ('Behavior');

insert into curation_topic (curtopic_name)
 values ('Electrophysiology');
