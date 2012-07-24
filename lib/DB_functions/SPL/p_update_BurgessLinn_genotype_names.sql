create procedure p_update_BurgessLinn_genotype_names ()
	
	define vFtrZDB varchar(50);

		foreach
			select tmp_feat_id
			  into vFtrZDB
			  from tmp_feature
			 
			execute procedure p_update_related_genotype_names(vFtrZDB) ;
					 
		end foreach
		   

end procedure ;