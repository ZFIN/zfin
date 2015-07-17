create procedure regen_genofig_finish(vUpdate boolean,phenoxId int8)
  
if (vUpdate != 't') then

    insert into genotype_figure_fast_search_new(gffs_geno_zdb_id,
						gffs_fig_zdb_id,
						gffs_morph_zdb_id,
						gffs_phenox_pk_id,
						gffs_fish_zdb_id,
						gffs_phenos_id,
						gffs_genox_Zdb_id)
      select distinct c.gffs_geno_zdb_id,
				c.gffs_fig_zdb_id,
				c.gffs_morph_zdb_id,
				c.gffs_phenox_pk_id,
				c.gffs_fish_zdb_id,
				c.gffs_phenos_id,
				c.gffs_genox_Zdb_id 
        from genotype_Figure_fast_Search c;

 

    delete from genotype_figure_fast_search_new
      where exists (Select 'x' from regen_genofig_input_zdb_id_temp
   	 		where gffs_phenox_pk_id = rgfg_id);

 
    insert into genotype_figure_fast_search_new
      (gffs_geno_zdb_id,
	gffs_fig_zdb_id,
	gffs_morph_zdb_id,
	gffs_phenox_pk_id,
	gffs_fish_zdb_id,
	gffs_phenos_id,
	gffs_genox_Zdb_id)
    select distinct rgf_geno_zdb_id,
    	   rgf_fig_zdb_id,
	   rgf_morph_zdb_id,
	   rgf_phenox_pk_id,
	   rgf_fish_zdb_id,
	   rgf_phenos_id,
	   rgf_genox_zdb_id
      from regen_genofig_temp;

    --let errorHint = "genotype_figure_fast_search_new create PK index";
    create unique index genotype_figure_fast_search_primary_key_index_transient
      on genotype_figure_fast_search_new (gffs_serial_id)
      fillfactor 100
      in idxdbs1;

    --let errorHint = "genotype_figure_fast_search_new create geno index";
    create index genotype_figure_fast_search_geno_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_geno_zdb_id)
      fillfactor 100
      in idxdbs3;

   --let errorHint = "genotype_figure_fast_search_new create fish index";
    create index genotype_figure_fast_search_fish_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_fish_zdb_id)
      fillfactor 100
      in idxdbs2;

    --let errorHint = "genotype_figure_fast_search_new create fig index";
    create index genotype_figure_fast_search_fig_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_fig_zdb_id)
      fillfactor 100
      in idxdbs1;

   -- let errorHint = "genotype_figure_fast_search_new create morph index";
    create index genotype_figure_fast_search_morph_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_morph_zdb_id)
      fillfactor 100
      in idxdbs3;

  
    create index genotype_figure_fast_search_phenox_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_phenox_pk_id)
      fillfactor 100
      in idxdbs3;
        
    update statistics high for table genotype_figure_fast_search_new;

        -- Make changes public for genotype_figure_fast_search_new
      --let errorHint = "drop genotype_figure_fast_search table ";
      drop table genotype_figure_fast_search;

      --let errorHint = "rename table gffs";
      rename table genotype_figure_fast_search_new to genotype_figure_fast_search;
      
      --let errorHint = "rename gffs indexes";
      rename index genotype_figure_fast_search_primary_key_index_transient
        to genotype_figure_fast_search_primary_key_index;
      rename index genotype_figure_fast_search_geno_foreign_key_index_transient
        to genotype_figure_fast_search_geno_zdb_id_foreign_key_index;
      rename index genotype_figure_fast_search_fig_foreign_key_index_transient 
        to genotype_figure_fast_search_fig_zdb_id_foreign_key_index;
      rename index genotype_figure_fast_search_morph_foreign_key_index_transient 
        to genotype_figure_fast_search_morph_zdb_id_foreign_key_index;
     rename index genotype_figure_fast_search_fish_foreign_key_index_transient 
        to genotype_figure_fast_search_fish_zdb_id_foreign_key_index;

      --let errorHint = "genotype_figure_fast_search add PK";
      alter table genotype_figure_fast_search add constraint primary key 
      (gffs_serial_id) constraint gffs_primary_key ;

      --let errorHint = "genotype_figure_fast_search add foreign key to reference genotype";
      alter table genotype_figure_fast_search add constraint (foreign key (gffs_geno_zdb_id) references genotype on 
      delete cascade constraint gffs_geno_zdb_id_foreign_key);
 
     --let errorHint = "genotype_figure_fast_search add foreign key to reference fish";
      alter table genotype_figure_fast_search add constraint (foreign key (gffs_fish_zdb_id) references fish on 
      delete cascade constraint gffs_fish_zdb_id_foreign_key);
    
      --let errorHint = "genotype_figure_fast_search add foreign key to reference figure";
      alter table genotype_figure_fast_search add constraint (foreign key (gffs_fig_zdb_id) references figure on 
      delete cascade constraint gffs_fig_zdb_id_foreign_key);
    
      --let errorHint = "genotype_figure_fast_search add foreign key to reference marker";
      alter table genotype_figure_fast_search add constraint (foreign key (gffs_morph_zdb_id) references marker on 
      delete cascade constraint gffs_morph_zdb_id_foreign_key);

     --let errorHint = "genotype_figure_fast_search add foreign key to reference phenotype_experiment";
      alter table genotype_figure_fast_search add constraint (foreign key (gffs_phenox_pk_id) references phenotype_Experiment on 
      delete cascade constraint gffs_phenox_pk_id_foreign_key);

else 

delete from genotype_figure_fast_search
 where gffs_phenox_pk_id = phenoxId;
 
    insert into genotype_figure_fast_search
      (gffs_geno_zdb_id,
	gffs_fig_zdb_id,
	gffs_morph_zdb_id,
	gffs_phenox_pk_id,
	gffs_fish_zdb_id,
	gffs_phenos_id,
	gffs_genox_Zdb_id)
    select distinct rgf_geno_zdb_id,
    	   rgf_fig_zdb_id,
	   rgf_morph_zdb_id,
	   rgf_phenox_pk_id,
	   rgf_fish_zdb_id,
	   rgf_phenos_id,
	   rgf_genox_zdb_id
      from regen_genofig_temp;

end if;

      grant select on genotype_figure_fast_search to "public";

end procedure;