begin work ;

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

load from term_secondary.unl
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
		       where term_ont_id = prim_id);

 
update sec_oks
  set sec_zdb_id = (select term_zdb_id 
      		       from term
		       where term_ont_id = sec_id);

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
		  where term_ont_id = sec_id) ;

unload to term_no_longer_secondary.txt
  select term_name, term_ont_id, term_zdb_id
    from term
    where term_is_secondary = 't'
    and not exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id) ;

--set these back to primary for now

update term
  set term_is_secondary = 'f'
  where not exists (Select 'x'
		  from sec_oks
		  where term_ont_id = sec_id) 
  and term_is_secondary = 't';


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
		    term where term_ont_id = sec_id) ;


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
    and genox_geno_zdb_id = geno_zdb_id ;

unload to 'sec_unload_report'
  select * from sec_unload_report;

update obo_file
  set (obofile_text, obofile_load_date, obofile_load_process) = (filetoblob("<!--|ROOT_PATH|-->/server_apps/data_transfer/LoadOntology/quality.obo","server"),CURRENT YEAR TO SECOND, "Automated PATO Load")
  where obofile_name = "quality.obo" ;

select lotofile(obofile_text, "<!--|ROOT_PATH|-->/j2ee/phenote/deploy/WEB-INF/data_transfer/quality.obo!","server")
  from obo_file
  where obofile_name = 'quality.obo' ;


commit work ;

--rollback work; 