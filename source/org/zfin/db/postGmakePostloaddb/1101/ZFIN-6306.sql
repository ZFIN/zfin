--liquibase formatted sql
--changeset pm:ZFIN-6306


create temp table tmp_geno (genoid text, genoname text);
insert into tmp_geno  select geno_zdb_id,geno_display_name from genotype, genotype_feature where genofeat_feature_zdb_id='ZDB-ALT-080528-2' and genofeat_geno_zdb_id=geno_zdb_id ;
select * from tmp_geno;
update tmp_geno set genoname=get_genotype_display(genoid);
update genotype set geno_display_name=(select genoname from tmp_geno where geno_zdb_id=genoid) where exists (select 'x' from tmp_geno where genoid=geno_zdb_id);
