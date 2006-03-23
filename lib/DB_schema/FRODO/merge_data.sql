begin work ;

update statistics for procedure ;

set constraints all deferred ;

select count(*) as counter, zircflalt_line_id as line_id
  from zirc_fish_line_alteration
  group by zircflalt_line_id
  having count(*) < 2 
  into temp tmp_zirc_singles;

insert into data_alias (dalias_zdb_id,
			dalias_data_zdb_id,
			dalias_alias,
			dalias_group)
  select get_id('DALIAS'),
	fish.zdb_id,
	zircflalt_line_id,
	'alias'
    from fish, alteration, zirc_fish_line_alteration
    where fish.allele = alteration.allele
    and zircflalt_alt_zdb_id = alteration.zdb_id
    and exists (select 'x'
			from tmp_zirc_singles
			where line_id = zircflalt_line_id);


create temp table tmp_tt (keyword varchar(100), 
                               stage varchar(50),
                               entity varchar(100),
                               entity_zdb_id varchar(50),
                               attribute varchar(50),
                               value varchar(50))
  with no log;


insert into genotype (geno_zdb_id ,
			geno_display_name,
			geno_handle,
			geno_date_entered,
			geno_is_wildtype)
  select get_id('GENO'),
	zircfl_line_id,
	zircfl_line_id,
	current year to second,
	'f'
    from zirc_fish_line
    where not exists (select 'x'
			from tmp_zirc_singles
                        where line_id = zircfl_line_id);


insert into genotype (geno_zdb_id ,
			geno_display_name,
			geno_handle,
			geno_supplier_stock_number,
			geno_date_entered,
			geno_name_order,
			geno_is_wildtype)
  select zdb_id,
		abbrev,
		allele,
		orig_crossnum,
		entry_time,
		fish_allele_order,
		  case 
			when line_type = 'mutant'
				then 'f' 
			when line_type = 'wild type'
				then 't'
			else NULL 
			end		 
   from fish ;


update genotype
  set geno_handle = 
	geno_handle||(select 
			case when father is null
				and mother is null then 'MFunspecified' 
			when father is null 
				and mother is not null then "M"||mother
			when mother is null 
				and father is not null then "F"||father
			when mother is not null and father is not null
				then "M"||mother||"F"||father
			else 'unkown' end
			from fish where zdb_id = geno_zdb_id)
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
!echo "ZIRC background"

--add zirc backgrounds without percents

insert into genotype_background (genoback_geno_zdb_id, 
					genoback_background_zdb_id)
  select geno_zdb_id, zircflback_fish_zdb_id
    from genotype, zirc_fish_line_background
    where geno_display_name = zircflback_line_id ;


create temp table tmp_zirc_background (genotype_id varchar(50),
					genotype_name varchar(50),
					zirc_back varchar(50))
 with no log;

insert into tmp_zirc_background (genotype_id, genotype_name, zirc_back)
 select dalias_data_zdb_id, dalias_alias, zircflback_fish_zdb_id as zirc_back
  from data_alias, zirc_fish_line_background
  where dalias_alias like 'ZL%' 
  and (dalias_data_zdb_id like 'ZDB-FISH-%'
	or dalias_data_zdb_id like 'ZDB-GENO-%') 
  and zircflback_line_id = dalias_alias ;

insert into tmp_zirc_background (genotype_id, genotype_name, zirc_back)
   select geno_zdb_id, geno_display_name, zircflback_fish_zdb_id as zirc_back
     from genotype, zirc_fish_line_background
    where geno_display_name = zircflback_line_id ;

--outstanding problem: waiting for ZIRC
!echo "outstanding problem: ZIRC not right background" ;

select * from tmp_zirc_background, genotype_background
  where genotype_id = genoback_geno_zdb_id
   and zirc_back != genoback_background_zdb_id ;


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

insert into feature (feature_zdb_id,
			feature_name,
			feature_type,
			feature_abbrev,
			feature_lab_of_origin,
			feature_date_entered)
  select zdb_id, 
	case 
	  when chrom_change = 'deficiency'
	     then (select fish.name||fish.allele
			from fish where fish.allele = alteration.allele)
	  when allele in (select allele 
			    from fish,locus
		   	    where fish.locus = locus.zdb_id
			    and locus_name like 'Tg%')
		then (select fish.name||fish.allele
			from fish where fish.allele = alteration.allele)
	     else allele
	     end,
	case 
	  when chrom_change = 'deficiency'
		then 'DELETION'
	  when chrom_change = 'translocation'
		then 'INTERCHROMOSOMAL_MUTATION'
	  when chrom_change = 'point'
	        then 'POINT_MUTATION'
	  when chrom_change = 'unknown'
	        then 'SEQUENCE_VARIANT'
	  when chrom_change in ('insertion', 'unknown')
		and allele in (select allele 
			    from fish,locus
		   	    where fish.locus = locus.zdb_id
			    and locus_name like 'Tg%')
		then 'TRANSGENIC_INSERTION'
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

--make replaced data records for Tg and Df loci.

insert into zdb_replaced_data (zrepld_old_zdb_id,
				zrepld_new_zdb_id,
				zrepld_old_name)
select locus.zdb_id,
	alteration.zdb_id,
	locus_name
  from alteration, locus
   where alteration.locus = locus.zdb_id
	and locus_name like 'Df%'
	and chrom_change = 'deficiency';

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
	and locus_name like 'Df%'
	and chrom_change = 'deficiency';

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
	and chrom_change in ('insertion', 'unknown');

update feature 
  set feature_lab_of_origin = 'ZDB-LAB-000914-1'
  where feature_lab_of_origin is null ;

update feature 
  set feature_lab_of_origin = 'ZDB-LAB-000914-1'
  where feature_lab_of_origin = '';

select feature_lab_of_origin
  from feature
  where not exists (select 'x'
			from lab
			where zdb_id = feature_lab_of_origin);

--!echo "missing features" ;
--select genofeat_feature_zdb_id from genotype_feature
--where genofeat_feature_zdb_id not in (select feature_zdb_id	
--					from feature);			

!echo "non-cloned_gene loci to feature as sequence_variants";

insert into marker (mrkr_zdb_id,
			mrkr_name,
			mrkr_type,
			mrkr_abbrev,
			mrkr_owner,
			mrkr_comments)
select zdb_id,
	locus_name,
	'GENE',
	lower(abbrev),
	owner,
	comments
  from locus
  where cloned_gene is null 
  and locus_name not like 'Df%'
  and locus_name not like 'Tg%';


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



insert into feature_marker_relationship (fmrel_zdb_id,
    					fmrel_type,
    					fmrel_ftr_zdb_id,
    					fmrel_mrkr_zdb_id)
  select get_id('FMREL'), 
		'Mutation is allele of gene',
		alteration.zdb_id,
		locus.zdb_id
		from alteration,locus
		where locus.cloned_gene is null
		and alteration.locus = locus.zdb_id
		and locus_name not like 'Df%'
		and locus_name not like 'Tg%';

insert into feature_marker_relationship (fmrel_zdb_id,
    					fmrel_type,
    					fmrel_ftr_zdb_id,
    					fmrel_mrkr_zdb_id)
  select get_id('FMREL'), 
		'Mutation is allele of gene',
		alteration.zdb_id,
		mrkr_zdb_id
		from alteration,locus,marker
		where locus.cloned_gene =mrkr_zdb_id
		and alteration.locus = locus.zdb_id
		and locus_name not like 'Df%'
		and locus_name not like 'Tg%';

insert into feature_marker_relationship (fmrel_zdb_id,
    					fmrel_type,
    					fmrel_ftr_zdb_id,
    					fmrel_mrkr_zdb_id)
  select get_id('FMREL'), 
		'sequence variant contains sequence feature',
		alteration.zdb_id,
		locus.zdb_id
		from alteration,locus
		where alteration.locus = locus.zdb_id
		and locus_name like 'Tg%' ;


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

load from phenoTabbed 
  insert into tmp_tt;


delete from tmp_tt 
  where entity_zdb_id = '<none>';

delete from tmp_tt 
	where entity_zdb_id is null;

update tmp_tt
  set stage = null 
  where stage = '<none>' ;

update tmp_tt
  set entity_zdb_id = null 
  where entity_zdb_id = '<none>' ;

update tmp_tt
  set entity = null 
  where entity = '<none>' ;

update tmp_tt
  set attribute = null 
  where attribute = '<none>' ;

update tmp_tt
  set value = null 
  where value = '<none>' ;

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

update tmp_tt
  set entity_zdb_id = (select goterm_zdb_id
			from go_term
			where "GO:"||goterm_go_id = entity_zdb_id)
  where entity_zdb_id like 'GO:%' 
  and exists (select 'x'
			from go_term
			where "GO:"||goterm_go_id = entity_zdb_id);

update tmp_tt
  set keyword = 'melanophore_e'
  where keyword = 'melanophore and eye pigment'
  and entity = 'melanophores';

update tmp_tt
  set keyword = 'melanophore_new_e'
  where keyword = 'melanophore and eye pigment'
  and entity = 'pigmented epithelium';

update tmp_tt
  set keyword = 'melanophore_i'
  where keyword = 'melanophores and iridophores'
  and entity = 'melanophores ';

update tmp_tt
  set keyword = 'melanophore_new_i'
  where keyword = 'melanophores and iridophores'
  and entity = 'iridophores';

update tmp_tt
  set keyword = 'melanophore_x'
  where keyword = 'melanophores and xanthophores'
  and entity = 'melanophores ';

update tmp_tt
  set keyword = 'melanophore_new_x'
  where keyword = 'melanophores and xanthophores'
  and entity = 'xanthophores ';

update tmp_tt
  set keyword = 'body shape and tail T'
  where keyword = 'body shape and tail'
  and entity = 'whole organism';

update tmp_tt
  set keyword = 'body shape and tail new T'
  where keyword = 'body shape and tail'
  and entity = 'tail';


update tmp_tt
  set keyword = 'jaw and melanophores J'
  where keyword = 'jaw and melanophores'
  and entity = 'pharyngeal arch 1 skeletal system';

update tmp_tt
  set keyword = 'jaw and melanophores new J'
  where keyword = 'jaw and melanophores'
  and entity = 'melanophores';

update tmp_tt
  set keyword = 'notochord and somites NS'
  where keyword = 'notochord and somites'
  and entity = 'notochord';

update tmp_tt
  set keyword = 'notochord and somites new NS'
  where keyword = 'notochord and somites'
  and entity = 'somites';

update tmp_tt
  set keyword = 'small eyes and small hyoid S'
  where keyword = 'small eyes and small hyoid'
  and entity = 'eye';

update tmp_tt
  set keyword = 'small eyes and small hyoid new S'
  where keyword = 'small eyes and small hyoid'
  and entity = 'hyoid arch';

update tmp_tt
  set keyword = 'xanthophores and iridophores xi'
  where keyword = 'xanthophores and iridophores'
  and entity = 'xanthophores';

update tmp_tt
  set keyword = 'xanthophores and iridophores new xi'
  where keyword = 'xanthophores and iridophores'
  and entity = 'iridophores';

select distinct attribute 
  from tmp_tt
  into temp tmp_attr ;

select distinct value
  from tmp_tt
  into temp tmp_value ;

insert into term (term_zdb_id,
    			term_name,
    			term_ontology)
select get_id('TERM'),
	attribute,
	'pato attribute ontology'
  from tmp_attr ;

insert into term (term_zdb_id,
    			term_name,
    			term_ontology)
select get_id('TERM'),
	value,
	'pato value ontology'
  from tmp_value ;

insert into term (term_zdb_id,
    			term_name,
    			term_ontology)
values( get_id('TERM'),
	'dominant',
	'pato context ontology') ;

insert into term (term_zdb_id,
    			term_name,
    			term_ontology)
values( get_id('TERM'),
	'recessive',
	'pato context ontology') ;

insert into term (term_zdb_id,
    			term_name,
    			term_ontology)
values( get_id('TERM'),
	'unspecified',
	'pato context ontology') ;


insert into term (term_zdb_id,
    			term_name,
    			term_ontology)
values( get_id('TERM'),
	'unspecified',
	'pato attribute ontology') ;


insert into term (term_zdb_id,
    			term_name,
    			term_ontology)
values( get_id('TERM'),
	'unspecified',
	'pato value ontology') ;


update term
  set term_ont_id = term_zdb_id ;


insert into zdb_active_data
  select term_zdb_id
    from term
  where not exists (select 'x'
			from zdb_active_data
			where term_zdb_id = zactvd_zdb_id);

create table tmp_pato (pato_geno_zdb_id varchar(50),
				pato_entity_zdb_id varchar(50))
in tbldbs1;

load from pheno_keywords_parsed
  insert into tmp_pato ;

delete from tmp_pato where pato_entity_zdb_id is null;

create table tmp_pato_full (pato_zdb_id varchar(50),
				pato_geno_zdb_id varchar(50),
				pato_genox_zdb_id varchar(50),
				pato_stage_zdb_id varchar(50),
				pato_context varchar(50),
				pato_entity_zdb_id varchar(50),
				pato_attribute varchar(50),
				pato_value varchar(50)
				)
in tbldbs1 ;


!echo "here it is" ;

insert into tmp_pato_full (pato_geno_zdb_id,
				pato_entity_zdb_id)
  select
	pato_geno_zdb_id,
	pato_entity_zdb_id
    from tmp_pato;


update tmp_pato_full
  set pato_genox_zdb_id = (select genox_zdb_id
				from genotype_experiment
				where genox_geno_zdb_id = pato_geno_zdb_id
				and genox_exp_zdb_id = (select exp_zdb_id
							  from experiment
							  where exp_name = "_Standard"));

--FIX DUPS!
update tmp_pato_full
  set pato_entity_zdb_id = 'melanophore pattern / number'
  where pato_entity_zdb_id = 'melanophore pattern/number' ;

update tmp_pato_full
  set pato_entity_zdb_id = 'pigmented retinal epithelium'
  where pato_entity_zdb_id = 'retinal pigment epithelium' ;

update tmp_pato_full
  set pato_entity_zdb_id = 'melanophore shape / size'
  where pato_entity_zdb_id = 'melanophore shape/size' ;

update tmp_pato_full
  set pato_entity_zdb_id = 'melanophore_e'
  where pato_entity_zdb_id = 'melanophore and eye pigment';

update tmp_pato_full
  set pato_entity_zdb_id = 'melanophore_i'
  where pato_entity_zdb_id = 'melanophores and iridophores';

update tmp_pato_full
  set pato_entity_zdb_id = 'melanophore_x'
  where pato_entity_zdb_id = 'melanophores and xanthophores';

update tmp_pato_full
  set pato_entity_zdb_id = 'body shape and tail T'
  where pato_entity_zdb_id = 'body shape and tail';

update tmp_pato_full
  set pato_entity_zdb_id = 'jaw and melanophores J'
  where pato_entity_zdb_id = 'jaw and melanophores';

update tmp_pato_full
  set pato_entity_zdb_id = 'notochord and somites NS'
  where pato_entity_zdb_id = 'notochord and somites';

update tmp_pato_full
  set pato_entity_zdb_id = 'small eyes and small hyoid S'
  where pato_entity_zdb_id = 'small eyes and small hyoid';

update tmp_pato_full
  set pato_entity_zdb_id = 'xanthophores and iridophores xi'
  where pato_entity_zdb_id = 'xanthophores and iridophores';

update tmp_pato_full
  set pato_entity_zdb_id = 'brain cns and pns'
  where pato_entity_zdb_id = 'brain, cns and pns';

update tmp_pato_full
  set pato_entity_zdb_id = 'xanthophores lethal'
  where pato_entity_zdb_id = 'xanthophores, lethal';

update tmp_pato_full
  set pato_entity_zdb_id = 'xanthophores viable'
  where pato_entity_zdb_id = 'xanthophores, viable';

insert into tmp_pato_full (pato_zdb_id,
				pato_geno_zdb_id,
				pato_entity_zdb_id)
select get_id('PATO'),
	pato_geno_zdb_id,
	'melanophore_new_e'
  from tmp_pato 
  where pato_entity_zdb_id = 'melanophore and eye pigment';


insert into tmp_pato_full (pato_zdb_id,
				pato_geno_zdb_id,
				pato_entity_zdb_id)
select get_id('PATO'),
	pato_geno_zdb_id,
	'melanophore_new_i'
  from tmp_pato 
  where pato_entity_zdb_id = 'melanophores and iridophores';


insert into tmp_pato_full (pato_zdb_id,
				pato_geno_zdb_id,
				pato_entity_zdb_id)
select get_id('PATO'),
	pato_geno_zdb_id,
	'melanophore_new_x'
  from tmp_pato 
  where pato_entity_zdb_id = 'melanophores and xanthophores';


insert into tmp_pato_full (pato_zdb_id,
				pato_geno_zdb_id,
				pato_entity_zdb_id)
select get_id('PATO'),
	pato_geno_zdb_id,
	'body shape and tail new T'
  from tmp_pato 
  where pato_entity_zdb_id = 'body shape and tail';


insert into tmp_pato_full (pato_zdb_id,
				pato_geno_zdb_id,
				pato_entity_zdb_id)
select get_id('PATO'),
	pato_geno_zdb_id,
	'xanthophores and iridophores new xi'
  from tmp_pato 
  where pato_entity_zdb_id = 'xanthophores and iridophores';

insert into tmp_pato_full (pato_zdb_id,
				pato_geno_zdb_id,
				pato_entity_zdb_id)
select get_id('PATO'),
	pato_geno_zdb_id,
	'small eyes and small hyoid new S'
  from tmp_pato 
  where pato_entity_zdb_id = 'small eyes and small hyoid';

insert into tmp_pato_full (pato_zdb_id,
				pato_geno_zdb_id,
				pato_entity_zdb_id)
select get_id('PATO'),
	pato_geno_zdb_id,
	'notochord and somites new NS'
  from tmp_pato 
  where pato_entity_zdb_id = 'notochord and somites';

update tmp_pato_full
  set pato_attribute = (select attribute
			  from tmp_tt
			  where keyword = pato_entity_zdb_id);

update tmp_pato_full
  set pato_stage_zdb_id = (select stage 
			from tmp_tt
			where keyword = pato_entity_zdb_id);

update tmp_pato_full 
  set pato_value = (select value 
			from tmp_tt
			where keyword = pato_entity_zdb_id);
update tmp_pato_full 
  set pato_entity_zdb_id = (select entity_zdb_id 
				from tmp_tt
				where keyword = pato_entity_zdb_id)
  where exists (select 'x'
			from tmp_tt
			where keyword = pato_entity_zdb_id);

update tmp_pato_full
  set pato_attribute = (select term_zdb_id
			  from term
			  where pato_attribute = term_name)
  where exists (select 'x' from term
			  where pato_attribute = term_name);


update tmp_pato_full
  set pato_value = (select term_zdb_id
			  from term
			  where pato_value = term_name)
  where exists (select 'x' from term
			  where pato_value = term_name);

update tmp_pato_full
  set pato_entity_zdb_id = (select anatitem_zdb_id
				from anatomy_item
				where anatitem_name = pato_entity_zdb_id)
  where pato_entity_zdb_id not like 'GO:%'
  and pato_entity_zdb_id not like 'ZDB-GOTERM-%'
  and pato_entity_zdb_id not like 'ZDB-ANAT-%'
  and exists (select 'x'
		from anatomy_item
		where anatitem_name = pato_entity_zdb_id);

update tmp_pato_full
  set pato_entity_zdb_id = (select dalias_data_zdb_id
				from data_alias
				where dalias_alias = pato_entity_zdb_id)
  where pato_entity_zdb_id not like 'GO:%'
  and pato_entity_zdb_id not like 'ZDB-GOTERM-%'
  and pato_entity_zdb_id not like 'ZDB-ANAT-%'
  and exists (select 'x'
		from data_alias
		where dalias_alias = pato_entity_zdb_id);

--update tmp_pato_full
--  set pato_entity_zdb_id = (select zrepld_new_zdb_id
--				from zdb_replaced_data
--				where zrepld_old_zdb_id = pato_entity_zdb_id)
--  where pato_entity_zdb_id like 'ZDB-ANAT-%'
--  and exists (select 'x'
--		from zdb_replaced_data
--		where zrepld_old_zdb_id = pato_entity_zdb_id);


delete from tmp_pato_full
where pato_entity_zdb_id in ('organs',
				'developmental stage', 
				'other organs',
				'adult phenotypes',
				'early phenotypes',
				'edema',
				'epiboly',
				'necrosis');

--unload to missing_entities.unl
--select distinct pato_entity_zdb_id from tmp_pato_full
--where not exists (select 'x'
--			from anatomy_item
--			where anatitem_zdb_id = pato_entity_zdb_id)
-- and pato_entity_zdb_id not like 'GO:%'
--  and pato_entity_zdb_id not like 'ZDB-GOTERM-%'
--  order by pato_entity_zdb_id;

--ZDB-ANAT-011113-37
--tectum vs 


update tmp_pato_full
  set pato_attribute = (select term_zdb_id from term
			where term_name = 'qualitative'
			and term_ontology = 'pato attribute ontology')
  where pato_attribute is null 
  and pato_entity_zdb_id != 'dominant';


update tmp_pato_full
  set pato_value = (select term_Zdb_id 
			from term
			where term_name = 'abnormal'
			and term_ontology = 'pato value ontology')
  where pato_value is null 
  and pato_entity_zdb_id != 'dominant';

select distinct pato_genox_zdb_id
  from tmp_pato_full
  where pato_entity_zdb_id = 'dominant'
  into temp tmp_dom ;

create index pato_genox_index
 on tmp_dom (pato_genox_zdb_id)
 using btree in idxdbs4;

create index pato_full_index
 on tmp_pato_full (pato_genox_zdb_id)
 using btree in idxdbs4 ;

update statistics for table tmp_pato_full;

update statistics for table tmp_dom ;

update tmp_pato_full
  set pato_context = 'dominant'
  where exists (select 'x'
		 from tmp_dom
		  where tmp_dom.pato_genox_zdb_id = 
			tmp_pato_full.pato_genox_zdb_id);

update tmp_pato_full
  set pato_context = (select term_zdb_id
			from term
			where term_name = 'dominant')
  where pato_context = 'dominant' ;

delete from tmp_pato_full
  where pato_entity_zdb_id = 'dominant';

update tmp_pato_full
  set pato_genox_zdb_id = (select genox_zdb_id
				from genotype_experiment
				where genox_geno_zdb_id = pato_geno_zdb_id
				and genox_exp_zdb_id = 
					(select exp_zdb_id
						from experiment
						where exp_name = "_Standard"))
  where pato_genox_zdb_id is null;


create table tmp_pato_full_no_dups (patod_zdb_id varchar(50),
				patod_geno_zdb_id varchar(50),
				patod_genox_zdb_id varchar(50),
				patod_stage_zdb_id varchar(50),
				patod_context varchar(50),
				patod_entity_zdb_id varchar(50),
				patod_attribute varchar(50),
				patod_value varchar(50)
				)
in tbldbs1 ;

!echo "distinct" ;

insert into tmp_pato_full_no_dups (patod_geno_zdb_id,
				patod_genox_zdb_id,
				patod_stage_zdb_id,
				patod_context,
				patod_entity_zdb_id,
				patod_attribute,
				patod_value)
 select distinct pato_geno_zdb_id,
			pato_genox_zdb_id,
			pato_stage_zdb_id,
			pato_context,
			pato_entity_zdb_id,
			pato_attribute,
			pato_value
   from tmp_pato_full ;

update tmp_pato_full_no_dups
  set patod_context = 'unspecified'
  where patod_context is null ;

update tmp_pato_full_no_dups
  set patod_zdb_id = get_id('PATO');

insert into phenotype_anatomy (pato_zdb_id ,
    pato_genox_zdb_id , 
    pato_start_stg_zdb_id ,
    pato_end_stg_zdb_id ,
    pato_entity_zdb_id ,
    pato_attribute_zdb_id ,
    pato_value_zdb_id,
    pato_context)
select patod_zdb_id, 
	patod_genox_zdb_id,
	patod_stage_zdb_id,
	patod_stage_zdb_id,
	patod_entity_zdb_id,
	patod_attribute,
	patod_value,
	patod_context
  from tmp_pato_full_no_dups 
  where patod_entity_zdb_id like 'ZDB-ANAT-%';


insert into phenotype_go (patog_zdb_id ,
    patog_genox_zdb_id , 
    patog_start_stg_zdb_id ,
    patog_end_stg_zdb_id ,
    patog_entity_zdb_id ,
    patog_attribute_zdb_id ,
    patog_value_zdb_id,
    patog_context)
select patod_zdb_id, 
	patod_genox_zdb_id,
	patod_stage_zdb_id,
	patod_stage_zdb_id,
	patod_entity_zdb_id,
	patod_attribute,
	patod_value,
	patod_context
  from tmp_pato_full_no_dups 
  where patod_entity_zdb_id like 'ZDB-GOTERM-%';

--insert into pato_figure, ao ones

insert into pato_figure (patofig_fig_zdb_id,
				patofig_pato_zdb_id)
  select fig_zdb_id, pato_zdb_id
    from figure, genotype_experiment, phenotype_anatomy
    where fig_label = genox_geno_zdb_id
    and genox_zdb_id = pato_genox_zdb_id ;

--insert into pato_figure, go ones

insert into pato_figure (patofig_fig_zdb_id,
				patofig_pato_zdb_id)
  select fig_zdb_id, patog_zdb_id
    from figure, genotype_experiment, phenotype_go
    where fig_label = genox_geno_zdb_id
    and genox_zdb_id = patog_genox_zdb_id ;


--insert into feature_assay

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
  select patog_zdb_id
    from phenotype_go ;

insert into zdb_active_data 
  select pato_zdb_id
    from phenotype_anatomy ;

insert into zdb_active_data 
  select geno_zdb_id
    from genotype
    where not exists (select 'x'
			from zdb_active_data
			where geno_zdb_id = zactvd_zdb_id) ;

update phenotype_anatomy
  set pato_entity_zdb_id = (select zrepld_new_zdb_id
				from zdb_replaced_data
				where zrepld_old_zdb_id = pato_entity_zdb_id
  				and zrepld_old_zdb_id like 'ZDB-ANAT-%')
  where exists (select 'x'
		from zdb_replaced_Data
 		where zrepld_old_zdb_id = pato_Entity_zdb_id);


select distinct pato_entity_zdb_id from phenotype_anatomy
  where pato_entity_zdb_id not in (Select anatitem_zdb_id from anatomy_item);


--add marker_relationship types for transgenic constructs


set constraints all immediate ;

drop table tmp_pato_full_no_dups;
drop table tmp_pato ; 
drop table tmp_pato_full;
drop table tmp_tt;

update marker
  set mrkr_comments = null
  where mrkr_comments = 'Provisional record for newly registered locus/allele';

update marker
  set mrkr_abbrev = lower(mrkr_name)
  where (mrkr_abbrev = 'null' or mrkr_abbrev = 'NULL' or mrkr_abbrev is null); 

update statistics for procedure ;

alter table fish_image
  drop fimg_fish_zdb_id ;

alter table fish_image
  drop fimg_bkup_thumb ;

alter table fish_image
  drop fimg_bkup_img ;

alter table fish_image
  drop fimg_bkup_annot ;

alter table fish_image
  drop has_image ;

alter table fish_image
  drop has_annot ;

insert into image
  select * from fish_image ;

insert into image_stage
  select * from fish_image_stage ;

drop table fish_image_stage ;

drop table fish_image ;

drop table fish_image_form ;

drop table fish_image_direction ;

drop table fish_image_view ;

drop table fish_image_preparation ;

commit work;
--rollback work ;