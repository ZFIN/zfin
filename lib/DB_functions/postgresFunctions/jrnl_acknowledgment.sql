create function 
jrnl_acknowledgment (
  jrnlZdbId        like journal.jrnl_zdb_id)

  returning lvarchar;

  -- Returns an html acknowledgment
  -- Template
  -- ZFIN wishes to thank the journal (link)Jrnl_Name for granting us 
  -- permission to reproduce figures from this article.  Please note 
  -- that this material may be protected by copyright.
  --
  -- A -746 error is returned if
  --   The parameter is null.  
  --   The parameter is not pub zdb_id  
 

  define jrnl_ackn      lvarchar;
  define zdb_jrnl_count integer;
  define jrnl_url       varchar(255);
  define journal_name   varchar(255);
  define add_on_text    varchar(50);  

  
  -- Check that the parameter is not null
  if (jrnlZdbId == '') then
    raise exception -746, 0,		 -- !!! ERROR EXIT
      'Parameter is null';
  end if

  
  -- Check that the parameter is a journal zdb_id
  select count(*)
    into zdb_jrnl_count
    from journal
    where jrnl_zdb_id = jrnlZdbId;
    
  if (zdb_jrnl_count == 0) then
    raise exception -746, 0,		 -- !!! ERROR EXIT
      'Parameter is not in the Journal table';
  end if
  
  select srcurl_url, jrnl_name
    into jrnl_url, journal_name
    from source_url, journal
   where srcurl_source_zdb_id = jrnlZdbId
     and srcurl_purpose = 'information'
     and jrnlZdbId = jrnl_zdb_id;
     
  return "<a href='" || jrnl_url || "'>" || journal_name || "</a> " ;
  
end function;
