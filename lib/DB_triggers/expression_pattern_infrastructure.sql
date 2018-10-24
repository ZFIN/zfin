drop trigger if exists expression_pattern_infrastructure_trigger on expression_pattern_infrastructure;

create or replace function expression_pattern_infrastructure()
returns trigger as
$BODY$

begin
     
   perform p_check_submitter_is_root(NEW.xpatinf_curator_zdb_id);
   perform p_check_fx_postcomposed_terms(NEW.xpatinf_superterm_zdb_id,NEW.xpatinf_subterm_zdb_id);
   perform p_term_is_not_obsolete_or_secondary(NEW.xpatinf_superterm_zdb_id);
   perform p_term_is_not_obsolete_or_secondary(NEW.xpatinf_subterm_zdb_id);	 


     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger expression_pattern_infrastructure_trigger after insert or update on expression_pattern_infrastructure
 for each row
 execute procedure expression_pattern_infrastructure();
