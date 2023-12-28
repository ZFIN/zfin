--liquibase formatted sql
--changeset rtaylor:ZFIN-8936.sql

-- email_privacy_preference

-- drop table if exists email_privacy_preference;
create table email_privacy_preference (
    epp_pk_id serial not null primary key,
    epp_name varchar(255) not null,
    epp_description text,
    epp_order int
);

insert into email_privacy_preference (epp_name, epp_description, epp_order) values ('Visible to All', 'This email will be visible to anyone viewing the website', 1);
insert into email_privacy_preference (epp_name, epp_description, epp_order) values ('Visible to Registered Users', 'This email will only be visible to other users of the website when they are logged in', 2);
insert into email_privacy_preference (epp_name, epp_description, epp_order) values ('Not Visible', 'This email will be hidden', 3);

alter table person add column pers_epp_pk_id int references email_privacy_preference(epp_pk_id) default 1 not null;