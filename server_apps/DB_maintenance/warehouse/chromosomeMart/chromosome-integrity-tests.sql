-- 1 -------------------------------------------------------------------------------------------------------------------

unload to chromosome_search_temp_records
  select count(*) as counter
  from chromosome_search_temp;

TEST (chromosome_search_temp_records < 60000) 'chromosome_search_temp table has fewer than 60,000 records: $x';
