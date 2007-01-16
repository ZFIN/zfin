create function get_genotype_backgrounds( genoZdbId varchar(50) )

  returning lvarchar;	

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

  define backName       like genotype.geno_display_name;
  define backgroundList lvarchar;
  
  let backgroundList = '';
  
  foreach
    select distinct geno_handle
      into backName
      from genotype_background, genotype
     where genoback_geno_zdb_id = genoZdbId
       and genoback_background_zdb_id = geno_zdb_id
    order by geno_handle
             

      if (backgroundList == '') then
    
          let backgroundList = backName ;
      
      else
  
          let backgroundList = backgroundList ||', '|| backName ;  
  
      end if

  end foreach

  return backgroundList;

end function;
