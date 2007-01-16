begin work ;

--create index xpatex_genox_foreign_key_index 
--  on expression_experiment (xpatex_genox_zdb_id)
--  using btree in idxdbs3 ;

--took out ODC from genotype to xpatex according to 6/7 frodo

alter table expression_experiment add constraint (foreign 
    key (xpatex_genox_zdb_id) references genotype_experiment 
     constraint xpatex_genox_foreign_key);

---create index primer_set_strain_id_foreign_key_index 
--  on primer_set (strain_id)
--  using btree in idxdbs3 ;

alter table primer_set add constraint (foreign key 
    (strain_id) references genotype constraint 
    primerset_strain_id_foreign_key);

--create index probe_library_strain_zdb_id_foreign_key_index 
--  on probe_library (probelib_strain_zdb_id)
--  using btree in idxdbs3 ;

alter table probe_library 
  add constraint (foreign key (probelib_strain_zdb_id)  
  references genotype on delete cascade constraint
  probelib_strain_foreign_key_odc);

commit work ;

--rollback work ;
