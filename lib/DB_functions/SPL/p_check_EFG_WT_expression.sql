create procedure p_check_EFG_WT_expression (vXpatGenoxZdbId varchar(50),
   				       vXpatGeneZdbId varchar(50))

define vGenoIsWildtype like genotype.geno_is_wildtype;
define vGeneIsEFG boolean ;

if (vXpatGeneZdbId like 'ZDB-EFG%')
then
	let vGenoIsWildtype = (Select geno_is_wildtype 
	    		      	      from genotype, genotype_experiment
				      where vXpatGenoxZdbId = genox_zdb_id
				      and genox_geno_zdb_id = geno_zdb_id);
	if (vGenoIsWildtype = 't')
	then 
	    raise exception -746,0,'FAIL!!: EFGs can not be used in WT expression' ;
  	end if ;

end if ;

end procedure ;
