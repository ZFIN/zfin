begin work ;

drop trigger locus_update_trigger ;
drop trigger fish_name_update_trigger ;
drop trigger locusreg_abbrev_update_trigger;
drop trigger locusreg_name_update_trigger;
--do locus updates

select * from data_alias
where dalias_alias = 'Df(LG24)';

set constraints all deferred ;

insert into data_alias (dalias_zdb_id,
				dalias_data_zdb_id,
				dalias_alias,
				dalias_group)
select get_id('DALIAS'),
	zdb_id,
	locus_name,
	'alias'
  from locus
  where (locus_name like 'Df(%'
  or locus_name like 'T(%')
  and not exists (Select 'x'
			from data_alias
			where dalias_data_zdb_id = zdb_id
			and dalias_alias = locus_name
			and dalias_group = 'alias');

insert into data_alias (dalias_zdb_id,
				dalias_data_zdb_id,
				dalias_alias,
				dalias_group)
select get_id('DALIAS'),
	zdb_id,
	abbrev,
	'alias'
  from locus
  where (locus_name like 'Df(%'
  or locus_name like 'T(%')
   and locus_name != abbrev
   and abbrev != 'NULL'
   and abbrev is not null
   and not exists (select 'x'
			from data_alias
			where dalias_data_zdb_id = zdb_id
			and dalias_alias = abbrev
			and dalias_group = 'alias');


update locus
  set (locus_name, abbrev)=
	(replace(locus_name,")",":"),
	  replace(abbrev,")",":"))
  where locus_name like 'Df(%'
  or locus_name like 'T(%';

update locus
  set (locus_name, abbrev)=
	(locus_name||")", abbrev||")")
  where locus_name like 'Df(%'
  or locus_name like 'T(%';

update locus
  set (locus_name, abbrev) =
	(replace(locus_name,":)",")"),
	  replace(abbrev,":)",")"))
  where locus_name like '%:)' ;
 
update locus
  set abbrev = locus_name
  where abbrev = 'NULL)' ;

--do fish updates

update fish
  set name=replace(name,")",":")
  where name like 'Df(%'
  or name like 'T(%';

update fish
  set name=name||")"
  where name like 'Df(%'
  or name like 'T(%';

update fish
  set name =replace(name,":)",")")
  where name like '%:)' ;

update fish
  set abbrev = name||"<sup>"||allele||"</sup>" 
  where name like 'Df%'
  or name like 'T(%' ;

---select fish.name, fish.abbrev from fish
--  where name like 'Df%'
--  or name like 'T(%' ;

----do locus reg updates

update locus_registration
  set (locus_name, abbrev)=
	(replace(locus_name,")",":"),
	  replace(abbrev,")",":"))
  where locus_name like 'Df(%'
  or locus_name like 'T(%';

update locus_registration
  set (locus_name, abbrev)=
	(locus_name||")", abbrev||")")
  where locus_name like 'Df(%'
  or locus_name like 'T(%';

update locus_registration
  set (locus_name, abbrev) =
	(replace(locus_name,":)",")"),
	  replace(abbrev,":)",")"))
  where locus_name like '%:)' ;

insert into data_alias (dalias_zdb_id,
				dalias_data_zdb_id,
				dalias_alias,
				dalias_group)
select get_id('DALIAS'),
	zdb_id,
	allele,
	'alias'
  from alteration
  where allele like 'un_Tg%'
     and not exists (Select 'x'
			from data_alias
			where dalias_data_zdb_id = zdb_id
			and dalias_alias = allele
			and dalias_group = 'alias');


update alteration
  set allele = allele||"unspecified" 
  where allele like 'un_Tg%' ;

update alteration
  set allele = allele||"unspecified" 
  where allele like 'un_tg%' ;


select count(*), allele
  from alteration
  group by allele
  having count(*) > 1;

update alteration
  set allele = replace(allele, "un_",'')
  where allele like 'un_Tg%';

update alteration
  set allele = replace(allele, "un_",'')
  where allele like 'un_tg%';

select count(*), allele
  from alteration
  group by allele
  having count(*) > 1;

update fish
  set abbrev = abbrev||"unspecified" 
  where allele like 'un_Tg%' ;

update fish
  set abbrev = abbrev||"unspecified" 
  where allele like 'un_tg%' ;


update fish
  set allele = allele||"unspecified" 
  where allele like 'un_Tg%' ;

update fish
  set allele = allele||"unspecified" 
  where allele like 'un_tg%' ;

update fish
  set fish.abbrev = replace(allele,"un_",'')
  where allele like 'un_Tg%';

update fish
  set fish.abbrev = replace(allele,"un_",'')
  where allele like 'un_tg%';

update fish
  set fish.allele = replace(allele,"un_",'')
  where allele like 'un_Tg%'; 

update fish
  set fish.allele = replace(allele,"un_",'')
  where allele like 'un_tg%'; 

select first 1 * from alteration
where allele like '%unspecified%' ;

insert into zdb_Active_data 
  select dalias_zdb_id 
   from data_alias
   where not exists (Select 'x'
			from zdb_Active_data
			where zactvd_zdb_id = dalias_zdb_id);

select count(*), dalias_data_zdb_id, dalias_alias
  from data_alias
  group by dalias_data_zdb_id, dalias_alias
  having count(*) > 1;

set constraints all immediate ;

commit work ;

--rollback work ;