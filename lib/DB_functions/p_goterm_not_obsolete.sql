-----------------------------------------------------------------------------------
--This procedure checks on insert or update of go terms in marker_go_term_evidence
--that the go terms in the term table is not obsolete.
--Obsolete go terms should not be assigned to valid markers.
--Terms are declared 'obsolete' by the gene_ontology consortium.
-----------------------------------------------------------------------------------

create or replace function p_goterm_not_obsolete (vGoTerm text)
returns void as $$
declare ok boolean := (select term_is_obsolete
           from term
           where vGoTerm = term_zdb_id);
begin
if ok then

  raise exception 'FAIL!: GO Term is OBSOLETE!';

elsif not ok then 

  ok = (select term_is_secondary
             from term
             where vGoTerm = term_zdb_id);
  if ok then 

    raise exception 'FAIL!: GO Term is SECONDARY!';

  end if ;

end if;
end
$$ LANGUAGE plpgsql
