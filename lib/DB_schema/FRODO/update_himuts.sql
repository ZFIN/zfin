begin work ;

create temp table tmp_virus (allele varchar(30), virus varchar(20))
with no log ;

load from virusTabbed
insert into tmp_virus ;

create temp table tmp_tg (tg varchar(20))
with no log;

insert into tmp_tg (tg)
  select distinct virus from tmp_virus 
where virus is not null;

set constraints all deferred ;

insert into marker (mrkr_zdb_id,
			mrkr_name,
			mrkr_abbrev, 
			mrkr_type,
			mrkr_owner)
select get_id('TGCONSTRCT'),
		tg,
		lower(tg)||"virus",
		'TGCONSTRCT',
		'ZDB-PERS-000914-2'
	from tmp_tg 
	where tg is not null
	and tg != '';

select count(*), mrkr_abbrev
  from marker
  group by mrkr_abbrev
  having count(*) >1;

insert into zdb_active_data
  select mrkr_Zdb_id from marker
		where not exists (Select 'x'
					from zdb_Active_data
					where mrkr_zdb_id = zactvd_zdb_id);

select distinct get_feature_type(feature_zdb_id)
  from feature, tmp_virus
  where feature_name = allele ;

select distinct get_obj_type(mrkr_zdb_id)
  from marker, tmp_virus
  where mrkr_name = virus ;

create temp table tmp_rel (fmrel_zdb_id varchar(50),
						fmrel_type varchar(50),
						fmrel_ftr_zdb_id varchar(50),
						fmrel_mrkr_zdb_id varchar(50)
) with no log ;

insert into marker_type_group 
   values ('TGCONSTRUCT', 'CONSTRUCT');

insert into tmp_rel (fmrel_zdb_id,
						fmrel_type,
						fmrel_ftr_zdb_id,
						fmrel_mrkr_zdb_id)
  select get_id('FMREL'),
	'contains sequence feature',
	(Select feature_zdb_id from feature
	  where feature_name = lower(allele)),
	(select mrkr_Zdb_id
		from marker
		where mrkr_name = virus)
   from tmp_virus 
   where allele not like 'd%'
   and exists (Select 'x'
		from feature where feature_name = lower(allele))
   and exists (Select 'x'
		from marker where mrkr_name = virus) ;

select count(*), 
						fmrel_type,
						fmrel_ftr_zdb_id,
						fmrel_mrkr_zdb_id
  from tmp_rel
  group by 
						fmrel_type,
						fmrel_ftr_zdb_id,
						fmrel_mrkr_zdb_id
  having count(*) >1 ;

insert into feature_marker_relationship (fmrel_zdb_id,
						fmrel_type,
						fmrel_ftr_zdb_id,
						fmrel_mrkr_zdb_id)
  select distinct fmrel_zdb_id,
						fmrel_type,
						fmrel_ftr_zdb_id,
						fmrel_mrkr_zdb_id
	from tmp_rel;

insert into zdb_active_data
  select fmrel_Zdb_id from feature_marker_relationship
		where not exists (Select 'x'
					from zdb_Active_data
					where fmrel_zdb_id = zactvd_zdb_id);

select * from marker_history
  where mhist_mrkr_abbrev_on_mhist_date is null;

set constraints all immediate; 

commit work ;

--rollback work ;
