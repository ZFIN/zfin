begin work;

-- The purpose of this SQL script is to update the omimp_termxref_mapping table with updated OMIM and DO data.

-- temp table with updated data
create temp table updated_omimp_termxref_mapping 
  (
    u_otm_tx_id int8,
    u_otm_omimp_id int8
  );

create index u_omimp_termxref_mapping_omimp_id_index on 
   updated_omimp_termxref_mapping (u_otm_omimp_id);

create index u_omimp_termxref_mapping_tx_id_index on
   updated_omimp_termxref_mapping (u_otm_tx_id);

create unique index u_omimp_termxref_mapping_primary_key_index 
    on updated_omimp_termxref_mapping (u_otm_tx_id, u_otm_omimp_id);


insert into updated_omimp_termxref_mapping (u_otm_omimp_id, u_otm_tx_id)
select distinct omimp_pk_id, tx_pk_id
  from omim_phenotype, term_xref
 where tx_prefix = 'OMIM'
   and tx_fdb_db_id = 24
   and tx_accession = omimp_omim_id;

-- delete the records that are not in the temp table with updated data
delete from omimp_termxref_mapping
 where not exists(select 1 from updated_omimp_termxref_mapping 
                   where u_otm_omimp_id = otm_omimp_id
                     and u_otm_tx_id = otm_tx_id); 

-- add records that are in the temp table of updated data but which are not in our database
insert into omimp_termxref_mapping (otm_omimp_id, otm_tx_id)
select distinct omimp_pk_id, tx_pk_id
  from omim_phenotype, term_xref
 where tx_prefix = 'OMIM'
   and tx_fdb_db_id = 24
   and tx_accession = omimp_omim_id
   and not exists(select 1 from omimp_termxref_mapping
                   where otm_omimp_id = omimp_pk_id  
                     and otm_tx_id = tx_pk_id);


--rollback work;

commit work;
