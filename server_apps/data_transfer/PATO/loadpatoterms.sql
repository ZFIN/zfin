begin work;


create temp table tmp_term_onto_with_dups (
		term_id			varchar(50),
		term_name		varchar(255),
		term_onto		varchar(30),
		term_definition		lvarchar,
		term_comment		lvarchar,
		term_is_obsolete	boolean default 'f'
	)with no log;

--load the ontology file

load from patoterm_parsed.unl insert into tmp_term_onto_with_dups;

create temp table tmp_term_onto_no_dups (
		term_id			varchar(50),
		term_name		varchar(255),
		term_onto		varchar(30),
		term_definition		lvarchar,
		term_comment		lvarchar,
		term_is_obsolete	boolean default 'f'
	)with no log;

insert into tmp_term_onto_no_dups
  select distinct term_id, 
			trim(term_name), 
			term_onto, 
			term_definition,
			term_comment, 
			term_is_obsolete
	from tmp_term_onto_with_dups ;


create index rterm_name_index 
  on tmp_term_onto_no_dups (term_name)
  using btree in idxdbs3 ;

create index rterm_id_index 
  on tmp_term_onto_no_dups (term_id)
  using btree in idxdbs2 ;

update statistics high for table tmp_term_onto_no_dups ;

--convert Erik's original PATO terms to real terms from qualities.obo

update term 
  set term_ont_id = (select term_id
			from tmp_term_onto_no_dups
			where tmp_term_onto_no_dups.term_name = term.term_name)
  where term_ont_id like 'ZDB-TERM-%' 
  and exists (select term_id
		from tmp_term_onto_no_dups
			where tmp_term_onto_no_dups.term_name = term.term_name);

unload to 'terms_missing_obo_id.txt'
select * from term
  where term_ont_id like 'ZDB-TERM-%' ;

create temp table tmp_term 	(
			 term_zdb_id		varchar(50),
			 term_id		varchar(50),
			 term_name		varchar(255),
			 term_ontology		varchar(30),
			 term_definition	lvarchar,
			 term_comment		lvarchar,
			 term_is_obsolete	boolean default 'f'
	)
with no log;


insert into tmp_term (term_zdb_id, 
			term_id, 
			term_name, 
			term_ontology,
			term_is_obsolete,
			term_definition,
			term_comment)
  select get_id('TERM'),
	term_id,
	term_name,
	term_onto,
	term_is_obsolete,
	term_definition,
	term_comment
    from tmp_term_onto_no_dups ;


--    where not exists (select 'x'
--			from term
--			where term_ont_id = term_id);

delete from tmp_term
  where exists (select 'x' from term where term_ont_id = term_id);
		
unload to 'new_terms.unl'
  select term_id, term_name
    from tmp_term
	where not exists (Select 'x'
			   from term
			   where term.term_ont_id = tmp_term.term_id);

unload to 'updated_pato_terms.unl'
  select n.term_name, g.term_name, g.term_id 
    from tmp_term_onto_no_dups n, tmp_term g 
    where n.term_id = g.term_id 
    and n.term_name not like g.term_name;



--!echo "update the term table with new names where the term id is the same term id in the qualiteis.obo file" ;

update statistics high for table tmp_term; 

update term
  set term_name = (select term_name 
			from tmp_term_onto_no_dups
			where term_ont_id = term_id)
  where exists (select 'x'
			from tmp_term_onto_no_dups
			where term_ont_id = term_id);

--!echo "insert the new term_ids into zdb_active_data" ;

insert into zdb_active_data 
  select term_Zdb_id
    from tmp_term 
    where not exists (Select 'x'
			from zdb_active_data
			where zactvd_zdb_id = term_zdb_id);

--!echo "insert the new terms into the term table, obsolete and all" ;

update tmp_term
  set term_is_obsolete = 'f'
  where term_is_obsolete is null ; 

select distinct term_ontology
  from tmp_term ;

insert into term (term_zdb_id, 
			term_ont_id, 
			term_name, 
			term_ontology,
			term_is_obsolete,
			term_comment)
  select term_zdb_id,
	term_id,
	term_name,
	term_ontology,
	term_is_obsolete,
	term_comment
    from tmp_term ;


create temp table tmp_Defs (termdef_Zdb_id varchar(50), 
				termdef_term_zdb_id varchar(50),
				termdef_definition lvarchar)
 with no log ;


delete from zdb_active_data
  where zactvd_zdb_id like 'ZDB-TERMDEF-%' ;

insert into tmp_defs (termdef_zdb_id,
			termdef_term_zdb_id,
			termdef_definition)
  select get_id('TERMDEF'),
	term_zdb_id,
	term_definition
    from tmp_term
    where term_definition is not null ;
	  			
insert into zdb_active_data
  select termdef_zdb_id  
    from tmp_defs ;

insert into term_definition (termdef_zdb_id,
				termdef_term_zdb_id,
				termdef_definition)
  select termdef_zdb_id,
			termdef_term_zdb_id,
			termdef_definition
	from tmp_defs;

update statistics high for table term ;

create temp table tmp_obsoletes (term_id varchar(50))
with no log ;

load from patoterm_obsolete.unl
  insert into tmp_obsoletes ;

unload to terms_becoming_obsolete.unl
  select term_name, 
		term_ont_id, 
		term_comment as suggested_replacement_term,
		apato_subterm_zdb_id as subterm,
		apato_superterm_zdb_id as superterm,
		apato_pub_zdb_id as pub_id,
		apato_tag as tag,
		apato_zdb_id,
		apato_start_stg_zdb_id as start_stage,
		apato_end_stg_zdb_id as end_stage,
		geno_handle,
		geno_display_name,
		exp_name as environment_name,
		apato_genox_zdb_id
	from term, atomic_phenotype, genotype_experiment, experiment, genotype
	where exists (select 'x'
			from tmp_obsoletes
			where term_ont_id = term_id)
	and exp_zdb_id = genox_exp_zdb_id
	and term_zdb_id = apato_quality_zdb_id
	and genox_zdb_id = apato_genox_zdb_id
	and genox_geno_zdb_id = geno_zdb_id 
	and genox_exp_zdb_id = exp_zdb_id;


update term
  set term_is_obsolete = 't'
  where exists (select 'x'
			from tmp_obsoletes
			where term_ont_id = term_id);

select count(*) as counter, apato_quality_zdb_id, term_name, term_ont_id 
	from atomic_phenotype, term
	where term_is_obsolete = 't'
	and term_zdb_id = apato_quality_zdb_id
	group by apato_quality_zdb_id, term_name, term_ont_id
	having count(*) > 1 into temp tmp_numAnnots ;

unload to number_annotations_per_obsolete_term.unl
  select * from tmp_numAnnots;

--secondary terms.


create temp table sec_dups 
  (
    prim_id varchar(50),
    sec_id varchar(50)
  );

load from patoterm_secondary.unl
  insert into sec_dups;

create temp table sec_oks 
  (
    prim_id varchar(50),
    sec_id varchar(50), 
    prim_zdb_id varchar(50), 
    sec_zdb_id varchar(50)
  );

--insert only the distinct secondary terms

insert into sec_oks (sec_id, prim_id)
  select distinct sec_id, prim_id
    from sec_dups ;

update sec_oks
  set prim_zdb_id = (select term_zdb_id 
      		       from term
		       where term_ont_id = prim_id
		       and term_ontology ='pato.quality');

 
update sec_oks
  set sec_zdb_id = (select term_zdb_id 
      		       from term
		       where term_ont_id = sec_id
		       and term_ontology='pato.quality');

create temp table sec_unload 
  (
    prim_id varchar(50),
    sec_id varchar(50)
  );

--update the secondary terms in ZFIN

!echo "here is the secondary update for pato terms" ;

update term
  set term_is_secondary = 't'
  where exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id
		  and term_ontology ='pato.quality') ;

unload to term_no_longer_secondary.txt
  select term_name, term_ont_id, term_zdb_id
    from term
    where term_is_secondary = 't'
    and term_ontology = 'pato.quality'
    and not exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id
		  ) ;

--set these back to primary for now

update term
  set term_is_secondary = 'f'
  where not exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id
		  ) 
  and term_is_secondary = 't'
  and term_ontology = 'pato.quality';


update atomic_phenotype
  set apato_quality_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = apato_quality_zdb_id)
  where exists (Select 'x' from term
  	       	       where term_is_Secondary = 't'
		       and apato_quality_zdb_id = term_zdb_id);    

delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_quality_zdb_id = sec_zdb_id);

update apato_infrastructure
  set api_quality_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = api_quality_zdb_id)
  where exists (Select 'x' from term
  	       	       where term_is_secondary = 't'
		       and api_quality_zdb_id = term_zdb_id);

--create a table for unload to report 

insert into sec_unload (sec_id, prim_id)
  select sec_id, prim_id
    from sec_oks
    where exists (select 'x' from
		    term where term_ont_id = sec_id
		    and term_ontology = 'pato.quality') ;


create temp table sec_unload_report 
  (
    sec_id varchar(50),
    prim_id varchar(50),
    term_name varchar(255),
    onto varchar(50),
    geno_handle	varchar(255),
    exp_name varchar(255),
    apato_pub_zdb_id varchar(50)
  );

insert into sec_unload_report
  select 'Now Secondary: '||sec_id, 
	'Now Primary: '||prim_id, 
	'Name: '||term_name, 
	'Ontology: '||term_ontology,
 	'Genotype Handle: '||geno_handle,
	'Experiment Name: '||exp_name,
	'Pub: '||apato_pub_zdb_id
    from sec_unload, 
		term, 
		atomic_phenotype, 
		genotype, 
		genotype_experiment, 
		experiment
    where sec_id = term_ont_id
    and apato_quality_zdb_id = term_zdb_id
    and apato_genox_zdb_id = genox_zdb_id
    and genox_exP_zdb_id = exp_zdb_id
    and genox_geno_zdb_id = geno_zdb_id 
    and term_ontology = 'pato.quality';

unload to 'sec_unload_report'
  select * from sec_unload_report;

--!echo "now deal with relationships" ;

create temp table tmp_rels (
	termrel_term_1_id varchar(50),
	termrel_term_2_id varchar(50),
	termrel_type varchar(40)
) with no log ;

create index rtermrels_term_1_id_index
  on tmp_rels (termrel_term_1_id)
  using btree in idxdbs1 ;

create index rtermrels_term_2_id_index
  on tmp_rels (termrel_term_2_id)
  using btree in idxdbs2 ;

load from patoterm_relationships.unl
  insert into tmp_rels ;

update statistics high for table tmp_rels ;
update statistics high for table term ;

create temp table tmp_zfin_rels  (
	termrel_zdb_id varchar(50),
	termrel_term_1_zdb_id varchar(50),
	termrel_term_2_zdb_id varchar(50),
	termrel_type varchar(40)
) with no log ;

insert into tmp_zfin_rels(
	termrel_zdb_id,
	termrel_term_1_zdb_id,
	termrel_term_2_zdb_id,
	termrel_type)
  select get_id('TERMREL'),
	(select a.term_zdb_id
	   from term a
	   where a.term_ont_id = termrel_term_1_id),
	(select b.term_zdb_id
	   from term b
	   where b.term_ont_id = termrel_term_2_id),
	termrel_type
	from tmp_rels ;

update statistics high for table zdb_active_data;

--!echo "add any new term relationship types" ;

insert into term_relationship_type 
  select distinct termrel_type
		from tmp_zfin_rels
		where not exists (Select 'x'
					from term_relationship_type
					where termreltype_name = termrel_type); 

delete from zdb_active_data
  where zactvd_zdb_id like 'ZDB-TERMREL-%';


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

update statistics high for table term_relationship ;

update obo_file
  set (obofile_text, obofile_load_date, obofile_load_process) = (filetoblob("<!--|ROOT_PATH|-->/server_apps/data_transfer/PATO/quality.obo","server"),CURRENT YEAR TO SECOND, "Automated PATO Load")
  where obofile_name = "quality.obo" ;

select lotofile(obofile_text, "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/quality.obo!","server")
  from obo_file
  where obofile_name = 'quality.obo' ;

--rollback work;
commit work;

