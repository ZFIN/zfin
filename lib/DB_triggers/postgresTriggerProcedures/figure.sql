drop trigger if exists figure_trigger on figure;

create or replace function figure()
returns trigger as
$BODY$
declare fig_label figure.fig_label%TYPE;
begin

     perform p_insert_into_record_attribution_tablezdbids (
			NEW.fig_zdb_id,
			NEW.fig_source_zdb_id);
     fig_label = (select zero_pad(NEW.fig_label));
     NEW.fig_label = fig_label;

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger figure_trigger before insert or update on figure
 for each row
 execute procedure figure();
