-- 1 -------------------------------------------------------------------------------------------------------------------

unload to fish_annotation_search_temp_records
  select count(*) as counter
  from fish_annotation_search_temp;

TEST (fish_annotation_search_temp_records < 16000) 'fish_annotation_search_temp table has fewer than 16,000 records: $x';

-- 2 -------------------------------------------------------------------------------------------------------------------

unload to genotypeGenoxAvailable
  select count(*) as counter
  from fish_annotation_search_temp
  where fas_genotype_group is null and fas_genox_group is null
  and fas_geno_name != "";

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


-- 9 -------------------------------------------------------------------------------------------------------------------

unload to btsContainssBrainCount
select
       count(distinct ftfs_fig_zdb_id)
   from
       figure_term_fish_search,
       fish_annotation_search,
           OUTER image where
       img_fig_zdb_id = ftfs_fig_zdb_id
       and ftfs_fas_id = fas_pk_id
       and (
           fas_genox_group = 'ZDB-GENOX-041102-1716'
           AND fas_genotype_group = 'ZDB-GENO-980202-899'
       )
       AND bts_contains(ftfs_term_group,
       ' ftfs_term_group:zdb\-term\-100331\-8', fas_all_score # real);

TEST (btsContainssBrainCount < 4)'bts index on figure_Term_fish_search_temp is inacurate: #figures = $x < 4';


-- 10 -------------------------------------------------------------------------------------------------------------------

unload to fasAllCharLengthExceeded
    select count(*) as counter from fish_annotation_search_temp where octet_length(fas_all) = 9000;

TEST (fasAllCharLengthExceeded > 0)'there are fish with fas_all longer than the lvarchar field restriction of 9000 = $x > 0';


-- 11 -------------------------------------------------------------------------------------------------------------------

unload to fasFeatureGroupCharLengthExceeded
    select count(*) as counter from fish_annotation_search_temp where octet_length(fas_feature_group) = 5000;

TEST (fasFeatureGroupCharLengthExceeded > 0)'there are fish with fas_feature_group longer than the lvarchar field restriction of 5000 = $x > 0';

-- 12 -------------------------------------------------------------------------------------------------------------------

unload to checkAllTablesConstraintsAreEnabled

SELECT count(*) as counter
FROM systables, sysobjstate
WHERE systables.tabid = sysobjstate.tabid
AND sysobjstate.objtype = "C"
AND sysobjstate.state = "D";

TEST (checkAllTablesConstraintsAreEnabled > 0)'there are tables with disabled constraints! Run SQL in case 8968  = $x > 0';
