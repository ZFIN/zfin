-- File: fixZecoAnnotations.sql
-- In this script annotations using ZECO are updated (experiment_conditions)
-- In particular, any term that has turned into a secondary term is
-- updated with its corresponding primary term, i.e. the term into which
-- the original term was merged into.


-- update zeco terms
unload to 'zeco_updates'
    select distinct expcond_zdb_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from experiment_condition, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and expcond_zeco_term_zdb_id = term_zdb_id)
      and super.term_zdb_id = expcond_zeco_term_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = expcond_zeco_term_zdb_id;

update experiment_condition
  set expcond_zeco_term_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = expcond_zeco_term_zdb_id)
  where exists (Select term_zdb_id from term , sec_oks
  	       	       where term_is_Secondary = 't'
		       and expcond_zeco_term_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id );

-- update chebi terms
unload to 'zeco_updates'
    select distinct expcond_zdb_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from experiment_condition, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and expcond_chebi_term_zdb_id = term_zdb_id)
      and super.term_zdb_id = expcond_chebi_term_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = expcond_chebi_term_zdb_id;

update experiment_condition
  set expcond_zeco_term_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = expcond_chebi_term_zdb_id)
  where exists (Select term_zdb_id from term , sec_oks
  	       	       where term_is_Secondary = 't'
		       and expcond_chebi_term_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id );

-- update zeco-taxonomy terms
unload to 'zeco_updates'
    select distinct expcond_zdb_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from experiment_condition, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and expcond_taxon_term_zdb_id = term_zdb_id)
      and super.term_zdb_id = expcond_taxon_term_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = expcond_taxon_term_zdb_id;

update experiment_condition
  set expcond_zeco_term_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = expcond_taxon_term_zdb_id)
  where exists (Select term_zdb_id from term , sec_oks
  	       	       where term_is_Secondary = 't'
		       and expcond_taxon_term_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id );

-- update AO terms
unload to 'zeco_updates'
    select distinct expcond_zdb_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from experiment_condition, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and expcond_ao_term_zdb_id = term_zdb_id)
      and super.term_zdb_id = expcond_ao_term_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = expcond_ao_term_zdb_id;

update experiment_condition
  set expcond_zeco_term_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = expcond_ao_term_zdb_id)
  where exists (Select term_zdb_id from term , sec_oks
  	       	       where term_is_Secondary = 't'
		       and expcond_ao_term_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id );

-- update AO terms
unload to 'zeco_updates'
    select distinct expcond_zdb_id, super.term_zdb_id, super.term_name, replacement.term_zdb_id, replacement.term_name
      from experiment_condition, term as super, term as replacement, sec_oks
      where exists (Select 'x' from term
                       where term_is_Secondary = 't'
                   and expcond_go_cc_term_zdb_id = term_zdb_id)
      and super.term_zdb_id = expcond_go_cc_term_zdb_id
      and replacement.term_zdb_id = prim_zdb_id
      and sec_zdb_id = expcond_go_cc_term_zdb_id;

update experiment_condition
  set expcond_zeco_term_zdb_id = (Select prim_zdb_id
      			        from sec_oks
				where sec_zdb_id = expcond_go_cc_term_zdb_id)
  where exists (Select term_zdb_id from term , sec_oks
  	       	       where term_is_Secondary = 't'
		       and expcond_go_cc_term_zdb_id = term_zdb_id
		       and term_zdb_id = sec_zdb_id );

