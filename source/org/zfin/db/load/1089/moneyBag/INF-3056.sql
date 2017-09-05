--liquibase formatted sql
--changeset pkalita:INF-3056

ALTER TABLE person
ADD pers_is_hidden BOOLEAN
DEFAULT 'f'
NOT NULL CONSTRAINT pers_is_hidden_not_null;

CREATE TEMP TABLE tmp_pers_ids(id INT, zdb_id VARCHAR(50));

INSERT INTO tmp_pers_ids
VALUES(1, get_id('PERS'));

INSERT INTO tmp_pers_ids
VALUES(2, get_id('PERS'));

INSERT INTO zdb_active_source(zactvs_zdb_id)
SELECT zdb_id FROM tmp_pers_ids;

INSERT INTO person(zdb_id, full_name, email, name, first_name, last_name, pers_is_hidden)
SELECT zdb_id, 'Pub Acquisition Script', 'informix@zfin.org', 'Pub Acquisition Script', 'Pub Acquisition', 'Script', 't'
FROM tmp_pers_ids WHERE id = 1;

INSERT INTO zdb_submitters(login, access, zdb_id, cookie, name, is_curator)
SELECT zdb_id, 'submit', zdb_id, 'vVHsB3uQLMVg3cXXubbr3FS7', 'Pub Acquisition Script', 'f'
FROM tmp_pers_ids WHERE id = 1;

INSERT INTO person(zdb_id, full_name, email, name, first_name, last_name, pers_is_hidden)
SELECT zdb_id, 'Pub Activation Script', 'informix@zfin.org', 'Pub Activation Script', 'Pub Activation', 'Script', 't'
FROM tmp_pers_ids WHERE id = 2;

INSERT INTO zdb_submitters(login, access, zdb_id, cookie, name, is_curator)
SELECT zdb_id, 'submit', zdb_id, 'qmFZrzaeMsJUJzjgsQfwPXgR', 'Pub Activation Script', 'f'
FROM tmp_pers_ids WHERE id = 2;

DROP TABLE tmp_pers_ids;
