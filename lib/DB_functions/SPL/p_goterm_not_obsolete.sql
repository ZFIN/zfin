-------------------------------------------------------------------------
--This procedure checks on insert or update of go_terms in marker_go_term
--that the go_term in the go_term table is not obsolete.
--Obsolete go terms should not be assigned to valid markers.
--Terms are declared 'obsolete' by the gene_ontology consortium.
-------------------------------------------------------------------------

create procedure p_goterm_not_obsolete (vGoTerm varchar(50))

define ok boolean;

let ok = (select goterm_is_obsolete 
           from go_term 
           where vGoTerm = goterm_zdb_id);

if ok then

  raise exception -746,0,'FAIL!: GO Term is OBSOLETE!';

elif not ok then 

  let ok = (select goterm_is_secondary
             from go_term 
             where vGoTerm = goterm_zdb_id);
  if ok then 

    raise exception -746,0,'FAIL!: GO Term is SECONDARY!';

  end if ;

end if;

end procedure;
