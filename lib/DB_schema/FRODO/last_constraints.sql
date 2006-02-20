begin work ;

alter table expression_experiment add constraint (foreign 
    key (xpatex_genox_zdb_id) references genotype_experiment 
     on delete cascade constraint xpatex_genox_foreign_key);

alter table primer_set add constraint (foreign key 
    (strain_id) references genotype constraint 
    primerset_strain_id_foreign_key);

alter table probe_library 
  add constraint (foreign key (probelib_strain_zdb_id)  
  references genotype on delete cascade constraint
  probelib_strain_foreign_key_odc);

commit work ;

--rollback work ;
