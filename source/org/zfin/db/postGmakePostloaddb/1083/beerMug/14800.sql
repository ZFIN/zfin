--liquibase formatted sql
--changeset pkalita:14800

alter table zdb_submitters
add is_curator boolean default 'f' not null constraint zdb_submitters_is_curator_not_null;

update zdb_submitters
set is_curator = 't'
where zdb_id in (
  'ZDB-PERS-000912-1',
  'ZDB-PERS-030520-3',
  'ZDB-PERS-990902-1',
  'ZDB-PERS-030612-1',
  'ZDB-PERS-030612-2',
  'ZDB-PERS-040722-4',
  'ZDB-PERS-050429-23',
  'ZDB-PERS-100329-1',
  'ZDB-PERS-981201-7',
  'ZDB-PERS-960805-646'
);
