create or replace function p_update_BurgessLinn_genotype_names ()
returns void as $$
	
	declare vFtrZDB varchar(50);
	begin
		for vFtrZDB in
			select tmp_feat_id
			  from tmp_feature
		loop
			execute procedure p_update_related_genotype_names(vFtrZDB) ;
					 
		end loop;
	end 
$$ LANGUAGE plpgsql
