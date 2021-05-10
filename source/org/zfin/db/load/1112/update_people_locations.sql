--liquibase formatted sql
--changeset sierra:update_people_locations.sql

create temp table tmp_pi_lab (p_country text, p_lab_id text);

insert into tmp_pi_lab (p_country, p_lab_id)
 select distinct person_country, target_id
   from person, int_person_lab
 where position_id in (2,7)
 and zdb_id = source_id
 and person_country is not null;


select count(*), p_country
 from tmp_pi_lab
 group by p_country
having count(*) > 1;

select * from  tmp_pi_lab, int_person_lab, person
                         where source_id = zdb_id
                         and target_id = p_lab_id
and source_id = 'ZDB-PERS-960805-527';

create temp table tmp_people_in_more_labs (country text, person_id text, counter int);
create temp table tmp_people_in_labs (country text, person_id text);

insert into tmp_people_in_labs (country, person_id)
 select p_country, source_id
   from tmp_pi_lab, int_person_lab, person
                         where source_id = zdb_id
                         and target_id = p_lab_id;

insert into tmp_people_in_more_labs (country, person_id, counter)
 select p_country, source_id, count(*)
                         from tmp_pi_lab, int_person_lab, person
                         where source_id = zdb_id
                         and target_id = p_lab_id
group by p_country, source_id
having count(*) > 1;

delete from tmp_people_in_labs
 where person_id in (select person_id from tmp_people_in_more_labs);

create temp table more_dups (person_id text, counter int);

insert into more_dups (select person_id, count(*) from tmp_people_in_labs
 group by person_id having count(*) > 1);

delete from tmp_people_in_labs
 where person_id in (select person_id from more_dups);

select * from tmp_people_in_labs
 where person_id = 'ZDB-PERS-121205-2';


update person
 set person_country = (select distinct country from tmp_people_in_labs
                      where zdb_id = person_id
                         )
where person_country is null
and exists (Select 'x' from tmp_people_in_labs where zdb_id = person_id);

