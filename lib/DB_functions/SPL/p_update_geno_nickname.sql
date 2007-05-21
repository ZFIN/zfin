create procedure p_update_geno_nickname (vZDBid varchar(50))

	update genotype
         set geno_nickname = geno_handle
	 where geno_Zdb_id = vZDBid
	 and geno_nickname != geno_handle ;

end procedure ;