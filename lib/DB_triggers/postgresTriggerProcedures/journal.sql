drop trigger if exists journal_trigger on journal;

create or replace function journal()
returns trigger as
$BODY$
declare jrnl_name journal.jrnl_name%TYPE;
declare jrnl_abbrev journal.jrnl_abbrev%TYPE;
declare jrnl_name_lower journal.jrnl_name_lower%TYPE;
declare jrnl_abbrev_lower journal.jrnl_abbrev_lower%TYPE;

begin
     
     jrnl_name = (select scrub_char(NEW.jrnl_name));
     NEW.jrnl_name = jrnl_name;

     jrnl_abbrev = (select scrub_char(NEW.jrnl_abbrev));
     NEW.jrnl_abbrev = jrnl_abbrev;
     
     jrnl_name_lower = lower(NEW.jrnl_name_lower);
     NEW.jrnl_name_lower = jrnl_name_lower;

     jrnl_abbrev_lower = lower(NEW.jrnl_abbrev_lower);
     NEW.jrnl_abbrev_lower = jrnl_abbrev_lower;



     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger journal_trigger before insert or update on journal
 for each row
 execute procedure journal();
