drop trigger if exists marker_sequence_trigger on marker_sequence;

create or replace function marker_sequence()
returns trigger as
$BODY$
declare recattrib_source_zdb_id text;
begin

     perform p_marker_sequence_unique(NEW.seq_mrkr_zdb_id, 
				     NEW.seq_sequence, 
				     NEW.seq_sequence_2);
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger marker_sequence_trigger before insert or update on marker_sequence
 for each row
 execute procedure marker_sequence();
