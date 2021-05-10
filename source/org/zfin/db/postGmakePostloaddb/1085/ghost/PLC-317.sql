--liquibase formatted sql
--changeset pkalita:PLC-317

UPDATE company
SET country = (
  SELECT country
  FROM tmp_company_country
  WHERE id = zdb_id
)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_company_country
  WHERE id = zdb_id
);

UPDATE lab
SET country = (
  SELECT country
  FROM tmp_lab_country
  WHERE id = zdb_id
)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_lab_country
  WHERE id = zdb_id
);

DROP TABLE tmp_company_country;
DROP TABLE tmp_lab_country;
