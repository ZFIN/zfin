-- 1 -------------------------------------------------------------------------------------------------------------------

unload to fish_annotation_search_temp_records
  select count(*) as counter
  from fish_annotation_search_temp;

TEST (fish_annotation_search_temp_records < 16000) 'fish_annotation_search_temp table has fewer than 16,000 records: $x';

-- 2 -------------------------------------------------------------------------------------------------------------------

unload to genotypeGenoxAvailable
  select count(*) as counter
  from fish_annotation_search_temp
  where fas_genotype_group is null and fas_genox_group is null;

TEST (genotypeGenoxAvailable > 0) '$x records are missing both a genotype and a genox_group';

-- 3 -------------------------------------------------------------------------------------------------------------------

unload to checkFasAllNotNull
    select count(*) as counter
    from fish_annotation_search_temp
    where fas_all is null;

TEST (checkFasAllNotNull > 0) '$x records have null fas_all values';

-- 4 -------------------------------------------------------------------------------------------------------------------

unload to constructAbbrevOrderMismatchCount
    select count(*) as counter
    from gene_feature_result_view_temp
    where gfrv_construct_zdb_id is null
     and gfrv_construct_abbrev_order is not null;

TEST (constructAbbrevOrderMismatchCount > 0)'$x records have mismatched construct abbrev/order mismatches in gene_feature_result_view_temp';

-- 5 -------------------------------------------------------------------------------------------------------------------

unload to fishSignificanceCount
    select count(*) as counter from fish_annotation_search_temp where fas_fish_significance < 999999;

TEST (fishSignificanceCount < 10)  '$x records have significance < 999999';

-- 6 -------------------------------------------------------------------------------------------------------------------

unload to geneFeatureResultViewCount
    select count(*) as counter from gene_feature_result_view_temp;

TEST (geneFeatureResultViewCount < 20312) 'gene_feature_result_view_temp table does not have enough records. < 20312';

-- 7 -------------------------------------------------------------------------------------------------------------------

unload to figureTermFishSearchCount
    select count(*) as counter from figure_term_fish_search_temp;

TEST (figureTermFishSearchCount < 13997) 'figure_term_fish_search_temp table does not have enough records.< 13997';

-- 8 -------------------------------------------------------------------------------------------------------------------

unload to btsContainssShhaCount
    select count(*) as counter from fish_annotation_search_temp where bts_contains(fas_all, "shha");

TEST (btsContainssShhaCount < 81)'bts index on fish_annotation_search_temp is inacurate: #shha = $x < 81';


