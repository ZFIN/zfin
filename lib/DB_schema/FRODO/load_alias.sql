
begin work ;

create temp table tmp_Alias (zdb_id varchar(50),alias varchar(255))
with no log ;

load from locus_alias.unl
  insert into tmp_alias ;

select * from tmp_alias
  where not exists (Select dalias_alias
 			from data_alias
			where dalias_alias = alias)
  and not exists (Select mrkr_name
			from marker
			where mrkr_name = alias)
  and not exists (select mrkr_abbrev
			from marker
			where mrkr_abbrev = alias);

set constraints all deferred ;
insert into data_alias (dalias_zdb_id,
			dalias_data_zdb_id,
			dalias_alias,
			dalias_group)
  select get_id('DALIAS'),zrepld_new_zdb_id, alias, 'alias'
    from zdb_replaced_data, tmp_alias
   where zrepld_old_zdb_id = zdb_id
   and not exists (Select dalias_alias
 			from data_alias
			where dalias_alias = alias)
  and not exists (Select mrkr_name
			from marker
			where mrkr_name = alias)
  and not exists (select mrkr_abbrev
			from marker
			where mrkr_abbrev = alias);

insert into zdb_active_data
  select dalias_zdb_id 
    from data_alias
   where not exists (select 'x'	
			from zdb_active_data
			where zactvd_zdb_id =dalias_zdb_id);

set constraints all immediate ;

--rollback work ;
commit work ;