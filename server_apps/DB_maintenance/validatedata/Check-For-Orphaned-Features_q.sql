unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select feature_zdb_id from feature
where not exists (
 select 'x' from record_attribution where
            recattrib_data_zdb_id = feature_zdb_id)
AND
not exists (
select 'x'
       from record_attribution ra ,  genotype_feature gf
       where  gf.genofeat_geno_zdb_id  = ra.recattrib_data_zdb_id
       and  feature_zdb_id = gf.genofeat_feature_zdb_id)
AND
not exists (
select 'x'
        from record_attribution ra , data_alias da
        where da.dalias_zdb_id = ra.recattrib_data_zdb_id
        and feature_zdb_id = da.dalias_data_zdb_id);
