select count(*), person_country 
  from person 
  where exists (select 'x' from int_person_lab 
                           where position_id in (2,7)) 
 and person_country is not null
 group by person_country order by count(*) desc;
