begin work;


create table publicNotes 
  (
    id text,
    name text,
    note text    
  );

create index pnts_id_idx on publicNotes(id);

create index pnts_nm_idx on publicNotes(name);

copy publicNotes from './notes' (delimiter '|');

alter table publicNotes add noteId text;

update publicNotes set noteId = get_id('EXTNOTE');

insert into zdb_active_data select noteId from publicNotes;

insert into external_note(extnote_zdb_id, extnote_data_zdb_id, extnote_note, extnote_note_type, extnote_source_zdb_id)
  select noteId, id, note, 'feature', 'ZDB-PUB-190125-19'
    from publicNotes;

drop table publicNotes;

commit work;

