create function get_allele_html( allele varchar(20) )

  returning varchar(20);	

  -- --------------------------------------------------------------------- 
  -- Display all alleles with un_% as unspecified. 
  --
  --  INPUT VARS: 
  --     allele   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     Allele with proper HTML formatting. 
  --     Currently, no additional HTML is added. 
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  if (allele like "un_%") then
      let allele = "unspecified";    
  end if

  return allele;

end function;
