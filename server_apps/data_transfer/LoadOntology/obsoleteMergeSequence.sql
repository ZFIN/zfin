-- report obsoleted SO term usage
unload to 'obsoleted_sequence_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       feature_name,
       recattrib_source_zdb_id
FROM   term
       join feature_dna_mutation_detail ON fdmd_dna_mutation_term_zdb_id = term_zdb_id
	  join feature on feature_zdb_id = fdmd_feature_zdb_id
	  join Record_attribution on recattrib_data_zdb_id = fdmd_zdb_id
WHERE  term_ontology = 'sequence'
	  AND term_is_obsolete = 't'
UNION
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       feature_name,
       recattrib_source_zdb_id
FROM   term
       join feature_protein_mutation_detail ON fpmd_protein_consequence_term_zdb_id = term_zdb_id
	  join feature on feature_zdb_id = fpmd_feature_zdb_id
	  join Record_attribution on recattrib_data_zdb_id = fpmd_zdb_id
WHERE  term_ontology = 'sequence'
	  AND term_is_obsolete = 't'
UNION
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       feature_name,
       recattrib_source_zdb_id
FROM   term
       join feature_transcript_mutation_detail ON ftmd_transcript_consequence_term_zdb_id = term_zdb_id
	  join feature on feature_zdb_id = ftmd_feature_zdb_id
	  join Record_attribution on recattrib_data_zdb_id = ftmd_zdb_id
WHERE  term_ontology = 'sequence'
	  AND term_is_obsolete = 't';


-- report secondary SO term usage
unload to 'secondary_terms_on_sequence'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       feature_name,
       recattrib_source_zdb_id
FROM   term
       join feature_dna_mutation_detail ON fdmd_dna_mutation_term_zdb_id = term_zdb_id
	  join feature on feature_zdb_id = fdmd_feature_zdb_id
	  join Record_attribution on recattrib_data_zdb_id = fdmd_zdb_id
WHERE  term_ontology = 'sequence'
	  AND term_is_secondary = 't'
UNION
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       feature_name,
       recattrib_source_zdb_id
FROM   term
       join feature_protein_mutation_detail ON fpmd_protein_consequence_term_zdb_id = term_zdb_id
	  join feature on feature_zdb_id = fpmd_feature_zdb_id
	  join Record_attribution on recattrib_data_zdb_id = fpmd_zdb_id
WHERE  term_ontology = 'sequence'
	  AND term_is_secondary = 't'
UNION
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       feature_name,
       recattrib_source_zdb_id
FROM   term
       join feature_transcript_mutation_detail ON ftmd_transcript_consequence_term_zdb_id = term_zdb_id
	  join feature on feature_zdb_id = ftmd_feature_zdb_id
	  join Record_attribution on recattrib_data_zdb_id = ftmd_zdb_id
WHERE  term_ontology = 'sequence'
	  AND term_is_secondary = 't';
