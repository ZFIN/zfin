-- File: fixAnnotationsUponOntologyLoad.sql
-- In this script all annotations, phenotype and expressions are
-- updated after a new ontology is loaded.
-- In particular, any term that has turned into a secondary term is
-- updated with its corresponding primary term, i.e. the term into which
-- the original term was merged into.

--begin work ;

-- Phenotype-related updates after a new ontology is loaded.


create temp table delete_phenotype_statement
  (
    phenos_id serial
  );

insert into delete_phenotype_statement (phenos_id)
select phenos_pk_id from phenotype_statement as ps
where exists (
select 'x' from phenotype_statement as psc, sec_oks
where ps.phenos_pk_id != psc.phenos_pk_id AND
      ps.phenos_phenox_pk_id = psc.phenos_phenox_pk_id AND
      prim_zdb_id =  psc.phenos_entity_1_superterm_zdb_id AND
      nvl(ps.phenos_entity_1_subterm_zdb_id,'1') =  nvl(psc.phenos_entity_1_subterm_zdb_id,'1') AND
      nvl(ps.phenos_entity_2_subterm_zdb_id,'1') =  nvl(psc.phenos_entity_2_subterm_zdb_id,'1') AND
      nvl(ps.phenos_entity_2_superterm_zdb_id,'1') =  nvl(psc.phenos_entity_2_superterm_zdb_id,'1') AND
      ps.phenos_quality_zdb_id =  psc.phenos_quality_zdb_id AND
      ps.phenos_tag =  psc.phenos_tag  AND
      ps.phenos_tag =  psc.phenos_tag  AND
      ps.phenos_entity_1_superterm_zdb_id = sec_zdb_id
);

insert into delete_phenotype_statement (phenos_id)
select phenos_pk_id from phenotype_statement as ps
where exists (
select 'x' from phenotype_statement as psc, sec_oks
where ps.phenos_pk_id != psc.phenos_pk_id AND
      ps.phenos_phenox_pk_id = psc.phenos_phenox_pk_id AND
      prim_zdb_id =  psc.phenos_entity_1_subterm_zdb_id AND
      nvl(ps.phenos_entity_1_superterm_zdb_id,'1') =  nvl(psc.phenos_entity_1_superterm_zdb_id,'1') AND
      nvl(ps.phenos_entity_2_subterm_zdb_id,'1') =  nvl(psc.phenos_entity_2_subterm_zdb_id,'1') AND
      nvl(ps.phenos_entity_2_superterm_zdb_id,'1') =  nvl(psc.phenos_entity_2_superterm_zdb_id,'1') AND
      ps.phenos_quality_zdb_id =  psc.phenos_quality_zdb_id AND
      ps.phenos_tag =  psc.phenos_tag  AND
      ps.phenos_tag =  psc.phenos_tag  AND
      nvl(ps.phenos_entity_1_subterm_zdb_id,'') = sec_zdb_id
);

insert into delete_phenotype_statement (phenos_id)
select phenos_pk_id from phenotype_statement as ps
where exists (
select 'x' from phenotype_statement as psc, sec_oks
where ps.phenos_pk_id != psc.phenos_pk_id AND
      ps.phenos_phenox_pk_id = psc.phenos_phenox_pk_id AND
      prim_zdb_id =  psc.phenos_entity_2_subterm_zdb_id AND
      nvl(ps.phenos_entity_1_superterm_zdb_id,'1') =  nvl(psc.phenos_entity_1_superterm_zdb_id,'1') AND
      nvl(ps.phenos_entity_1_subterm_zdb_id,'1') =  nvl(psc.phenos_entity_1_subterm_zdb_id,'1') AND
      nvl(ps.phenos_entity_2_superterm_zdb_id,'1') =  nvl(psc.phenos_entity_2_superterm_zdb_id,'1') AND
      ps.phenos_quality_zdb_id =  psc.phenos_quality_zdb_id AND
      ps.phenos_tag =  psc.phenos_tag  AND
      ps.phenos_tag =  psc.phenos_tag  AND
      nvl(ps.phenos_entity_2_subterm_zdb_id,'') = sec_zdb_id
);

insert into delete_phenotype_statement (phenos_id)
select phenos_pk_id from phenotype_statement as ps
where exists (
select 'x' from phenotype_statement as psc, sec_oks
where ps.phenos_pk_id != psc.phenos_pk_id AND
      ps.phenos_phenox_pk_id = psc.phenos_phenox_pk_id AND
      prim_zdb_id =  psc.phenos_entity_2_superterm_zdb_id AND
      nvl(ps.phenos_entity_1_superterm_zdb_id,'1') =  nvl(psc.phenos_entity_1_superterm_zdb_id,'1') AND
      nvl(ps.phenos_entity_1_subterm_zdb_id,'1') =  nvl(psc.phenos_entity_1_subterm_zdb_id,'1') AND
      nvl(ps.phenos_entity_2_subterm_zdb_id,'1') =  nvl(psc.phenos_entity_2_subterm_zdb_id,'1') AND
      ps.phenos_quality_zdb_id =  psc.phenos_quality_zdb_id AND
      ps.phenos_tag =  psc.phenos_tag  AND
      ps.phenos_tag =  psc.phenos_tag  AND
      nvl(ps.phenos_entity_2_superterm_zdb_id,'') = sec_zdb_id
);

select * From delete_phenotype_statement;

delete from phenotype_statement
where exists(
select 'x' from delete_phenotype_statement
where phenos_id = phenos_pk_id
);

drop table delete_phenotype_statement;

-- update super terms
unload to 'phenotype-superterm-updates.unl'
    select distinct phenos_pk_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from phenotype_statement, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and phenos_entity_1_superterm_zdb_id = term_zdb_id)
      and super.term_zdb_id = phenos_entity_1_superterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = phenos_entity_1_superterm_zdb_id;

update phenotype_statement
  set phenos_entity_1_superterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = phenos_entity_1_superterm_zdb_id)
  where exists (Select term_zdb_id from term , sec_oks
  	       	       where term_is_Secondary = 't'
		       and phenos_entity_1_superterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id );

-- update entity sub terms
unload to 'phenotype-subterm-updates.unl'
    select distinct phenos_pk_id, sub.term_zdb_id, sub.term_name, replacement.term_zdb_id, replacement.term_name
      from phenotype_statement, term as sub, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and phenos_entity_1_subterm_zdb_id = term_zdb_id)
      and sub.term_zdb_id = phenos_entity_1_subterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = phenos_entity_1_subterm_zdb_id;

update phenotype_statement
  set phenos_entity_1_subterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = phenos_entity_1_subterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and phenos_entity_1_subterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- update related super terms
unload to 'phenotype-related-entity-superterm-updates.unl'
    select distinct phenos_pk_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from phenotype_statement, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and phenos_entity_2_superterm_zdb_id = term_zdb_id)
      and super.term_zdb_id = phenos_entity_2_superterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = phenos_entity_2_superterm_zdb_id;

update phenotype_statement
  set phenos_entity_2_superterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = phenos_entity_2_superterm_zdb_id)
  where exists (Select term_zdb_id from term , sec_oks
  	       	       where term_is_Secondary = 't'
		       and phenos_entity_2_superterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id );

-- update related entity sub terms
unload to 'phenotype-related-entity-subterm-updates.unl'
    select distinct phenos_pk_id, sub.term_zdb_id, sub.term_name, replacement.term_zdb_id, replacement.term_name
      from phenotype_statement, term as sub, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and phenos_entity_2_subterm_zdb_id = term_zdb_id)
      and sub.term_zdb_id = phenos_entity_2_subterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = phenos_entity_2_subterm_zdb_id;

update phenotype_statement
  set phenos_entity_2_subterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = phenos_entity_2_subterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and phenos_entity_2_subterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- update quality terms
unload to 'phenotype-subterm-updates.unl'
    select distinct phenos_pk_id, quality.term_zdb_id, quality.term_name, replacement.term_zdb_id, replacement.term_name
      from phenotype_statement, term as quality, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and phenos_quality_zdb_id = term_zdb_id)
      and quality.term_zdb_id = phenos_quality_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = phenos_quality_zdb_id;

update phenotype_statement
  set phenos_quality_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = phenos_quality_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and phenos_quality_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- Should we update the pile structure rather than removing it?
-- remove superterms
delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_entity_1_superterm_zdb_id = sec_zdb_id);
-- remove subterms
delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_entity_1_subterm_zdb_id = sec_zdb_id);

-- remove superterms
delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_entity_2_superterm_zdb_id = sec_zdb_id);
-- remove subterms
delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_entity_2_subterm_zdb_id = sec_zdb_id);

-- remove quality terms
delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_quality_zdb_id = sec_zdb_id);

-- entity Superterm update
update apato_infrastructure
  set api_entity_1_superterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = api_entity_1_superterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_secondary = 't'
		       and api_entity_1_superterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- entity subterm update
update apato_infrastructure
  set api_entity_1_subterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = api_entity_1_subterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_secondary = 't'
		       and api_entity_1_subterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- Superterm update
update apato_infrastructure
  set api_entity_2_superterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = api_entity_2_superterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_secondary = 't'
		       and api_entity_2_superterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- subterm update
update apato_infrastructure
  set api_entity_2_subterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = api_entity_2_subterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_secondary = 't'
		       and api_entity_2_subterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- Quality update
update apato_infrastructure
  set api_quality_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = api_quality_zdb_id)
  where exists (Select term_zdb_id  from term, sec_oks
  	       	       where term_is_secondary = 't'
		       and api_quality_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);



-- Expression-related updates after a new ontology is loaded.
-- update super terms
unload to 'expression-superterm-updates.unl'
    select distinct efs_xpatex_zdb_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from expression_result2, expression_figure_stage, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and xpatres_superterm_zdb_id = term_zdb_id)
      and xpatres_efs_id = efs_pk_id
      and super.term_zdb_id = xpatres_superterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = xpatres_superterm_zdb_id;

update expression_result2
  set xpatres_superterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = xpatres_superterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and xpatres_superterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- update sub terms
unload to 'debug'
  select * from sec_oks;

unload to 'expression-subterm-updates.unl'
    select distinct efs_xpatex_zdb_id, sub.term_zdb_id, sub.term_name, replacement.term_zdb_id, replacement.term_name
      from expression_result2, expression_figure_stage, term as sub, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and xpatres_subterm_zdb_id = term_zdb_id)
      and xpatres_efs_id = efs_pk_id
      and sub.term_zdb_id = xpatres_superterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = xpatres_subterm_zdb_id;

delete from expression_result2
where exists (
select 'x' from sec_oks
where xpatres_subterm_zdb_id = sec_zdb_id
and exists (
select 'x' from expression_result2 as er2
 where xpatres_efs_id = er2.xpatres_efs_id
 and xpatres_expression_found= er2.xpatres_expression_found
 and xpatres_superterm_zdb_id = er2.xpatres_superterm_zdb_id
 and er2.xpatres_subterm_zdb_id = prim_zdb_id
)
);


update expression_result2
  set xpatres_subterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = xpatres_subterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and xpatres_subterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);


-- update go annotations 
-- only those records that are created by ZFIN, not the imported annotations. 
unload to 'go-evidence-updates.unl'
    select distinct mrkrgoev_source_zdb_id, mrkrgoev_mrkr_zdb_id, sub.term_zdb_id, sub.term_name, replacement.term_zdb_id, replacement.term_name
      from marker_go_term_evidence, term as sub, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and mrkrgoev_term_zdb_id = term_zdb_id)
      and sub.term_zdb_id = mrkrgoev_term_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = mrkrgoev_term_zdb_id;

update marker_go_term_evidence
  set mrkrgoev_term_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = mrkrgoev_term_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and mrkrgoev_term_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id 
		       and mrkrgoev_annotation_organization_created_by = 'ZFIN');


insert into sec_unload (sec_id, prim_id)
  select sec_id, prim_id
    from sec_oks
    where exists (select 'x' from
            term where term_ont_id = sec_id) ;

unload to 'debug'
  select * from sec_unload;

create temp table sec_unload_report
  (
    sec_id varchar(50),
    prim_id varchar(50),
    term_name varchar(255),
    onto varchar(50),
    geno_handle    varchar(255),
    exp_name varchar(255),
    apato_pub_zdb_id varchar(50)
  );

insert into sec_unload_report
  select sec_id,
    prim_id,
    term_name,
    term_ontology,
     geno_handle,
    exp_name,
    fig_source_zdb_id
    from sec_unload,
        term,
        phenotype_statement,
        phenotype_experiment,
        genotype,
        fish,
        fish_experiment,
        experiment,
        figure
    where sec_id = term_ont_id
    and phenos_quality_zdb_id = term_zdb_id
    and phenox_genox_zdb_id = genox_zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
    and phenox_fig_zdb_id = fig_zdb_id
    and genox_exP_zdb_id = exp_zdb_id
    and genox_fish_zdb_id = fish_zdb_id
    and fish_genotype_zdb_id = geno_zdb_id ;

unload to 'sec_unload_report.unl'
  select * from sec_unload_report;

drop table sec_unload_report;

--commit work ;

--rollback work; 
