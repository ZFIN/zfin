--begin work ;

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


--commit work ;

--rollback work; 