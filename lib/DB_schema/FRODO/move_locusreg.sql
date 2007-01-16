begin work ;

set constraints all deferred ;

update statistics for procedure; 

create temp table tmp_locus_registration (locus_name varchar(30), 
						owner varchar(50),
						pheno_descr lvarchar,
						abbrev varchar(20))
  with no log ;


insert into tmp_locus_registration (locus_name,abbrev, owner)
  select distinct locus_name,abbrev,owner
    from locus_registration
    where locusreg_public_release_date is null
    and not exists (select 'x'
			from marker
			where mrkr_name = locus_name); 
 
insert into marker (mrkr_zdb_id, 
			mrkr_name,
			mrkr_type,
			mrkr_owner,
			mrkr_abbrev)
select get_id('GENE'),
	locus_name,
	'GENE',
	owner,
	lower(abbrev)
  from tmp_locus_registration
  where locus_name not like 'Tg%'
  and locus_name not like 'Df%'
  and locus_name not like 'T(%';

insert into marker (mrkr_zdb_id, 
			mrkr_name,
			mrkr_type,
			mrkr_owner,
			mrkr_abbrev)
select get_id('TGCONSTRCT'),
	locus_name,
	'TGCONSTRCT',
	owner,
	lower(abbrev)
  from tmp_locus_registration
  where locus_name like 'Tg%';

insert into feature (feature_zdb_id,
			feature_name,
			feature_abbrev,
			feature_type)
  select get_id('ALT'),
	tmp_locus_registration.locus_name||allele,
	allele,
	'TRANSLOCATION'
     from tmp_locus_registration, locus_registration
	where tmp_locus_registration.locus_name = locus_registration.locus_name
     and tmp_locus_registration.locus_name like 'T(%' ;

insert into feature (feature_zdb_id,
			feature_name,
			feature_abbrev,
			feature_type)
  select get_id('ALT'),
	tmp_locus_registration.locus_name||allele,
	allele,
	'DEFICIENCY'
        from tmp_locus_registration, locus_registration
	where tmp_locus_registration.locus_name = locus_registration.locus_name
     and tmp_locus_registration.locus_name like 'Df%' ;


insert into marker_history (mhist_zdb_id,
				mhist_mrkr_zdb_id,
				mhist_event,
				mhist_reason,
				mhist_date,
				mhist_mrkr_name_on_mhist_date,
				mhist_mrkr_abbrev_on_mhist_date)
  select get_id('NOMEN'),
		mrkr_zdb_id,
		'reserved',
		'per personal communication with authors',
		current year to second,
		mrkr_name,
		mrkr_abbrev
    from tmp_locus_registration, marker
    where mrkr_name = locus_name
	and mrkr_abbrev = lower(abbrev);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('FHIST', '11/15/2005','1','','feature_history', 
	  'fhist_zdb_id', 't','f', '2') ;


!echo "here is the second marker dup trial"

select mrkr_name, count(*) as count
 from marker
 group by mrkr_name
 having count(*) >1 
  into temp tmp_name;

--select * from tmp_name ;

--select mrkr_name, count(*) as count
-- from marker
-- group by mrkr_name
-- having count(*) >1 ;

select mrkr_abbrev, count(*) as count
  from marker
  group by mrkr_abbrev
  having count(*) >1
  into temp tmp_abbrev;

--select mrkr_abbrev, count(*) as count
--  from marker
--  group by mrkr_abbrev
--  having count(*) >1;

update marker
  set mrkr_abbrev = mrkr_abbrev||"_"||lower(mrkr_zdb_id)
  where mrkr_abbrev in (select mrkr_abbrev 
  				from tmp_abbrev
  				);

update marker
  set mrkr_name = mrkr_name||"_"||mrkr_zdb_id
  where mrkr_name in (select mrkr_name 
  				from tmp_name
  				) ;

--update marker
--  set mrkr_abbrev = lower(mrkr_abbrev||mrkr_zdb_id)
--  where exists (select 'x'	
---		  from tmp_locus_registration
--		  where mrkr_name = locus_name
--		  and mrkr_abbrev = abbrev
--		  and mrkr_owner = owner)
--   and mrkr_zdb_id like 'ZDB-GENE-%';


--select mrkr_name, count(*) as count
-- from marker
-- group by mrkr_name
-- having count(*) >1 ;


insert into genotype (geno_zdb_id,
			geno_display_name,
			geno_handle,
			geno_is_wildtype)
 select get_id('FISH'),
	locus_name||'<sup>'||allele||'</sup>UNKU',
	locus_name||allele||'UNKU',
	'f'
   from locus_registration 
    where locusreg_public_release_date is null 
    and locus_name not like 'Tg%'
    and locus_name not like 'T(%'
    and locus_name not like 'Df%';

insert into genotype (geno_zdb_id,
			geno_display_name,
			geno_handle,
			geno_is_wildtype)
 select get_id('FISH'),
	locus_name||allele||'UNKU',
	locus_name||allele||'UNKU',
	'f'
   from locus_registration 
    where locusreg_public_release_date is null 
    and ( locus_name like 'T(%'
    or locus_name like 'Df%'
    or locus_name like 'Tg%');


select count(*), geno_handle
  from genotype
  group by geno_handle
  having count(*) > 1;

insert into feature (feature_zdb_id,
			feature_name,
			feature_type,
			feature_abbrev)
  select get_id('ALT'),
	case 
	when allele not like 'un_Tg%'
             then allele
        else locus_name||"unspecified"
        end,
	'SEQUENCE_VARIANT',
	allele
    from locus_registration 
	where locusreg_public_release_date is null 
	and not exists (select 'x'
			from feature
			where feature_abbrev = allele);


insert into feature_history (fhist_zdb_id,
				fhist_ftr_zdb_id,
				fhist_event,
				fhist_reason,
				fhist_date,
				fhist_ftr_name_on_fhist_date,
				fhist_ftr_abbrev_on_fhist_date)
  select get_id('FHIST'),
		feature_zdb_id,
		'reserved',
		'per personal communication with authors',
		current year to second,
		feature_name,
		feature_abbrev
    from feature, locus_registration
    where feature_abbrev = allele
	and locusreg_public_release_date is null;

insert into zdb_active_data
  select fhist_zdb_id
    from feature_history
   where not exists (Select 'x'
			from zdb_active_data
			where zactvd_zdb_id = fhist_zdb_id);


!echo "fmrel inserts";
create temp table tmp_featrel (feature_id varchar(50), 
				mrkr_id varchar(50))
with no log ;

insert into tmp_featrel (feature_id, mrkr_id)
  select distinct feature_zdb_id, mrkr_zdb_id
    from feature, marker, locus_Registration
    where feature_name = allele
	and mrkr_name = locus_name
	and locusreg_public_release_date is null ;

insert into feature_marker_relationship (fmrel_zdb_id,
					fmrel_type,
					fmrel_ftr_zdb_id,
					fmrel_mrkr_zdb_id)
  select get_id('FMREL'),
	'is allele of',
	feature_id,
	mrkr_id
     from tmp_featrel 
	where get_obj_type(mrkr_id) = 'GENE';
	
insert into feature_marker_relationship (fmrel_zdb_id,
					fmrel_type,
					fmrel_ftr_zdb_id,
					fmrel_mrkr_zdb_id)
  select get_id('FMREL'),
	'contains sequence feature',
	feature_id,
	mrkr_id
     from tmp_featrel 
	where get_obj_type(mrkr_id) = 'TGCONSTRCT';

create temp table tmp_genofeat (genotype_id varchar(50), 
				feat_id varchar(50))
with no log ;
		
insert into tmp_genofeat(genotype_id, feat_id)
  select distinct geno_zdb_id, feature_zdb_id
    from genotype, feature, locus_registration
    where feature_name = allele
    and geno_handle = locus_name||allele||'UNKU' ;
	
insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  select get_id('GENOFEAT'),
	genotype_id,
	feat_id,
	(Select zyg_zdb_id
		from zygocity
		where zyg_name = 'unknown'),
	(select zyg_zdb_id
		from zygocity
		where zyg_name = 'unknown'),
	(select zyg_zdb_id
		from zygocity
		where zyg_name = 'unknown')	
    from tmp_genofeat;

update statistics high for table genotype_feature ;
update statistics high for table genotype ;
update statistics high for table feature ;

insert into zdb_active_Data
  select fmrel_Zdb_id from feature_marker_relationship
	where not exists (select 'x'
				from zdb_Active_data
				where zactvd_zdb_id = fmrel_zdb_id);
				
insert into zdb_active_Data
  select geno_Zdb_id from genotype
	where not exists (select 'x'
				from zdb_Active_data
				where zactvd_zdb_id = geno_zdb_id);

insert into zdb_active_Data
  select genofeat_Zdb_id from genotype_feature
	where not exists (select 'x'
				from zdb_Active_data
				where zactvd_zdb_id = genofeat_zdb_id);

insert into zdb_active_Data
  select mrkr_Zdb_id from marker
	where not exists (select 'x'
				from zdb_Active_data
				where zactvd_zdb_id = mrkr_zdb_id);

insert into zdb_active_Data
  select mhist_Zdb_id from marker_history
	where not exists (select 'x'
				from zdb_Active_data
				where zactvd_zdb_id = mhist_zdb_id);

insert into zdb_active_Data
  select feature_zdb_id from feature
	where not exists (select 'x'
				from zdb_Active_data
				where zactvd_zdb_id = feature_zdb_id);

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id)
  select distinct feature_zdb_id, 'ZDB-PUB-040824-1'
    from feature, locus_registration
    where feature_name = allele
	and locusreg_public_release_date is null ;

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id)
  select distinct mrkr_zdb_id, 'ZDB-PUB-040824-1'
    from marker, locus_registration
    where mrkr_name = locus_name
	and locusreg_public_release_date is null ;

!echo "GENOTYPE RECATTRIB ENTER" ;

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id)
  select distinct geno_zdb_id, 'ZDB-PUB-040824-1'
    from genotype, locus_registration
    where geno_display_name = locus_name||'<sup>'||allele||'</sup>UNKU'
	and locusreg_public_release_date is null ;

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id)
  select distinct geno_zdb_id, 'ZDB-PUB-040824-1'
    from genotype, locus_registration
    where geno_display_name = locus_name||allele||'UNKU'
	and locusreg_public_release_date is null ;

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id)
  select distinct genofeat_zdb_id, 'ZDB-PUB-040824-1'
    from genotype_feature, locus_registration
    where exists (select 'x'
			from tmp_genofeat
			where genotype_id = genofeat_geno_zdb_id
			and feat_id = genofeat_feature_zdb_id);

create temp table tmp_attrib (rad_id varchar(50),
			      ras_id varchar(50),
			      source_type varchar(30))
with no log;

insert into tmp_attrib
  select recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type
   from record_attribution
   where recattrib_data_zdb_id like 'ZDB-GENO-%'
  and recattrib_source_type = 'standard' ;

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id,
				recattrib_source_type)
select distinct genofeat_zdb_id, ras_id, source_type
   from tmp_attrib, genotype_feature
  where rad_id = genofeat_geno_zdb_id 
  and ras_id != 'ZDB-PUB-040824-1';


--case 1445 see fix_zdb_ids.sql too.

insert into data_note (dnote_zdb_id,
			dnote_data_zdb_id,
			dnote_curator_zdb_id,
			dnote_date,
			dnote_text)
  select get_id('DNOTE'),
	genofeat_geno_zdb_id,
	'ZDB-PERS-980622-10',
	current year to second,
	pheno_descr||" loaded from Frodo data conversion scripts" 
    from genotype_feature, feature, locus_registration
    where genofeat_feature_zdb_id = feature_zdb_id
	and feature_abbrev = allele
        and pheno_descr is not null
        and pheno_descr != '';
		
insert into zdb_active_data
  select dnote_zdb_id
    from data_note
    where not exists (select 'x'
			from zdb_active_data
			where zactvd_zdb_id = dnote_zdb_id);


set constraints all immediate ;

commit work ;

--rollback work ;
