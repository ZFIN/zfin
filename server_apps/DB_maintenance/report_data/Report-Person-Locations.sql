unload to total_rows_returned_for_per_country_count
select count(*), person_country 
  from person 
  where exists (select 'x' from int_person_lab 
                           and source_id = zdb_id) 
 and person_country is not null
 group by person_country order by count(*) desc;

unload to percentage_with_country
select 
  sum(case when person_country is not null then 1 else 0 end)/count(*)::decimal*100
  as pct_with_country
 from person
 where exists (select 'x' from int_person_lab 
                           where source_id = zdb_id);
   
