unload to pis_per_country
select count(*), person_country 
  from person 
  where exists (select 'x' from int_person_lab 
                           where position_id in (2,7)
                           and source_id = zdb_id) 
 and person_country is not null
 group by person_country order by count(*) desc;

unload to count_pis_without_country
select count(*)
  from person 
  where exists (select 'x' from int_person_lab 
                           where position_id in (2,7)
                           and source_id = zdb_id) 
 and person_country is null;
