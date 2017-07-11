drop trigger if exists term_relationship_trigger on term_relationship;

create or replace function term_relationship()
returns trigger as
$BODY$
begin
	perform p_check_anatrel_stg_consistent (NEW.termrel_term_1_zdb_id,
                                               NEW.termrel_term_2_zdb_id,
                                               NEW.termrel_type);
	return null;
end;
$BODY$ LANGUAGE plpgsql;

create trigger term_relationship_trigger before insert or update on term_relationship
 for each row
 execute procedure term_relationship();
