create or replace function get_genotype_backgrounds( genoZdbId varchar ) returns varchar as $background$ 	

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

  declare backName       genotype.geno_display_name%TYPE;
   backgroundList varchar := '';
  
  begin
  for backName in 
    select distinct geno_handle
      from genotype_background, genotype
     where genoback_geno_zdb_id = genoZdbId
       and genoback_background_zdb_id = geno_zdb_id
    order by geno_handle
  loop        

      if (backgroundList = '') then
    
          backgroundList := backName ;
      
      else
  
          backgroundList := backgroundList ||', '|| backName ;  
  
      end if;

  end loop;

  return backgroundList;
end

$background$ LANGUAGE plpgsql;
