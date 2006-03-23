begin work ;

set constraints all deferred ;

alter table external_note 
  add (extnote_note_type varchar(30));

--insert into external_note (extnote_zdb_id, 
--				extnote_data_zdb_id,
--				extnote_note,
--				extnote_note_type)
--select get_id('EXTNOTE'),
--	genox_zdb_id, 
--	fish.comments, 
--	'original submitter comments'
--  from genotype_experiment, experiment, fish
--  where exp_name = '_Standard'
--  and genox_exp_zdb_id = exp_zdb_id 
--  and genox_geno_zdb_id = zdb_id 
--  and fish.comments is not null 
--  and fish.comments != ''
--  and fish.comments not like '%rovisional record for newly registered locus/allele'
--  and fish.comments != 'This record has been created in support of data for which a publication has not specified an allele.'
--  and fish.comments not like 'discovered%';
 
--insert into external_note (extnote_zdb_id, 
--				extnote_data_zdb_id,
--				extnote_note,
--				extnote_note_type)    
--select get_id('EXTNOTE'),
--	genox_zdb_id, 
--	fish.segregation, 
--	'original segregation comments'
--   from fish, genotype_experiment
--   where zdb_id = genox_geno_zdb_id
--   and fish.segregation is not null
--   and fish.segregation != '';

--insert into phenotype_old (pold_genox_zdb_id,
--				pold_segregation)
--  select genox_zdb_id, segregation
--    from fish, genotype_experiment
--	where genox_zdb_id not in (select pold_genox_zdb_id
--						from phenotype_old)
--	and zdb_id = genox_geno_zdb_id
--	and segregation is not null
--	and segregation != '' ;

create temp table tmp_tt (keyword varchar(100), 
				stage varchar(50),
				entity varchar(100),
				entity_zdb_id varchar(50),
				attribute varchar(50),
				value varchar(50))
 with no log;


create temp table tmp_notes (id varchar(50), note lvarchar, pub_id varchar(50))
with no log ;

create unique index note_index on tmp_notes (id)
 using btree in idxdbs1 ;

load from notes_for_marker.txt
insert into tmp_notes ;

create temp table tmp_id_conversion (id varchar(50), alt_id varchar(50))
with no log;

create temp table tmp_altid_conversion (ftr_id varchar(50), mrkr_id varchar(50))
with no log;

insert into tmp_id_conversion
  select distinct id, fmrel_mrkr_zdb_id
   from tmp_notes, fish, alteration, feature_marker_relationship
   where alteration.zdb_id = fmrel_ftr_zdb_id
   and fish.zdb_id = tmp_notes.id
   and alteration.allele = fish.allele;

insert into tmp_altid_conversion
  select distinct alteration.zdb_id, fmrel_mrkr_zdb_id
   from tmp_notes, fish, alteration, feature_marker_relationship
   where alteration.zdb_id = fmrel_ftr_zdb_id
   and fish.zdb_id = tmp_notes.id
   and alteration.allele = fish.allele;

create temp table tmp_distinct_marker_note (dist_id varchar(50),
						note lvarchar)
with no log ;

insert into tmp_distinct_marker_note 
  select distinct tmp_id_conversion.alt_id, note
    from tmp_notes, tmp_id_conversion
    where tmp_notes.id = tmp_id_conversion.id ; 

select dist_id
  from tmp_distinct_marker_note
  group by dist_id
  having count(*) > 1 
 into temp tmp_deleters;

delete from tmp_distinct_marker_note
  where dist_id in (select * from tmp_deleters) ; 

update marker
  set mrkr_comments = (select note 
			  from tmp_distinct_marker_note
			  where mrkr_zdb_id = dist_id)
  where exists (select 'x' from tmp_distinct_marker_note
			where dist_id = mrkr_zdb_id);


delete from tmp_notes ;

load from notes_for_genotype.txt
insert into tmp_notes ;

insert into external_note (extnote_zdb_id, extnote_note, extnote_data_zdb_id)
  select get_id('EXTNOTE'), note, genox_zdb_id
    from genotype_experiment, zdb_replaced_data, tmp_notes, experiment
    where genox_geno_zdb_id = zrepld_new_zdb_id 
    and zrepld_old_zdb_id = tmp_notes.id
    and genox_exp_zdb_id = exp_zdb_id
    and exp_name = '_Standard' ;

insert into zdb_active_data
  select extnote_zdb_id from external_note
    where not exists (Select 'x'
			from zdb_active_data
			where zactvd_zdb_id = extnote_zdb_id);

delete from tmp_notes ;

insert into tmp_notes 
  values ('ZDB-FISH-010911-1','Does not complement <em>nic-1</em>','ZDB-PUB-030129-1');

load from feature.try
  insert into tmp_notes;

update feature
  set feature_comments = (select note from tmp_notes, fish, alteration
				where fish.allele = alteration.allele
				and fish.zdb_id = tmp_notes.id 
				and alteration.zdb_id = feature_zdb_id)
  where exists (select 'x' from fish, alteration, tmp_notes
				where fish.allele = alteration.allele
				and fish.zdb_id = id 
				and alteration.zdb_id = feature_zdb_id);


set constraints all immediate ;

commit work ;

--rollback work;
