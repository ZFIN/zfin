alter table figure_term_Fish_search_temp
  add constraint (Foreign key (ftfs_genox_Zdb_id)
  references genotype_experiment on delete cascade constraint ftfst_genox_foreign_key
   );

alter table figure_term_Fish_search_temp
  add constraint (Foreign key (ftfs_fig_Zdb_id) 
  references figure on delete cascade constraint ftfst_fig_foreign_key
   );
