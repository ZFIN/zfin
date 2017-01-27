--liquibase formatted sql
--changeset pm:14985

UPDATE person
SET country =  (SELECT tmp_pers_country
  FROM tmp_person_country where zdb_id=tmp_pers_id)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_person_country
  WHERE tmp_pers_id = Zdb_id
);

DROP TABLE tmp_person_country;