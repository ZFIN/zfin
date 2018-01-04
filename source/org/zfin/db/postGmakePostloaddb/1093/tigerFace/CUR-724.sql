--liquibase formatted sql
--changeset cmpich:CUR-724

CREATE temp TABLE features
  (
     id    VARCHAR(50),
     fk_id VARCHAR(50)
  );

-- collect all records that need to be updated
INSERT INTO features
SELECT ftmd_feature_zdb_id,
       get_id('FTMD')
FROM   feature_transcript_mutation_detail
WHERE  ftmd_transcript_consequence_term_zdb_id = 'ZDB-TERM-130401-1898';

-- create PK
INSERT INTO zdb_active_data
SELECT fk_id
FROM   features;

-- create 'premature stop' transcript consequences for all given features
INSERT INTO feature_transcript_mutation_detail
            (ftmd_zdb_id,
             ftmd_transcript_consequence_term_zdb_id,
             ftmd_feature_zdb_id)
SELECT fk_id,
       'ZDB-TERM-130401-1580',
       id
FROM   features;

-- replace frameshift truncation' with 'frameshift'
UPDATE feature_transcript_mutation_detail
SET    ftmd_transcript_consequence_term_zdb_id = 'ZDB-TERM-130401-1581'
WHERE  ftmd_transcript_consequence_term_zdb_id = 'ZDB-TERM-130401-1898';

-- remove 'frameshift truncation' so it disappears from the UI
delete From mutation_detail_controlled_vocabulary where mdcv_term_zdb_id = 'ZDB-TERM-130401-1898';
