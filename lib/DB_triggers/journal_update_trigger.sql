create trigger journal_update_trigger 
   update on journal 
     referencing new as new_journal old as old_j
     for each row (
       execute function scrub_char(new_journal.jrnl_name)
         into jrnl_name,
       execute function scrub_char(new_journal.jrnl_abbrev)
         into jrnl_abbrev,
       execute function lower(new_journal.jrnl_name)
         into jrnl_name_lower,
       execute function lower(new_journal.jrnl_abbrev)
         into jrnl_abbrev_lower,
       execute procedure addSourceAlias(old_j.jrnl_zdb_id,old_j.jrnl_name)
     );
