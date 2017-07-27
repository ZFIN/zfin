create or replace function regen_genofig_finish(vUpdate boolean,pgId int8)
returns text as $regen_genofig_finish$

 begin 
   if (vUpdate != 't') then

    insert into genotype_figure_fast_search_new(gffs_geno_zdb_id,
						gffs_fig_zdb_id,
						gffs_morph_zdb_id,
						gffs_pg_id,
						gffs_fish_zdb_id,
						gffs_psg_id,
						gffs_genox_Zdb_id)
      select distinct c.gffs_geno_zdb_id,
				c.gffs_fig_zdb_id,
				c.gffs_morph_zdb_id,
				c.gffs_pg_id,
				c.gffs_fish_zdb_id,
				c.gffs_psg_id,
				c.gffs_genox_Zdb_id 
        from genotype_Figure_fast_Search c;

 

    delete from genotype_figure_fast_search_new
      where exists (Select 'x' from regen_genofig_input_zdb_id_temp
   	 		where gffs_pg_id = rgfg_id);

   delete from genotype_figure_fast_search_new
     where gffs_psg_id not in (select psg_id from phenotype_observation_generated);

    insert into genotype_figure_fast_search_new
      (gffs_geno_zdb_id,
	gffs_fig_zdb_id,
	gffs_morph_zdb_id,
	gffs_pg_id,
	gffs_fish_zdb_id,
	gffs_psg_id,
	gffs_genox_Zdb_id)
    select distinct rgf_geno_zdb_id,
    	   rgf_fig_zdb_id,
	   rgf_morph_zdb_id,
	   rgf_pg_id,
	   rgf_fish_zdb_id,
	   rgf_psg_id,
	   rgf_genox_zdb_id
      from regen_genofig_temp;

    --let errorHint = "genotype_figure_fast_search_new create geno index";
    create index genotype_figure_fast_search_geno_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_geno_zdb_id);

   --let errorHint = "genotype_figure_fast_search_new create fish index";
    create index genotype_figure_fast_search_fish_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_fish_zdb_id);

    --let errorHint = "genotype_figure_fast_search_new create fig index";
    create index genotype_figure_fast_search_fig_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_fig_zdb_id);

   -- let errorHint = "genotype_figure_fast_search_new create morph index";
    create index genotype_figure_fast_search_morph_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_morph_zdb_id);

  
    create index genotype_figure_fast_search_pg_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_pg_id);

    create index genotype_figure_fast_search_psg_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_psg_id);
        

        -- Make changes public for genotype_figure_fast_search_new
      --let errorHint = "drop genotype_figure_fast_search table ";
      
     drop table if exists genotype_figure_fast_search;

      --let errorHint = "rename table gffs";
     alter table  genotype_figure_fast_search_new rename to genotype_figure_fast_search;
      
      --let errorHint = "rename gffs indexes";
    
      alter index genotype_figure_fast_search_geno_foreign_key_index_transient
        rename to genotype_figure_fast_search_geno_zdb_id_foreign_key_index;
      alter index genotype_figure_fast_search_fig_foreign_key_index_transient 
        rename to genotype_figure_fast_search_fig_zdb_id_foreign_key_index;
      alter index genotype_figure_fast_search_morph_foreign_key_index_transient 
        rename to genotype_figure_fast_search_morph_zdb_id_foreign_key_index;
      alter index genotype_figure_fast_search_fish_foreign_key_index_transient 
        rename to genotype_figure_fast_search_fish_zdb_id_foreign_key_index;
      alter index genotype_figure_fast_search_pg_foreign_key_index_transient 
        rename to genotype_figure_fast_search_pg_id_foreign_key_index;

      alter index genotype_figure_fast_search_psg_foreign_key_index_transient 
        rename to genotype_figure_fast_search_psg_id_foreign_key_index;


      --let errorHint = "genotype_figure_fast_search add foreign key to reference genotype";
      alter table genotype_figure_fast_search add constraint gffs_geno_zdb_id_foreign_key  
      	    foreign key (gffs_geno_zdb_id) references genotype (geno_Zdb_id) on delete cascade;
 
     --let errorHint = "genotype_figure_fast_search add foreign key to reference fish";
      alter table genotype_figure_fast_search add constraint gffs_fish_zdb_id_foreign_key
      	    foreign key (gffs_fish_zdb_id) references fish (fish_zdb_id) on delete cascade;
    
      --let errorHint = "genotype_figure_fast_search add foreign key to reference figure";
      alter table genotype_figure_fast_search add constraint gffs_fig_zdb_id_foreign_key
      	    foreign key (gffs_fig_zdb_id) references figure (fig_zdb_id) on delete cascade ;
    
      --let errorHint = "genotype_figure_fast_search add foreign key to reference marker";
      alter table genotype_figure_fast_search add constraint gffs_morph_zdb_id_foreign_key
      	    foreign key (gffs_morph_zdb_id) references marker on delete cascade;


   else 
   	delete from genotype_figure_fast_search where gffs_pg_id = pgId;

        delete from genotype_figure_fast_search
 	       where gffs_psg_id not in (select psg_id from phenotype_observation_generated);
 
        insert into genotype_figure_fast_search
      	       (gffs_geno_zdb_id,
		gffs_fig_zdb_id,
		gffs_morph_zdb_id,
		gffs_pg_id,
		gffs_fish_zdb_id,
		gffs_psg_id,
		gffs_genox_Zdb_id)
           select distinct rgf_geno_zdb_id,
    	   	  rgf_fig_zdb_id,
	   	  rgf_morph_zdb_id,
	   	  rgf_pg_id,
	   	  rgf_fish_zdb_id,
	   	  rgf_psg_id,
	   	  rgf_genox_zdb_id
             from regen_genofig_temp
      	     where not exists (Select 'x' 
	     	       	      	      from genotype_figure_fast_Search
      	    	       	       	      where gffs_psg_id = rgf_psg_id);

  end if;

  delete from regen_genofig_temp;
  delete from regen_genofig_input_zdb_id_temp;

  return 'regen_genox() completed without error; success!';
  exception when raise_exception then
  	    return errorHint; 

end;

$regen_genofig_finish$ LANGUAGE plpgsql;
