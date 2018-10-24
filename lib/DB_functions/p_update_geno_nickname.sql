create or replace function p_update_geno_nickname (vZDBid text, 
						handle varchar(255))

returns void as $$
begin
	update genotype
         set geno_nickname = geno_handle
	 where geno_Zdb_id = vZDBid
	 and geno_nickname != geno_handle 
         and geno_handle = handle ;
end
$$ LANGUAGE plpgsql
