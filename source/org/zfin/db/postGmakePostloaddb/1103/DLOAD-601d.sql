--liquibase formatted sql
--changeset pm:DLOAD-601d

drop table ftCq;
create temp table ftCq (ftmd varchar(50),featureAbb varchar(50),featurezdb varchar(50),cq1 varchar(50),cqzdb varchar(50));

insert into ftCq
       select distinct ftr,ftr,ftr,cons,cons
        from ftrconsequence,feature
	where cons is not null
	 and ftr =feature_abbrev and feature_zdb_id like 'ZDB-ALT-1812%' and feature_abbrev like 'sa%';
	 insert into ftCq
       select distinct ftr,ftr,ftr,cons,cons
        from ftrconsequence,feature
	where cons is not null
	 and ftr =feature_abbrev and feature_zdb_id like 'ZDB-ALT-19%' and feature_abbrev like 'sa%';


update ftCq set ftmd =  get_id('FTMD');

insert into zdb_active_data
  select ftmd from ftCq;

update ftCq
 set featurezdb=(select feature_zdb_id
     			from feature
			where featureAbb=feature_abbrev);

update ftCq
 set cqzdb=(select mdcv_term_zdb_id
     		   from mutation_detail_controlled_vocabulary
     		   inner join term on mutation_detail_controlled_vocabulary.mdcv_term_zdb_id = term.term_zdb_id
		       where cq1=term_name);

insert into feature_transcript_mutation_detail (ftmd_zdb_id,ftmd_transcript_consequence_term_zdb_id,ftmd_feature_zdb_id)
  select distinct ftmd,cqzdb,featurezdb
   from ftCq
   where featurezdb not in (select ftmd_feature_zdb_id
   	 	    	   	   from feature_transcript_mutation_detail)
   and cqzdb is not null;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
  select distinct ftmd,'ZDB-PUB-130425-4'
   from ftCq;

