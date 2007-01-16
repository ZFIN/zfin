begin work ;

set constraints all deferred ;

insert into genotype_marker (genomrkr_zdb_id,
				genomrkr_geno_zdb_id,
				genomrkr_mrkr_zdb_id,
				genomrkr_dad_zygocity,
				genomrkr_mom_zygocity,
				genomrkr_zygocity)
  select get_id('GENOMRKR'),
		genofeat_geno_zdb_id,
		mrkr_zdb_id,
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),		
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown')
     from genotype_feature, feature_marker_relationship,feature, marker
	where genofeat_feature_zdb_id = fmrel_ftr_zdb_id
	and fmrel_mrkr_zdb_id = mrkr_zdb_id
	and feature_zdb_id = fmrel_ftr_zdb_id
	and genofeat_feature_zdb_id = feature_zdb_id 
        and feature_name like '%un_Tg%';


create temp table tmp_attribs (rad_id varchar(50),
			      ras_id varchar(50),
			      source_type varchar(30))
with no log;

insert into tmp_attribs
  select recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type
   from record_attribution
   where recattrib_data_zdb_id like 'ZDB-GENO-%'
  and recattrib_source_type = 'standard' ;

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id,
				recattrib_source_type)
select distinct genomrkr_zdb_id, ras_id, source_type
   from tmp_attribs, genotype_marker
  where rad_id = genomrkr_geno_zdb_id ;

insert into zdb_active_data
  select genomrkr_zdb_id
    from genotype_marker 
    where not exists (select 'x'
			from zdb_active_data
			where genomrkr_zdb_id = zactvd_zdb_id);


create temp table tmp_attrib (rado_id varchar(50),
				radn_id varchar(50),
				rads_id varchar(50),
				source_type varchar(30))
with no log ;

insert into tmp_attrib (rado_id, radn_id, rads_id, source_type)
  Select feature_zdb_id, genofeat_geno_zdb_id, recattrib_source_zdb_id,
		recattrib_source_type
	from feature, genotype_Feature, record_attribution
	where feature_name like '%un_Tg%'
	and genofeat_feature_zdb_id = feature_zdb_id
	and recattrib_data_zdb_id = feature_zdb_id;


select * from tmp_attrib
  where rads_id = 'ZDB-PUB-040824-1'
  and rado_id = '';


create temp table tmp_alias (rado_id varchar(50),
				radn_id varchar(50),
				alias varchar(255),
				type varchar(30),
				dalias_id varchar(50))
with no log ;			

insert into tmp_alias  (rado_id, 
			radn_id, 
			alias, 
			type, 
			dalias_id)
  select dalias_data_zdb_id, 
	genofeat_geno_zdb_id,
	dalias_alias, 
	dalias_group, 
	dalias_zdb_id
    from data_alias, genotype_Feature, feature
    where feature_name like '%un_Tg%'
	and genofeat_feature_zdb_id = feature_zdb_id
	and dalias_data_zdb_id = feature_zdb_id;

delete from data_alias
  where dalias_zdb_id in (select dalias_id
				from tmp_alias);

insert into data_alias (dalias_Zdb_id, dalias_data_zdb_id, dalias_alias,
				dalias_group)
  select dalias_id, radn_id, alias, type 
    from tmp_alias ;

delete from record_attribution
  where exists (Select 'x'
			from tmp_Attrib
			where recattrib_data_zdb_id = rado_id
			and recattrib_source_zdb_id = rads_id
			and recattrib_source_type = source_type);

delete from record_attribution
  where exists (Select 'x'
			from tmp_Attrib
			where recattrib_data_zdb_id = radn_id
			and recattrib_source_zdb_id = rads_id
			and recattrib_source_type = source_type);

insert into record_Attribution (recattrib_data_zdb_id, 
				recattrib_source_zdb_id,
				recattrib_source_type)
  select distinct radn_id, rads_id, source_type
   from tmp_attrib;


--select * from feature
--  where feature_name like '%un_Tg%'
--  and feature_type != 'Insertion';

select * from genotype_feature, feature, genotype, feature_marker_relationship,
	marker
where genofeat_zdb_id = 'ZDB-GENOFEAT-060830-4030' 
 and genofeat_geno_zdb_id = geno_zdb_id
 and genofeat_feature_zdb_id = feature_zdb_id
 and feature_zdb_id = fmrel_ftr_zdb_id
 and mrkr_zdb_id = fmrel_mrkr_zdb_id
 ;


!echo "No rows found for zdb_replaced data for the ZDB-ALT-ids of unkwnown tg insertions or seq variants" ;

select  * from zdb_replaced_data, feature
  where zrepld_new_zdb_id = feature_zdb_id
  and feature_name like '%un_Tg%' ;


select  * from zdb_replaced_data, feature
  where zrepld_old_zdb_id = feature_zdb_id
  and feature_name like '%un_Tg%' ;



delete from genotype_feature
  where exists (select 'x'
                  from genotype_marker
                  where genofeat_geno_zdb_id = genomrkr_geno_zdb_id)
  and exists (select 'x'
                from feature_marker_relationship, marker
                where mrkr_zdb_id = fmrel_mrkr_zdb_id);


delete from feature
  where feature_name like '%un_Tg%' ;


select * from record_Attribution
  where not exists (select 'x'
			from genotype_feature
			where genofeat_zdb_id = recattrib_data_zdb_id)
  and recattrib_data_zdb_id like 'ZDB-GENOFEAT-%';

select * from data_alias
  where not exists (select 'x'
			from feature
			where dalias_data_zdb_id = feature_zdb_id)
  and dalias_data_zdb_id like 'ZDB-FEATURE-%';


delete from zdb_active_data
where zactvd_zdb_id like 'ZDB-GENOFEAT%'
and not exists (select 'x'
		  from genotype_feature
			where zactvd_zdb_id = genofeat_zdb_id); 

set constraints all immediate ;

commit work ;

--rollback work ;
