--liquibase formatted sql
--changeset christian:PUB-553

alter table zdb_submitters add column is_student boolean;

ALTER TABLE zdb_submitters ALTER COLUMN is_student SET DEFAULT false;

update zdb_submitters set is_student = false;

update zdb_submitters set is_student = true where login in ('mfleui','ehaney');
