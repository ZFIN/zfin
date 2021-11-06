--liquibase formatted sql
--changeset cmpich:ZFIN-7584


update feature_dna_mutation_detail
set fdmd_dna_mutation_term_zdb_id = vocab.mdcv_term_zdb_id
from mutation_detail_controlled_vocabulary vocab,
     ensdarg_temp
where fdmd_feature_zdb_id = et_id
  AND mdcv_term_display_name = et_reference || '>' || et_variant;

select * from feature_dna_mutation_detail,
 mutation_detail_controlled_vocabulary vocab,
     ensdarg_temp
where fdmd_feature_zdb_id = et_id
  AND mdcv_term_display_name = et_reference || '>' || et_variant
AND fdmd_dna_mutation_term_zdb_id = vocab.mdcv_term_zdb_id;
