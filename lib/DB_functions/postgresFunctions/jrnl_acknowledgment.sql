create or replace function 
jrnl_acknowledgment (
  jrnlZdbId        journal.jrnl_zdb_id%TYPE)

  returns text as $$

  -- Returns an html acknowledgment
  -- Template
  -- ZFIN wishes to thank the journal (link)Jrnl_Name for granting us 
  -- permission to reproduce figures from this article.  Please note 
  -- that this material may be protected by copyright.
  --
  -- A -746 error is returned if
  --   The parameter is null.  
  --   The parameter is not pub zdb_id  
 

  declare jrnl_ackn      text;
   zdb_jrnl_count integer;
   jrnl_url       varchar(255);
   journal_name   varchar(255);
   add_on_text    varchar(50);  

 begin  
  -- Check that the parameter is not null
  if (jrnlZdbId = '') then
    raise exception 'Parameter is null';
  end if;

  
  -- Check that the parameter is a journal zdb_id
  select count(*)
    into zdb_jrnl_count
    from journal
    where jrnl_zdb_id = jrnlZdbId;
    
  if (zdb_jrnl_count = 0) then
    raise exception 'Parameter is not in the Journal table';
  end if;
  
  select srcurl_url, jrnl_name
    into jrnl_url, journal_name
    from source_url, journal
   where srcurl_source_zdb_id = jrnlZdbId
     and srcurl_purpose = 'information'
     and jrnlZdbId = jrnl_zdb_id;
     
  return "<a href='" || jrnl_url || "'>" || journal_name || "</a> " ;
  
end 

$$ LANGUAGE plpgsql
