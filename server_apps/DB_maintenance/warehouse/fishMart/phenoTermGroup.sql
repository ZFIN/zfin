begin work;

!echo "term group" ;

insert into term_group (tg_genox_group)
  select genox_zdb_id from genotype_Experiment
  where exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_zdb_id);

insert into term_group_member (tgm_group_id, tgm_member_name, tgm_member_id)
    select distinct tg_group_pk_id, term_name, alltermcon_container_Zdb_id
     from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains, term
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
     and alltermcon_contained_Zdb_id = term_Zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

insert into term_group_member (tgm_group_id, tgm_member_name, tgm_member_id)
    select distinct tg_group_pk_id, term_name, alltermcon_container_Zdb_id
     from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains, term
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_1_subterm_zdb_id
     and alltermcon_contained_Zdb_id = term_Zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

insert into term_group_member (tgm_group_id, tgm_member_name, tgm_member_id)
    select distinct tg_group_pk_id, term_name, alltermcon_container_Zdb_id
     from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains, term
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_Zdb_id = term_Zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_2_superterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

insert into term_group_member (tgm_group_id, tgm_member_name, tgm_member_id)
    select  distinct tg_group_pk_id, term_name, alltermcon_container_Zdb_id
     from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains, term
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_2_subterm_zdb_id
     and alltermcon_contained_Zdb_id = term_Zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

update statistics high for table term_group;
set pdqpriority 80;


create temp table tmp_term (phenox_genox_zdb_id varchar(50), term varchar(50), category char(2))
 with no log;

insert into tmp_term (phenox_genox_zdb_id, term, category)
select distinct phenox_genox_zdb_id,alltermcon_container_zdb_id as term, 1
  from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

--alter table all_term_contains
-- modify (alltermcon_container_zdb_id varchar(30) not null constraint alltermcon_container_zdb_id_not_null);

select replace(replace(replace(substr(multiset (select distinct item alltermcon_container_zdb_id
						  	    from phenotype_Experiment, phenotype_Statement, all_Term_contains
     							    where tg_genox_group = phenox_genox_zdb_id
     							    and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
     							    and phenox_pk_id = phenos_phenox_pk_id
							  and phenos_tag != 'normal'
						 )::lvarchar,11),""),"'}",""),"'","") as tg_name, tg_genox_group as ttg_genox_group
   from term_group 
into temp tmp_tg;


delete from tmp_Tg where tg_name is null;

create index tgtemp_genox on tmp_tg(ttg_genox_group)
  using btree in idxdbs2;

update statistics high for table tmp_Tg;

update term_group
  set tg_group_name = (Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null );


!echo "max octet length term_Group_name";
select max(octet_length(tg_group_name)) from term_group; 

drop table tmp_tg;  

create index term_genox on tmp_term(phenox_genox_zdb_id)
  using btree in idxdbs3;

create index term_genox2 on tmp_term(term)
  using btree in idxdbs1;


insert into tmp_term (phenox_genox_zdb_id, term, category)
select distinct phenox_genox_zdb_id,alltermcon_container_zdb_id as term, 2
  from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_1_subterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal'
     and phenos_entity_1_subterm_zdb_id is not null
     and not exists (Select 'x' from tmp_term where phenotype_experiment.phenox_genox_zdb_id = tmp_term.phenox_genox_zdb_id
     	     	    	    and alltermcon_container_zdb_id = tmp_term.term);


select phenox_genox_zdb_id, term
  from tmp_term
  where category =2
into temp tmp_term2;


create index term_genox3 on tmp_term2(phenox_genox_zdb_id)
  using btree in idxdbs2;

update statistics high for table tmp_term;
update statistics high for table tmp_term2;

select replace(replace(replace(substr(multiset (
							  select distinct item term from tmp_term2
							  where tg_genox_group = phenox_genox_zdb_id
						
							 )::lvarchar,11),""),"'}",""),"'","") as tg_name, tg_genox_group as ttg_genox_group
  from term_group
  where tg_genox_group in (Select phenox_genox_zdb_id from tmp_term2)
into temp tmp_tg;

delete from tmp_tg where tg_name = '';
delete from tmp_tg where tg_name is null;

create index tgtemp_Genox on tmp_tg(ttg_genox_group)
  using btree in idxdbs2;

update statistics high for table tmp_Tg;

update term_group 
  set tg_group_name = tg_group_name||(Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is not null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
    and exists (Select 'x' from tmp_term2 where phenox_genox_zdb_id = tg_genox_group);

update term_group 
  set tg_group_name = (Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
  and exists (Select 'x' from tmp_term2 where phenox_genox_zdb_id = tg_genox_group);

drop table tmp_tg;
--drop table tmp_Term;

insert into tmp_term (phenox_genox_zdb_id, term, category)
select distinct phenox_genox_zdb_id,alltermcon_container_zdb_id as term, 3 
  from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_2_superterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal'
     and phenos_entity_2_superterm_zdb_id is not null
 and not exists (Select 'x' from tmp_term where phenotype_experiment.phenox_genox_zdb_id = tmp_term.phenox_genox_zdb_id
     	 		and phenos_entity_2_superterm_zdb_id = tmp_term.term)
;

update statistics high for table tmp_term;

select phenox_genox_zdb_id, term
  from tmp_term
  where category =3
into temp tmp_term3;


create index term_genox4 on tmp_term3(phenox_genox_zdb_id)
  using btree in idxdbs1;

update statistics high for table tmp_term;
update statistics high for table tmp_term3;

select replace(replace(replace(substr(multiset (
							  select distinct item term from tmp_term3
							  where tg_genox_group = phenox_genox_zdb_id
						
							 )::lvarchar,11),""),"'}",""),"'","") as tg_name,tg_genox_group as ttg_genox_group
  from term_group
  where tg_genox_group in (Select phenox_genox_zdb_id from tmp_term3)
into temp tmp_tg;

delete from tmp_tg where tg_name = '';
delete from tmp_tg where tg_name is null;

update term_group 
  set tg_group_name = tg_group_name||(Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is not null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
    and exists (Select 'x' from tmp_term3 where phenox_genox_zdb_id = tg_genox_group);

update term_group 
  set tg_group_name = (Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
  and exists (Select 'x' from tmp_term3 where phenox_genox_zdb_id = tg_genox_group);


--drop table tmp_term;
drop table tmp_tg;

insert into tmp_term (phenox_genox_zdb_id, term, category)
select distinct phenox_genox_zdb_id,alltermcon_container_zdb_id  as term, 4
 from term_group, phenotype_Experiment, phenotype_Statement, all_Term_contains
     where tg_genox_group = phenox_genox_zdb_id
     and alltermcon_contained_zdb_id = phenos_entity_2_subterm_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal'
     and phenos_entity_2_subterm_zdb_id is not null
 and not exists (Select 'x' from tmp_term where phenotype_experiment.phenox_genox_zdb_id = tmp_term.phenox_genox_zdb_id
     	 		and phenos_entity_2_subterm_zdb_id = tmp_term.term);


update statistics high for table tmp_term;


select phenox_genox_zdb_id, term
  from tmp_term
  where category =4
into temp tmp_term4;

create index term_genox5 on tmp_term4(phenox_genox_zdb_id)
  using btree in idxdbs3;

update statistics high for table tmp_term;
update statistics high for table tmp_term4;

select replace(replace(replace(substr(multiset (
							  select distinct item term from tmp_term4
							  where tg_genox_group = phenox_genox_zdb_id
						
							 )::lvarchar,11),""),"'}",""),"'","") as tg_name,tg_genox_group as ttg_genox_group
  from term_group
  where tg_genox_group in (Select phenox_genox_zdb_id from tmp_term4)
into temp tmp_tg;


create index tgtemp_Genox on tmp_tg(ttg_genox_group)
  using btree in idxdbs2;

update statistics high for table tmp_Tg;

update term_group 
  set tg_group_name = tg_group_name||(Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is not null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
  and exists (Select 'x' from tmp_term4 where phenox_genox_zdb_id = tg_genox_group);

update term_group 
  set tg_group_name = (Select tg_name from tmp_tg where ttg_genox_group = tg_genox_group and tg_name is not null)
  where tg_group_name is null
  and exists (select 'x' from tmp_tg where ttg_genox_group = tg_genox_group)
  and exists (Select 'x' from tmp_term4 where phenox_genox_zdb_id = tg_genox_group);

drop table tmp_term;
drop table tmp_tg;

set pdqpriority 0;

!echo "check lvarchars for term group name"
select max(octet_length(tg_group_name)) from term_group;


--rollback work ;

commit work ;