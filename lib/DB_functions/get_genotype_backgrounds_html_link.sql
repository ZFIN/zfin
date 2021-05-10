
create or replace function get_genotype_backgrounds_html_link( genoZdbId text )

  returns text as $backgroundList$	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, returns a comma seperated list of 
  -- backgrounds for the genotype.
  --
  --  INPUT VARS: 
  --     genoZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     Comma seperated list of backgrounds.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  declare backName       varchar(255);
   backNameDisplay	 genotype.geno_display_name%TYPE;
   backHandle		 genotype.geno_handle%TYPE;
   backZdbId		 genotype.geno_zdb_id%TYPE;
   backgroundList text :='unspecified';
  
  begin 
  for backZdbId, backNameDisplay, backHandle in 
    select geno_zdb_id, geno_display_name, geno_handle
      from genotype_background, genotype
     where genoback_geno_zdb_id = genoZdbId
       and genoback_background_zdb_id = geno_zdb_id
    order by geno_display_name
  loop         
      -- if background(wildtype) abbrev is the same as name, then only show name
      if (backNameDisplay = backHandle) then
	 backName = '<a href="/' ||
		backZdbId || '">' || '<span class="wildtype">' || backNameDisplay || '</span>' || '</a>';
      -- otherwise, show abbrev in parenthesis after name
      else
	 backName = '<a href="/' ||
		backZdbId || '">' || '<span class="wildtype">' || backNameDisplay || '</span>' || '</a>' ||
		' (' || backHandle || ')';
      end if;
		
      if (backgroundList = 'unspecified') then
    
           backgroundList = backName ;
      
      else
  
           backgroundList = backgroundList ||', '|| backName ;  
  
      end if;

  end loop;

  return backgroundList;
end
$backgroundList$ LANGUAGE plpgsql
