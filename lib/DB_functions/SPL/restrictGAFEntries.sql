create procedure restrictGAFEntries (vTermZdbId varchar(50))


if exists (Select 'x' from term_subset, ontology_subset
   	  	  where termsub_term_zdb_id = vTermZdbId
		  and termsub_subset_id = osubset_pk_id
		  and osubset_subset_name in ('gocheck_do_not_annotate','gocheck_do_not_manually_annotate'))
then 
  raise exception -746,0,"FAIL!: GO term can not be in do-no-annotate subset";
end if;

end procedure;