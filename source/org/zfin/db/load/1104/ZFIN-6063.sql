--liquibase formatted sql
--changeset sierra:ZFIN-6063.sql

alter table publication 
add pub_is_curatable boolean default 't' ;

update publication
  set pub_is_curatable = 'f' 
  where jtype not in ('Journal','Active Curation');

update publication
  set jtype = 'Unpublished'
  where exists (select 'x' from journal where jrnl_zdb_id =
			pub_jrnl_zdb_id and
	jrnl_zdb_id = 'ZDB-JRNL-181119-2');

update journal 
 set (jrnl_name, jrnl_abbrev) = ('ZebraShare','ZebraShare')
 where jrnl_name = 'zebraShare';

alter table publication
  alter column pub_is_curatable set not null;
  
