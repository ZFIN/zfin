begin work ;

--update statistics high ;

alter table external_note 
  add (extnote_note_type varchar(30));

set constraints all deferred ;


insert into external_note (extnote_zdb_id, 
				extnote_data_zdb_id,
				extnote_note,
				extnote_note_type)
select get_id('EXTNOTE'),
	zrepld_new_zdb_id, 
	fish.comments, 
	'original submitter comments'
  from fish, zdb_replaced_data
  where fish.comments is not null 
  and fish.comments != ''
  and fish.comments not like '%rovisional record for newly registered locus/allele'
  and fish.comments != 'This record has been created in support of data for which a publication has not specified an allele.'
  and fish.comments not like 'discovered%'
  and fish.zdb_id = zrepld_old_zdb_id
  and zrepld_new_zdb_id like 'ZDB-GENO-%';


insert into external_note (extnote_zdb_id, 
				extnote_data_zdb_id,
				extnote_note,
				extnote_note_type)
select get_id('EXTNOTE'),
	zrepld_new_zdb_id,
	phenotype,
	'original submitter comments'
  from fish, zdb_replaced_data
  where line_type = 'wild type'
  and fish.zdb_id = zrepld_old_zdb_id
  and zrepld_new_zdb_id like 'ZDB-GENO-%'
  and phenotype is not null
  and phenotype != '';
	
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
  where dist_id in (select dist_id from tmp_deleters) ; 

!echo "number of makrer comments from phenotype conversion list" ;

update marker
  set mrkr_comments = mrkr_comments||(select note 
			  from tmp_distinct_marker_note
			  where mrkr_zdb_id = dist_id)
  where exists (select 'x' from tmp_distinct_marker_note
			where dist_id = mrkr_zdb_id);

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select distinct tmp_distinct_marker_note.dist_id, tmp_notes.pub_id
    from tmp_notes, tmp_distinct_marker_note, tmp_id_conversion
    where tmp_notes.id = tmp_id_conversion.id
    and tmp_id_conversion.alt_id = tmp_distinct_marker_note.dist_id 
    and not exists (select 'x' from record_attribution b
			where b.recattrib_datA_zdb_id = 
				tmp_distinct_marker_note.dist_id
			and b.recattrib_source_zdb_id =
				tmp_notes.pub_id
                        and b.recattrib_source_type = 'standard');

--select recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type
--  from record_attribution
--  where recattrib_datA_zdb_id = 'ZDB-GENO-000821-1'
--  order by recattrib_source_zdb_id;

--delete from record_attribution
--  where recattrib_source_type = 'segregation'
--  and exists (select 'x'
--		from tmp_distinct_marker_note, tmp_notes, tmp_id_conversion
--  		where dist_id = recattrib_data_zdb_id
--		and recattrib_source_zdb_id = tmp_notes.pub_id 
--		and tmp_notes.id = tmp_id_conversion.id
--		and tmp_id_conversion.id = tmp_distinct_marker_note.dist_id);


delete from tmp_notes ;

load from notes_for_genotype.txt
insert into tmp_notes ;

--select first 1 * from tmp_notes;

--select * from tmp_notes
--  where id = 'ZDB-FISH-000821-1';

!echo "number of genotype notes from phenotype text file" ;

insert into external_note (extnote_zdb_id, extnote_note, extnote_data_zdb_id)
  select get_id('EXTNOTE'), note, genox_zdb_id
    from genotype_experiment, zdb_replaced_data, tmp_notes, experiment
    where genox_geno_zdb_id = zrepld_new_zdb_id 
    and zrepld_old_zdb_id = tmp_notes.id
    and genox_exp_zdb_id = exp_zdb_id
    and exp_name = '_Standard' ;


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select distinct extnote_zdb_id, pub_id
    from external_note, tmp_notes
     where extnote_note = tmp_notes.note 
     and not exists (select 'x' 	
			from record_Attribution b
			where b.recattrib_data_zdb_id = extnote_zdb_id
			and b.recattrib_source_zdb_id = pub_id);

--delete from record_attribution
--  where exists  (select 'x'
--			from tmp_notes, external_note
--			where recattrib_data_zdb_id = extnote_zdb_id
--			and extnote_note = tmp_notes.note)
--  and recattrib_source_type = 'segregation'
--  and (recattrib_data_zdb_id like 'ZDB-GENO-%' 
--	or recattrib_data_zdb_id like 'ZDB-FISH-%');

!echo "how many seg recattribs left? : " ;

select count(*) from record_attribution
  where recattrib_source_type = 'segregation' ;

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

!echo "number of feature comments from phenotype text file" ;

update feature
  set feature_comments = (select distinct note from tmp_notes, fish, alteration
				where fish.allele = alteration.allele
				and fish.zdb_id = tmp_notes.id 
				and alteration.zdb_id = feature_zdb_id)
  where exists (select 'x' from fish, alteration, tmp_notes
				where fish.allele = alteration.allele
				and fish.zdb_id = id 
				and alteration.zdb_id = feature_zdb_id);

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select feature_zdb_id, tmp_notes.pub_id
    from  tmp_notes, fish, alteration, feature
	where fish.allele = alteration.allele
	and fish.zdb_id = tmp_notes.id 
	and alteration.zdb_id = feature_zdb_id
    and not exists (select 'x'
			from record_attribution b
			where b.recattrib_data_zdb_id = feature_zdb_id
			and b.recattrib_source_zdb_id = tmp_notes.pub_id);

--delete from record_attribution
--  where recattrib_source_type = 'segregation'
--  and recattrib_data_zdb_id like 'ZDB-ALT-%'
--  and exists (select 'x'
--		from tmp_notes
--		where recattrib_data_zdb_id = tmp_notes.id);

!echo "last count of segregation attributions" ;

select count(*) from record_Attribution
  where recattrib_source_type = 'segregation' ;

--select count(*), recattrib_data_zdb_id, recattrib_source_zdb_id,
--   recattrib_source_type from record_attribution
--   group by recattrib_data_zdb_id, recattrib_source_zdb_id,
--   recattrib_source_type
--   having count(*) > 1;



set constraints all immediate ;

alter table external_note
  modify (extnote_note lvarchar(8192)
		not null constraint extnote_note_not_null);

commit work ;

--rollback work;
