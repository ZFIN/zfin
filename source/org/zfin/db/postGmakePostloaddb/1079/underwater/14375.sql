--liquibase formatted sql
--changeset pkalita:updateMutationDetailReferences

CREATE TEMP TABLE tmp_new_refs (
  rec_attr_id INT,
  reference_zdb_id VARCHAR(50)
);

INSERT INTO tmp_new_refs
  SELECT recattrib_pk_id, reference_zdb_id
  FROM tmp_md_file
  INNER JOIN feature_dna_mutation_detail ON fdmd_feature_zdb_id = feature_zdb_id
  INNER JOIN record_attribution ON recattrib_data_zdb_id = fdmd_zdb_id;

INSERT INTO tmp_new_refs
  SELECT recattrib_pk_id, reference_zdb_id
  FROM tmp_md_file
  INNER JOIN feature_transcript_mutation_detail ON ftmd_feature_zdb_id = feature_zdb_id
  INNER JOIN record_attribution ON recattrib_data_zdb_id = ftmd_zdb_id;

INSERT INTO tmp_new_refs
  SELECT recattrib_pk_id, reference_zdb_id
  FROM tmp_md_file
  INNER JOIN feature_protein_mutation_detail ON fpmd_feature_zdb_id = feature_zdb_id
  INNER JOIN record_attribution ON recattrib_data_zdb_id = fpmd_zdb_id;

UPDATE record_attribution
SET record_attribution.recattrib_source_zdb_id = (
  SELECT tmp_new_refs.reference_zdb_id
  FROM tmp_new_refs
  WHERE record_attribution.recattrib_pk_id = tmp_new_refs.rec_attr_id
)
WHERE record_attribution.recattrib_pk_id IN (
  SELECT tmp_new_refs.rec_attr_id
  FROM tmp_new_refs
);

DROP TABLE tmp_new_refs;
