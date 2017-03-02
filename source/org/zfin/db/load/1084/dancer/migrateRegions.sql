--liquibase formatted sql
--changeset sierra:14959

begin work;
set constraints all deferred;

insert into zdb_object_type (zobjtype_name,
                    zobjtype_day,
        zobjtype_app_page,
        zobjtype_home_table,
        zobjtype_home_zdb_id_column,
        zobjtype_is_data,
        zobjtype_is_source)
 values ('EREGION',current,'marker','marker','mrkr_zdb_id','t','f');
create sequence eregion_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

update zdb_replaced_data
 set zrepld_old_zdb_id = replace(zrepld_old_Zdb_id, 'REGION','EREGION')
 where zrepld_old_zdb_id like 'ZDB-REGION%';

update zdb_replaced_data
 set zrepld_new_zdb_id = replace(zrepld_new_Zdb_id, 'REGION','EREGION')
 where zrepld_new_zdb_id like 'ZDB-REGION%';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
 select mrkr_Zdb_id, replace(mrkr_zdb_id, 'REGION','EREGION')
   from marker
 where mrkr_zdb_id like 'ZDB-REGION%';

update marker
 set mrkr_type = 'EREGION'
where mrkr_Type = 'REGION';

update marker_types
 set marker_type = 'EREGION'
 where marker_type = 'REGION';

update marker_types
 set mrkrtype_type_display = 'Engineered Region'
 where marker_type = 'EREGION';

update marker_type_group_member
  set mtgrpmem_mrkr_type = 'EREGION'
 where mtgrpmem_mrkr_type = 'REGION';


update marker
 set mrkr_zdb_id = replace(mrkr_Zdb_id, 'REGION','EREGION')
where mrkr_zdb_id like 'ZDB-REGION%';

update marker_relationship
 set mrel_mrkr_1_zdb_id = replace(mrel_mrkr_1_Zdb_id, 'REGION','EREGION')
where mrel_mrkr_1_zdb_id like 'ZDB-REGION%';

update marker_relationship
 set mrel_mrkr_2_zdb_id = replace(mrel_mrkr_2_Zdb_id, 'REGION','EREGION')
where mrel_mrkr_2_zdb_id like 'ZDB-REGION%';

update zdb_active_data
 set zactvd_zdb_id = replace(zactvd_zdb_id, 'REGION','EREGION')
 where zactvd_zdb_id like 'ZDB-REGION%';

update record_Attribution
 set recattrib_data_zdb_id = replace(recattrib_data_zdb_id, 'REGION','EREGION')
 where recattrib_data_zdb_id like 'ZDB-REGION%';

update updates
 set rec_id = replace(rec_id, 'REGION','EREGION')
 where rec_id like 'ZDB-REGION%';


update marker_history_audit
 set mha_mrkr_zdb_id = replace(mha_mrkr_zdb_id,'REGION','EREGION')
where mha_mrkr_zdb_id like 'ZDB-REGION%';

update data_alias
 set dalias_data_zdb_id = replace(dalias_data_zdb_id, 'REGION','EREGION')
 where dalias_data_zdb_id like 'ZDB-REGION%';

--!echo "data note";

update data_note
 set dnote_data_zdb_id = replace(dnote_data_zdb_id, 'REGION','EREGION')
 where dnote_data_zdb_id like 'ZDB-REGION%';

update construct_marker_relationship
  set conmrkrrel_mrkr_zdb_id = replace(conmrkrrel_mrkr_zdb_id, 'REGION','EREGION')
 where conmrkrrel_mrkr_zdb_id like 'ZDB-REGION%';

update construct_component
 set cc_component_zdb_id = replace(cc_component_zdb_id, 'REGION','EREGION')
 where cc_component_zdb_id like 'ZDB-REGION%';

update all_map_names
 set allmapnm_zdb_id = replace(allmapnm_zdb_id, 'REGION','EREGION')
 where allmapnm_zdb_id like 'ZDB-REGION%';

update construct_component_search
 set ccs_gene_zdb_id = replace(ccs_gene_zdb_id, 'REGION','EREGION')
 where ccs_gene_zdb_id like 'ZDB-REGION%';

update construct_component_search_backup
 set ccs_gene_zdb_id = replace(ccs_gene_zdb_id, 'REGION','EREGION')
 where ccs_gene_zdb_id like 'ZDB-REGION%';

update construct_component_search_temp
 set ccs_gene_zdb_id = replace(ccs_gene_zdb_id, 'REGION','EREGION')
 where ccs_gene_zdb_id like 'ZDB-REGION%';

update marker_history 
 set mhist_mrkr_zdb_id = replace(mhist_mrkr_zdb_id, 'REGION','EREGION')
 where mhist_mrkr_zdb_id like 'ZDB-REGION%';

update marker_type_group_member
  set mtgrpmem_mrkr_type_group = 'ENGINEERED_REGION'
 where mtgrpmem_mrkr_type_group = 'REGION';

update marker_relationship_type
 set mreltype_mrkr_type_group_1 = 'ENGINEERED_REGION'
 where mreltype_mrkr_type_group_1 = 'REGION';

update marker_relationship_type
 set mreltype_mrkr_type_group_2 = 'ENGINEERED_REGION'
 where mreltype_mrkr_type_group_2 = 'REGION';

delete from marker_Type_group
 where mtgrp_name = 'REGION';

update marker_relationship_type
 set mreltype_mrkr_type_group_1 = 'GENEDOM_EFG_EREGION_K'
 where mreltype_mrkr_type_group_1 = 'GENEDOM_EFG_REGION_K';

update marker_relationship_type
 set mreltype_mrkr_type_group_2 = 'GENEDOM_EFG_EREGION_K'
 where mreltype_mrkr_type_group_2 = 'GENEDOM_EFG_REGION_K';

update marker_type_group
 set mtgrp_name = 'GENEDOM_EFG_EREGION_K'
 where mtgrp_name = 'GENEDOM_EFG_REGION_K';

update marker_type_group_member
 set mtgrpmem_mrkr_type_group = 'GENEDOM_EFG_EREGION_K'
 where mtgrpmem_mrkr_type_group = 'GENEDOM_EFG_REGION_K';

update marker_relationship_type
 set mreltype_mrkr_type_group_1 = 'GENEDOM_EFG_EREGION'
 where mreltype_mrkr_type_group_1 = 'GENEDOM_EFG_REGION';

update marker_relationship_type
 set mreltype_mrkr_type_group_2 = 'GENEDOM_EFG_EREGION'
 where mreltype_mrkr_type_group_2 = 'GENEDOM_EFG_REGION';

update marker_type_group
 set mtgrp_name = 'GENEDOM_EFG_EREGION'
 where mtgrp_name = 'GENEDOM_EFG_REGION';

update marker_type_group_member
 set mtgrpmem_mrkr_type_group = 'GENEDOM_EFG_EREGION'
 where mtgrpmem_mrkr_type_group = 'GENEDOM_EFG_REGION';

set constraints all immediate;

--rollback work;
commit work;
