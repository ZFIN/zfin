--liquibase formatted sql
--changeset sierra:restore-expression

insert into zdb_active_data (zactvd_zdb_id)
 select xpatex_zdb_id from tmp_xpatex
 where not exists (select 'x' from zdb_active_data where zactvd_zdb_id = xpatex_zdb_id);

update tmp_xpatex
 set xpatex_probe_feature_zdb_id = null
 where xpatex_probe_feature_zdb_id = '';

update tmp_xpatex
 set xpatex_dblink_zdb_id = null
 where xpatex_dblink_zdb_id = '';

update tmp_xpatex
 set xpatex_atb_zdb_id = null
 where xpatex_atb_zdb_id = '';

update tmp_xpatex
 set xpatex_gene_zdb_id = null
 where xpatex_gene_zdb_id = '';

update tmp_xpatex
 set xpatex_probe_feature_zdb_id = null
 where xpatex_probe_feature_zdb_id = '';

delete from tmp_xpatex
 where xpatex_zdb_id in (select xpatex_zdb_id from expression_experiment2);

delete from tmp_xpatex as txpatex
  where exists (select 'x' from expression_experiment2 as xpatex
                       where xpatex.xpatex_gene_zdb_id = txpatex.xpatex_gene_zdb_id
                       and xpatex.xpatex_assay_name = txpatex.xpatex_assay_name
                       and xpatex.xpatex_atb_zdb_id = txpatex.xpatex_atb_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id = txpatex.xpatex_probe_feature_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id is not null
                       and xpatex.xpatex_gene_zdb_id is not null
                       and xpatex.xpatex_atb_zdb_id is not null)
    and txpatex.xpatex_probe_feature_zdb_id is not null
                       and txpatex.xpatex_gene_zdb_id is not null
                       and txpatex.xpatex_atb_zdb_id is not null;

delete from tmp_xpatex as txpatex
  where exists (select 'x' from expression_experiment2 as xpatex
                       where xpatex.xpatex_gene_zdb_id = txpatex.xpatex_gene_zdb_id
                       and xpatex.xpatex_assay_name = txpatex.xpatex_assay_name
                       and xpatex.xpatex_atb_zdb_id = txpatex.xpatex_atb_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id = txpatex.xpatex_probe_feature_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id is null
                       and xpatex.xpatex_gene_zdb_id is not null
                       and xpatex.xpatex_atb_zdb_id is not null)
    and txpatex.xpatex_probe_feature_zdb_id is null
                       and txpatex.xpatex_gene_zdb_id is not null
                       and txpatex.xpatex_atb_zdb_id is not null;



delete from tmp_xpatex as txpatex
  where exists (select 'x' from expression_experiment2 as xpatex
                       where xpatex.xpatex_gene_zdb_id = txpatex.xpatex_gene_zdb_id
                       and xpatex.xpatex_assay_name = txpatex.xpatex_assay_name
                       and xpatex.xpatex_atb_zdb_id = txpatex.xpatex_atb_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id = txpatex.xpatex_probe_feature_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id is not null
                       and xpatex.xpatex_gene_zdb_id is null
                       and xpatex.xpatex_atb_zdb_id is not null)
    and txpatex.xpatex_probe_feature_zdb_id is not null
                       and txpatex.xpatex_gene_zdb_id is null
                       and txpatex.xpatex_atb_zdb_id is not null;

delete from tmp_xpatex as txpatex
  where exists (select 'x' from expression_experiment2 as xpatex
                       where xpatex.xpatex_gene_zdb_id = txpatex.xpatex_gene_zdb_id
                       and xpatex.xpatex_assay_name = txpatex.xpatex_assay_name
                       and xpatex.xpatex_atb_zdb_id = txpatex.xpatex_atb_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id = txpatex.xpatex_probe_feature_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id is not null
                       and xpatex.xpatex_gene_zdb_id is not null
                       and xpatex.xpatex_atb_zdb_id is  null)
    and txpatex.xpatex_probe_feature_zdb_id is not null
                       and txpatex.xpatex_gene_zdb_id is not null
                       and txpatex.xpatex_atb_zdb_id is null;


delete from tmp_xpatex as txpatex
  where exists (select 'x' from expression_experiment2 as xpatex
                       where xpatex.xpatex_gene_zdb_id = txpatex.xpatex_gene_zdb_id
                       and xpatex.xpatex_assay_name = txpatex.xpatex_assay_name
                       and xpatex.xpatex_atb_zdb_id = txpatex.xpatex_atb_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id = txpatex.xpatex_probe_feature_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id is null
                       and xpatex.xpatex_gene_zdb_id is not null
                       and xpatex.xpatex_atb_zdb_id is null)
    and txpatex.xpatex_probe_feature_zdb_id is null
                       and txpatex.xpatex_gene_zdb_id is not null
                       and txpatex.xpatex_atb_zdb_id is null;


delete from tmp_xpatex as txpatex
  where exists (select 'x' from expression_experiment2 as xpatex
                       where xpatex.xpatex_gene_zdb_id = txpatex.xpatex_gene_zdb_id
                       and xpatex.xpatex_assay_name = txpatex.xpatex_assay_name
                       and xpatex.xpatex_atb_zdb_id = txpatex.xpatex_atb_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id = txpatex.xpatex_probe_feature_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id is  null
                       and xpatex.xpatex_gene_zdb_id is  null
                       and xpatex.xpatex_atb_zdb_id is  not null)
    and txpatex.xpatex_probe_feature_zdb_id is null
                       and txpatex.xpatex_gene_zdb_id is null
                       and txpatex.xpatex_atb_zdb_id is not null;

delete from tmp_xpatex as txpatex
  where exists (select 'x' from expression_experiment2 as xpatex
                       where xpatex.xpatex_gene_zdb_id = txpatex.xpatex_gene_zdb_id
                       and xpatex.xpatex_assay_name = txpatex.xpatex_assay_name
                       and xpatex.xpatex_atb_zdb_id = txpatex.xpatex_atb_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id = txpatex.xpatex_probe_feature_zdb_id
                       and xpatex.xpatex_probe_feature_zdb_id is  null
                       and xpatex.xpatex_gene_zdb_id is not null
                       and xpatex.xpatex_atb_zdb_id is not null)
    and txpatex.xpatex_probe_feature_zdb_id is  null
                       and txpatex.xpatex_gene_zdb_id is not null
                       and txpatex.xpatex_atb_zdb_id is not null;


insert into expression_experiment2 (xpatex_zdb_id,
                                    xpatex_assay_name,
                                    xpatex_probe_feature_zdb_id,
                                    xpatex_gene_zdb_id,
                                    xpatex_direct_submission_date,
                                    xpatex_dblink_zdb_id,
                                    xpatex_genox_zdb_id,
                                    xpatex_atb_zdb_id,
                                    xpatex_source_zdb_id)
select xpatex_zdb_id,
                                    xpatex_assay_name,
                                    xpatex_probe_feature_zdb_id,
                                    xpatex_gene_zdb_id,
                                    xpatex_direct_submission_date,
                                    xpatex_dblink_zdb_id,
                                    xpatex_genox_zdb_id,
                                    xpatex_atb_zdb_id,
                                    xpatex_source_zdb_id
  from tmp_xpatex ;
