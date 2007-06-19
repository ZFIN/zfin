create procedure p_update_geno_nickname (vZDBid varchar(50), 
						handle varchar(255))

	update genotype
         set geno_nickname = geno_handle
	 where geno_Zdb_id = vZDBid
	 and geno_nickname != geno_handle 
         and geno_handle = handle ;

end procedure ;