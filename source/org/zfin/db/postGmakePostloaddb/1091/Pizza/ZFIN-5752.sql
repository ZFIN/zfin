--liquibase formatted sql
--changeset patrick:ZFIN-5752

UPDATE experiment_condition
SET expcond_chebi_term_zdb_id = 'ZDB-TERM-160831-192755'   -- CHEBI:86194
WHERE expcond_chebi_term_zdb_id = 'ZDB-TERM-160831-192756' -- CHEBI:86195
AND expcond_exp_zdb_id NOT IN (
  'ZDB-EXP-060201-2',
  'ZDB-EXP-110426-1',
  'ZDB-EXP-120921-2',
  'ZDB-EXP-070920-19'
)
