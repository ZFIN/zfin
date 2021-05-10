create or replace function p_check_EFG_WT_expression (vXpatGenoxZdbId text,
   				       vXpatGeneZdbId text)
returns void as $$
declare vGenoIsWildtype genotype.geno_is_wildtype%TYPE;
 vGeneIsEFG boolean ;

begin 
if (vXpatGeneZdbId like 'ZDB-EFG%')
then
	vGenoIsWildtype = (Select geno_is_wildtype 
	    		      	      from genotype, fish_experiment, fish
				      where vXpatGenoxZdbId = genox_zdb_id
				      and genox_fish_zdb_id = fish_zdb_id
				      and fish_genotype_zdb_id = geno_zdb_id);
	if (vGenoIsWildtype = 't')
	then 
	    raise exception 'FAIL!!: EFGs can not be used in WT expression' ;
  	end if ;

end if ;

end
$$ LANGUAGE plpgsql
