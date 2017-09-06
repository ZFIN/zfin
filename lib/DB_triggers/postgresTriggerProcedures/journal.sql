drop trigger if exists journal_trigger on journal;

create or replace function journal()
returns trigger as
$BODY$
declare jrnl_name journal.jrnl_name%TYPE := scrub_char(NEW.jrnl_name);
declare jrnl_abbrev journal.jrnl_abbrev%TYPE := scrub_char(NEW.jrnl_abbrev);
declare jrnl_name_lower journal.jrnl_name_lower%TYPE := lower(NEW.jrnl_name_lower);
declare jrnl_abbrev_lower journal.jrnl_abbrev_lower%TYPE := lower(NEW.jrnl_abbrev_lower);

begin
     
     NEW.jrnl_name = jrnl_name;

     NEW.jrnl_abbrev = jrnl_abbrev;
     
     NEW.jrnl_name_lower = jrnl_name_lower;

     NEW.jrnl_abbrev_lower = jrnl_abbrev_lower;

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger journal_trigger before insert or update on journal
 for each row
 execute procedure journal();
