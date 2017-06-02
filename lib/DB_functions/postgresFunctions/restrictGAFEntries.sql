create or replace function restrictGAFEntries (vTermZdbId varchar(50), vEvidenceCode varchar(3))
returns void as $$

begin 
if (vEvidenceCode != "IEA")
then
  if exists (Select 'x' from term_subset, ontology_subset
   	  	  where termsub_term_zdb_id = vTermZdbId
		  and termsub_subset_id = osubset_pk_id
		  and osubset_subset_name in ('gocheck_do_not_annotate','gocheck_do_not_manually_annotate'))
  then 
    raise exception 'FAIL!: GO term can not be in do-not-annotate subset';
  end if;
end if;

if (vEvidenceCode = 'IEA')
then
  if exists (Select 'x' from term_subset, ontology_subset
   	  	  where termsub_term_zdb_id = vTermZdbId
		  and termsub_subset_id = osubset_pk_id
		  and osubset_subset_name in ('gocheck_do_not_annotate'))
  then 
    raise exception 'FAIL!: GO term can not be in do-not-annotate subset';
  end if;
end if;
end

$$ LANGUAGE plpgsql;
