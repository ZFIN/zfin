select zdb_id, str.mrkr_abbrev, str.mrkr_zdb_id, gene.mrkr_abbrev, gene.mrkr_zdb_id
from publication, record_attribution, marker str, marker gene, marker_relationship
-- STR is attributed to pub
where recattrib_source_zdb_id = zdb_id
and recattrib_data_zdb_id = str.mrkr_zdb_id
-- STR targets a gene ...
and str.mrkr_zdb_id = mrel_mrkr_1_zdb_id
and gene.mrkr_zdb_id = mrel_mrkr_2_zdb_id
and mrel_type = 'knockdown reagent targets gene'
-- ... and only one gene
and str.mrkr_zdb_id in (
  select mrel_mrkr_1_zdb_id
  from marker_relationship
  where mrel_type = 'knockdown reagent targets gene'
  group by mrel_mrkr_1_zdb_id
  having count(*) = 1
)
-- and the gene doesn't have any associated features
and gene.mrkr_zdb_id not in (
  select distinct fmrel_mrkr_zdb_id
  from feature_marker_relationship
)
-- and the gene doesn't have STRs with phenotypes
and gene.mrkr_zdb_id not in (
  select distinct mrkr_zdb_id
  from marker, marker_relationship, fish_str, fish_experiment, phenotype_experiment
  where mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
  and mrel_mrkr_2_zdb_id = mrkr_zdb_id
  and mrel_type = 'knockdown reagent targets gene'
  and fishstr_fish_zdb_id = genox_fish_zdb_id
  and phenox_genox_zdb_id = genox_zdb_id
)
-- and the pub is open and 'recent'
and pub_arrival_date >= '2009-01-01'
and pub_completion_date is null
order by 1, 2, 4