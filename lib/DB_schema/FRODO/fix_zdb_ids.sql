begin work ;

--don't really understand this constraint...

select * from genotype
  where geno_zdb_id = 'ZDB-FISH-061010-1' ;


select * from genotype
  where geno_zdb_id = 'ZDB-GENO-061010-1' ;

alter table int_data_supplier
  drop constraint idsup_data_zdb_id_must_equal_idsup_acc_num_for_zirc;

--change xpatex fks

alter table expression_experiment
  drop constraint xpatex_featexp_foreign_key ;

alter table expression_Experiment
  drop constraint expression_experiment_alternate_key ;

alter table expression_experiment
  drop constraint xpatex_gene_zdb_id_foreign_key ;

alter table expression_experiment
  add constraint (foreign key (xpatex_gene_zdb_id)
  references marker constraint xpatex_gene_zdb_id_foreign_key);

rename column expression_experiment.xpatex_featexp_zdb_id 
  to xpatex_genox_zdb_id ;

--create unique index xpatex_alternate_key_index 
--  on expression_experiment (xpatex_source_zdb_id,xpatex_genox_zdb_id,
--	xpatex_assay_name, xpatex_probe_feature_zdb_id,
--	xpatex_gene_zdb_id,xpatex_dblink_zdb_id)
--  using btree in idxdbs3 ;

alter table expression_experiment add constraint unique 
    (xpatex_source_zdb_id,xpatex_genox_zdb_id,xpatex_assay_name,
    xpatex_probe_feature_zdb_id,xpatex_gene_zdb_id,xpatex_dblink_zdb_id) 
    constraint expression_experiment_alternate_key ;

update statistics high for table expression_experiment ;

--drop constraints so we can preserve the old tables in case
--there is a n issue, but not get any errors while changing old_ids
--in zdb_active_data.

--drop old records from zobjtype

delete from zdb_object_type
  where zobjtype_name in ('FISH', 'FEATEXP', 'LOCUS','CHROMO');


!echo "drop feature_experiment constraints" ;

alter table feature_experiment drop constraint
    feature_experiment_alternate_key  ;

alter table feature_experiment drop constraint feature_experiment_primary_key;

alter table feature_experiment drop constraint 
	feature_experiment_genome_fish_foreign_key;

alter table feature_experiment drop constraint 
	feature_experiment_exp_foreign_key;

alter table feature_experiment drop constraint 
	feature_experiment_zdb_active_data_foreign_key;

!echo "Drop fish constraints" ;

alter table fish drop constraint fish_abbrev_unique  ;
alter table fish drop constraint fish_primary_key  ;
alter table fish drop constraint fish_allele_foreign_key;
alter table fish drop constraint fish_zdb_id_foreign_key;
--alter table fish drop constraint fish_father_foreign_key;
--alter table fish drop constraint fish_mother_foreign_key;
alter table fish drop constraint fish_locus_foreign_key;
    
!echo "Drop locus constraints" ;

alter table locus drop constraint locus_primary_key;
alter table locus drop constraint locus_cloned_gene_foreign_key;
alter table locus drop constraint locus_zdb_id_foreign_key;

!echo "drop int_fish_chromo constraints" ;

alter table int_fish_chromo drop constraint intfc_source_id_unique ;
alter table int_fish_chromo drop constraint intfc_target_id_unique ;
alter table int_fish_chromo drop constraint int_fish_chromo_primary_key ;
--alter table int_fish_chromo drop constraint intfc_source_id_foreign_key;
alter table int_fish_chromo drop constraint intfc_target_id_foreign_key;

!echo "drop chromosome constraints" ;

alter table chromosome drop constraint chromosome_primary_key ;

!echo "drop locus registration constraints" ;

alter table locus_registration drop constraint locus_registration_primary_key;
--alter table locus_registration drop constraint 
--	locusreg_fish_record_foreign_key;

!echo "drop alteration constraints" ;

alter table alteration drop constraint alteration_chrom_id_unique  ;
alter table alteration drop constraint alteration_allele_unique  ;
alter table alteration drop constraint alteration_primary_key ;
alter table alteration drop constraint alteration_chrom_change_foreign_key;
alter table alteration drop constraint alteration_zdb_id_foreign_key;
--alter table alteration drop constraint alteration_chrom_id_foreign_key;
--alter table alteration drop constraint alteration_locus_foreign_key;
alter table alteration drop constraint alteration_mutagen_foreign_key;
alter table alteration drop constraint alteration_protocol_foreign_key;


!echo "drop zirc fish line constraints" ;

alter table zirc_fish_line drop constraint zirc_fish_line_primary_key ;

!echo "drop zirc fish line alteration constraints" ;

alter table zirc_fish_line_alteration drop constraint
    zirc_fish_line_alteration_primary_key  ;
--alter table zirc_fish_line_alteration drop constraint 
--    zircflalt_line_id_foreign_key;
--alter table zirc_fish_line_alteration drop constraint 
--  zircflalt_alt_zdb_id_foreign_key;

!echo "drop zirc fish line background constraints" ;

alter table zirc_fish_line_background drop constraint 
    zirc_fish_line_background_primary_key  ;
--alter table zirc_fish_line_background drop constraint 
--   zircflback_line_id_foreign_key;
--alter table zirc_fish_line_background drop constraint 
--	zircflback_fish_zdb_id_foreign_key;

!echo "drop fish supplier status constraints" ;

alter table fish_supplier_status drop constraint
	fish_supplier_status_primary_key  ;
alter table fish_supplier_status drop constraint
    fsupstat_int_data_supplier_foreign_key;
--alter table fish_supplier_status drop constraint 
--	fsupstat_fish_zdb_id_foreign_key;
alter table fish_supplier_status drop constraint 
	fsupstat_fish_status_foreign_key;

!echo "drop zirc fish line status constraints " ;


alter table zirc_fish_line_status drop constraint
	zirc_fish_line_status_primary_key  ;
--alter table zirc_fish_line_status drop constraint
--	zircflstat_line_id_foreign_key;
alter table zirc_fish_line_status drop constraint 
	zircflstat_status_foreign_key;


!echo "drop  alteration type constraints" ;

alter table alteration_type drop constraint alteration_type_primary_key;

set constraints all deferred ;

create temp table tmp_convert_mrkr (
					locus_id varchar(50), 
					new_gene_id varchar(50))
with no log ;


--first do new genes (no cloned gene, not Df or Tg lines)

insert into tmp_Convert_mrkr (locus_id, new_gene_id)
  select mrkr_zdb_id, get_id('GENE')
    from marker
    where mrkr_zdb_id like 'ZDB-LOCUS-%' 
    and mrkr_type != 'TGCONSTRCT'
    and mrkr_name not like 'Tg%';

create unique index conv_idx
  on tmp_convert_mrkr (locus_id, new_gene_id)
  using btree in idxdbs3 ;

update statistics high for table tmp_convert_mrkr ;

update marker 
  set mrkr_zdb_id = (select new_gene_id
			from tmp_convert_mrkr
			where locus_id = mrkr_zdb_id)		
  where mrkr_zdb_id like 'ZDB-LOCUS-%'
  and mrkr_type != 'TGCONSTRCT' ;

insert into zdb_replaced_data (zrepld_old_zdb_id,
				zrepld_new_zdb_id,
				zrepld_old_name)
select locus_id, new_gene_id, mrkr_name
  from tmp_convert_mrkr, marker
   where new_gene_id = mrkr_zdb_id
    and not exists (Select 'x'
			from zdb_replaced_data
			where zrepld_old_zdb_id = locus_id
			and zrepld_new_zdb_id = new_gene_id
			and zrepld_old_name = mrkr_name);

--select count(*), zrepld_new_zdb_id, zrepld_old_zdb_id 
-- from zdb_replaced_data
--  group by zrepld_new_zdb_id, zrepld_old_zdb_id
--  having count(*) > 1 ;


delete from tmp_convert_mrkr ;

--now do TG lines in marker

insert into tmp_Convert_mrkr (locus_id, new_gene_id)
  select mrkr_zdb_id, get_id('TGCONSTRCT')
    from marker
    where mrkr_zdb_id like 'ZDB-LOCUS-%' 
    and mrkr_type = 'TGCONSTRCT'
    and mrkr_name like 'Tg%';

update marker 
  set mrkr_zdb_id = (select new_gene_id
			from tmp_convert_mrkr
			where locus_id = mrkr_zdb_id)		
  where mrkr_zdb_id like 'ZDB-LOCUS-%'
  and mrkr_type = 'TGCONSTRCT' ;

insert into zdb_replaced_data (zrepld_old_zdb_id,
				zrepld_new_zdb_id,
				zrepld_old_name)
select locus_id, new_gene_id, mrkr_name
  from tmp_convert_mrkr, marker
   where new_gene_id = mrkr_zdb_id 
   and not exists (Select 'x'
			from zdb_replaced_data
			where zrepld_old_zdb_id = locus_id
			and zrepld_new_zdb_id = new_gene_id
			and zrepld_old_name = mrkr_name);



create temp table tmp_recattrib (rad_id varchar(50),
				oldid varchar(50))
with no log;

!echo "start of new recattrib section" ;

delete from zdb_replaced_data
 where zrepld_old_zdb_id = 'ZDB-LOCUS-020130-2'
 and zrepld_new_zdb_id like 'ZDB-GENE-%';

update record_attribution
  set recattrib_data_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = 
					recattrib_data_zdb_id
				and zrepld_old_name != 'Df(LG14)wnt8a')
  where exists (select 'x'
		  from zdb_replaced_data
		   where zrepld_old_zdb_id = recattrib_data_zdb_id);



select count(*), recattrib_data_zdb_id, 
		recattrib_source_zdb_id, 
		recattrib_source_type
  from record_attribution
  group by recattrib_data_zdb_id, 
		recattrib_source_zdb_id, 
		recattrib_source_type
  having count(*) > 1 ;


--select distinct get_obj_type(zrepld_new_zdb_id) as mrkr, 
--	get_obj_type(fmrel_ftr_zdb_id) as ftr,
--	get_obj_type(zrepld_old_zdb_id) as oldmrkr,
--	fmrel_type
--  from feature_marker_relationship, zdb_replaced_data
--  where zrepld_old_Zdb_id = fmrel_mrkr_zdb_id
--  and fmrel_mrkr_zdb_id like 'ZDB-LOCUS-%';

--select distinct zrepld_new_zdb_id as new, 
--	fmrel_ftr_zdb_id as ftr,
--	zrepld_old_zdb_id as oldmrkr,
--	fmrel_type
--  from feature_marker_relationship, zdb_replaced_data
--  where zrepld_old_Zdb_id = fmrel_mrkr_zdb_id
--  and fmrel_mrkr_zdb_id like 'ZDB-LOCUS-%'
--  and get_obj_type(zrepld_new_zdb_id) = 'TRANSLOC'
--  and get_obj_type(fmrel_ftr_zdb_id) = 'ALT'
--  and fmrel_type = 'is allele of';


update feature_marker_relationship
  set fmrel_mrkr_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = fmrel_mrkr_zdb_id)
  where exists (select 'x'
		  from zdb_replaced_data
		   where zrepld_old_zdb_id = fmrel_mrkr_zdb_id)
  and fmrel_mrkr_zdb_id like 'ZDB-LOCUS-%' ;

select *
  from marker
  where mrkr_zdb_id is null 
;


insert into zdb_active_data
  select mrkr_zdb_id 
    from marker
    where not exists (select 'x'
			from zdb_active_data
			where mrkr_zdb_id = zactvd_zdb_id);

select *
  from zdb_active_data
  where zactvd_zdb_id is null 
;


update marker_history
  set mhist_mrkr_zdb_id = (Select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = mhist_mrkr_zdb_id)
  where exists (select 'x'
		  from zdb_replaced_data
		   where zrepld_old_zdb_id = mhist_mrkr_zdb_id)
  and mhist_mrkr_zdb_id like 'ZDB-LOCUS-%' ;

delete from tmp_convert_mrkr ;

--now do fish in genotype table

create temp table tmp_convert_fish (
					fish_id varchar(50), 
					new_geno_id varchar(50))
with no log ;

insert into tmp_convert_fish (fish_id, new_geno_id)
  select geno_zdb_id, 'ZDB-GENO'||substring(geno_zdb_id from 9 for 12)
    from genotype
    where geno_zdb_id like 'ZDB-FISH-%';

update tmp_convert_fish
  set new_geno_id = scrub_char(new_geno_id);

create unique index cffindex
  on tmp_convert_fish (fish_id)
  using btree in idxdbs3;

create unique index cfgindex
  on tmp_convert_fish (new_geno_id)
  using btree in idxdbs3;

create unique index cffgindex
  on tmp_convert_fish (new_geno_id,fish_id)
  using btree in idxdbs3;

update statistics for table tmp_convert_fish;

!echo "genotype" ;

update genotype
  set geno_zdb_id = (select new_geno_id from tmp_convert_fish
			where fish_id = geno_zdb_id)
  where geno_zdb_id like 'ZDB-FISH-%';

select * from genotype
  where geno_zdb_id = 'ZDB-GENO-061010-1';

--!echo "data_note" ; 

--case 1445 see also move_locusreg.sql

update data_note
  set dnote_data_zdb_id = (select new_geno_id from tmp_convert_fish
			where fish_id = dnote_data_zdb_id)
  where dnote_data_zdb_id like 'ZDB-FISH-%';

!echo "genotype feature" ;

update genotype_feature
  set genofeat_geno_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = genofeat_geno_zdb_id) 
  where genofeat_geno_zdb_id like 'ZDB-FISH-%';

select * from genotype_feature
  where genofeat_geno_zdb_id like 'ZDB-FISH-%';


--!echo "genotype marker" ;

--update genotype_marker
--  set genomrkr_geno_zdb_id = (select new_geno_id
--				from tmp_convert_fish
--				where fish_id = genomrkr_geno_zdb_id) 
-- where genomrkr_geno_zdb_id like 'ZDB-FISH-%';

!echo "genotype background" ;

update genotype_background
  set genoback_geno_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = genoback_geno_zdb_id) 
  where genoback_geno_zdb_id like 'ZDB-FISH-%';

update genotype_background
  set genoback_background_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = genoback_background_zdb_id) 
  where genoback_background_zdb_id like 'ZDB-FISH-%';


!echo "genotype experiment" ;

update genotype_experiment
  set genox_geno_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = genox_geno_zdb_id) 
  where genox_geno_zdb_id like 'ZDB-FISH-%';

!echo "record_attribution" 

update record_attribution
  set recattrib_data_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = recattrib_data_zdb_id)
  where recattrib_data_zdb_id like 'ZDB-FISH-%' ; 


select count(*), recattrib_data_zdb_id, 
		recattrib_source_zdb_id, 
		recattrib_source_type
  from record_attribution
  group by recattrib_data_zdb_id, 
		recattrib_source_zdb_id, 
		recattrib_source_type
  having count(*) > 1 ;

!echo 'zdb_active_data' ;

update zdb_active_data
  set zactvd_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = zactvd_zdb_id)
  where zactvd_zdb_id like 'ZDB-FISH-%' ; 

!echo "ZDBACTIVEDATANULL";

select *
  from zdb_active_data
  where zactvd_zdb_id is null 
;


!echo 'record_attribution' ;

update record_attribution
  set recattrib_data_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = recattrib_data_zdb_id)
  where recattrib_data_zdb_id like 'ZDB-FISH-%' ; 

select count(*), recattrib_data_zdb_id, 
		recattrib_source_zdb_id, 
		recattrib_source_type
  from record_attribution
  group by recattrib_data_zdb_id, 
		recattrib_source_zdb_id, 
		recattrib_source_type
  having count(*) > 1 ;

!echo 'data_alias' ;

update data_alias
  set dalias_data_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = dalias_data_zdb_id)
  where dalias_data_zdb_id like 'ZDB-FISH-%' ; 

create temp table tmp_l_alias (id varchar(50),
				alias varchar(30),
				gene_id varchar(50))
  with no log;

!echo "no replaced data record for this locus" ;

--select * from locus
--  where not exists (Select 'x'
--			from zdb_replaced_Data
--			where zrepld_old_zdb_id = locus.zdb_id);

insert into tmp_l_alias (id, alias)
  select dalias_data_zdb_id, dalias_alias
    from data_alias where dalias_Data_zdb_id like 'ZDB-LOCUS-%';

update tmp_l_alias
  set gene_id = (select zrepld_new_zdb_id
			from zdb_replaced_data
			where zrepld_old_zdb_id = id
			and zrepld_old_zdb_id like 'ZDB-LOCUS-%'
			and zrepld_new_zdb_id like 'ZDB-GENE-%');

update tmp_l_alias
  set gene_id = (select zrepld_new_zdb_id
			from zdb_replaced_data
			where zrepld_old_zdb_id = id
			and zrepld_old_zdb_id like 'ZDB-LOCUS-%'
			and zrepld_new_zdb_id like 'ZDB-TGCONSTRCT-%')
  where gene_id is null;


update tmp_l_alias
  set gene_id = (select zrepld_new_zdb_id
			from zdb_replaced_data
			where zrepld_old_zdb_id = id
			and zrepld_old_zdb_id like 'ZDB-LOCUS-%'
			and zrepld_new_zdb_id like 'ZDB-ALT-%')
  where gene_id is null;

--select count(*), gene_id
--  from tmp_l_alias
--  group by gene_id having count(*) > 1;

select * from zdb_replaced_data
where zrepld_old_zdb_id = 'ZDB-LOCUS-011016-4' ;

select count(*)
  from tmp_l_alias
  where gene_id is null;

delete from tmp_l_alias
  where gene_id is null ;

delete from tmp_l_alias
  where exists (select 'x'
		  from data_alias
		  where dalias_alias = alias
		  and dalias_data_zdb_id = gene_id);

--create temp table tmp_alias (tdalias_zdb_id varchar(50),
--				tdalias_data_zdb_id varchar(50),
--				tdalias_alias varchar(255),
--				tdalias_group varchar(30))
--with no log ;

--isnert into tmp_alias 
--select dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group
--  from data_alias
--  where dalias_data_zdb_id like 'ZDB-LOCUS-%';
				
--delete from data_alias  where dalias_data_zdb_id like 'ZDB-LOCUS-%';

update data_alias
  set dalias_data_zdb_id = (select gene_id from tmp_l_alias
				where dalias_data_zdb_id = id
				and dalias_alias = alias)
   where dalias_data_zdb_id like 'ZDB-LOCUS-%' 
   and exists (Select 'x'
		from tmp_l_alias
		where dalias_data_zdb_id = id
		and dalias_alias = alias);

select * from data_alias
  where dalias_data_zdb_id is null;

select count(*)
  from data_alias
  where dalias_data_zdb_id like 'ZDB-LOCUS-%'; 

select *
  from data_alias, record_attribution
  where dalias_data_zdb_id like 'ZDB-LOCUS-%'
  and dalias_data_zdb_id = recattrib_data_zdb_id ;

delete from data_alias
  where dalias_data_zdb_id like 'ZDB-LOCUS-%'
  and dalias_data_zdb_id != 'ZDB-LOCUS-011016-4' ;

!echo 'primer set' ;

select first 1 * from tmp_convert_fish ;

update primer_set
  set strain_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = strain_id)
  where strain_id like 'ZDB-FISH-%' ; 



select first 10 * from primer_set
  where strain_id is null ;

--!echo "fish_image" ;

--update fish_image
--  set fimg_fish_zdb_id = (select new_geno_id
--				from tmp_convert_fish
--				where fish_id = fimg_fish_zdb_id)
--  where fimg_fish_zdb_id like 'ZDB-FISH-%' ; 


!echo "inference_group_member" ;

update inference_group_member
  set infgrmem_inferred_from = replace(infgrmem_inferred_from,"FISH","GENO") 
  where infgrmem_inferred_from like 'ZFIN:ZDB-FISH-%'  ;

update inference_group_member
  set infgrmem_inferred_from = (select new_geno_id
				  from tmp_convert_fish, fish
				  where 'ZFIN:'||locus = 
					infgrmem_inferred_from
				  and allele like 'un_%'
				  and fish.zdb_id = fish_id)
  where infgrmem_inferred_from like 'ZFIN:ZDB-LOCUS-%' 
  and exists (select 'x'
		from fish, tmp_convert_fish
		where 'ZFIN:'||fish.locus = infgrmem_inferred_from
		and fish.allele like 'un_%'
		and fish.zdb_id = fish_id);

!echo "inference group still has fish"
select * from inference_group_member
  where infgrmem_inferred_from like 'ZFIN:ZDB-FISH-%';

select count(*), infgrmem_mrkrgoev_zdb_id, infgrmem_inferred_from
  from inference_group_member
  group by  infgrmem_mrkrgoev_zdb_id, infgrmem_inferred_from
  having count(*) > 1;

select * from inference_group_member
  where infgrmem_mrkrgoev_zdb_id is null
   or infgrmem_inferred_from is null ;

!echo "locus still in inference_group_member" ;

!echo "all_map_names" ;

update all_map_names
  set allmapnm_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = allmapnm_zdb_id)
  where allmapnm_zdb_id like 'ZDB-FISH-%' ; 


--!echo "column attribution" ;

--update column_attribution  
-- set colattrib_data_zdb_id = (select new_geno_id
--				from tmp_convert_fish
--				where fish_id = colattrib_data_zdb_id)
--  where colattrib_data_zdb_id like 'ZDB-FISH-%' ; 


!echo "fish search";

update fish_search
  set fish_id = (select new_geno_id
				from tmp_convert_fish
				where fish_search.fish_id = 
					tmp_convert_fish.fish_id)
  where fish_id like 'ZDB-FISH-%' ;

!echo "linkage member" ;

update linkage_member
  set lnkgmem_member_zdb_id = (select alteration.zdb_id 
				from alteration, fish
				where fish.zdb_id = lnkgmem_member_zdb_id
				and fish.allele = alteration.allele)
  where lnkgmem_member_zdb_id like 'ZDB-FISH-%' ;

--update linkage_member
--  set lnkgmem_member_zdb_id = (select new_geno_id
--				from tmp_convert_fish
--				where fish_id = lnkgmem_member_zdb_id)
--  where lnkgmem_member_zdb_id like 'ZDB-FISH-%' ; 

!echo "linkage pair member" 

update linkage_pair_member
  set lpmem_member_zdb_id = (select alteration.zdb_id 
				from alteration, fish
				where fish.zdb_id = lpmem_member_zdb_id
				and fish.allele = alteration.allele)
  where lpmem_member_zdb_id like 'ZDB-FISH-%' ; 

!echo "mapped_marker" 

update mapped_marker
  set marker_id = (select alteration.zdb_id 
				from alteration, fish
				where fish.zdb_id = marker_id
				and fish.allele = alteration.allele)
  where marker_id like 'ZDB-FISH-%' ; 


!echo "paneled_markers" 

--update paneled_markers
--  set zdb_id = (select new_geno_id
--				from tmp_convert_fish
--				where fish_id = zdb_id)
--  where zdb_id like 'ZDB-FISH-%' ; 

update paneled_markers
  set zdb_id = (select alteration.zdb_id 
				from alteration, fish
				where fish.zdb_id = paneled_markers.zdb_id
				and fish.allele = alteration.allele)
  where zdb_id like 'ZDB-FISH-%' ; 



!echo "probe_library" 

update probe_library
  set probelib_strain_Zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = probelib_strain_zdb_id)
  where probelib_strain_zdb_id like 'ZDB-FISH-%' ; 

update probe_library
  set probelib_strain_Zdb_id = null
  where  probelib_strain_Zdb_id = '';

!echo "zmap_pub_pan_mark" 

update zmap_pub_pan_mark
  set zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = zdb_id)
  where zdb_id like 'ZDB-FISH-%' ; 

!echo "TEMP FISH SUPP" ;

create temp table tmp_supplier (sup_id varchar(50), data_id varchar(50),
				acc_num varchar(50), allele varchar(50))
with no log ;

insert into tmp_supplier
  select idsup_supplier_zdb_id , idsup_Data_zdb_id, idsup_acc_num,'alt_zdb_id'
    from int_data_supplier
   where idsup_data_zdb_id like 'ZDB-FISH-%'
  and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53' ;

update tmp_supplier
  set allele = (select alteration.zdb_id
		from fish, alteration, int_data_supplier
		where fish.allele = alteration.allele
		and idsup_data_zdb_id = alteration.zdb_id
		and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53');

delete from int_data_supplier
  where exists (Select 'x' from tmp_supplier
		  where allele = idsup_data_zdb_id
		  and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53' );

update int_data_supplier
  set idsup_data_zdb_id = (Select alteration.zdb_id
				from fish, alteration
				where fish.allele = alteration.allele
				and fish.zdb_id = idsup_data_zdb_id)
  where idsup_supplier_zdb_id = 'ZDB-LAB-991005-53'
  and exists (Select 'x'
		from alteration, fish where
			fish.allele = alteration.allele
				and fish.zdb_id = idsup_data_zdb_id) ;


update int_data_supplier
  set idsup_acc_num = idsup_data_zdb_id
  where idsup_acc_num like 'ZDB-FISH-%';


--delete from int_data_supplier
--  where exists (Select 'x'
--			from tmp_Supplier
--			where sup_id = idsup_supplier_zdb_id
--			and data_id = idsup_data_zdb_id)
--  and idsup_Data_zdb_id like 'ZDB-FISH-%' ;

update int_data_supplier
  set idsup_data_zdb_id = replace(idsup_data_zdb_id,"FISH","GENO")
  where idsup_data_zdb_id like 'ZDB-FISH-%';


update int_data_supplier
  set idsup_avail_state = null
  where idsup_supplier_zdb_id = 'ZDB-LAB-991005-53' ;

select count(*), idsup_Data_zdb_id, idsup_supplier_zdb_id
  from int_data_supplier
  group by idsup_Data_zdb_id, idsup_supplier_zdb_id
  having count(*) > 1;



update zdb_replaced_data
  set zrepld_new_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = zrepld_new_zdb_id)
  where zrepld_new_zdb_id like 'ZDB-FISH-%' ; 


insert into zdb_replaced_data (zrepld_old_zdb_id,
				zrepld_new_zdb_id,
				zrepld_old_name)
  select fish_id, new_geno_id, (select geno_display_name
				  from genotype
				  where geno_zdb_id = new_geno_id)
    from tmp_convert_fish 
    where not exists (Select 'x'
			from zdb_replaced_data
			where zrepld_old_zdb_id = fish_id
			and zrepld_new_zdb_id = new_geno_id);


select * from genotype
  where geno_zdb_id = 'ZDB-GENO-061010-1' ;



select count(*), new_geno_id, fish_id
  from tmp_convert_fish
  group by  new_geno_id, fish_id
  having count(*) > 1;

select count(*), new_geno_id
  from tmp_convert_fish
  group by  new_geno_id
  having count(*) > 1;

select count(*),fish_id
  from tmp_convert_fish
  group by  fish_id
  having count(*) > 1;

select count(*),geno_Zdb_id
  from genotype
  group by  geno_zdb_id
  having count(*) > 1;



--now do expression experiment replacements

drop table tmp_convert_fish ;

!echo "MAKE GENOX IDS" ;

create temp table tmp_convert_featexp (
					featexp_id varchar(50), 
					new_genox_id varchar(50))
with no log ;

insert into tmp_convert_featexp (featexp_id, new_genox_id)
  select featexp_zdb_id, replace(featexp_zdb_id,'FEATEXP','GENOX')
    from feature_experiment
    where featexp_zdb_id like 'ZDB-FEATEXP-%';


update tmp_convert_featexp
  set new_genox_id = scrub_char(new_genox_id);

create unique index cffindex
  on tmp_convert_featexp (featexp_id)
  using btree in idxdbs3;

create unique index cfgindex
  on tmp_convert_featexp (new_genox_id)
  using btree in idxdbs3;

create unique index cffgindex
  on tmp_convert_featexp (new_genox_id,featexp_id)
  using btree in idxdbs3;

update statistics for table tmp_convert_featexp;

--now do featexp to genox

update genotype_experiment
  set genox_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where featexp_id = genox_zdb_id) 
  where genox_zdb_id like 'ZDB-FEATEXP-%';


update record_attribution
  set recattrib_data_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where featexp_id = recattrib_data_zdb_id) 
  where recattrib_data_zdb_id like 'ZDB-FEATEXP-%';


update expression_experiment
  set xpatex_genox_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where xpatex_genox_zdb_id = featexp_id) 
  where xpatex_genox_zdb_id  like 'ZDB-FEATEXP-%';


--update phenotype_old
--  set pold_genox_zdb_id = (select new_genox_id
--			from tmp_convert_featexp
--			where pold_genox_zdb_id = featexp_id) 
--  where pold_genox_zdb_id  like 'ZDB-FEATEXP-%'; 

update atomic_phenotype
  set apato_genox_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where apato_genox_zdb_id = featexp_id) 
  where apato_genox_zdb_id like 'ZDB-FEATEXP-%';


update genotype_experiment
  set genox_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where genox_zdb_id = featexp_id) 
  where genox_zdb_id  like 'ZDB-FEATEXP-%';


update feature_experiment
  set featexp_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where featexp_zdb_id = featexp_id) 
  where featexp_zdb_id  like 'ZDB-FEATEXP-%';


update zdb_active_data
  set zactvd_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where featexp_id = zactvd_zdb_id) 
  where zactvd_zdb_id like 'ZDB-FEATEXP-%'
  and exists (select 'x'
		from tmp_convert_featexp
		where zactvd_zdb_id = featexp_id);



!echo "ZDBACTIVEDATA NULL" ;

select *
  from zdb_active_data
  where zactvd_zdb_id is null ;


select count(*), recattrib_data_zdb_id, 
		recattrib_source_zdb_id, 
		recattrib_source_type
  from record_attribution
  group by recattrib_data_zdb_id, 
		recattrib_source_zdb_id, 
		recattrib_source_type
  having count(*) > 1 ;

select count(*), idsup_data_zdb_id, idsup_supplier_zdb_id
  from int_data_supplier
  group by idsup_data_zdb_id, idsup_supplier_zdb_id
  having count(*) > 1;

set constraints all immediate ;

drop table feature_experiment ;

commit work ;

--rollback work ;
