create or replace function get_genotype_handle( genoZdbId varchar ) returns varchar as $handle$
	

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
declare genoHandle varchar;
   featAbbrev	  marker.mrkr_abbrev%TYPE; --b/c mrkr_abbrev is longer
   genoBackground  varchar;
   zygFish	  zygocity.zyg_abbrev%TYPE;
   zygMom 	  zygocity.zyg_abbrev%TYPE;
   zygDad 	  zygocity.zyg_abbrev%TYPE;
   featDisplay     varchar;
   startHandle      genotype.geno_handle%TYPE;
   wildtype         genotype.geno_is_wildtype%TYPE;
  
begin
  select geno_handle, geno_is_wildtype 
  into startHandle, wildtype 
  from genotype where geno_zdb_id = genoZdbId;

  if ( wildtype != 't') then
  
     genoHandle := '';

    for featAbbrev, zygFish, zygMom, zygDad in
       select distinct feature_abbrev, z1.zyg_abbrev, z2.zyg_abbrev, z3.zyg_abbrev
         from feature, genotype_feature, zygocity z1, zygocity z2, zygocity z3
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = z1.zyg_zdb_id
          and genofeat_mom_zygocity = z2.zyg_zdb_id
          and genofeat_dad_zygocity = z3.zyg_zdb_id
     order by 1

     loop

     featDisplay := featAbbrev||'['||zygFish||','||zygMom||','||zygDad||']';
     
        if (genoHandle = '') then
          genoHandle := featDisplay ;
          
        else
          genoHandle := genoHandle ||' '|| featDisplay ;
        
        end if;
        
    end loop;

    select get_genotype_backgrounds(genoZdbId) 
    into genoBackground
    from single;
  
    if (genoBackground != '') then
     
      genoHandle := genoHandle || genoBackground ;
     
    end if;
  
  else
  
    genoHandle := startHandle;
  
  end if;

  return genoHandle;
end
$handle$ LANGUAGE plpgsql;
