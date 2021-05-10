create or replace function get_ao_name_html_link( aoZdbId varchar ) returns varchar as $htmlLink$
 

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of an AO, this returns the AO name
  -- properly formatted and embedded in an HTML link to the AO view page.
  --
  -- INPUT VARS: 
  --   aoZdbId   
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   AO name with proper HTML formatting, embedded in an HTML link
  --   NULL if aoZdbId does not exist in feature table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  declare aoName	 term.term_name%TYPE;
   	  aoNameHTML		 varchar;

  begin
  select term_name
    into aoName
    from term
   where term_zdb_id = aoZdbId;

  aoNameHTML := '<span class="mutant">' || aoName || '</span>';

  return 
    '<a href="/action/ontology/term-detail/' || aoZdbId || '">' ||aoNameHTML || '</a>';
  end
 
$htmlLink$ LANGUAGE plpgsql;
