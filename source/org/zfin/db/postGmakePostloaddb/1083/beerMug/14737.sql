--liquibase formatted sql
--changeset sierra:14737

update expression_experiment2
 set xpatex_gene_zdb_id = null
 where xpatex_zdb_id in ('ZDB-XPAT-060710-148',
'ZDB-XPAT-060810-204',
'ZDB-XPAT-030521-35',
'ZDB-XPAT-060810-395',
'ZDB-XPAT-070503-338',
'ZDB-XPAT-050107-212',
'ZDB-XPAT-050208-350',
'ZDB-XPAT-050208-473',
'ZDB-XPAT-050809-180')
 and exists (select 'x' from marker
     	    	    where mrkr_zdb_id = xpatex_gene_zdb_id
		    and mrkr_name like 'WITHDRAWN%');


update expression_experiment2
 set xpatex_gene_zdb_id = (select distinct mrel_mrkr_1_zdb_id from
     			  	  marker_relationship
				  where mrel_mrkr_2_zdb_id = xpatex_probe_feature_zdb_id)
 where not exists (Select 'x' from marker_relationship
     	 		where mrel_mrkr_1_zdb_id = xpatex_gene_zdb_id
			and mrel_mrkr_2_zdb_id = xpatex_probe_feature_zdb_id)
and xpatex_probe_feature_zdb_id is not null
 and xpatex_gene_zdb_id is not null
and exists (Select 'x' from marker_relationship  
                where mrel_mrkr_2_zdb_id = xpatex_probe_feature_zdb_id
                        and mrel_mrkr_1_zdb_id != xpatex_gene_zdb_id
and mrel_type = 'gene encodes small segment');

select xpatex_gene_zdb_id, a.mrkr_name, xpatex_probe_feature_zdb_id, b.mrkr_name
 from expression_Experiment2, marker a, marker b
 where xpatex_gene_Zdb_id = a.mrkr_zdb_id
 and xpatex_probe_feature_zdb_id = b.mrkr_Zdb_id
 and not exists (Select 'x' from marker_relationship
     	 		where mrel_mrkr_1_zdb_id = xpatex_gene_zdb_id
			and mrel_mrkr_2_zdb_id = xpatex_probe_feature_zdb_id);

