create trigger lab_insert_trigger
  insert on lab
  referencing new as new_lab
  for each row (
    execute function 
      scrub_char(new_lab.phone) into phone,
    execute function 
      scrub_char(new_lab.fax) into fax,
    execute function 
      scrub_char(new_lab.email) into email,
    execute function 
      scrub_char(new_lab.url) into url,
    execute function 
      scrub_char(new_lab.name) into name
  );
