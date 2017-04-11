select  a.cc_construct_zdb_id, a.cc_component, mrkr_name, recattrib_source_zdb_id
from construct_component a, construct_component b, controlled_vocabulary, construct, record_Attribution, marker
 where a.cc_construct_zdb_id = b.cc_construct_zdb_id
and exists (Select 'x' from marker,marker_type_group_member where mrkr_type=mtgrpmem_mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM_EFG_EREGION_K' and mrkr_zdb_id = a.cc_component_zdb_id)
and b.cc_component_zdb_id = cv_zdb_id
and b.cc_order + 2 = a.cc_order
and recattrib_datA_zdb_id = mrkr_zdb_id
and mrkr_zdb_id = a.cc_construct_zdb_id
and cv_foreign_species is not null
union
select  a.cc_construct_zdb_id, a.cc_component, mrkr_name, recattrib_source_zdb_id
from construct_component a, construct_component b, controlled_vocabulary, construct, record_Attribution, marker
 where a.cc_construct_zdb_id = b.cc_construct_zdb_id
and exists  (Select 'x' from markermarker_type_group_member where mrkr_type=mtgrpmem_mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM_EFG_EREGION_K' and mrkr_Zdb_id = a.cc_component_zdb_id)
and recattrib_datA_zdb_id = mrkr_zdb_id
and mrkr_zdb_id = a.cc_construct_zdb_id
and b.cc_component_zdb_id = cv_zdb_id
and b.cc_order + 1 = a.cc_order
and cv_foreign_species is not null;
