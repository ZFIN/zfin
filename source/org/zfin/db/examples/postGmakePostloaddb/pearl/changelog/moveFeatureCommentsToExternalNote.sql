begin work;

alter table external_note 
  add (extnote_source_zdb_id varchar(50));

update external_note
 set extnote_source_zdb_id = (Select recattrib_source_zdb_id from
     			     	     record_attribution
				     where recattrib_data_zdb_id = extnote_zdb_id);

update external_note
 set extnote_source_zdb_id = 'ZDB-PUB-060606-1'
 where extnote_data_zdb_id like 'ZDB-GENO%'
 and extnote_note like "%Zebrafish Models for Human Development and Disease."
 and extnote_source_zdb_id is null;

update external_note
 set extnote_source_zdb_id = 'ZDB-PUB-160331-1'
 where extnote_Source_zdb_id is null;

alter table external_note
 modify (extnote_source_zdb_id varchar(50) not null constraint extnote_source_zdb_id_not_null);

insert into external_note_type (extntype_name)
 values ('feature');

create temp table tmp_extnote (extnote_id varchar(50),
       	    	  	       extnote_note lvarchar,
			       extnote_data_id varchar(50),
			       extnote_note_type varchar(30) default 'feature',
			       extnote_source_id varchar(50))
with no log;

insert into tmp_extnote (extnote_note, extnote_data_id, extnote_source_id)
 select feature_comments, feature_zdb_id, 'ZDB-PUB-160331-1'
 from feature
 where feature_comments is not null
 and feature_comments != "";

update tmp_extnote
 set extnote_id = get_id('EXTNOTE');

insert into zdb_active_data 
 select extnote_id from tmp_extnote;

insert into external_note (extnote_zdb_id, extnote_data_zdb_id, extnote_note, extnote_note_type, extnote_sourcE_zdb_id)
 select extnote_id, extnote_data_id, extnote_note, extnote_note_type, extnote_source_id
   from tmp_extnote
 where extnote_source_id is not null;

alter table feature 
  drop feature_comments;

delete from record_attribution
 where recattrib_data_zdb_id like 'ZDB-EXTNOTE%';


commit work;

--rollback work;
