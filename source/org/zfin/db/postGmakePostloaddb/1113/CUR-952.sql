
--liquibase formatted sql
--changeset cmpich:CUR-952


delete from phenotype_statement where exists (select * From phenotype_experiment where phenos_Phenox_pk_id = phenox_pk_id and exists (select * from fish_experiment where phenox_genox_zdb_id = genox_zdb_id and not exists (select * from experiment where exp_zdb_id = genox_exp_zdb_id)));
Delete from phenotype_experiment where exists (select * from fish_experiment where phenox_genox_zdb_id = genox_zdb_id and not exists (select * from experiment where exp_zdb_id = genox_exp_zdb_id));
Delete  from expression_experiment2 where exists (select * from fish_experiment where genox_zdb_id = xpatex_genox_zdb_id AND  not exists (select * from experiment where exp_zdb_id = genox_exp_zdb_id));
Delete from fish_experiment  where not exists (select * from experiment where exp_zdb_id = genox_exp_zdb_ID);
