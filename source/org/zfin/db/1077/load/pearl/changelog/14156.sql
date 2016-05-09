update external_note set extnote_source_zdb_id = 'ZDB-PUB-030905-1'
where extnote_source_zdb_id like 'ZDB-PERS-%' and
extnote_note_type = 'orthology';
