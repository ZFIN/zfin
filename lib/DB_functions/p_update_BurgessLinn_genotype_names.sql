create or replace function p_update_BurgessLinn_genotype_names ()
returns void as $$
	
	declare vFtrZDB text;
	begin
		for vFtrZDB in
			select tmp_feat_id
			  from tmp_feature
		loop
			select p_update_related_genotype_names(vFtrZDB) ;
					 
		end loop;
	end 
$$ LANGUAGE plpgsql
