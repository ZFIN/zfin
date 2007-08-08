create function get_genotype_handle( genoZdbId varchar(50) )

  returning lvarchar;	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, returns the genotype Handle name in 
  --  format. 
  -- [the same value that is stored in the genotype table. this function
  --  generates the value that is stored.]
  --
  --  INPUT VARS: 
  --     genoZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     genotype Handle with proper  formatting. 
  --     NULL if genoZdbId does not exist in genotype table.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define genoHandle lvarchar;
  define featAbbrev	 like marker.mrkr_abbrev; --b/c mrkr_abbrev is longer
  define genoBackground  lvarchar;
  define zygFish	 like zygocity.zyg_abbrev;
  define zygMom 	 like zygocity.zyg_abbrev;
  define zygDad 	 like zygocity.zyg_abbrev;
  define featDisplay     lvarchar;
  define startHandle     like genotype.geno_handle;
  define wildtype        like genotype.geno_is_wildtype;
  
  select geno_handle, geno_is_wildtype 
  into startHandle, wildtype 
  from genotype where geno_zdb_id = genoZdbId;

  if ( wildtype != 't') then
  
    let genoHandle = '';

    foreach
       select distinct feature_abbrev, z1.zyg_abbrev, z2.zyg_abbrev, z3.zyg_abbrev
         into featAbbrev, zygFish, zygMom, zygDad
         from feature, genotype_feature, zygocity z1, zygocity z2, zygocity z3
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = z1.zyg_zdb_id
          and genofeat_mom_zygocity = z2.zyg_zdb_id
          and genofeat_dad_zygocity = z3.zyg_zdb_id
     order by 1

     let featDisplay = featAbbrev||'['||zygFish||','||zygMom||','||zygDad||']';
     
        if (genoHandle == '') then
          let genoHandle = featDisplay ;
          
        else
          let genoHandle = genoHandle ||' '|| featDisplay ;
        
        end if
        
    end foreach

    select get_genotype_backgrounds(genoZdbId) 
    into genoBackground
    from single;
  
    if (genoBackground != '') then
     
      let genoHandle = genoHandle || genoBackground ;
     
    end if
  
  else
  
    let genoHandle = startHandle;
  
  end if

  return genoHandle;

end function;
