--liquibase formatted sql
--changeset sierra:ZFIN-6329.sql


update genotype
 set geno_display_name = get_genotype_display(geno_zdb_id) 
where geno_zdb_id in (select geno_zdb_id
from feature_marker_relationship, marker, genotype_feature, genotype 
where fmrel_type = 'is allele of'
and exists(select 1 from zdb_replaced_data
where zrepld_new_zdb_id = fmrel_mrkr_zdb_id)
and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
and fmrel_mrkr_zdb_id = mrkr_zdb_id
and genofeat_geno_zdb_id = geno_zdb_id
and geno_display_name not like '%' || (select mrkr_abbrev from marker where mrkr_zdb_id = fmrel_mrkr_zdb_id) || '%'
order by geno_display_name);
