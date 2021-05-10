--liquibase formatted sql
--changeset sierra:zebrashare_pub_table_edits.sql

create temp table tmp_id (id text);

insert into tmp_id (id)
  select get_id('JRNL');

insert into zdb_active_source (zactvs_zdb_id)
 select id from tmp_id;

insert into journal(jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_is_nice)
 select id, 'zebraShare','zebraShare','t'
   from tmp_id;

alter table publication
 add pub_zebrashare_is_public boolean;

update publication
  set pub_zebrashare_is_public = 't';

alter table publication 
  alter column pub_zebrashare_is_public set not null;

alter table figure
  add fig_inserted_date date;

alter table figure
 add fig_updated_date date;

alter table figure
 add fig_inserted_by text;

alter table figure
 add fig_updated_by text;

alter table image 
 add img_inserted_date date;

alter table image
 add img_updated_date date;

alter table image
 add img_inserted_by text;

alter table image
 add img_updated_by text;

