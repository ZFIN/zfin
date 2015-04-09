select  a.cc_construct_zdb_id, a.cc_component, mrkr_name, recattrib_source_zdb_id
from construct_component a, construct_component b, foreign_species, construct, record_Attribution
 where a.cc_construct_zdb_id = b.cc_construct_zdb_id
and exists (Select 'x' from marker where mrkr_type in ('GENE','REGION','EFG') and mrkr_zdb_id = a.cc_component_zdb_id)
and b.cc_component_zdb_id = cv_zdb_id
and b.cc_order + 2 = a.cc_order
and recattrib_datA_zdb_id = mrkr_zdb_id
and mrkr_zdb_id = a.cc_construct_zdb_id
union
select  a.cc_construct_zdb_id, a.cc_component, mrkr_name, recattrib_source_zdb_id
from construct_component a, construct_component b, foreign_species, construct, record_Attribution
 where a.cc_construct_zdb_id = b.cc_construct_zdb_id
and exists  (Select 'x' from marker where mrkr_type in ('GENE','REGION','EFG') and mrkr_Zdb_id = a.cc_component_zdb_id)
and recattrib_datA_zdb_id = mrkr_zdb_id
and mrkr_zdb_id = a.cc_construct_zdb_id
and b.cc_component_zdb_id = cv_zdb_id
and b.cc_order + 1 = a.cc_order;
