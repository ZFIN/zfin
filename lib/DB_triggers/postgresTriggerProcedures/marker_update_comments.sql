drop trigger if exists marker_update_comments_trigger on marker_update_comments;

create or replace function marker_update_comments()
returns trigger as
$BODY$
declare mrkr_comments marker.mrkr_comments%TYPE;
begin

     mrkr_comments = (select scrub_char(NEW.mrkr_comments));
     NEW.mrkr_comments = mrkr_comments;

     return new;
end;
$BODY$ LANGUAGE plpgsql;

create trigger marker_update_comments_trigger before update on marker
 for each row
 when (OLD.mrkr_comments IS DISTINCT FROM NEW.mrkr_comments)
 execute procedure marker_update_comments();
