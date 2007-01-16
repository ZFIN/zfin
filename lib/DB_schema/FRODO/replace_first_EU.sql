begin work ;

create temp table tmp_EU (fish_id varchar(50),
				allele varchar(20),
				pheno_keywords lvarchar,
				phenotype lvarchar,
				comments lvarchar)
with no log ;

load from EUTabbed
  insert into tmp_EU ;

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
  where fish.zdb_id like 'ZDB-FISH-060608-%'
  and allele like 't3%';

update fish
  set comments = (select comments
       from tmp_EU 
	where fish_id = zdb_id)
  where fish.zdb_id like 'ZDB-FISH-060608-%'
  and allele like 't3%';

--execute function scrub_char('test') ;

--drop trigger fish_pheno_keywords_update_trigger ;

update fish
  set pheno_keywords = (select pheno_keywords
       from tmp_EU 
	where fish_id = zdb_id)
  where allele like 't3%'
  and exists (Select 'x'
		from tmp_eu
		where tmp_eu.fish_id = fish.fish_id);

delete from record_Attribution
  where recattrib_data_zdb_id like 'ZDB-FISH-060608-%'
  and recattrib_source_zdb_id = 'ZDB-PUB-030129-1' ;

insert into record_Attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id,
				recattrib_source_type)
  select zdb_id, 'ZDB-PUB-060606-1', 'standard'
    from fish, tmp_eu
    where allele like 't3%' 
    and tmp_eu.fish_id = fish.fish_id;



--commit work ;

rollback work ;