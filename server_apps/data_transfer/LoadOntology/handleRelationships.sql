!echo "now deal with relationships" ;

create temp table tmp_rels (
	termrel_term_1_id varchar(50),
	termrel_term_2_id varchar(50),
	termrel_type varchar(100)
 ) with no log ;


load from term_relationships.unl
  insert into tmp_rels ;

select distinct termrel_type from tmp_rels;

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
 ) with no log ;

insert into tmp_rels_zdb (ttermrel_ont_id_1, ttermrel_ont_id_2, ttermrel_type)
  select termrel_term_1_id, termrel_term_2_id, termrel_type
   from tmp_rels;


create index rtermrels_term_1_id_index
  on tmp_rels_zdb (ttermrel_term_1_zdb_id)
  using btree in idxdbs1 ;

create index rtermrels_term_2_id_index
  on tmp_rels_zdb (ttermrel_term_2_zdb_id)
  using btree in idxdbs2 ;

update tmp_rels_zdb
  set ttermrel_term_1_zdb_id = (Select term_Zdb_id from term
      			      	      where term_ont_id = ttermrel_ont_id_1);

update tmp_rels_zdb
  set ttermrel_term_2_zdb_id = (Select term_Zdb_id from term
      			      	      where term_ont_id = ttermrel_ont_id_2);

--update statistics high for table tmp_rels ;
--update statistics high for table term ;

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
) with no log ;


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
  set termrel_zdb_id = get_id("TERMREL");


create index tmp_rel_1_index
  on tmp_zfin_rels (termrel_term_1_zdb_id)
  using btree in idxdbs2;

create index tmp_rel_2_index
  on tmp_zfin_rels (termrel_term_2_zdb_id)
  using btree in idxdbs2;

create index tmp_reltype_index_zfin_rels
  on tmp_zfin_rels (termrel_type)
  using btree in idxdbs2;


create index tmp_rels_1_index
  on tmp_rels (termrel_term_1_id)
  using btree in idxdbs3;

create index tmp_rels_2_index
  on tmp_rels (termrel_term_2_id)
  using btree in idxdbs3;

create index tmp_reltype_index_rels
  on tmp_rels (termrel_type)
  using btree in idxdbs3;


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

!echo "delete from term relationship";
delete from term_relationship
 where not exists (Select 'x' from term a, term b, tmp_rels
       	   	  	  where a.term_ont_id = termrel_term_1_id
			  and b.term_ont_id = termrel_term_2_id
			  and termrel_term_1_Zdb_id = a.term_zdb_id
			  and termrel_term_2_zdb_id = b.term_zdb_id
			  and term_relationship.termrel_type = tmp_rels.termrel_type)
 and exists (select 'x' from tmp_term_onto_no_dups, term
     	    	    	where term_id = term_ont_id
			and term_zdb_id = termrel_term_2_zdb_id);


delete from term_relationship
 where not exists (Select 'x' from term a, term b, tmp_rels
       	   	  	  where a.term_ont_id = termrel_term_1_id
			  and b.term_ont_id = termrel_term_2_id
			  and termrel_term_1_Zdb_id = a.term_zdb_id
			  and termrel_term_2_zdb_id = b.term_zdb_id
			  and term_relationship.termrel_type = tmp_rels.termrel_type)
 and exists (select 'x' from tmp_term_onto_no_dups, term
     	    	    	where term_id = term_ont_id
			and term_zdb_id = termrel_term_1_zdb_id);
