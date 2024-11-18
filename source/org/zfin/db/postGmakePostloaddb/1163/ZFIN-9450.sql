--liquibase formatted sql
--changeset rtaylor:ZFIN-9450.sql

create table zdb_personal_feature_flag (
    zpff_pk_id serial primary key,
    zpff_person_zdb_id text not null,
    zpff_flag_name text,
    zpff_enabled boolean not null
);

alter table zdb_personal_feature_flag
add constraint zdb_personal_feature_flag_name_fk
    foreign key (zpff_flag_name)
    references zdb_feature_flag(zfeatflag_name);

alter table zdb_personal_feature_flag
add constraint zdb_personal_feature_flag_person_fk
    foreign key (zpff_person_zdb_id)
    references person(zdb_id);

alter table zdb_personal_feature_flag
add constraint zdb_personal_feature_flag_unique_key
    unique (zpff_person_zdb_id, zpff_flag_name);
