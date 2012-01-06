

begin work;



create temp table tmp_mrkr
(
   tmp_mrkr_zdb_id   varchar(50),
   tmp_mrkr_abbrev   varchar(50),
   tmp_mrkr_name     varchar(100)
)
with no log;

create temp table tmp_geno
(
   tmp_geno_pk   serial8,
   tmp_geno_id   varchar(50),
   tmp_plate_ID  varchar(50),
   tmp_gname      varchar(255),
   tmp_handle    varchar(255)
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


{
delete from burgess_lin2 
where exists (
  select * 
  from plate_missing_la_acc
  where bl2_plate = pmla_plate
);
}
--unload to 'plate_missing_la_acc.unl' select * from plate_missing_la_acc;


update distinct_plate set count = (select count(*) from burgess_lin2 where d_plate = bl2_plate);

select count(*) from distinct_plate where count < 1;


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

--FEATURE
insert into tmp_feature (tmp_name, tmp_abbrev, tmp_type) 
select "Tg(nLacz-GTvirus)" || "la" || bl1_la_acc[3,8] , "la"||bl1_la_acc[3,8] , "TRANSGENIC_INSERTION" 
from burgess_lin1
where bl1_gene_zdbid is null;

--select tmp_name from tmp_feature;

insert into tmp_feature (tmp_name, tmp_abbrev, tmp_type) 
select "la"||bl1_la_acc[3,8]||"Tg", "la"||bl1_la_acc[3,8]||"Tg", "TRANSGENIC_INSERTION" 
from burgess_lin1
where bl1_gene_zdbid is not null;

update tmp_feature set tmp_feat_id = get_id("ALT");

insert into zdb_active_data(zactvd_zdb_id) select tmp_feat_id from tmp_feature;


insert into feature (feature_zdb_id, feature_abbrev, feature_name, feature_type, feature_lab_prefix_id, feature_line_number, feature_known_insertion_site)
select tmp_feat_id, tmp_abbrev, tmp_name, tmp_type, 85, tmp_abbrev[3,8], 't'
from tmp_feature;


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_feat_id, "ZDB-PUB-110915-1"
from tmp_feature;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_feat_id, "ZDB-PUB-070726-29"
from tmp_feature;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select tmp_feat_id, "ZDB-PUB-110915-1", "feature type"
from tmp_feature;

insert into feature_assay (featassay_feature_zdb_id, featassay_mutagen, featassay_mutagee)
select tmp_feat_id, "DNA", "embryos"
from tmp_feature;

!echo DATA_ALIAS

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
select tmp_da_id, "ZDB-PUB-110915-1"
from tmp_dalias;



!echo GENOTYPE

insert into tmp_geno (tmp_plate_id ) select distinct bl2_plate from burgess_lin2; 

update tmp_geno set tmp_geno_id = get_id("GENO");

insert into zdb_active_data (zactvd_zdb_id) select tmp_geno_id from tmp_geno;

insert into genotype (geno_zdb_id, geno_display_name, geno_handle) select tmp_geno_id, tmp_plate_id, tmp_plate_id from tmp_geno;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_geno_id, "ZDB-PUB-110915-1"
from tmp_geno;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_geno_id, "ZDB-PUB-070726-29"
from tmp_geno;


!echo GENOTYPE_BACKGROUND


insert into genotype_background (genoback_geno_zdb_id, genoback_background_zdb_id)
select tmp_geno_id , "ZDB-GENO-010924-10"
from tmp_geno;



!echo "new FEATURE RELATIONSHP TYPE"

{
fmreltype_name      is allele of
fmreltype_ftr_typ+  MUTANT
fmreltype_mrkr_ty+  GENEDOM
fmreltype_1_to_2_+  Is Allele Of
fmreltype_2_to_1_+  Has Allele
fmreltype_produce+  t


insert into feature_marker_relationship_type
(
   fmreltype_name, fmreltype_ftr_type_group, fmreltype_mrkr_type_group, 
   fmreltype_1_to_2_comments, fmreltype_2_to_1_comments, fmreltype_produces_affected_marker
)
values 
(
   "F1 insertion", "TG_INSERTION", "CONSTRUCT", "Is Mapped", "Mapped In", 'f'
);

}


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

update tmp_fmrel set tmp_fmrel_id = get_id("FMREL");

insert into zdb_active_data (zactvd_zdb_id) select tmp_fmrel_id from tmp_fmrel;

insert into feature_marker_relationship
(
  fmrel_zdb_id, fmrel_mrkr_zdb_id, fmrel_ftr_zdb_id, fmrel_type
)
select tmp_fmrel_id, tmp_fmrel_mrkr_id, tmp_fmrel_feat_id, tmp_fmrel_type
from tmp_fmrel;


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select distinct bl1_gene_zdbid, "ZDB-PUB-110915-1"
from burgess_lin1
where bl1_gene_zdbid is not null
  and not exists (
      select * 
      from record_attribution 
      where recattrib_source_zdb_id = "ZDB-PUB-110915-1"
      and recattrib_data_zdb_id = bl1_gene_zdbid)
;



insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
values ("ZDB-TGCONSTRCT-070117-175", "ZDB-PUB-070726-29");

!echo DB_LINK
{
insert into tmp_dblink (tmp_dbl_id, tmp_dbl_feat, tmp_acc, tmp_fdbcont)
select get_id('DBLINK'), tmp_feat_id, bl1_vec_acc, 'ZDB-FDBCONT-040412-36'
from tmp_feature, burgess_lin1
where bl1_la_acc = tmp_abbrev;
}
insert into tmp_dblink (tmp_dbl_id, tmp_dbl_feat, tmp_acc, tmp_fdbcont)
select get_id('DBLINK'), tmp_feat_id, bl1_gss_acc, 'ZDB-FDBCONT-040412-36'
from tmp_feature, burgess_lin1
where bl1_la_acc = tmp_abbrev;

insert into tmp_dblink (tmp_dbl_id, tmp_dbl_feat, tmp_acc, tmp_fdbcont)
select get_id('DBLINK'), tmp_feat_id, bl1_gss_acc, 'ZDB-FDBCONT-040412-36'
from tmp_feature, burgess_lin1
where bl1_la_acc||"Tg" = tmp_abbrev ;

insert into tmp_dblink (tmp_dbl_id, tmp_dbl_feat, tmp_acc, tmp_fdbcont)
values (get_id('DBLINK'), "ZDB-TGCONSTRCT-070117-175", "JN244738", 'ZDB-FDBCONT-040412-36')
;

insert into zdb_active_data (zactvd_zdb_id) select tmp_dbl_id from tmp_dblink;

insert into db_link
(
  dblink_zdb_id, dblink_acc_num, dblink_linked_recid, dblink_fdbcont_zdb_id
)
select tmp_dbl_id, tmp_acc, tmp_dbl_feat, tmp_fdbcont
from tmp_dblink;

insert into one_to_one_accession (ooa_feature_zdb_id, ooa_dblink_zdb_id)
select tmp_dbl_feat, tmp_dbl_id
from tmp_dblink
where tmp_dbl_feat like "ZDB-ALT%";

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select tmp_dbl_id, "ZDB-PUB-110915-1"
from tmp_dblink;




!echo LAB_ORIGIN

insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id)
select tmp_feat_id, 'ZDB-LAB-111117-3'
from tmp_feature;



!echo FEATURE_MRKR_NAME

    

update tmp_feature set tmp_fmrkr_name = 
  (select mrkr_abbrev from marker, feature_marker_relationship
  where tmp_feat_id = fmrel_ftr_zdb_id
    and fmrel_mrkr_zdb_id = mrkr_zdb_id
    and mrkr_zdb_id like "ZDB-GENE%");

update tmp_feature set tmp_fmrkr_name = "Tg(nLacz-GTvirus)"
where tmp_fmrkr_name is null;


update feature set feature_mrkr_abbrev = 
(select tmp_fmrkr_name from tmp_feature where tmp_feat_id = feature_zdb_id)
where feature_zdb_id in (select tmp_feat_id from tmp_feature);



!echo GENOTYPE_FEATURE
create temp table tmp_genofeat
(
   tmp_genofeat_id   varchar(50),
   tmp_genofeat_geno varchar(50),
   tmp_genofeat_feat varchar(50)   
)
with no log;

insert into tmp_genofeat (tmp_genofeat_geno, tmp_genofeat_feat)
select distinct tmp_geno_id, tmp_feat_id 
from tmp_feature, tmp_geno, burgess_lin2
where bl2_plate = tmp_plate_id
  and bl2_la_acc[3,8] = tmp_abbrev[3,8];

select tmp_name, tmp_plate_id, count(*)
from tmp_genofeat, tmp_feature, tmp_geno
where tmp_genofeat_geno = tmp_geno_id
  and tmp_genofeat_feat = tmp_feat_id
group by 1,2
having count(*) > 1;

update tmp_genofeat set tmp_genofeat_id = get_id("GENOFEAT");

insert into zdb_active_data (zactvd_zdb_id) select tmp_genofeat_id from tmp_genofeat;

insert into genotype_feature 
(
  genofeat_zdb_id, genofeat_feature_zdb_id, genofeat_geno_zdb_id,
  genofeat_dad_zygocity, genofeat_mom_zygocity, genofeat_zygocity
)
select distinct tmp_genofeat_id, tmp_genofeat_feat, tmp_genofeat_geno,
       "ZDB-ZYG-070117-7", "ZDB-ZYG-070117-7", "ZDB-ZYG-070117-7"
from tmp_genofeat;


--GENOTYPE NAME

--select get_feature_abbrev_display(tmp_feat_id) from tmp_feature;

{
select distinct get_feature_abbrev_display(feature_zdb_id) as fad,  
              zyg_allele_display, 
	      case
		when mrkr_abbrev is null 
		then lower(get_feature_abbrev_display(feature_zdb_id))
		else lower(mrkr_abbrev)||get_feature_abbrev_display(feature_zdb_id)
                end as fad2,
	      feature_Abbrev,
	      feature_type,
	      case 
	      	   when feature_type = 'TRANSGENIC_INSERTION'
		   then ftrtype_significance +2
		   when feature_type = 'UNSPECIFIED'
		   then ftrtype_significance -2
		   else ftrtype_significance
		   end,
	     zyg_abbrev, feature_mrkr_abbrev, 1 as priority
         from feature, genotype_feature, zygocity, feature_type,  outer (feature_marker_relationship, outer marker)
        where genofeat_geno_zdb_id = "ZDB-GENO-110919-7"
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
	  and feature_type = ftrtype_name
	  and fmrel_mrkr_zdb_id = mrkr_zdb_id
	  and fmrel_ftr_zdb_id = feature_zdb_id
	  and fmrel_type = 'is allele of'
	  union 
	select distinct get_feature_abbrev_display(feature_zdb_id) as fad, 
                            zyg_allele_display, 
              lower(get_feature_abbrev_display(feature_zdb_id)) as fad2,
	      feature_Abbrev,
	      feature_type,
	      case 
	      	   when feature_type = 'TRANSGENIC_INSERTION'
		   then ftrtype_significance +2
	   when feature_type = 'UNSPECIFIED'
		   then ftrtype_significance -2
		   else ftrtype_significance
		   end,
		   zyg_abbrev, feature_mrkr_abbrev, 2 as priority
         from feature, genotype_feature, zygocity, feature_type,
	      feature_marker_relationship
        where genofeat_geno_zdb_id = "ZDB-GENO-110919-7"
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
	  and feature_type = ftrtype_name
	  and fmrel_ftr_zdb_id = feature_zdb_id
	  and fmrel_type not in ('is allele of')
       order by priority asc, zyg_abbrev  , fad2, fad asc;



}
update tmp_geno set tmp_gname = get_genotype_display(tmp_geno_id);
update tmp_geno set tmp_handle = get_genotype_handle(tmp_geno_id);

--unload to 'gname.unl' select tmp_plate_ID, tmp_gname from tmp_geno order by 2;

--unload to 'ghandle.unl' select tmp_handle from tmp_geno order by 1;

select tmp_gname as dup_gname, count(*) as dup_count from tmp_geno group by 1 having count(*) > 1 into temp tmp_dup_geno;

--unload to 'gdup.unl' select * from tmp_dup_geno;

update tmp_dup_geno set dup_count = (select min(tmp_geno_pk) from tmp_geno where dup_gname = tmp_gname);



delete from zdb_active_data where exists (select * from tmp_dup_geno, tmp_geno where tmp_gname = dup_gname and dup_count != tmp_geno_pk and tmp_geno_id = zactvd_zdb_id);


update feature set feature_name = feature_name where feature_zdb_id in
(select tmp_feat_id from tmp_feature);


--rollback work;
commit work;
