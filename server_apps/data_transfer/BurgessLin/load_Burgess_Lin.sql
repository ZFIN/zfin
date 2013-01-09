begin work;

create temp table pre_burgess_lin1(
	pre_bl1_la_acc varchar(10) ,
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

create temp table burgess_lin1(
	bl1_la_acc varchar(10),
	bl1_vec_acc varchar(10),    -- all are JN244738
	bl1_gss_acc varchar(10),    -- suppose to be unique
	bl1_gene_sym varchar(40),   -- 2/3 to 3/4 are blank
	bl1_intron varchar(20),
	bl1_gene_zdbid varchar(50),  -- all blank
	bl1_chr   int,
	bl1_start int,
	bl1_end   int
) with no log
;

create temp table pre_burgess_lin2(
	pre_bl2_la_acc varchar(10), -- can not be unique
	pre_bl2_plate  varchar(20),
	pre_bl2_parent varchar(10)
) with no log;


create temp table burgess_lin2(
	bl2_la_acc varchar(10), -- can not be unique
	bl2_plate  varchar(20),
	bl2_parent varchar(10)
)with no log
;

create unique index burgess_lin1_bl1_gss_acc_idx
 on burgess_lin1(bl1_la_acc) in idxdbs2
;


! echo "read la_gb_chr_loc.tab into table burgess_lin1"

unload to 'notinZFIN.unl'
select * from pre_burgess_lin1
where trim(pre_bl1_la_acc) not in (select trim(dalias_alias) from data_alias where dalias_data_zdb_id like 'ZDB-ALT%' and dalias_alias like 'la%');

load from notinZFIN.unl insert into burgess_lin1;



!echo 'delete features that are alreday in ZFIN from input data set'
delete from burgess_lin1
where trim(bl1_la_acc)  in (select trim(feature_abbrev) from feature);

! echo "read la_fish_parent.tab into table burgess_lin2"
load from 'la_fish_parent.tab' delimiter '	' insert into pre_burgess_lin2;

!echo 'check to see if plates have been duplicated'

unload to 'plate_exists_in_zfin' select pre_bl2_plate from pre_burgess_lin2, bl_plate_tracking where blpt_plate=pre_bl2_plate;


unload to 'platenotinZFIN.unl'
select * from pre_burgess_lin2
where trim(pre_bl2_la_acc) not in (select trim(dalias_alias) from data_alias where dalias_data_zdb_id like 'ZDB-ALT%' and dalias_alias like 'la%');

load from platenotinZFIN.unl insert into burgess_lin2;

delete from burgess_lin2
where trim(bl2_la_acc)  in (select trim(feature_abbrev) from feature);

unload to 'blplates.unl' select distinct bl2_plate, '1' from burgess_lin2;

insert into bl_plate_tracking (blpt_plate, blpt_load_number) select distinct bl2_plate, '1' from burgess_lin2;


update statistics high for table burgess_lin1;
update statistics high for table burgess_lin2;
-----------------------------------------------
--- Brock's code
-----------------------------------------------
create temp table tmp_mrkr
(
   tmp_mrkr_zdb_id   varchar(50),
   tmp_mrkr_abbrev   varchar(50),
   tmp_mrkr_name     varchar(100)
)
with no log;



create temp table tmp_linked_recid
(
   tmp_link_id   varchar(50),
   tmp_acc       varchar(50),
   tmp_fdbcont   varchar(50)
)
with no log;

create temp table tmp_feature
(
   tmp_feat_id    varchar(50),
   tmp_name       varchar(50),
   tmp_abbrev     varchar(50),
   tmp_type       varchar(50),
   tmp_fmrkr_name varchar(255)
)
with no log;

create temp table tmp_dblink
(
   tmp_dbl_id    varchar(50),
   tmp_acc       varchar(50),
   tmp_fdbcont   varchar(50),
   tmp_dbl_feat  varchar(50)
)
with no log;

create temp table tmp_dalias
(
   tmp_da_id     varchar(50),
   tmp_da_f_id   varchar(50),
   tmp_da_alias  varchar(50)
)
with no log;

select distinct bl2_plate as d_plate, 0 as count
from burgess_lin2
into temp distinct_plate;

select bl2_plate as pmla_plate, bl2_la_acc as pmla_acc
from burgess_lin2
where not exists (
  select *
  from burgess_lin1
  where bl1_la_acc = bl2_la_acc
)
into temp plate_missing_la_acc;

--get the list of plate/alleles that are not in the consensus file
unload to 'plate_missing_la_acc.unl' select * from plate_missing_la_acc;


delete from burgess_lin1
where bl1_la_acc not in (select bl2_la_acc from burgess_lin2);

delete from burgess_lin2
where bl2_la_acc not in (select bl1_la_acc from burgess_lin1);


--get the list of plates that do not have a consensus allele
{
select d_plate from distinct_plate
where not exists (select * from burgess_lin2 where bl2_plate = d_plate)
order by 1;
}

update burgess_lin1
set bl1_gene_zdbid = (select mrkr_zdb_id from marker where mrkr_abbrev =  bl1_gene_sym)
where bl1_gene_sym is not null
  and exists (select * from marker where mrkr_abbrev = bl1_gene_sym)
;


select feature_zdb_id as bl_feat_id
from feature
where feature_lab_prefix_id = 85
  and feature_zdb_id[9,14] in ("120130","120806")
into temp bl_feature;

!echo 'add new attribution to bl features from prev loads'
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)select bl_feat_id, 'ZDB-PUB-121121-1' from bl_feature;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id,recattrib_source_type) select bl_feat_id, 'ZDB-PUB-121121-1' , 'feature type' from bl_feature;


insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id) select dalias_zdb_id, 'ZDB-PUB-121121-1' from data_alias, bl_feature where dalias_data_zdb_id=bl_feat_id;



insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id) select dblink_zdb_id , 'ZDB-PUB-121121-1' from db_link, bl_feature where dblink_linked_recid=bl_feat_id;

insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id) select fmrel_zdb_id , 'ZDB-PUB-121121-1' from feature_marker_relationship, bl_feature where fmrel_ftr_zdb_id=bl_feat_id;

--FEATURE


insert into tmp_feature (tmp_name, tmp_abbrev, tmp_type)
select "Tg(nLacz-GTvirus)" || "la" || bl1_la_acc[3,8] , "la"||bl1_la_acc[3,8] , "TRANSGENIC_INSERTION"
from burgess_lin1
where bl1_gene_zdbid is null;


insert into tmp_feature (tmp_name, tmp_abbrev, tmp_type)
select "la"||bl1_la_acc[3,8]||"Tg", "la"||bl1_la_acc[3,8]||"Tg", "TRANSGENIC_INSERTION"
from burgess_lin1
where bl1_gene_zdbid is not null;


!echo 'insert new features'
update tmp_feature set tmp_feat_id = get_id("ALT");

insert into zdb_active_data(zactvd_zdb_id) select tmp_feat_id from tmp_feature;

unload to 'newfeatures.unl' select tmp_name, tmp_abbrev from tmp_feature;
unload to 'newftrcount.unl' select count(*) from tmp_feature;
 
insert into feature (feature_zdb_id, feature_abbrev, feature_name, feature_type, feature_lab_prefix_id, feature_line_number, feature_known_insertion_site)
select tmp_feat_id, tmp_abbrev, tmp_name, tmp_type, 85, tmp_abbrev[3,8], 't'
from tmp_feature;


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_feat_id, "ZDB-PUB-121121-1"
from tmp_feature;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_feat_id, "ZDB-PUB-070726-29"
from tmp_feature;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select tmp_feat_id, "ZDB-PUB-121121-1", "feature type"
from tmp_feature;

insert into feature_assay (featassay_feature_zdb_id, featassay_mutagen, featassay_mutagee)
select tmp_feat_id, "DNA", "embryos"
from tmp_feature;

!echo DATA_ALIAS

insert into tmp_dalias (tmp_da_f_id, tmp_da_alias)
select tmp_feat_id, tmp_abbrev
from tmp_feature
where tmp_name not like "%Tg";

insert into tmp_dalias (tmp_da_f_id, tmp_da_alias)
select tmp_feat_id, tmp_name[1,8]
from tmp_feature
where tmp_name like "%Tg";

update tmp_dalias
set tmp_da_id = get_id("DALIAS");

insert into zdb_active_data(zactvd_zdb_id)
select tmp_da_id from tmp_dalias;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
select tmp_da_id, tmp_da_f_id, tmp_da_alias, '1'
from tmp_dalias;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_da_id, "ZDB-PUB-121121-1"
from tmp_dalias;






!echo FEATURE_MARKER_RELATIONSHIP

create temp table tmp_fmrel
(
   tmp_fmrel_id       varchar(50),
   tmp_fmrel_mrkr_id  varchar(50),
   tmp_fmrel_feat_id  varchar(50),
   tmp_fmrel_type     varchar(50)
)
with no log;

insert into tmp_fmrel (tmp_fmrel_mrkr_id, tmp_fmrel_feat_id, tmp_fmrel_type)
select "ZDB-TGCONSTRCT-070117-175", tmp_feat_id, "contains innocuous sequence feature"
from tmp_feature, burgess_lin1
where tmp_abbrev[3,8] = bl1_la_acc[3,8];

insert into tmp_fmrel (tmp_fmrel_mrkr_id, tmp_fmrel_feat_id, tmp_fmrel_type)
select bl1_gene_zdbid, tmp_feat_id , "is allele of"
from tmp_feature, burgess_lin1
where bl1_la_acc[3,8] = tmp_name[3,8]
  and bl1_gene_zdbid is not null;

unload to 'genenotinzfin.unl' select tmp_fmrel_mrkr_id from tmp_fmrel where tmp_fmrel_mrkr_id not in (select mrkr_zdb_id from marker); 

update tmp_fmrel set tmp_fmrel_id = get_id("FMREL");

insert into zdb_active_data (zactvd_zdb_id) select tmp_fmrel_id from tmp_fmrel;

insert into feature_marker_relationship
(
  fmrel_zdb_id, fmrel_mrkr_zdb_id, fmrel_ftr_zdb_id, fmrel_type
)
select tmp_fmrel_id, tmp_fmrel_mrkr_id, tmp_fmrel_feat_id, tmp_fmrel_type
from tmp_fmrel;


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select distinct bl1_gene_zdbid, "ZDB-PUB-121121-1"
from burgess_lin1
where bl1_gene_zdbid is not null
  and not exists (
      select *
      from record_attribution
      where recattrib_source_zdb_id = "ZDB-PUB-121121-1"
      and recattrib_data_zdb_id = bl1_gene_zdbid)
;



insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
values ("ZDB-TGCONSTRCT-070117-175", "ZDB-PUB-121121-1");

!echo DB_LINK

insert into tmp_dblink (tmp_dbl_id, tmp_dbl_feat, tmp_acc, tmp_fdbcont)
select get_id('DBLINK'), tmp_feat_id, bl1_gss_acc, 'ZDB-FDBCONT-040412-36'
from tmp_feature, burgess_lin1
where bl1_la_acc = tmp_abbrev;

insert into tmp_dblink (tmp_dbl_id, tmp_dbl_feat, tmp_acc, tmp_fdbcont)
select get_id('DBLINK'), tmp_feat_id, bl1_gss_acc, 'ZDB-FDBCONT-040412-36'
from tmp_feature, burgess_lin1
where bl1_la_acc||"Tg" = tmp_abbrev ;

unload to 'weirdaccession1.unl' select tmp_dbl_feat, tmp_acc from tmp_dblink where tmp_acc in (select dblink_acc_num from db_link) ;


--insert into tmp_dblink (tmp_dbl_id, tmp_dbl_feat, tmp_acc, tmp_fdbcont)
--values (get_id('DBLINK'), "ZDB-TGCONSTRCT-070117-175", "JN244738", 'ZDB-FDBCONT-040412-36')
--;

insert into zdb_active_data (zactvd_zdb_id) select tmp_dbl_id from tmp_dblink;

insert into db_link
(
  dblink_zdb_id, dblink_acc_num, dblink_linked_recid, dblink_fdbcont_zdb_id
)
select tmp_dbl_id, tmp_acc, tmp_dbl_feat, tmp_fdbcont
from tmp_dblink;

unload to "newdblinks.unl" select tmp_acc, tmp_dbl_feat from tmp_dblink;

update db_link set dblink_acc_num='JM426446' where dblink_linked_recid='ZDB-ALT-120130-601';
update db_link set dblink_acc_num_display='JM426446' where dblink_linked_recid='ZDB-ALT-120130-601';
update one_to_one_accession set ooa_dblink_acc_num='JM426446' where ooa_feature_zdb_id='ZDB-ALT-120130-601';

insert into one_to_one_accession (ooa_feature_zdb_id, ooa_dblink_zdb_id, ooa_dblink_acc_num)
select tmp_dbl_feat, tmp_dbl_id, tmp_acc
from tmp_dblink
where tmp_dbl_feat like "ZDB-ALT%";

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_dbl_id, "ZDB-PUB-121121-1"
from tmp_dblink;




!echo LAB_ORIGIN

insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id)
select tmp_feat_id, 'ZDB-LAB-111117-3'
from tmp_feature;

create index ftr_id_index on tmp_feature (tmp_feat_id)
using btree in idxdbs3;




update feature set feature_name = feature_name where exists (Select 'x' from tmp_feature where tmp_feat_id = feature_zdb_id);


--rollback work;
commit work;
-- commit/rollback handled externaly

