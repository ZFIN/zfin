create procedure check_xpat_null_valid (vXpatex_gene_zdb_id varchar(50), vXpatex_probe_feature_zdb_id varchar(50), vXpatex_atb_zdb_id varchar(50))

define vMrkrNameWithdrawn boolean;

if (vXpatex_gene_zdb_id is null and vXpatex_atb_zdb_id is null)
   then 
   	if not exists (select mrkr_name from marker where mrkr_zdb_id = vXpatex_gene_zdb_id and mrkr_name like 'WITHDRAWN%')
	then
		 raise exception -746,0,"FAIL!: gene or atb must not be empty, or probe must be withdrawn.";
	
	end if;

end if;

end procedure;
