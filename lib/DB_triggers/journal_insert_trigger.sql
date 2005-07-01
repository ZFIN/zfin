create trigger journal_insert_trigger 
   insert on journal 
     referencing new as new_journal
     for each row (
       execute function scrub_char(new_journal.jrnl_name)
         into jrnl_name,
       execute function scrub_char(new_journal.jrnl_abbrev)
         into jrnl_abbrev,
       execute function lower(new_journal.jrnl_name)
         into jrnl_name_lower,
       execute function lower(new_journal.jrnl_abbrev)
         into jrnl_abbrev_lower
     );
