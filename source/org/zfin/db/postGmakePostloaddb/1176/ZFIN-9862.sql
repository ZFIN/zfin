--liquibase formatted sql
--changeset cmpich:ZFIN-9862

select get_id('PERS')
into temp table temp_id
    ;

insert into zdb_active_source values ((select get_id from temp_id))
;

insert into person (zdb_id, full_name, name, entry_time, first_name, last_name, pers_is_deceased)
VALUES ((select get_id from temp_id),
        'ABC-Indexing Priority Classifier',
        'ABC-Indexing Priority Classifier',
        now(),
        'ABC-Indexing Priority',
        'Classifier',
        false)
;
