begin work ;

update figure
  set fig_comments = replace(fig_comments, 'FISH','GENO')
  where fig_Comments like 'ZDB-FISH-%' ;

insert into apato_figure (apatofig_apato_zdb_id,
				apatofig_fig_zdb_id)
  select distinct apato_zdb_id, fig_zdb_id
    from figure, atomic_phenotype, genotype_Experiment
    where fig_comments = genox_geno_zdb_id
    and genox_zdb_id = apato_genox_zdb_id
    and genox_geno_zdb_id like 'ZDB-GENO-060608-%';

insert into apato_figure (
                                apatofig_apato_zdb_id,
                                apatofig_fig_zdb_id)
  select distinct apato_zdb_id, fig_zdb_id
    from figure, atomic_phenotype, genotype_Experiment
    where fig_comments = genox_geno_zdb_id
    and genox_zdb_id = apato_genox_zdb_id
    and genox_geno_zdb_id like 'ZDB-GENO-061101-%' ;

--rollback work ;

commit work ;
