begin work;

        set pdqpriority high;

	create temp table pre_delete(
		rec_data_zdb_id		varchar(50)
		)with no log;
		
	insert into pre_delete
		select recattrib_data_zdb_id from record_attribution
		where recattrib_source_zdb_id = 'ZDB-PUB-031118-3';

		
--!echo '//Delete from record_attribution records from ec2go'
	delete from record_attribution
		where recattrib_source_zdb_id = 'ZDB-PUB-031118-3';
		  
--!echo '//Take the records that have other sources from the delete list' 

	delete from pre_delete
		where rec_data_zdb_id in (
			select recattrib_data_zdb_id 
			from record_attribution);


--!echo '//Delete from zdb_active_data and cause delete cascades on DB link, MRKRGOEV and EXT note records'
	delete from zdb_active_data
		where zactvd_zdb_id in (select * from pre_delete);

unload to 'checkDeletedEc2go' select * from pre_delete;

commit work;
