-- File: fixAnnotationsUponOntologyLoad.sql
-- In this script alll annotations, phenotype and expressions are
-- updated after a new ontology is loaded.
-- In particular, any term that has turned into a secondary term is
-- updated with its corresponding primary term, i.e. the term into which
-- the original term was merged into.

--begin work ;

-- Phenotype-related updates after a new ontology is loaded.
-- update super terms
unload to 'phenotype-superterm-updates.unl'
    select distinct apato_zdb_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from atomic_phenotype, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and apato_superterm_zdb_id = term_zdb_id)
      and super.term_zdb_id = apato_superterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = apato_superterm_zdb_id;

update atomic_phenotype
  set apato_superterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = apato_superterm_zdb_id)
  where exists (Select term_zdb_id from term , sec_oks
  	       	       where term_is_Secondary = 't'
		       and apato_superterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id );

-- update sub terms
unload to 'phenotype-subterm-updates.unl'
    select distinct apato_zdb_id, sub.term_zdb_id, sub.term_name, replacement.term_zdb_id, replacement.term_name
      from atomic_phenotype, term as sub, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and apato_subterm_zdb_id = term_zdb_id)
      and sub.term_zdb_id = apato_subterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = apato_subterm_zdb_id;

update atomic_phenotype
  set apato_subterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = apato_subterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and apato_subterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- update quality terms
unload to 'phenotype-subterm-updates.unl'
    select distinct apato_zdb_id, quality.term_zdb_id, quality.term_name, replacement.term_zdb_id, replacement.term_name
      from atomic_phenotype, term as quality, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and apato_quality_zdb_id = term_zdb_id)
      and quality.term_zdb_id = apato_quality_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = apato_quality_zdb_id;

update atomic_phenotype
  set apato_quality_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = apato_quality_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and apato_quality_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- Should we update the pile structure rather than removing it?
-- remove superterms
delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_superterm_zdb_id = sec_zdb_id);
-- remove subterms
delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_subterm_zdb_id = sec_zdb_id);

-- remove quality terms
delete from apato_infrastructure
  where exists  (Select 'x' from sec_oks
    	  	 	 where api_quality_zdb_id = sec_zdb_id);


-- Superterm update
update apato_infrastructure
  set api_superterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = api_superterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_secondary = 't'
		       and api_superterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- subterm update
update apato_infrastructure
  set api_subterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = api_subterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_secondary = 't'
		       and api_subterm_zdb_id = term_zdb_id
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
    select distinct xpatres_zdb_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from expression_result, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and xpatres_superterm_zdb_id = term_zdb_id)
      and super.term_zdb_id = xpatres_superterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = xpatres_superterm_zdb_id;

update expression_result
  set xpatres_superterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = xpatres_superterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and xpatres_superterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);

-- update sub terms
unload to 'expression-subterm-updates.unl'
    select distinct xpatres_zdb_id, sub.term_zdb_id, sub.term_name, replacement.term_zdb_id, replacement.term_name
      from expression_result, term as sub, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and xpatres_subterm_zdb_id = term_zdb_id)
      and sub.term_zdb_id = xpatres_superterm_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = xpatres_subterm_zdb_id;

update expression_result
  set xpatres_subterm_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = xpatres_subterm_zdb_id)
  where exists (Select term_zdb_id from term, sec_oks
  	       	       where term_is_Secondary = 't'
		       and xpatres_subterm_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id);


--commit work ;

--rollback work; 