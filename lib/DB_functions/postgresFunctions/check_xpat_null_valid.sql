create or replace function check_xpat_null_valid (vXpatex_gene_zdb_id text, vXpatex_probe_feature_zdb_id text, vXpatex_atb_zdb_id text)

returns void as $$
declare vMrkrNameWithdrawn boolean;
begin
if (vXpatex_gene_zdb_id is null and vXpatex_atb_zdb_id is null)
   then 
   	if not exists (select mrkr_name from marker where mrkr_zdb_id = vXpatex_gene_zdb_id and mrkr_name like 'WITHDRAWN%')
	then
		 raise exception 'FAIL!: gene or atb must not be empty, or probe must be withdrawn.';
	
	end if;

end if;
end
$$ LANGUAGE plpgsql
