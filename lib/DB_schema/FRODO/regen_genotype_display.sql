begin work ;

set constraints all deferred ;

create temp table tmp_genotype (genotype_id varchar(255), 
				genotype_handle varchar(255),
				genotype_display varchar(255))
with no log ;

insert into tmp_genotype
  select geno_zdb_id, 'test', 'test'
    from genotype 
    where geno_is_wildtype = 'f';

create unique index tg_index
  on tmp_genotype(genotype_id)
  using btree in idxdbs4; 

update statistics high for table tmp_genotype ;

update tmp_genotype
  set genotype_handle = get_genotype_handle(genotype_id) ;

select genotype_id, count(*)
  from tmp_genotype
  group by genotype_id
  having count(*) > 1;

update tmp_genotype
  set genotype_display = get_genotype_display(genotype_id);

unload to missing_handle_null
  select * from tmp_genotype
  where genotype_handle is null ;

unload to missing_handle_empty
  select * from tmp_genotype
  where genotype_handle = '' ;

delete from tmp_genotype
  where genotype_handle is null ;

delete from tmp_genotype
  where genotype_handle = '' ;

update genotype
  set geno_handle = (select genotype_handle
			from tmp_genotype
			where genotype_id = geno_zdb_id
                         and genotype_handle is not null 
			 and genotype_handle != '')
  where exists (select 'x'
		  from tmp_genotype
                  where geno_zdb_id = genotype_id);

update genotype
  set geno_display_name = (select genotype_display
			from tmp_genotype
			where genotype_id = geno_zdb_id
                         and genotype_display is not null 
			 and genotype_display != '')
  where exists (select 'x'
		  from tmp_genotype
                  where geno_zdb_id = genotype_id);


select count(*), geno_handle
  from genotype 
  group by geno_handle
  having count(*) > 1 ;

select count(*), geno_display_name
  from genotype 
  group by geno_display_name
  having count(*) > 1 ;

update genotype
  set geno_nickname = (select genotype_handle
			from tmp_genotype
			where genotype_id = geno_zdb_id
                         and genotype_handle is not null 
			 and genotype_handle != '')
  where exists (select 'x'
		  from tmp_genotype
                  where geno_zdb_id = genotype_id);



set constraints all immediate ;

commit work ;

--rollback work ;