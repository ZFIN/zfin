-- 1 -------------------------------------------------------------------------------------------------------------------

unload to construct_search_temp_records
  select count(*) as counter
  from construct_search_temp;

TEST (construct_search_temp_records < 1000) 'construct_search_temp table has fewer than 1,000 records: $x';


-- 2 -------------------------------------------------------------------------------------------------------------------

unload to checkCCSPromoter
    select count(*) as counter
    from construct_component_search_temp
    where ccs_promoter_All_names is null;

TEST (checkCCSPromoter < 1) '$x records have construct component search promoter all names values';

-- 3 -------------------------------------------------------------------------------------------------------------------

unload to checkCCSCoding
    select count(*) as counter
    from construct_component_search_temp
    where ccs_coding_All_names is null;

TEST (checkCCSCoding < 1) '$x records have construct component search coding all names values';


-- 4 -------------------------------------------------------------------------------------------------------------------

unload to checkCCSER
    select count(*) as counter
    from construct_component_search_temp
    where ccs_engineered_region_All_names is null;

TEST (checkCCSER < 1) '$x records have construct component search engineered region all names values';

-- 5 -------------------------------------------------------------------------------------------------------------------

unload to figureTermConstructSearchCount
    select count(*) as counter from figure_term_construct_search_temp;

TEST (figureTermConstructSearchCount < 3000) 'figure_term_construct_search_temp table does not have enough records.< 3000';

-- 6 -------------------------------------------------------------------------------------------------------------------

unload to btsContainssShhaCount
    select count(*) as counter from construct_search_temp where bts_contains(cons_all_names, "shha");

TEST (btsContainssShhaCount < 15)'bts index on construct_search_temp is inacurate: #shha = $x < 15';


-- 7 -------------------------------------------------------------------------------------------------------------------

