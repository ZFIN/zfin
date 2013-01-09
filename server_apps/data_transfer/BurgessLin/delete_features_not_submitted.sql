begin work;

create temp table pre_burgess_lin1(
	pre_bl1_la_acc varchar(10) primary key,
	pre_bl1_vec_acc varchar(10),    -- all are JN244738
	pre_bl1_gss_acc varchar(10),    -- suppose to be unique
	pre_bl1_gene_sym varchar(40),   -- 2/3 to 3/4 are blank
	pre_bl1_intron varchar(20),
	pre_bl1_gene_zdbid varchar(50),  -- all blank
	pre_bl1_chr   int,
	pre_bl1_start int,
	pre_bl1_end   int
) with no log;
-- extents first  extents next

load from 'la_gb_chr_loc.tab' delimiter '	'  insert into pre_burgess_lin1;

!echo delete features and related tables for features that are not in submission but in ZFIN

create temp table blfeatures (laftrs varchar(50)) with no log;

create temp table zfinlaalias (laalias varchar(50), lazdbid varchar(50)) with no log;

insert into blfeatures select trim(pre_bl1_la_acc) from pre_burgess_lin1;

insert into zfinlaalias select dalias_alias, dalias_data_zdb_id from data_alias where dalias_data_zdb_id like 'ZDB-ALT%' and dalias_alias like 'la0%';

unload to 'featuresnotsubmitted.unl' select lazdbid, laalias from zfinlaalias where laalias not in (select laftrs from blfeatures);

create temp table featuresnotsubmitted (feat_zdb_id varchar(50), feat_abbrev varchar(50)) with no log;
load from  featuresnotsubmitted.unl insert into featuresnotsubmitted;

--insert into new table to load delete features
!echo 'insert deleted features to new withdrawn_data table'

insert into withdrawn_data(wd_old_zdb_id, wd_new_zdb_id, wd_display_note) select feat_zdb_id, 'ZDB-PUB-121121-1', "deleted b/c not submitted in 2012-10 B/L load" from featuresnotsubmitted;
 
insert into withdrawn_data(wd_old_zdb_id, wd_new_zdb_id, wd_display_note) select dblink_zdb_id, 'ZDB-PUB-121121-1', "deleted b/c not submitted in 2012-10 B/L load" from db_link, featuresnotsubmitted where dblink_linked_recid=feat_zdb_id;
 
delete from one_to_one_accession
where ooa_feature_zdb_id in (select feat_zdb_id from featuresnotsubmitted);

delete from db_link where dblink_linked_recid in (select feat_zdb_id from featuresnotsubmitted);

delete from zdb_active_data
where zactvd_zdb_id in (select feat_zdb_id from featuresnotsubmitted);

--GENOTYPE
!echo 'delete B/L genotypes'

select feature_zdb_id as bl_feat_id
from feature
where feature_lab_prefix_id = 85
  and feature_zdb_id[9,14] in ("120130","120806")
into temp bl_feature;

--genotypes

select distinct genofeat_geno_zdb_id as bl_geno_id
from genotype_feature, bl_feature
where genofeat_feature_zdb_id = bl_feat_id
into temp bl_genotype;

create index blgeno_geno_index
 on bl_genotype (bl_geno_id)
 using btree in idxdbs3;

unload to "bl_delete_genotype.unl"
select geno_zdb_id, geno_display_name
from genotype, bl_genotype
where geno_zdb_id = bl_geno_id
order by geno_display_name;


!echo 'load deleted genotypes into zdb_replaced_data'
insert into zdb_replaced_data (zrepld_old_zdb_id,zrepld_new_zdb_id) select bl_geno_id, 'ZDB-TGCONSTRCT-070117-175' from bl_genotype;

insert into withdrawn_data(wd_old_zdb_id, wd_new_zdb_id, wd_display_note) select bl_geno_id, 'ZDB-PUB-121121-1', "deleted  B/L genotype" from bl_genotype;

delete from zdb_active_data
where exists (select bl_geno_id from bl_genotype where zactvd_zdb_id=bl_geno_id);


--rollback work;
commit work;
-- commit/rollback handled externaly
