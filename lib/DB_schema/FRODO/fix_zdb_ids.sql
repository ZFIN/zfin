begin work ;

--don't really understand this constraint...

alter table int_data_supplier
  drop constraint idsup_data_zdb_id_must_equal_idsup_acc_num_for_zirc;

--change xpatex fks

alter table expression_experiment
  drop constraint xpatex_featexp_foreign_key ;

alter table expression_Experiment
  drop constraint expression_experiment_alternate_key ;

rename column expression_experiment.xpatex_featexp_zdb_id 
  to xpatex_genox_zdb_id ;

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

update record_attribution
  set recattrib_data_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = 
					recattrib_data_zdb_id)
  where exists (select 'x'
		  from zdb_replaced_data
		   where zrepld_old_zdb_id = recattrib_data_zdb_id);


update feature_marker_relationship
  set fmrel_mrkr_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = fmrel_mrkr_zdb_id)
  where exists (select 'x'
		  from zdb_replaced_data
		   where zrepld_old_zdb_id = fmrel_mrkr_zdb_id)
  and fmrel_mrkr_zdb_id like 'ZDB-LOCUS-%' ;

insert into zdb_active_data
  select mrkr_zdb_id 
    from marker
    where not exists (select 'x'
			from zdb_active_data
			where mrkr_zdb_id = zactvd_zdb_id);
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


!echo "genotype feature" ;

update genotype_feature
  set genofeat_geno_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = genofeat_geno_zdb_id) 
  where genofeat_geno_zdb_id like 'ZDB-FISH-%';

select * from genotype_feature
  where genofeat_geno_zdb_id like 'ZDB-FISH-%';


!echo "genotype marker" ;

update genotype_marker
  set genomrkr_geno_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = genomrkr_geno_zdb_id) 
  where genomrkr_geno_zdb_id like 'ZDB-FISH-%';

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


!echo 'zdb_active_data' ;

update zdb_active_data
  set zactvd_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = zactvd_zdb_id)
  where zactvd_zdb_id like 'ZDB-FISH-%' ; 


!echo 'data_alias' ;

update data_alias
  set dalias_data_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = dalias_data_zdb_id)
  where dalias_data_zdb_id like 'ZDB-FISH-%' ; 


!echo 'primer set' ;

update primer_set
  set strain_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = strain_id)
  where strain_id like 'ZDB-FISH-%' ; 

select strain_id from primer_set
  where strain_id is null ;

--!echo "fish_image" ;

--update fish_image
--  set fimg_fish_zdb_id = (select new_geno_id
--				from tmp_convert_fish
--				where fish_id = fimg_fish_zdb_id)
--  where fimg_fish_zdb_id like 'ZDB-FISH-%' ; 


!echo "inference_group_member" ;

update inference_group_member
  set infgrmem_inferred_from = (select new_geno_id
				from tmp_convert_fish
				where fish_id = infgrmem_inferred_from)
  where infgrmem_inferred_from like 'ZDB-FISH-%' ; 

!echo "mapped_marker" ;

update mapped_marker
  set marker_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = marker_id)
  where marker_id like 'ZDB-FISH-%' ; 


!echo "all_map_names" ;

update all_map_names
  set allmapnm_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = allmapnm_zdb_id)
  where allmapnm_zdb_id like 'ZDB-FISH-%' ; 


!echo "column attribution" ;

update column_attribution  
 set colattrib_data_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = colattrib_data_zdb_id)
  where colattrib_data_zdb_id like 'ZDB-FISH-%' ; 


!echo "fish search";

update fish_search
  set fish_id = (select new_geno_id
				from tmp_convert_fish
				where fish_search.fish_id = 
					tmp_convert_fish.fish_id)
  where fish_id like 'ZDB-FISH-%' ;

!echo "linkage member" 

update linkage_member
  set lnkgmem_member_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = lnkgmem_member_zdb_id)
  where lnkgmem_member_zdb_id like 'ZDB-FISH-%' ; 

!echo "linkage pair member" 

update linkage_pair_member
  set lpmem_member_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = lpmem_member_zdb_id)
  where lpmem_member_zdb_id like 'ZDB-FISH-%' ; 

!echo "mapped_marker" 

update mapped_marker
  set marker_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = marker_id)
  where marker_id like 'ZDB-FISH-%' ; 

!echo "paneled_markers" 

update paneled_markers
  set zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = zdb_id)
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


update int_data_supplier
  set idsup_data_zdb_id = (select new_geno_id
				from tmp_convert_fish
				where fish_id = idsup_data_zdb_id)
  where idsup_data_zdb_id like 'ZDB-FISH-%' ; 

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



--now do expression experiment replacements

drop table tmp_convert_fish ;

--make genox ids


create temp table tmp_convert_featexp (
					featexp_id varchar(50), 
					new_genox_id varchar(50))
with no log ;

insert into tmp_convert_featexp (featexp_id, new_genox_id)
  select featexp_zdb_id, 'ZDB-GENOX'||substring(featexp_zdb_id from 12 for 12)
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

update phenotype_anatomy
  set pato_genox_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where pato_genox_zdb_id = featexp_id) 
  where pato_genox_zdb_id like 'ZDB-FEATEXP-%';

update phenotype_go
  set patog_genox_zdb_id = (select new_genox_id
			from tmp_convert_featexp
			where patog_genox_zdb_id = featexp_id) 
  where patog_genox_zdb_id like 'ZDB-FEATEXP-%';

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
  where zactvd_zdb_id like 'ZDB-FEATEXP-%';

set constraints all immediate ;

drop table feature_experiment ;

commit work ;

--rollback work ;