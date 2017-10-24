--liquibase formatted sql
--changeset kevin:inf-3165.sql


insert into record_attribution (recattrib_data_zdb_id,
                                recattrib_source_zdb_id,
                                recattrib_source_type )
select distinct gf.genofeat_feature_zdb_id, geno_ra.recattrib_source_zdb_id, 'standard'
from genotype_feature gf
     join record_attribution geno_ra on geno_ra.recattrib_data_zdb_id = gf.genofeat_geno_zdb_id
where not exists
       (select 'x' from record_attribution feat_ra
        where feat_ra.recattrib_data_zdb_id = gf.genofeat_feature_zdb_id
        and feat_ra.recattrib_source_zdb_id = geno_ra.recattrib_source_zdb_id)
;

