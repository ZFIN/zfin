drop trigger if exists phenotype_statement_trigger on phenotype_statement;

create or replace function phenotype_statement()
returns trigger as
$BODY$
begin

	perform p_term_is_not_obsolete_or_secondary(NEW.phenos_quality_zdb_id);
        perform p_term_is_not_obsolete_or_secondary(NEW.phenos_entity_1_superterm_zdb_id);
        perform p_term_is_not_obsolete_or_secondary(NEW.phenos_entity_1_subterm_zdb_id);
	perform p_term_is_not_obsolete_or_secondary(NEW.phenos_entity_2_superterm_zdb_id);
	perform p_term_is_not_obsolete_or_secondary(NEW.phenos_entity_2_subterm_zdb_id);

   RETURN NULL;
end;

$BODY$ LANGUAGE plpgsql;

create trigger phenotype_statement_trigger after insert or update on phenotype_statement
 for each row
 execute procedure phenotype_statement();
