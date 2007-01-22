-------------------------------------------------------------------------
--This procedure checks on insert or update of pato_terms in atomic_phenotype
--that the pato_term in the go_term table is not obsolete.
--Obsolete pato terms should not be assigned to valid markers.
--Terms are declared 'obsolete' by the pato_ontology consortium.
-------------------------------------------------------------------------

create procedure p_quality_term_not_obsolete_or_secondary (vTerm varchar(50))

define ok boolean;

let ok = (select term_is_obsolete 
           from term 
           where vTerm = term_zdb_id);

if ok then

  raise exception -746,0,'FAIL!: GO Term is OBSOLETE!';

elif not ok then 

  let ok = (select term_is_secondary
             from term 
             where vTerm = term_zdb_id);
  if ok then 

    raise exception -746,0,'FAIL!: GO Term is SECONDARY!';

  end if ;

end if;

end procedure;
