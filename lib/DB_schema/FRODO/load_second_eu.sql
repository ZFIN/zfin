begin work ;


create table tmp_new_eu_fish (allele varchar(20),
				fish_abbrev varchar(70),
				locus_name varchar(70),
				locus_abbrev varchar(70),
				mutation varchar(20),
				mutagen varchar(50),
				m_bkgrnd varchar(50),
				f_bkgrnd varchar(50),
				reffer varchar(50))
in tbldbs1 extent size 64 next size 64;

load from EUNewTabbed
  insert into tmp_new_eu_fish ;

delete from tmp_new_eu_fish
  where allele = 'allele';

select distinct m_bkgrnd from tmp_new_eu_fish
  where m_bkgrnd not in (Select fish.zdb_id from fish);

alter table tmp_new_eu_fish
  add (new_fish_id varchar(50));

alter table tmp_new_eu_fish
  add (chrom_id varchar(50));

alter table tmp_new_eu_fish
  add (alt_id varchar(50));

alter table tmp_new_eu_fish
  add (locus_zdb_id varchar(50));

update tmp_new_eu_fish
 set locus_zdb_id = get_id("LOCUS")
  where not exists (select 'x'
			from fish
			where fish.allele = tmp_new_eu_fish.allele);

update tmp_new_eu_fish
  set new_fish_id = get_id("FISH")
  where not exists (select 'x'
			from fish
			where fish.allele = tmp_new_eu_fish.allele);

update tmp_new_eu_fish
  set alt_id = get_id("ALT")
  where not exists (select 'x'
			from fish
			where fish.allele = tmp_new_eu_fish.allele);

update tmp_new_eu_fish
  set chrom_id = get_id("CHROMO")
  where not exists (select 'x'
			from fish
			where fish.allele = tmp_new_eu_fish.allele);

set constraints all deferred ;

insert into fish (zdb_id,
			name,
			abbrev,
			line_type,
			father,
			mother,
			allele,
			locus)
  select new_fish_id,
	locus_name,
	locus_abbrev||"<sup>"||allele||"</sup>",
	'mutant',
	f_bkgrnd,
	m_bkgrnd,
	allele,
	locus_zdb_id
     from tmp_new_eu_fish ;

insert into locus (zdb_id, 
			locus_name,
			abbrev)
  select locus_zdb_id,
	locus_name,
	locus_abbrev
    from tmp_new_eu_fish ;

insert into alteration (zdb_id,
			chrom_id,
			chrom_change,
			allele,
			mutagen,
			protocol,
			locus)
  select alt_id,
	chrom_id,
	'unknown',
	allele,
	'ENU',
	'not specified',
	locus_zdb_id
    from tmp_new_eu_fish 
    where not exists (Select 'x'
			from alteration
			where alteration.allele = tmp_new_eu_fish.allele);

insert into chromosome (abbrev,
			zdb_id,
			print_name,
			name)
  select locus_abbrev,
	chrom_id,
	locus_name||"<sup>"||allele||"</sup>",
	locus_name
    from tmp_new_eu_fish ;



insert into int_fish_chromo (source_id, 
				target_id,
				status)
select new_fish_id,
	chrom_id,
	'original'
  from tmp_new_eu_fish;


insert into zdb_active_data
  select new_fish_id
    from tmp_new_eu_fish ;

insert into zdb_active_data
  select locus_zdb_id
    from tmp_new_eu_fish ;

insert into zdb_active_data
  select alt_id
    from tmp_new_eu_fish ;

insert into zdb_active_data
  select chrom_id
    from tmp_new_eu_fish ;

insert into record_attribution (recattrib_data_zdb_id, 
			recattrib_source_zdb_id)
  select new_fish_id, reffer
    from tmp_new_eu_fish ;

insert into record_attribution (recattrib_data_zdb_id, 
		recattrib_source_zdb_id)
  select locus_zdb_id, reffer
    from tmp_new_eu_fish ;

insert into record_attribution (recattrib_data_zdb_id, 
	recattrib_source_zdb_id)
  select alt_id, reffer
    from tmp_new_eu_fish ;

select recattrib_data_zdb_id, 
	recattrib_source_zdb_id,
	recattrib_source_type,
	count(*)
  from record_attribution
  group by recattrib_data_zdb_id, 
	recattrib_source_zdb_id,
	recattrib_source_type 
  having count(*) > 1;

set constraints all immediate ;


create temp table tmp_EU (fish_id varchar(50),
				allele varchar(20),
				pheno_keywords lvarchar,
				phenotype lvarchar,
				comments lvarchar)
with no log ;

load from EUTabbed
  insert into tmp_EU ;

update tmp_EU
  set comments = replace(comments, '\"";""', ',');

update tmp_EU
  set comments = replace(comments, '""', '"');

update tmp_EU
  set comments = replace(comments, '"Assay', 'Assay');

update tmp_EU
  set comments = replace(comments, 'Disease."', 'Disease.');

update tmp_EU
  set comments = replace(comments, '";"', ',');


update tmp_EU
  set fish_id = (Select zdb_id
			from fish
			where fish.allele = tmp_eu.allele)
  where fish_id is null;

create index fish_index
  on tmp_EU (fish_id)
  using btree in idxdbs3;

update statistics high for table tmp_EU ;

select count(*), fish_id
  from tmp_EU
  group by fish_id
  having count(*) > 1;

update fish
  set phenotype = (select phenotype
       from tmp_EU 
	where fish_id = zdb_id)
  where allele like 't3%'
  and exists (select 'x' from tmp_EU
		where fish_id = zdb_id);

update fish
  set comments = (select comments
       from tmp_EU 
	where fish_id = zdb_id)
  where allele like 't3%'
  and exists (select 'x' from tmp_EU
		where fish_id = zdb_id) ;

--execute function scrub_char('test') ;

--drop trigger fish_pheno_keywords_update_trigger ;

update fish
  set pheno_keywords = (select pheno_keywords
       from tmp_EU 
	where fish_id = zdb_id)
  where allele like 't3%'
  and exists (Select 'x'
		from tmp_eu
		where tmp_eu.fish_id = fish.zdb_id);

delete from record_Attribution
  where recattrib_data_zdb_id like 'ZDB-FISH-060608-%'
  and recattrib_source_zdb_id = 'ZDB-PUB-030129-1' ;

delete from record_Attribution
  where recattrib_data_zdb_id like 'ZDB-ALT-060608-%'
  and recattrib_source_zdb_id = 'ZDB-PUB-030129-1' ;

delete from record_Attribution
  where recattrib_data_zdb_id like 'ZDB-LOCUS-060608-%'
  and recattrib_source_zdb_id = 'ZDB-PUB-030129-1' ;

insert into record_Attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id,
				recattrib_source_type)
  select zdb_id, 'ZDB-PUB-060606-1', 'standard'
    from fish, tmp_eu
    where fish.allele like 't3%' 
    and tmp_eu.fish_id = fish.zdb_id
	and not exists (Select 'x'
			  from record_attribution
			  where recattrib_data_zdb_id = zdb_id
			   and recattrib_source_zdb_id = 'ZDB-PUB-060606-1'
				and recattrib_source_type = 'standard');


insert into record_Attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id,
				recattrib_source_type)
select zdb_id, 'ZDB-PUB-060606-1', 'standard'
  from alteration
  where zdb_id like 'ZDB-ALT-060608-%' ;

insert into record_Attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id,
				recattrib_source_type)
select zdb_id, 'ZDB-PUB-060606-1', 'standard'
  from locus
  where zdb_id like 'ZDB-LOCUS-060608-%' ;



select first 10 comments
  from fish where comments like '%"%';

drop table tmp_new_eu_fish ;

commit work;

--rollback work ;