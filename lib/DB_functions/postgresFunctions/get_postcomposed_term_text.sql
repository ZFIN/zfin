create or replace function get_postcomposed_term_text ( supertermZdbId text, 
                                                 subtermZdbId text  )

  returns text as $resultHtml$

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of term, 
  -- this returns the name of the entity

  --
  -- INPUT VARS: 
  --   entityZdbId   Get the name of this entity
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Name of entity with proper text formatting.
  --   NULL if entityZdbId does not exist the term table.
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  declare resultHtml text;
   supertermId text;
   supertermName text;
   supertermOntology text;
   subtermId text;
   subtermName text;
   subtermOntology text; 

  begin 

  select term_name, term_ont_id, term_ontology
  into supertermName, supertermID, supertermOntology
  from term
  where term_zdb_id = supertermZdbId;

  select term_name, term_ont_id, term_ontology
  into subtermName, subtermID, subtermOntology
  from term
  where term_zdb_id = subtermZdbId;


  if (subtermID is null) then
     resultHtml = supertermName;
  else 
     resultHtml = supertermName || ': ' || subtermName;
  end if;

  return resultHtml;

  end
$resultHtml$ LANGUAGE plpgsql
