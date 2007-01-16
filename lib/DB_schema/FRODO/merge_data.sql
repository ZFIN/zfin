begin work ;

update statistics for procedure ;

alter table locus
  modify (abbrev varchar(120)) ;

update locus
  set abbrev = locus_name
  where abbrev = 'NULL'
  or abbrev is null;

insert into apato_tag 
  values ('absent') ;

insert into apato_tag 
  values ('present') ;

--insert into apato_tag 
--  values ('abnormal') ;

set constraints all deferred ;

insert into genotype (geno_zdb_id ,
			geno_display_name,
			geno_handle,
			geno_supplier_stock_number,
			geno_date_entered,
			geno_name_order,
			geno_is_wildtype,
			geno_is_extinct)

	select fish.zdb_id,
         case
           when cloned_gene is not null
                    then (select 
				mrkr_abbrev||"<sup>"||allele||"</sup>"
					    from marker
					    where mrkr_zdb_id = a.cloned_gene)
           when cloned_gene is null 
		    then a.abbrev||"<sup>"||allele||"</sup>"
	   end,
	 allele,
	 orig_crossnum,
	 entry_time,
	 fish_allele_order,
	 'f',
	fish_extinct	 
   	from fish, locus a
  	 where fish.locus = a.zdb_id
         and line_type = 'mutant'
	 and locus_name not like 'Df%'
         and locus_name not like 'Tg%'
	 and locus_name not like 'T(%';

insert into genotype (geno_zdb_id ,
			geno_display_name,
			geno_handle,
			geno_supplier_stock_number,
			geno_date_entered,
			geno_name_order,
			geno_is_wildtype,
			geno_is_extinct)

	select fish.zdb_id,
         case
           when cloned_gene is not null
                    then (select 
			    mrkr_abbrev||"<sup>"||a.abbrev||allele||"</sup>"
					    from marker
					    where mrkr_zdb_id = a.cloned_gene)
           when cloned_gene is null 
		    then a.abbrev||allele
	   end,
	 allele,
	 orig_crossnum,
	 entry_time,
	 fish_allele_order,
	 'f',
	fish_extinct	 
   	from fish, locus a
  	 where fish.locus = a.zdb_id
         and line_type = 'mutant'
	 and (locus_name like 'Df%'
         or locus_name like 'Tg%'
	 or locus_name like 'T(%');

insert into genotype (geno_zdb_id ,
			geno_display_name,
			geno_handle,
			geno_supplier_stock_number,
			geno_date_entered,
			geno_name_order,
			geno_is_wildtype,
			geno_is_extinct)

	select fish.zdb_id,
         name, 
	fish.abbrev,
	 orig_crossnum,
	 entry_time,
	 fish_allele_order,
	 't',
	fish_extinct		 
   	from fish
        where line_type = 'wild type';

--select geno_display_name, count(*)
--  from genotype
--  group by geno_display_name
--  having count(*) > 1;

!echo "here's the HANDLE update!" ;

--select count(*) from fish
--where not exists (select 'x' from genotype
--			where geno_zdb_id = fish.zdb_id);


update genotype
  set geno_handle = 
	geno_handle||(select 
			case when father is null
				and mother is null then 'UNK'||'U' 
			when father is null 
				and mother is not null 
			  then (select a.abbrev||'U' from fish a
					where b.mother = a.zdb_id)
			when b.mother is null 
				and b.father is not null 
			  then (select a.abbrev||'U' 
					from fish a
					where b.father = a.zdb_id)
			when b.mother is not null 
			   and b.father is not null
		         then (select 
				case 
				 when a.abbrev = c.abbrev
					and c.zdb_id = b.father
					and a.zdb_id = b.mother

				  then a.abbrev||'U'
				 when a.abbrev != c.abbrev
					and c.zdb_id = b.father
					and a.zdb_id = b.mother
				  then a.abbrev||"U"||","||c.abbrev||"U"
				  else null end
				from fish a, fish c
			 	where a.zdb_id = b.mother
				and c.zdb_id = b.father
			)
			else 'UNKU' end
			from fish b 
                        where b.zdb_id = geno_zdb_id)
  where geno_is_wildtype = 'f';


update genotype
  set geno_handle = geno_display_name
  where geno_is_wildtype = 't' ;


update genotype
  set geno_handle = geno_display_name
  where geno_handle is null ;

update genotype
  set geno_handle = geno_zdb_id
  where geno_handle is null ;

--insert into genotype background
!echo "mother background" ;

insert into genotype_background (genoback_geno_zdb_id, 
					genoback_background_zdb_id)
  select zdb_id,mother
    from fish
    where mother is not null
    and mother != '' ;
 
!echo "father background" ;
insert into genotype_background (genoback_geno_zdb_id, 
					genoback_background_zdb_id)
  select zdb_id,father
    from fish
    where father is not null
    and father != '' 
    and not exists (select 'x' from genotype_background
			where genoback_geno_zdb_id = 
				zdb_id
			and genoback_background_zdb_id =
				father);

--fill up genotype feature

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_zygocity)
  select get_id('GENOFEAT'), 
		zdb_id, 
		(select zyg_zdb_id 
			from zygocity 
			where zyg_name = 'unknown')
	  from fish
	  where line_type != 'wild type';


--fix these for Tg and Df lines!!

update genotype_feature
  set genofeat_feature_zdb_id = (select alteration.zdb_id
					from alteration, fish
					where genofeat_geno_zdb_id =
					fish.zdb_id
					and alteration.allele = fish.allele)
  where genofeat_feature_zdb_id is null;

!echo "chromosome updates" 

create temp table tmp_linkage (zdb_id varchar(50), 
				alt_id varchar(50), 
				lg int)
with no log ;


insert into tmp_linkage (zdb_id,
			lg,
			alt_id)
  select get_id('LINK'), chrom_num, feature_zdb_id
  from int_fish_chromo, chromosome, feature, genotype_feature
  where genofeat_geno_zdb_id = source_id	
  and target_id = chromosome.zdb_id
  and feature_zdb_id = genofeat_feature_zdb_id;

insert into linkage (lnkg_zdb_id,lnkg_or_lg)
 select zdb_id, lg 
   from tmp_linkage 
  where not exists (select 'x'
			from linkage, linkage_member
			where lnkg_zdb_id = lnkgmem_linkage_zdb_id
			and lnkgmem_member_zdb_id = alt_id
			and lnkg_or_lg = lg);

insert into linkage_member (lnkgmem_linkage_zdb_id,
				lnkgmem_member_zdb_id)
  select zdb_id, alt_id
    from tmp_linkage
    where not exists (select 'x'
			from linkage, linkage_member
			where lnkg_zdb_id = lnkgmem_linkage_zdb_id
			and lnkgmem_member_zdb_id = alt_id
			and lnkg_or_lg = lg);

update genotype_feature
  set genofeat_chromosome = (select chrom_num
					from int_fish_chromo, chromosome
					where genofeat_geno_zdb_id =
					source_id	
					and target_id = chromosome.zdb_id)
  where genofeat_chromosome is null;

--!! allele not found


--deficiencies to SO term 'deletion'
--translocation to SO term 'interchromosomal_mutation'
--points to SO term 'point_mutation'

!echo "features from alteration" ;

insert into data_alias (dalias_zdb_id,
				dalias_data_zdb_id,
				dalias_alias,
				dalias_group)
select get_id('DALIAS'),
	zdb_id,
	allele,
	'alias'
  from alteration
  where exists  (Select 'x'
			from fish, locus
			where fish.allele = alteration.allele
			and fish.locus = locus.zdb_id
			and (locus_name like 'Tg%'
				or locus_name like 'Df%'
				or locus_name like 'T(%')
			    and alteration.allele not like '%unspecified%')
  and not exists (select 'x'
			from data_alias
			where dalias_alias = allele
			and dalias_data_zdb_id = zdb_id);

insert into zdb_Active_data 
  select dalias_zdb_id 
   from data_alias
   where not exists (Select 'x'
			from zdb_Active_data
			where zactvd_zdb_id = dalias_zdb_id);


insert into feature (feature_zdb_id,
			feature_name,
			feature_type,
			feature_abbrev,
			feature_lab_of_origin,
			feature_date_entered)
  select zdb_id, 
	case 
	  when exists (select 'x' 
			    from fish,locus
		   	    where fish.locus = locus.zdb_id
			    and alteration.allele = fish.allele
			    and (locus_name like 'Tg%'
				or locus_name like 'Df%'
				or locus_name like 'T(%')
			    and alteration.allele not like '%unspecified%')
		then (select fish.name||fish.allele
			from fish where fish.allele = alteration.allele)
	  when alteration.allele like '%unspecified%'
		then (select fish.name||"unspecified"
			from fish where fish.allele = alteration.allele)
	  else allele
	  end,
	case 
	  when chrom_change = 'deficiency'
		then 'DEFICIENCY'
	  when chrom_change = 'translocation'
		then 'TRANSLOC'
	  when chrom_change = 'point'
	        then 'POINT_MUTATION'
	  when chrom_change in ('insertion', 'unknown')
		and allele in (select allele 
			    from fish,locus
		   	    where fish.locus = locus.zdb_id
			    and locus_name like 'Tg%')
		then 'TRANSGENIC_INSERTION'
	  when chrom_change = 'unknown'
	        then 'SEQUENCE_VARIANT'
	  else upper(chrom_change)
	  end,
	allele,
	(select lab
		from fish
		where fish.allele = alteration.allele),
	(select entry_time
		from fish
		where fish.allele = alteration.allele)
  from alteration;

select * from feature where feature_zdb_id = 'ZDB-ALT-051001-4';

!echo "FEATURE NAME UNIQUE??" ;

select count(*), feature_name
  from feature
  group by feature_name
  having count(*) > 1; 

select * from feature
where feature_name = 'Tg(rag2:EGFP-bcl2)zdf9' ;

--make replaced data records for Tg and Df loci.

alter table mapped_deletion
  drop constraint mapdel_allele_foreign_key_odc ;


update mapped_deletion
  set allele = (select fish.name||fish.allele
		  from fish
		  where fish.allele = mapped_deletion.allele
		  and (fish.name like 'Tg%'
			or fish.name like 'T(%'
			or fish.name like 'Df%'))
  where exists (select 'x'
		  from fish
		  where fish.allele = mapped_deletion.allele
		  and (fish.name like 'Tg%'
			or fish.name like 'T(%'
			or fish.name like 'Df%'));


insert into zdb_replaced_data (zrepld_old_zdb_id,
				zrepld_new_zdb_id,
				zrepld_old_name)
select locus.zdb_id,
	fish.zdb_id,
	locus_name
  from locus,fish
   where fish.locus = locus.zdb_id
	and (locus_name like 'Df%'
		or locus_name like 'T(%');

--for Df(LG14)wnt8a we have a replaced data record for this locus
--pointing at both the affected gene and the alt.  the alt was 
--added during frodo for consistancy with other deficiencies.
--not sure about the gene?? (TALK TO ERIK).

--don't replaced Tg's--they are going to exist in marker
--table as constructs.  They should have relationships
--to TG insertions (contains relationships).

--insert into zdb_replaced_data (zrepld_old_zdb_id,
--				zrepld_new_zdb_id,
--				zrepld_old_name)
--select locus.zdb_id,
--	alteration.zdb_id,
--	locus_name
--  from alteration, locus
--  where alteration.locus = locus.zdb_id
--	and locus_name like 'Tg%'
--	and chrom_change in ('insertion', 'unknown');

--make previous names for trangenic insertions
--and deficiencies

insert into data_alias (dalias_zdb_id,
				dalias_data_zdb_id,
				dalias_group,
				dalias_alias)
  select get_id('DALIAS'),
	alteration.zdb_id,
	'alias',
	locus_name
    from alteration, locus
   where alteration.locus = locus.zdb_id
	and (locus_name like 'Df%'
		or locus_name like 'T(%')
	and (chrom_change = 'deficiency'
		or chrom_change = 'translocation')
   and not exists (Select 'x'
			from data_alias
			where dalias_data_zdb_id = alteration.zdb_id
			and dalias_alias = locus_name);

insert into data_alias (dalias_zdb_id,
				dalias_data_zdb_id,
				dalias_group,
				dalias_alias)
  select get_id('DALIAS'),
	alteration.zdb_id,
	'alias',
	locus_name
    from alteration, locus
   where alteration.locus = locus.zdb_id
	and locus_name like 'Tg%'
	and chrom_change in ('insertion', 'unknown')
     and not exists (select 'x'
			from data_alias
			where dalias_data_zdb_id = alteration.zdb_id
			and dalias_alias = locus_name);

--update feature 
--  set feature_lab_of_origin = 'ZDB-LAB-000914-1'
--  where feature_lab_of_origin is null ;

--update feature 
--  set feature_lab_of_origin = 'ZDB-LAB-000914-1'
--  where feature_lab_of_origin = '';

--select feature_lab_of_origin
--  from feature
--  where not exists (select 'x'
--			from lab
--			where zdb_id = feature_lab_of_origin);

--!echo "missing features" ;
--select genofeat_feature_zdb_id from genotype_feature
--where genofeat_feature_zdb_id not in (select feature_zdb_id	
--					from feature);			

!echo "non-cloned_gene loci to feature as sequence_variants";

insert into marker (mrkr_zdb_id,
			mrkr_name,
			mrkr_type,
			mrkr_abbrev,
			mrkr_owner)
select zdb_id,
	locus_name,
	'GENE',
	lower(abbrev),
	owner
  from locus
  where cloned_gene is null 
  and locus_name not like 'Df%'
  and locus_name not like 'Tg%'
  and locus_name not like 'T(%';


!echo "transgenic constructs to marker" ;

insert into marker (mrkr_zdb_id,
			mrkr_name,
			mrkr_type,
			mrkr_abbrev,
			mrkr_owner,			
			mrkr_comments)

select zdb_id,
	locus_name,
	'TGCONSTRCT',
	lower(abbrev),
	owner,
	comments
  from locus
  where locus_name like 'Tg%' ;


update marker
  set mrkr_owner = null
  where mrkr_owner = '' ;

update marker
  set mrkr_owner = 'ZDB-PERS-980622-10'
  where not exists (select 'x'
			from person
			where zdb_id = mrkr_owner)
   and mrkr_owner is not null ;

update marker
  set mrkr_owner = 'ZDB-PERS-980622-10'
  where mrkr_owner is null ;

--provisional locus and alteration names
--these are the dups between locus,alteration,marker

select mrkr_name, count(*) as count
 from marker
 group by mrkr_name
 having count(*) >1 
  into temp tmp_name;

select mrkr_abbrev, count(*) as count
  from marker
  group by mrkr_abbrev
  having count(*) >1
  into temp tmp_abbrev;

update marker
  set mrkr_abbrev = mrkr_abbrev||"_"||lower(mrkr_zdb_id)
  where mrkr_abbrev in (select mrkr_abbrev 
  				from tmp_abbrev
  				) 
  and mrkr_zdb_id like 'ZDB-LOCUS-%';

update marker
  set mrkr_name = mrkr_name||"_"||mrkr_zdb_id
  where mrkr_name in (select mrkr_name 
  				from tmp_name
  				) 
  and mrkr_zdb_id like 'ZDB-LOCUS-%';

--now do the same for feature


select feature_name, count(*) as count
 from feature
 group by feature_name
 having count(*) >1 
  into temp tmp_ftr_name;

select feature_abbrev, count(*) as count
  from feature
  group by feature_abbrev
  having count(*) >1
  into temp tmp_ftr_abbrev;

update feature
  set feature_abbrev = feature_abbrev||"_"||feature_zdb_id
  where feature_abbrev in (select feature_abbrev 
  				from tmp_ftr_abbrev
  				) 
  and feature_zdb_id like 'ZDB-LOCUS-%';

update feature
  set feature_name = feature_name||"_"||feature_zdb_id
  where feature_name in (select feature_name 
  				from tmp_ftr_name) 
  and feature_zdb_id like 'ZDB-LOCUS-%';

select * from feature
where feature_name = 'Tg(rag2:EGFP-bcl2)zdf9' ;


--156 non identified Tg and Df lines
--467 
--623 total not in

insert into data_alias (dalias_zdb_id,
			 dalias_data_zdb_id,
		 	 dalias_alias,
			 dalias_group)
  select get_id('DALIAS'),
		(select mrkr_zdb_id
			from marker
			where cloned_gene=mrkr_zdb_id),
		locus_name,
		'alias'
	from locus
        where cloned_gene is not null 
        and cloned_gene != ''
	and not exists (select 'x'
			  from data_alias
				where dalias_data_zdb_id = cloned_gene
				and dalias_alias = locus_name);

insert into data_alias (dalias_zdb_id,
			 dalias_data_zdb_id,
		 	 dalias_alias,
			 dalias_group)
  select get_id('DALIAS'),
		(select mrkr_zdb_id
			from marker
			where cloned_gene=mrkr_zdb_id),
		abbrev,
		'alias'
	from locus
        where cloned_gene is not null 
        and cloned_gene != ''
	and not exists (select 'x'
			  from data_alias
				where dalias_data_zdb_id = cloned_gene
				and dalias_alias = abbrev);


insert into zdb_replaced_data (zrepld_old_zdb_id,
				zrepld_new_zdb_id,
				zrepld_old_name)
  select zdb_id,
	cloned_gene,
	locus_name
    from locus
    where cloned_gene is not null
     and cloned_gene != '';	

insert into zdb_active_data
  select dalias_zdb_id
   from data_alias
   where not exists (select 'x' 
			from zdb_active_data
			where zactvd_zdb_id = dalias_zdb_id);


!echo "this is the feature_marker_relationship add" ;

insert into feature_marker_relationship (fmrel_zdb_id,
    					fmrel_type,
    					fmrel_ftr_zdb_id,
    					fmrel_mrkr_zdb_id)
  select get_id('FMREL'), 
		'is allele of',
		alteration.zdb_id,
		locus.zdb_id
		from alteration,locus
		where locus.cloned_gene is null
		and alteration.locus = locus.zdb_id
		and locus_name not like 'Df%'
		and locus_name not like 'Tg%'
		and locus_name not like 'T(%';

insert into feature_marker_relationship (fmrel_zdb_id,
    					fmrel_type,
    					fmrel_ftr_zdb_id,
    					fmrel_mrkr_zdb_id)
  select get_id('FMREL'), 
		'is allele of',
		alteration.zdb_id,
		mrkr_zdb_id
		from alteration,locus,marker
		where locus.cloned_gene =mrkr_zdb_id
		and alteration.locus = locus.zdb_id
		and locus_name not like 'Df%'
		and locus_name not like 'Tg%'
		and locus_name not like 'T(%';

insert into feature_marker_relationship (fmrel_zdb_id,
    					fmrel_type,
    					fmrel_ftr_zdb_id,
    					fmrel_mrkr_zdb_id)
  select get_id('FMREL'), 
		'contains sequence feature',
		alteration.zdb_id,
		locus.zdb_id
		from alteration,locus
		where alteration.locus = locus.zdb_id
		and locus_name like 'Tg%';


insert into zdb_active_data 
  select fmrel_zdb_id from feature_marker_relationship
   where not exists (select 'x'
			from zdb_active_data
			where zactvd_zdb_id = fmrel_zdb_id);

insert into zdb_active_data 
  select zyg_zdb_id from zygocity;

insert into zdb_active_data
  select genofeat_zdb_id from genotype_feature ;

!echo "GENOTYPE EXPERIMENT then figure"

insert into genotype_experiment (genox_zdb_id,
				genox_geno_zdb_id,
				genox_exp_zdb_id)
  select featexp_zdb_id, 
	featexp_genome_feature_zdb_id,
	featexp_exp_Zdb_id 
    from feature_experiment ;


insert into genotype_experiment (genox_zdb_id,
					genox_geno_zdb_id,
					genox_exp_zdb_id)
  select get_id('GENOX'), 
	fish.zdb_id,
	(select exp_zdb_id
		from experiment
		where exp_name = '_Standard')
    from fish 
    where not exists (select 'x'
			from feature_experiment, experiment 
			where featexp_genome_feature_zdb_id = fish.zdb_id
			and exp_zdb_id = featexp_exp_zdb_id
			and exp_name = '_Standard');

--select distinct genox_geno_zdb_id, name from genotype_experiment, fish
--  where not exists (select 'x' from genotype
--			where geno_zdb_id = genox_geno_zdb_id)
--  and fish.zdb_id = genox_geno_zdb_id;


--insert into genotype_experiment_figure (genoxfig_genox_zdb_id,
--					genoxfig_fig_zdb_id)
--  select genox_zdb_id, fig_zdb_id
--    from genotype_experiment, figure, experiment
--    where genox_geno_zdb_id = fig_label 
--    and genox_exp_zdb_id = exp_zdb_id
--    and exp_name = '_Standard';

--get the ones that haven't been entered already? 


insert into zdb_active_data
  select genox_zdb_id
    from genotype_experiment
    where not exists (select 'x'
			from zdb_active_data
			where zactvd_zdb_id = genox_zdb_id);


create temp table tmp_tt (keyword varchar(100), 
                               stage varchar(50),
                               entity_group varchar(100),
                               entity_group_zdb_id varchar(50),
                               quality varchar(50),
			       quality_pato_id varchar(40),
                               tag varchar(25))
  with no log;

create index tmp_tt_keyword_index
 on tmp_tt(keyword) using btree in idxdbs4;

create index tmp_tt_egi_index
 on tmp_tt(entity_group_zdb_id) using btree in idxdbs4;

create index tmp_tt_eg_index
 on tmp_tt(entity_group) using btree in idxdbs4;

create index tmp_tt_quality_index
 on tmp_tt(quality) using btree in idxdbs4;


!echo "start phenotype changes" ;

load from phenoTabbed 
  insert into tmp_tt;

delete from tmp_tt 
  where entity_group_zdb_id = '<none>';

!echo "null entity removed from tmp_tt" ;

delete from tmp_tt 
	where entity_group_zdb_id is null;

update tmp_tt
  set stage = null 
  where stage = '<none>' ;

update tmp_tt
  set entity_group_zdb_id = null 
  where entity_group_zdb_id = '<none>' ;

update tmp_tt
 set entity_group = null 
  where entity_group = '<none>' ;

update tmp_tt
  set quality = null 
  where quality = '<none>' ;

update tmp_tt
  set quality = null 
  where quality = '' ;

update tmp_tt
  set tag = null 
  where tag = '<none>' ;

update tmp_tt
  set stage = null 
  where stage = '<none>' ;

update tmp_tt
  set stage = null
  where stage = 'unspecified' ;

update tmp_tt
  set stage = (select stg_zdb_id
		from stage
		where stg_name = 'Adult')
  where stage = 'Adult' ;

!echo "add stage unknown" ;

update tmp_tt
  set stage = (select stg_zdb_id
		from stage
		where stg_name = 'Unknown')
  where (stage is null 
		or stage = 'Unknown');


update tmp_tt
  set entity_group_zdb_id = (select goterm_zdb_id
			from go_term
			where "GO:"||goterm_go_id = entity_group_zdb_id)
  where entity_group_zdb_id like 'GO:%' 
  and exists (select 'x'
			from go_term
			where "GO:"||goterm_go_id = entity_group_zdb_id);

update tmp_tt
  set entity_group_zdb_id = (select zrepld_new_zdb_id
			from zdb_replaced_data
			where zrepld_old_Zdb_id = entity_group_zdb_id)
  where entity_group_zdb_id like 'ZDB-ANAT-%'
  and exists (select 'x'
			from zdb_replaced_data
			where zrepld_old_Zdb_id = entity_group_zdb_id);

select * from tmp_tt
  where entity_group_zdb_id like 'GO:%' ;

update tmp_tt
  set keyword = 'melanophore_e'
  where keyword = 'melanophore and eye pigment'
  and entity_group = 'melanophores';

update tmp_tt
  set keyword = 'melanophore_new_e'
  where keyword = 'melanophore and eye pigment'
  and entity_group = 'pigmented epithelium';

update tmp_tt
  set keyword = 'melanophore_i'
  where keyword = 'melanophores and iridophores'
  and entity_group = 'melanophores ';

update tmp_tt
  set keyword = 'melanophore_new_i'
  where keyword = 'melanophores and iridophores'
  and entity_group = 'iridophores';

update tmp_tt
  set keyword = 'melanophore_x'
  where keyword = 'melanophores and xanthophores'
  and entity_group = 'melanophores ';

update tmp_tt
  set keyword = 'melanophore_new_x'
  where keyword = 'melanophores and xanthophores'
  and entity_group = 'xanthophores ';

update tmp_tt
  set keyword = 'body shape and tail T'
  where keyword = 'body shape and tail'
  and entity_group = 'whole organism';

update tmp_tt
  set keyword = 'body shape and tail new T'
  where keyword = 'body shape and tail'
  and entity_group = 'tail';


update tmp_tt
  set keyword = 'jaw and melanophores J'
  where keyword = 'jaw and melanophores'
  and entity_group = 'pharyngeal arch 1 skeletal system';

update tmp_tt
  set keyword = 'jaw and melanophores new J'
  where keyword = 'jaw and melanophores'
  and entity_group = 'melanophores';

update tmp_tt
  set keyword = 'notochord and somites NS'
  where keyword = 'notochord and somites'
  and entity_group = 'notochord';

update tmp_tt
  set keyword = 'notochord and somites new NS'
  where keyword = 'notochord and somites'
  and entity_group = 'somite';

update tmp_tt
  set keyword = 'small eyes and small hyoid S'
  where keyword = 'small eyes and small hyoid'
  and entity_group = 'eye';

update tmp_tt
  set keyword = 'small eyes and small hyoid new S'
  where keyword = 'small eyes and small hyoid'
  and entity_group = 'pharyngeal arch 2 skeleton';

update tmp_tt
  set keyword = 'xanthophores and iridophores xi'
  where keyword = 'xanthophores and iridophores'
  and entity_group = 'xanthophores';

update tmp_tt
  set keyword = 'xanthophores and iridophores new xi'
  where keyword = 'xanthophores and iridophores'
  and entity_group = 'iridophores';

select distinct quality 
  from tmp_tt
  into temp tmp_attr ;

select distinct tag
  from tmp_tt
  into temp tmp_tag ;

--insert into term (term_zdb_id,
--    			term_name,
--    			term_ontology)
--select get_id('TERM'),
--	quality,
--	'quality'
--  from tmp_attr 
--  where not exists (select 'x'
--			from term where
--			term_name = quality);

insert into term (term_zdb_id,
    			term_name,
    			term_ontology)
values ( get_id('TERM'),
	'unspecified',
	'quality') ;

update term
  set term_ont_id = term_zdb_id 
  where term_ont_id is null;

!echo "count of null term_ont_id" ;

select count(*)
  from term
  where term_ont_id like 'ZDB-TERM-%' ;

insert into zdb_active_data
  select term_zdb_id
    from term
  where not exists (select 'x'
			from zdb_active_data
			where term_zdb_id = zactvd_zdb_id);

create table tmp_apato (apato_geno_zdb_id varchar(50),
				apato_apatoeg_zdb_id varchar(50))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 1024 next size 1024
lock mode row;

load from pheno_keywords_parsed
  insert into tmp_apato ;

!echo "null entity removal from pheno_keywords_parsed" ;

delete from tmp_apato
  where apato_apatoeg_zdb_id is null ;

delete from tmp_apato
  where apato_apatoeg_zdb_id = '';

create table tmp_eu_apato (euapato_geno_zdb_id varchar(50),
				euapato_stage_name varchar(35),
				euapato_entity_name varchar(50),
				euapato_entity_id varchar(40),
				euapato_quality varchar(40),
				euapato_tag varchar(25))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 64  next size 64
lock mode row;

load from EUKeywordNew_v1.csv
 insert into tmp_eu_apato ;

update tmp_eu_apato
  set euapato_entity_name = 'nasal bone'
  where euapato_entity_name = 'nasal bones' ;

update tmp_eu_apato
  set euapato_entity_name = 'median fin skeleton'
  where euapato_entity_name = 'fin skeletal system' ;

update tmp_eu_apato
  set euapato_entity_name = 'virion penetration into host cell'
  where euapato_entity_name = 'virion penetration' ;

create index entity_name_index
  on tmp_eu_apato (euapato_entity_name);

update statistics high for table tmp_eu_apato ;

!echo "TEETH";


create temp table tmp_new_term (name varchar(100))
 with no log ;

insert into tmp_new_term
  select distinct euapato_quality
    from tmp_eu_apato 
    where not exists (select term_name
                        from term
			where term_name = euapato_quality);

update term
  set term_ont_id = term_zdb_id
  where term_ont_id = 'term_id' ;

drop table tmp_new_term ;

update statistics high for table term ;

update tmp_eu_apato
  set euapato_stage_name = 'unk'
  where euapato_stage_name = 'unknown' ;

--select euapato_stage_name, euapato_geno_zdb_id from tmp_eu_apato
--  where not exists (Select 'x' from stage
--			where euapato_stage_name = stg_abbrev);


!echo "number of unknown entities from EU data" ;

update tmp_eu_apato
 set euapato_entity_id = (Select anatitem_zdb_id
			   from anatomy_item
			   where anatitem_name = 'macrophage')
  where euapato_entity_name = 'macrophages' ;

update tmp_eu_apato
 set euapato_entity_id = (Select anatitem_zdb_id
			   from anatomy_item
			   where anatitem_name = 'ceratobranchial 5 tooth')
  where euapato_entity_name = 'teeth' ;

select euapato_entity_name, euapato_geno_zdb_id from tmp_eu_apato
  where not exists (select 'x'
			from anatomy_item
			where anatitem_name = euapato_entity_name)
  and not exists (select 'x'
			from go_term
			where goterm_name = euapato_entity_name)
  and euapato_entity_id not like 'ZDB-%'
 into temp tmp_missing_entities;

unload to euapato_missing_entities 
  select * from tmp_missing_entities;


!echo "number of unkown entities updated from EU data updates";

update tmp_eu_apato
 set euapato_entity_name = 'unspecified'
 where exists (select 'x' from tmp_missing_entities
			where tmp_missing_entities.euapato_entity_name =
				tmp_eu_apato.euapato_entity_name);

update tmp_eu_apato
  set euapato_entity_id = (select goterm_zdb_id
				from go_term
				where goterm_name = euapato_entity_name)
  where euapato_entity_id like 'GO:%' ;			

alter table tmp_eu_apato
  add (euapato_stage_id varchar(50));

update tmp_eu_apato
  set euapato_stage_id = (select stg_zdb_id
				from stage
				where stg_abbrev = euapato_stage_name) ;

--select count(*) from tmp_eu_apato 
--  where euapato_entity_id is null;

delete from tmp_eu_apato
  where euapato_entity_id is null ;

--deal with non-eu data

!echo "null entity removal from tmp_apato again";

delete from tmp_apato where apato_apatoeg_zdb_id is null;

--remove eu data from apato table

delete from tmp_apato where apato_geno_zdb_id like 'ZDB-FISH-060608-%' ;

delete from tmp_apato where apato_apatoeg_zdb_id like '%:%' ;

create table tmp_apato_full (apato_zdb_id varchar(50),
				apato_geno_zdb_id varchar(50),
				apato_genox_zdb_id varchar(50),
				apato_stage_zdb_id varchar(50),
				apato_apatoeg_zdb_id varchar(50),
				apato_quality varchar(50),
				apato_tag varchar(25)
				)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 1024 next size 1024 ;


!echo "here it is" ;

insert into tmp_apato_full (apato_geno_zdb_id,
				apato_apatoeg_zdb_id)
  select
	apato_geno_zdb_id,
	apato_apatoeg_zdb_id 
    from tmp_apato ;

!echo "non-eu" ;


insert into tmp_apato_full (apato_geno_zdb_id, 
				apato_stage_zdb_id,
				apato_apatoeg_zdb_id,
				apato_quality,
				apato_tag)
select euapato_geno_zdb_id,
	euapato_stage_name,
	euapato_entity_id,
	euapato_quality,
	euapato_tag
  from tmp_eu_apato ;


!echo "eu null entity groups" ;

--select count(*) from tmp_eu_apato where euapato_entity_id is null;

update tmp_apato_full
  set apato_genox_zdb_id = (select genox_zdb_id
				from genotype_experiment
				where genox_geno_zdb_id = apato_geno_zdb_id
				and genox_exp_zdb_id = (select exp_zdb_id
							  from experiment
							  where exp_name = "_Standard"));
                          
--FIX DUPS!7
update tmp_apato_full
  set apato_apatoeg_zdb_id = 'melanophore pattern / number'
  where apato_apatoeg_zdb_id = 'melanophore pattern/number' ;

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'pigmented retinal epithelium'
  where apato_apatoeg_zdb_id = 'retinal pigment epithelium' ;

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'melanophore shape / size'
  where apato_apatoeg_zdb_id = 'melanophore shape/size' ;

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'melanophore_e'
  where apato_apatoeg_zdb_id = 'melanophore and eye pigment';

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'melanophore_i'
  where apato_apatoeg_zdb_id = 'melanophores and iridophores';

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'melanophore_x'
  where apato_apatoeg_zdb_id = 'melanophores and xanthophores';

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'body shape and tail T'
  where apato_apatoeg_zdb_id = 'body shape and tail';

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'jaw and melanophores J'
  where apato_apatoeg_zdb_id = 'jaw and melanophores';

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'notochord and somites NS'
  where apato_apatoeg_zdb_id = 'notochord and somites';

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'small eyes and small hyoid S'
  where apato_apatoeg_zdb_id = 'small eyes and small hyoid';

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'xanthophores and iridophores xi'
  where apato_apatoeg_zdb_id = 'xanthophores and iridophores';

--update tmp_apato_full
--  set apato_apatoeg_zdb_id = 'brain; cns and pns'
--  where apato_apatoeg_zdb_id = 'brain, cns and pns';
--update tmp_apato_full
--  set apato_apatoeg_zdb_id = 'xanthophores; lethal'
--  where apato_apatoeg_zdb_id = 'xanthophores, lethal';

--update tmp_apato_full
--  set apato_apatoeg_zdb_id = 'xanthophores; viable'
--  where apato_apatoeg_zdb_id = 'xanthophores, viable';

update tmp_apato_full
  set apato_apatoeg_zdb_id = 'macrophage'
  where apato_apatoeg_zdb_id = 'macrophages';

insert into tmp_apato_full (apato_zdb_id,
				apato_geno_zdb_id,
				apato_apatoeg_zdb_id)
select get_id('APATO'),
	apato_geno_zdb_id,
	'melanophore_new_e'
  from tmp_apato 
  where apato_apatoeg_zdb_id = 'melanophore and eye pigment';


insert into tmp_apato_full (apato_zdb_id,
				apato_geno_zdb_id,
				apato_apatoeg_zdb_id)
select get_id('APATO'),
	apato_geno_zdb_id,
	'melanophore_new_i'
  from tmp_apato 
  where apato_apatoeg_zdb_id = 'melanophores and iridophores';


insert into tmp_apato_full (apato_zdb_id,
				apato_geno_zdb_id,
				apato_apatoeg_zdb_id)
select get_id('APATO'),
	apato_geno_zdb_id,
	'melanophore_new_x'
  from tmp_apato 
  where apato_apatoeg_zdb_id = 'melanophores and xanthophores';


insert into tmp_apato_full (apato_zdb_id,
				apato_geno_zdb_id,
				apato_apatoeg_zdb_id)
select get_id('APATO'),
	apato_geno_zdb_id,
	'body shape and tail new T'
  from tmp_apato 
  where apato_apatoeg_zdb_id = 'body shape and tail';


insert into tmp_apato_full (apato_zdb_id,
				apato_geno_zdb_id,
				apato_apatoeg_zdb_id)
select get_id('APATO'),
	apato_geno_zdb_id,
	'xanthophores and iridophores new xi'
  from tmp_apato 
  where apato_apatoeg_zdb_id = 'xanthophores and iridophores';

insert into tmp_apato_full (apato_zdb_id,
				apato_geno_zdb_id,
				apato_apatoeg_zdb_id)
select get_id('APATO'),
	apato_geno_zdb_id,
	'small eyes and small hyoid new S'
  from tmp_apato 
  where apato_apatoeg_zdb_id = 'small eyes and small hyoid';

insert into tmp_apato_full (apato_zdb_id,
				apato_geno_zdb_id,
				apato_apatoeg_zdb_id)
select get_id('APATO'),
	apato_geno_zdb_id,
	'notochord and somites new NS'
  from tmp_apato 
  where apato_apatoeg_zdb_id = 'notochord and somites';

create index tapt_apatoeg_index
 on tmp_apato_full (apato_apatoeg_zdb_id)
 using btree in idxdbs4 ;

create index tapt_quality_index
 on tmp_apato_full (apato_quality)
 using btree in idxdbs4 ;

update statistics high for table tmp_tt ;
update statistics high for table tmp_apato_full ;

!echo "translate keywords into pato names using tt file";

update tmp_apato_full
  set apato_quality = (select quality
			  from tmp_tt
			  where keyword = apato_apatoeg_zdb_id)
  where exists (select 'x' 
		  from tmp_tt
		  where keyword = apato_apatoeg_zdb_id);

--select first 1 * from tmp_apato_full
--  where apato_quality is null ;

--select distinct apato_apatoeg_zdb_id
--  from tmp_apato_full
--  where apato_quality is null ;

--select distinct apato_quality
--  from tmp_apato_full
--  where not exists (select 'x'
--			from tmp_tt
--			where keyword = apato_apatoeg_zdb_id) ;


create temp table tmp_new_term (name varchar(100))
 with no log ;

insert into tmp_new_term
  select distinct apato_quality
    from tmp_apato_full
     where not exists (select 'x'
			from tmp_tt
			where keyword = apato_apatoeg_zdb_id) 
     and apato_quality is not null 
    and not exists (select 'x' from term
			where term_name = apato_quality);

--insert into term (term_zdb_id,
--			term_ont_id,
--			term_name,
--			term_ontology)
--  select get_id('TERM'),
--	'term_id',
--	name,
--	'quality'
--    from tmp_new_term 
--	where not exists (select term_name
--                             from term
--			     where term_name = name);

update term
  set term_ont_id = term_zdb_id
  where term_ont_id = 'term_id' ;

unload to terms_need_tt_file.unl
  select * from term
where term_ont_id like 'ZDB-TERM-%' ;

update tmp_apato_full
  set apato_stage_zdb_id = (select stage 
			from tmp_tt
			where keyword = apato_apatoeg_zdb_id);

--select distinct tag from tmp_tt ;

update tmp_apato_full 
  set apato_tag = (select tag 
			from tmp_tt
			where keyword = apato_apatoeg_zdb_id);

update statistics high for table tmp_apato_full;



update tmp_apato_full 
  set apato_apatoeg_zdb_id = (select entity_group_zdb_id 
				from tmp_tt
				where keyword = apato_apatoeg_zdb_id)
  where exists (select 'x'
			from tmp_tt
			where keyword = apato_apatoeg_zdb_id);


--select count(*), term_name
--  from term
--  group by term_name
--  having count(*) > 1 ;

!echo "10K updated with qualities from term!" ;

select count(*), term_name
  from term
  group by term_name
  having count(*) > 1;

update tmp_apato_full
  set apato_quality = (select term_zdb_id
			  from term
			  where apato_quality = term_name
			  and term_is_obsolete = 'f')
  where exists (select 'x' from term
			  where apato_quality = term_name);

-- changed "unspecified" to quality on 11/2/2006 to reduce
-- number of ZFIN-only ontology terms.

update tmp_apato_full
  set apato_quality = (select term_zdb_id
			  from term
			  where term_name = 'quality'
			  and term_is_obsolete = 'f')
  where apato_quality is null ;

update tmp_apato_full
  set apato_quality = (select term_zdb_id
			  from term
			  where term_name = 'quality'
			  and term_is_obsolete = 'f')
  where apato_quality = 'unspecified' 
  or exists (Select 'x'
	       from term
	       where term_name = 'unspecified'
	       and term_Zdb_id = apato_quality);


!echo "update goids with zdbs for patoannots" ;

--select count(*)
--  from tmp_apato_full 
--  where not exists (select 'x' from term
--			  where apato_quality = term_name);


--update tmp_apato_full
--  set apato_tag = (select term_zdb_id
--			  from term
--			  where apato_tag = term_name)
--  where exists (select 'x' from term
--			  where apato_tag = term_name);

update statistics high for table tmp_apato_full ;

update tmp_apato_full
  set apato_apatoeg_zdb_id = (select anatitem_zdb_id
				from anatomy_item
				where anatitem_name = apato_apatoeg_zdb_id)
  where apato_apatoeg_zdb_id not like 'GO:%'
  and apato_apatoeg_zdb_id not like 'ZDB-GOTERM-%'
  and apato_apatoeg_zdb_id not like 'ZDB-ANAT-%'
  and exists (select 'x'
		from anatomy_item
		where anatitem_name = apato_apatoeg_zdb_id);

update tmp_apato_full
  set apato_apatoeg_zdb_id = (select dalias_data_zdb_id
				from data_alias
				where dalias_alias = apato_apatoeg_zdb_id)
  where apato_apatoeg_zdb_id not like 'GO:%'
  and apato_apatoeg_zdb_id not like 'ZDB-GOTERM-%'
  and apato_apatoeg_zdb_id not like 'ZDB-ANAT-%'
  and exists (select 'x'
		from data_alias
		where dalias_alias = apato_apatoeg_zdb_id);


!echo "null entity count"
Select count(*) from tmp_apato_full
  where apato_apatoeg_zdb_id is null ;

update statistics high for table tmp_apato_full ;

update statistics high for table zdb_replaced_data ;

update tmp_apato_full
  set apato_apatoeg_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = apato_apatoeg_zdb_id)
  where apato_apatoeg_zdb_id like 'ZDB-ANAT-%'
  and exists (select 'x'
		from zdb_replaced_data
		where zrepld_old_zdb_id = apato_apatoeg_zdb_id)
  and apato_apatoeg_zdb_id like 'ZDB-ANAT-%';


delete from tmp_apato_full
where apato_apatoeg_zdb_id in ('developmental stage',
				'early phenotypes',
				'edema',
				'epiboly',
				'necrosis');

--unload to missing_entities.unl
--select distinct apato_apatoeg_zdb_id from tmp_apato_full
--where not exists (select 'x'
--			from anatomy_item
--			where anatitem_zdb_id = apato_apatoeg_zdb_id)
-- and apato_apatoeg_zdb_id not like 'GO:%'
--  and apato_apatoeg_zdb_id not like 'ZDB-GOTERM-%'
--  order by apato_apatoeg_zdb_id;

--ZDB-ANAT-011113-37
--tectum vs 

--select term_zdb_id from term
--			where term_name = 'qualitative'
--			and term_ontology = 'quality' ;

update tmp_apato_full
  set apato_quality = (select term_zdb_id from term
			where term_name = apato_quality
			and term_ontology = 'quality'
			and term_is_obsolete = 'f')
  where apato_quality is null 
  and apato_apatoeg_zdb_id != 'dominant';

unload to distinct_missing_qualities
  select distinct apato_quality from tmp_apato_full
    where apato_quality not like 'ZDB-TERM-%' ;

update tmp_apato_full
  set apato_tag = 'abnormal'
  where apato_tag is null 
  and apato_apatoeg_zdb_id != 'dominant';

select distinct apato_genox_zdb_id
  from tmp_apato_full
  where apato_apatoeg_zdb_id = 'dominant'
  into temp tmp_dom ;

create index apato_genox_index
 on tmp_dom (apato_genox_zdb_id)
 using btree in idxdbs4;

create index apato_full_index
 on tmp_apato_full (apato_genox_zdb_id)
 using btree in idxdbs4 ;

update statistics for table tmp_apato_full;

update statistics for table tmp_dom ;

--update tmp_apato_full
--  set apato_context = 'dominant'
--  where exists (select 'x'
--		 from tmp_dom
--		  where tmp_dom.apato_genox_zdb_id = 
--			tmp_apato_full.apato_genox_zdb_id);

--update tmp_apato_full
--  set apato_context = (select term_zdb_id
--			from term
--			where term_name = 'dominant')
--  where apato_context = 'dominant' ;

delete from tmp_apato_full
  where apato_apatoeg_zdb_id = 'dominant';

update tmp_apato_full
  set apato_genox_zdb_id = (select genox_zdb_id
				from genotype_experiment
				where genox_geno_zdb_id = apato_geno_zdb_id
				and genox_exp_zdb_id = 
					(select exp_zdb_id
						from experiment
						where exp_name = "_Standard"))
  where apato_genox_zdb_id is null;


create table tmp_apato_full_no_dups (apatod_zdb_id varchar(50),
				apatod_geno_zdb_id varchar(50),
				apatod_genox_zdb_id varchar(50),
				apatod_stage_zdb_id varchar(50),
				apatod_entity_group_zdb_id varchar(50),
				apatod_quality varchar(50),
				apatod_tag varchar(25)
				)
in tbldbs1 ;

!echo "distinct" ;

insert into tmp_apato_full_no_dups (apatod_geno_zdb_id,
				apatod_genox_zdb_id,
				apatod_stage_zdb_id,
				apatod_entity_group_zdb_id,
				apatod_quality,
				apatod_tag)
 select distinct apato_geno_zdb_id,
			apato_genox_zdb_id,
			apato_stage_zdb_id,
			apato_apatoeg_zdb_id,
			apato_quality,
			apato_tag
   from tmp_apato_full ;

select * from tmp_apato_full_no_dups
  where apatod_genox_zdb_id = 'ZDB-FEATEXP-041102-2586'
  and apatod_quality = 'ZDB-TERM-061103-2' ;

delete from tmp_apato_full_no_dups
  where apatod_genox_zdb_id = 'ZDB-FEATEXP-041102-2586'
  and apatod_entity_group_zdb_id = 'ZDB-ANAT-010921-532'
  and apatod_stage_zdb_id is null ;

delete from tmp_apato_full_no_dups
  where apatod_genox_zdb_id = 'ZDB-FEATEXP-041102-2099'
  and apatod_entity_group_zdb_id = 'ZDB-ANAT-010921-575'
  and apatod_stage_zdb_id is null ;

--echo "delete the EU mutants for now...come back to this."
--echo "should delete 656 records or zero if already deleted above" ;

--delete from tmp_apato_full_no_dups
--  where exists (Select 'x' from genotype_experiment
--		  where apatod_genox_zdb_id = genox_zdb_id
--		  and genox_geno_zdb_id like 'ZDB-FISH-060608-%');

--update tmp_apato_full_no_dups
--  set apatod_context = 'unspecified'
--  where apatod_context is null ;

update tmp_apato_full_no_dups
  set apatod_zdb_id = get_id('APATO');

!echo "number with missing entity groups" ;

update tmp_apato_full_no_dups
  set apatod_entity_group_zdb_id = 'ZDB-ANAT-010921-574'
  where apatod_entity_group_zdb_id = 'photoreceptor cell layer' ;

update tmp_apato_full_no_dups
  set apatod_entity_group_zdb_id = (Select anatitem_zdb_id
					from anatomy_item
					where anatitem_name = 'pharyngeal arch 1 skeletal system')
  where apatod_entity_group_zdb_id = 'jaw and melanophores J' ;

update tmp_apato_full_no_dups
  set apatod_stage_zdb_id = 'ZDB-STAGE-050211-1'
  where apatod_stage_zdb_id is null ;

select distinct apatod_entity_group_zdb_id
  from tmp_apato_full_no_dups
  where apatod_entity_group_zdb_id not like 'ZDB-%' ;

update tmp_apato_full_no_dups
  set apatod_entity_group_zdb_id = replace(apatod_entity_group_zdb_id," (abnormal)",'') 
  where apatod_entity_group_zdb_id like '%(abnormal)%' ;

update tmp_apato_full_no_dups
  set apatod_entity_group_zdb_id = (select goterm_zdb_id
					from go_term
					where goterm_name = 
						apatod_entity_group_zdb_id)
  where apatod_entity_group_zdb_id not like 'ZDB-%' 
  and exists (select 'x' 
		from go_term
		where goterm_name =apatod_entity_group_zdb_id) ; 

!echo "count of null apato entities" ;

select distinct apatod_entity_group_zdb_id
  from tmp_apato_full_no_dups
 where apatod_entity_group_zdb_id not like 'ZDB-%';

select count(*) from tmp_apato_full_no_dups
  where apatod_entity_group_zdb_id is null ;

!echo "FIX ME WITH TT FILE IF NOT ZERO" ;

--update tmp_apato_full_no_dups
--  set apatod_quality = (select term_zdb_id
--			  from term
--			  where term_name = 'dwarf')
--  where apatod_entity_group_zdb_id is null ;

update tmp_apato_full_no_dups
  set apatod_entity_group_zdb_id = (select anatitem_zdb_id
					from anatomy_item	
					where anatitem_name = 
							'whole organism')
  where apatod_entity_group_zdb_id is null;

--!echo "number with null qualities" ;

--update tmp_apato_full_no_dups
--  set apatod_quality = (select term_zdb_id
--					from term	
--					where term_name = 
--							'quality'
--					and term_is_obsolete = 'f')
--  where apatod_quality is null;

--update tmp_apato_full_no_dups
--  set apatod_stage_zdb_id = (select stg_zdb_id
--					from stage	
--					where stg_name = 
--							'Unknown')
 -- where apatod_stage_Zdb_id is null;


--update tmp_apato_full_no_dups
--  set apatod_tag = 'abnormal'
--  where apatod_tag is null;


--update tmp_apato_full_no_dups
--  set apatod_quality = (select term_Zdb_id
--			  from term
--			  where apatod_quality = term_name
--				and term_is_obsolete = 'f')
--  where exists (Select 'x'
--		  from term
--		  where term_name = apatod_quality)
--  and apatod_quality not like 'ZDB-TERM-%';

!echo "still no term for these" ;

select distinct apatod_quality from tmp_apato_full_no_dups
where apatod_quality not like 'ZDB-%';

delete from tmp_apato_full_no_dups
where apatod_quality = '<delete>';

drop table tmp_new_term ;

create temp table tmp_new_term (name varchar(100))
 with no log ;

insert into tmp_new_term
  select distinct apatod_quality
    from tmp_apato_full_no_dups
     where not exists (select 'x' from term
			where term_name = apatod_quality);

insert into term (term_zdb_id,
			term_ont_id,
			term_name,
			term_ontology)
  select get_id('TERM'),
	'term_id',
	name,
	'quality'
    from tmp_new_term 
	where not exists (select term_name
                             from term
			     where term_name = name);

delete from term
  where term_name like 'ZDB-%' ;

update term
  set term_ont_id = term_zdb_id
  where term_ont_id = 'term_id' ;

unload to terms_need_tt_file.unl
  select * from term
where term_ont_id like 'ZDB-TERM-%' ;


update tmp_apato_full_no_dups
  set apatod_quality = (select term_Zdb_id
			  from term
			  where apatod_quality = term_name
			  and term_is_obsolete = 'f')
  where exists (Select 'x'
		  from term
		  where term_name = apatod_quality)
  and apatod_quality not like 'ZDB-TERM-%';

!echo "HERE IS THE DUP CHECK" ;

--select * from genotype_experiment
--  where genox_zdb_id = 'ZDB-GENOX-060719-965' ;
 
select count(*),
	apatod_genox_zdb_id,
	apatod_stage_zdb_id,
	apatod_stage_zdb_id,
	apatod_entity_group_zdb_id,
	apatod_quality, term_name,
	apatod_tag
  from tmp_apato_full_no_dups, term
  where term_zdb_id = apatod_quality 
  group by apatod_genox_zdb_id,
	apatod_stage_zdb_id,
	apatod_stage_zdb_id,
	apatod_entity_group_zdb_id,
	apatod_quality, term_name,
	apatod_tag
  having count(*) > 1;

insert into atomic_phenotype (apato_zdb_id ,
    apato_genox_zdb_id , 
    apato_start_stg_zdb_id ,
    apato_end_stg_zdb_id ,
    apato_entity_a_zdb_id ,
    apato_quality_zdb_id ,
    apato_tag)
select apatod_zdb_id, 
	apatod_genox_zdb_id,
	apatod_stage_zdb_id,
	apatod_stage_zdb_id,
	apatod_entity_group_zdb_id,
	apatod_quality,
	apatod_tag
  from tmp_apato_full_no_dups ;

--select * from atomic_phenotype, genotype_experiment
-- where genox_zdb_id = apato_genox_zdb_id
--  and genox_geno_zdb_id = 'ZDB-FISH-980410-94';

update statistics high for table apato_infrastructure;

update statistics high for table genotype_experiment ;
update statistics high for table atomic_phenotype ;

update atomic_phenotype
  set apato_pub_zdb_id = 'ZDB-PUB-030129-1' 
  where exists (select 'x' from genotype_experiment
                  where genox_zdb_id = apato_genox_zdb_id
		  and genox_geno_zdb_id like 'ZDB-FISH-060608-%') ;

update atomic_phenotype
  set apato_pub_zdb_id = 'ZDB-PUB-060503-2' 
  where apato_pub_zdb_id is null ;


--insert into apato_figure, ao ones

!echo "HOW MANY NEW FIGURES IN APATO_FIGURE" ;

insert into apato_figure (apatofig_fig_zdb_id,
				apatofig_apato_zdb_id)
  select fig_zdb_id, apato_zdb_id
    from figure, genotype_experiment, atomic_phenotype
    where fig_label = genox_geno_zdb_id
    and genox_zdb_id = apato_genox_zdb_id ;

update figure
  set fig_label = (select "Fig. for ("||fish.allele||")"
			from fish
			where fig_label = fish.zdb_id)
  where fig_label like 'ZDB-FISH-%' ;


select count(*) from apato_figure ;

update statistics high for table apato_figure ;
update statistics high for table atomic_phenotype ;

--select count(*) 
--  from atomic_phenotype
--  where not exists (select 'x'
--			from apato_figure
--			where apatofig_apato_zdb_id = apato_zdb_id);

!echo "done with pato, now insert into feature_assay" ;

insert into feature_assay (featassay_feature_zdb_id,
				featassay_mutagen,
				featassay_mutagee)
  select alteration.zdb_id,
	mutagen, 
	protocol
	from alteration ;

update feature_assay
  set featassay_mutagen = null
  where featassay_mutagen = '' ;

update feature_assay
  set featassay_mutagee = null
  where featassay_mutagee = '' ;

insert into zdb_active_data
  select apato_zdb_id
    from atomic_phenotype ;

insert into zdb_active_data 
  select geno_zdb_id
    from genotype
    where not exists (select 'x'
			from zdb_active_data
			where geno_zdb_id = zactvd_zdb_id) ;

update statistics high for table atomic_phenotype ;

update atomic_phenotype
  set apato_entity_a_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = apato_entity_a_zdb_id
  				and zrepld_old_zdb_id like 'ZDB-ANAT-%')
  where exists (select 'x'
		from zdb_replaced_Data
 		where zrepld_old_zdb_id = apato_entity_a_zdb_id)
  and apato_entity_a_zdb_id like 'ZDB-ANAT-%';

update atomic_phenotype
  set apato_tag = 'abnormal'
  where apato_tag is null ;

update statistics high for table atomic_phenotype ;
update statistics high for table zdb_active_data ;

update statistics for procedure ;

insert into zdb_active_data
   select apato_zdb_id
     from atomic_phenotype
     where not exists (select 'x'
			 from zdb_active_data
 			where zactvd_zdb_id = apato_zdb_id);


insert into zdb_active_data
  select term_zdb_id from term
   where not exists (select 'x' from zdb_active_data
			where zactvd_zdb_id = term_zdb_id);

update statistics for procedure ;

select apato_entity_a_zdb_id
  from atomic_phenotype
  where apato_entity_a_zdb_id not like 'ZDB-ANAT-%'
	and apato_entity_a_zdb_id not like 'ZDB-GOTERM-%';


!echo "number qualities updated to ventralized" ;

update atomic_phenotype
  set apato_quality_zdb_id = (Select term_zdb_id
			from term
			where term_name = 'ventralized')
  where apato_entity_a_zdb_id not like 'ZDB-%';

update atomic_phenotype
  set apato_entity_a_zdb_id = (select dalias_data_zdb_id
					from data_alias	
					where dalias_alias = 
						apato_entity_a_zdb_id)
  where apato_entity_a_zdb_id not like 'ZDB-%';


--update atomic_phenotype
--  set apato_entity_a_zdb_id = (select zrepld_new_zdb_id
--					from zdb_replaced_data	
--					where zrepld_old_name = 
--						apato_entity_a_zdb_id)
--  where apato_entity_a_zdb_id not like 'ZDB-%';


update atomic_phenotype
  set apato_entity_a_zdb_id = (select anatitem_zdb_id
					from anatomy_item	
					where anatitem_name = 
							'whole organism')
  where apato_entity_a_zdb_id not like 'ZDB-%';

select *
  from atomic_phenotype
  where apato_entity_a_zdb_id is null ;

select  * from fish, genotype_experiment
where genox_zdb_id = 'ZDB-GENOX-061105-534' 
and zdb_id = genox_geno_Zdb_id;

update atomic_phenotype 
  set apato_entity_a_zdb_id = (Select anatitem_Zdb_id
				from anatomy_item
				where anatitem_name = 'whole organism')
  where apato_entity_a_zdb_id is null ;

update feature
  set feature_lab_of_origin = null
  where feature_lab_of_origin = '' ;

select feature_lab_of_origin
  from feature
 where feature_lab_of_origin not in (select lab.zdb_id from lab);

select distinct apato_tag
  from atomic_phenotype 
  where not exists (Select 'x' from apato_tag
		     where apatotag_name = apato_tag);

--select count(*), apato_entity_a_zdb_id, apato_entity_b_zdb_id,
--  apato_quality_zdb_id, apato_start_stg_zdb_id, apato_end_stg_zdb_id,
--  apato_tag
--  from atomic_phenotype
--  group by apato_entity_a_zdb_id, apato_entity_b_zdb_id,
--  apato_quality_zdb_id, apato_start_stg_zdb_id, apato_end_stg_zdb_id,
--  apato_tag
--  having count(*) > 1;

select count(*),
	apato_genox_zdb_id,
	apato_start_stg_zdb_id,
	apato_end_stg_zdb_id,
	apato_entity_a_zdb_id,
	apato_quality_zdb_id, term_name,
	apato_tag
  from atomic_phenotype, term
  where term_zdb_id = apato_quality_zdb_id
  group by apato_genox_zdb_id,
	apato_start_stg_zdb_id,
	apato_end_stg_zdb_id,
	apato_entity_a_zdb_id,
	apato_quality_zdb_id, term_name,
	apato_tag
  having count(*) > 1;

set constraints all immediate ;

update statistics for procedure ;

drop table tmp_apato_full_no_dups;
drop table tmp_apato ; 
drop table tmp_apato_full;
drop table tmp_tt;
drop table tmp_eu_apato ;

update statistics for procedure ;

update marker
  set mrkr_comments = null
  where mrkr_comments = 'Provisional record for newly registered locus/allele';

update marker
  set mrkr_abbrev = lower(mrkr_name)
  where (mrkr_abbrev = 'null' or mrkr_abbrev = 'NULL' or mrkr_abbrev is null); 

update statistics for procedure ;

alter table fish_image
  drop fimg_fish_zdb_id ;

update fish_image
  set fimg_bkup_thumb = null ;

alter table fish_image
  drop fimg_bkup_thumb ;

update fish_image
  set fimg_bkup_img = null  ;

alter table fish_image
  drop fimg_bkup_img ;

update statistics for procedure ;

update fish_image
  set fimg_bkup_annot = null ;

alter table fish_image
  drop fimg_bkup_annot ;

update fish_image
  set has_image = null ;

alter table fish_image
  drop has_image ;

update fish_image
  set has_annot = null ;

alter table fish_image
  drop has_annot ;

update statistics for procedure ;

update zygocity
  set zyg_abbrev = 'i'
  where zyg_name = 'hemizygous' ;

update zdb_object_type
  set zobjtype_app_page = 'aa-genotypeview.apg' 
  where zobjtype_name = 'GENO';

commit work;
--rollback work ;
