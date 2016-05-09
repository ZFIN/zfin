begin work;

create temp table ftrConsequence (ftr varchar(50),cons1 varchar(50), cons2 varchar(50)) with no log;
load from transcriptCorrected.csv insert into ftrConsequence;

--laoding first consequence

create temp table ftCq1 (ftmd varchar(50),featureAbb varchar(50),featurezdb varchar(50),cq1 varchar(50),cqzdb varchar(50)) with no log;

insert into ftCq1 
       select distinct ftr,ftr,ftr,cons1,cons1 
        from ftrConsequence 
	where cons1 is not null 
	 and ftr in (Select feature_abbrev from feature);

update ftCq1 set ftmd =  get_id('FTMD');

insert into zdb_active_data 
  select ftmd from ftCq1;

update ftCq1 
 set featurezdb=(select feature_zdb_id 
     			from feature 
			where featureAbb=feature_abbrev);

update ftCq1 
 set cqzdb=(select mdcv_term_zdb_id 
     		   from mutation_detail_controlled_vocabulary 
		   where cq1=mdcv_term_display_name); 

--laoding second consequence
create temp table ftCq2 (ftmd varchar(50),featureAbb varchar(50),featurezdb varchar(50),cq2 varchar(50),cqzdb2 varchar(50)) with no log;


insert into ftCq2 
 select distinct ftr,ftr,ftr,cons2,cons2 
  from ftrConsequence 
  where cons2!='' 
  and ftr in (Select feature_abbrev from feature);

update ftCq2 
       set ftmd = get_id('FTMD');

insert into zdb_active_data 
  select ftmd from ftCq2;

update ftCq2 
  set featurezdb=(select feature_zdb_id 
      		    from feature 
		    where featureAbb=feature_abbrev); 

update ftCq2 
  set cqzdb2=(select mdcv_term_zdb_id 
      		     from mutation_Detail_controlled_vocabulary
		     where cq2=mdcv_term_display_name); 


insert into feature_transcript_mutation_detail (ftmd_zdb_id,ftmd_transcript_consequence_term_zdb_id,ftmd_feature_zdb_id) 
  select distinct ftmd,cqzdb,featurezdb 
   from ftCq1 
   where featurezdb not in (select ftmd_feature_zdb_id 
   	 	    	   	   from feature_transcript_mutation_detail)
   and cqzdb is not null;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) 
  select distinct ftmd,'ZDB-PUB-130425-4','standard' 
   from ftCq1;

insert into feature_transcript_mutation_detail (ftmd_zdb_id,ftmd_transcript_consequence_term_zdb_id,ftmd_feature_zdb_id) 
  select distinct ftmd,cqzdb2,featurezdb 
    from ftCq2 
    where featurezdb is not null
     and cqzdb2 is not null
 and not exists (select 'x' from feature_transcript_mutation_detail
     	 		where cqzdb2 = ftmd_transcript_consequence_term_zdb_id
			and featurezdb = ftmd_feature_zdb_id);

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) 
  select distinct ftmd,'ZDB-PUB-130425-4','standard' from ftCq2;


--rollback work;
commit work;
