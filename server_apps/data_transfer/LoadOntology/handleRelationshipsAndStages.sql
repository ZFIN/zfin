!echo "now deal with relationships" ;

!echo "Remove term relationships that use merged terms";

unload to removed_term_relationships_with_merged_terms.unl
select * From term_relationship where exists(
select 'x' from term
where (term_zdb_id = termrel_term_1_zdb_id or term_zdb_id = termrel_term_2_zdb_id) AND
term_is_secondary = 't'
);

delete From term_relationship
where exists(
select 'x' from term
where (term_zdb_id = termrel_term_1_zdb_id or term_zdb_id = termrel_term_2_zdb_id) AND
term_is_secondary = 't'
);

create temp table tmp_rels (
	termrel_term_1_id varchar(50),
	termrel_term_2_id varchar(50),
	termrel_type varchar(100)
 ) ;


load from term_relationships.unl
  insert into tmp_rels ;

insert into term_relationship_type (termreltype_name)
  select distinct termrel_type from tmp_rels
  	 where not exists (Select 'x' from term_relationship_type
	       	   	  	  where termrel_type = termreltype_name);

create temp table tmp_rels_zdb (
	ttermrel_term_1_zdb_id varchar(50),
	ttermrel_term_2_zdb_id varchar(50),
	ttermrel_ont_id_1 varchar(50),
	ttermrel_ont_id_2 varchar(50),
	ttermrel_type varchar(100),
	ttermrel_ontology varchar(30)
 ) ;

!echo  'insert from tmp_rels into tmp_rels_zdb';
  select termrel_term_1_id, termrel_term_2_id, termrel_type
   from tmp_rels;

insert into tmp_rels_zdb (ttermrel_ont_id_1, ttermrel_ont_id_2, ttermrel_type)
  select termrel_term_1_id, termrel_term_2_id, termrel_type
   from tmp_rels;


create index rtermrels_term_1_id_index
  on tmp_rels_zdb (ttermrel_term_1_zdb_id);

create index rtermrels_term_2_id_index
  on tmp_rels_zdb (ttermrel_term_2_zdb_id);

create index rtermrels_type_id_index
  on tmp_rels_zdb (ttermrel_type);

update tmp_rels_zdb
  set ttermrel_term_1_zdb_id = (Select term_Zdb_id from term
      			      	      where term_ont_id = ttermrel_ont_id_1);

update tmp_rels_zdb
  set ttermrel_term_2_zdb_id = (Select term_Zdb_id from term
      			      	      where term_ont_id = ttermrel_ont_id_2);

--update statistics high for table tmp_rels ;
--update statistics high for table term ;


create temp table  term_stage_temp  (
	term_zdb_id_temp varchar(50),
	start_zdb_id_temp varchar(50),
	end_zdb_id_temp varchar(50)
)  ;

create index term_stage_term_id_index
  on term_stage_temp (term_zdb_id_temp);

create index term_stage_start_id_index
  on term_stage_temp (start_zdb_id_temp);

create index term_stage_end_id_index
  on term_stage_temp (end_zdb_id_temp);


 select ttermrel_term_2_zdb_id, stage.stg_zdb_id,  '' from tmp_rels_zdb, stage as stage, term as term
  where ttermrel_type = 'start stage' AND
   stage.stg_obo_id = term.term_ont_id AND
   term.term_zdb_Id = ttermrel_term_1_zdb_id
  AND exists (
    select 'x' from stage, term
    where
    term_zdb_id = ttermrel_term_1_zdb_id AND
    stg_obo_id = term_ont_id
    );

-- insert start stage info
insert into term_stage_temp
 select ttermrel_term_2_zdb_id, stage.stg_zdb_id,  '' from tmp_rels_zdb, stage as stage, term as term
  where ttermrel_type = 'start stage' AND
   stage.stg_obo_id = term.term_ont_id AND
   term.term_zdb_Id = ttermrel_term_1_zdb_id
  AND exists (
    select 'x' from stage, term
    where
    term_zdb_id = ttermrel_term_1_zdb_id AND
    stg_obo_id = term_ont_id
    );

-- update end stage info
update term_stage_temp
 set end_zdb_id_temp = (
 select stage.stg_zdb_id from tmp_rels_zdb, stage as stage, term as term
  where ttermrel_type = 'end stage' AND
   stage.stg_obo_id = term.term_ont_id AND
   term.term_zdb_Id = ttermrel_term_1_zdb_id AND
    term_zdb_id_temp = ttermrel_term_2_zdb_id  );

-- remove records in TERM_STAGE that do not match a record in the temp table
-- they are either removed from the obo file or have been changed

delete from term_stage
 where not exists (
   select 'x' from term_stage_temp
   where
     term_zdb_id_temp = ts_term_zdb_id AND
     start_zdb_id_temp = ts_start_stg_zdb_id AND
     end_zdb_id_temp = ts_end_stg_zdb_id
 );

--insert into term_stage
insert into term_stage
 select term_zdb_id_temp, start_zdb_id_temp,end_zdb_id_temp
 from term_stage_temp
 where not exists (
  select 'x' from term_stage as tt
  where tt.ts_term_zdb_id = term_zdb_id_temp AND
  tt.ts_start_stg_zdb_id = start_zdb_id_temp AND
  tt.ts_end_stg_zdb_id = end_zdb_id_temp
  );

-------------------------------------------------------
!echo 'Delete records from TMP_RELS_ZDB that exist in term_relationship';
delete from tmp_rels_zdb
 where exists (Select 'x' from term_relationship a
       	      	      where ttermrel_term_1_zdb_id = a.termrel_term_1_zdb_id
		      and ttermrel_term_2_zdb_id = a.termrel_term_2_zdb_id
		      and ttermrel_type = a.termrel_type);

create temp table tmp_zfin_rels  (
	termrel_zdb_id varchar(50),
	termrel_term_1_zdb_id varchar(50),
	termrel_term_2_zdb_id varchar(50),
	termrel_type varchar(100)
) ;


insert into tmp_zfin_rels(
	termrel_term_1_zdb_id,
	termrel_term_2_zdb_id,
	termrel_type)
  select distinct
  	ttermrel_term_1_zdb_id,
	ttermrel_term_2_zdb_id,
	ttermrel_type
	from tmp_rels_zdb ;

update tmp_zfin_rels
  set termrel_zdb_id = get_id('TERMREL');

create index tmp_rel_1_index
  on tmp_zfin_rels (termrel_term_1_zdb_id);

create index tmp_rel_2_index
  on tmp_zfin_rels (termrel_term_2_zdb_id);

create index tmp_reltype_index_zfin_rels
  on tmp_zfin_rels (termrel_type);


create index tmp_rels_1_index
  on tmp_rels (termrel_term_1_id);

create index tmp_rels_2_index
  on tmp_rels (termrel_term_2_id);

create index tmp_reltype_index_rels
  on tmp_rels (termrel_type);


--update statistics high for table zdb_active_data;
--update statistics high for table tmp_zfin_rels ;
--update statistics high for table tmp_rels_zdb;
--update statistics high for table tmp_rels;

!echo "add any new term relationship types" ;

insert into term_relationship_type
  select distinct termrel_type
		from tmp_zfin_rels
		where not exists (Select 'x'
					from term_relationship_type
					where termreltype_name = termrel_type);

!echo "term relationships with null term_2s?";

delete from tmp_zfin_rels
  where termrel_term_2_zdb_id is null;

insert into zdb_active_data
  select termrel_zdb_id
    from tmp_zfin_rels
	where not exists (select 'x'
				from zdb_active_data
				where zactvd_zdb_id = termrel_zdb_id);


!echo "populate the TERM_STAGE table with new start/end stages";

select * from term_stage
 where not exists (
   select 'x' from term_stage_temp
   where
    term_zdb_id_temp = ts_term_zdb_id AND
    start_zdb_id_temp = ts_start_stg_zdb_id AND
    end_zdb_id_temp = ts_end_stg_zdb_id
 );

--delete from term_stage
-- where not exists (
--   select 'x' from term_stage_temp
--   where
--    term_zdb_id_temp = ts_term_zdb_id AND
--    start_zdb_id_temp = ts_start_stg_zdb_id AND
--    end_zdb_id_temp = ts_end_stg_zdb_id
-- );

select termrel_zdb_id,
	c.term_ont_id,
	p.term_ont_id,
	termrel_type
    from tmp_zfin_rels, term as c, term as p
    where
     termrel_term_1_zdb_id = c.term_zdb_id AND
     termrel_term_2_zdb_id = p.term_zdb_id
    ;

insert into term_relationship (termrel_zdb_id,
    				termrel_term_1_zdb_id,
    				termrel_term_2_zdb_id,
    				termrel_type)
  select termrel_zdb_id,
	termrel_term_1_zdb_id,
	termrel_term_2_zdb_id,
	termrel_type
    from tmp_zfin_rels ;

--update statistics high for table term_relationship ;

--!!! NOT OBVIOUS logic: if the second term in the relationship belongs to this ontology load, then it is
--!!! safe to check for deletions. Don't want to delete other load relationships.

unload to removed_relationships
SELECT parent.term_ont_id,
       parent.term_name,
       child.term_ont_id,
       child.term_name,
       termrel_type,
       termrel_zdb_id
FROM   term_relationship,
       term AS parent,
       term AS child
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   term a,
                          term b,
                          tmp_rels
                   WHERE  a.term_ont_id = termrel_term_1_id
                          AND b.term_ont_id = termrel_term_2_id
                          AND termrel_term_1_zdb_id = a.term_zdb_id
                          AND termrel_term_2_zdb_id = b.term_zdb_id
                          AND term_relationship.termrel_type =
                              tmp_rels.termrel_type)
       AND EXISTS (SELECT 'x'
                   FROM   tmp_term_onto_no_dups,
                          term
                   WHERE  term_id = term_ont_id
                          AND term_zdb_id = termrel_term_2_zdb_id)
       AND EXISTS (SELECT 'x'
                   FROM   tmp_term_onto_no_dups,
                          term
                   WHERE  term_id = term_ont_id
                          AND term_zdb_id = termrel_term_1_zdb_id)
       AND parent.term_zdb_id = termrel_term_1_zdb_id
       AND child.term_zdb_id = termrel_term_2_zdb_id;
       
!echo "delete from term relationship";
DELETE FROM term_relationship
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   term a,
                          term b,
                          tmp_rels
                   WHERE  a.term_ont_id = termrel_term_1_id
                          AND b.term_ont_id = termrel_term_2_id
                          AND termrel_term_1_zdb_id = a.term_zdb_id
                          AND termrel_term_2_zdb_id = b.term_zdb_id
                          AND term_relationship.termrel_type =
                              tmp_rels.termrel_type)
       AND EXISTS (SELECT 'x'
                   FROM   tmp_term_onto_no_dups,
                          term
                   WHERE  term_id = term_ont_id
                          AND term_zdb_id = termrel_term_2_zdb_id)
       AND EXISTS (SELECT 'x'
                   FROM   tmp_term_onto_no_dups,
                          term
                   WHERE  term_id = term_ont_id
                          AND term_zdb_id = termrel_term_1_zdb_id);
