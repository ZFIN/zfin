begin work;

 --       set pdqpriority high;

	create temp table pre_delete(
		rec_data_zdb_id		text
		);
		
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
		where zactvd_zdb_id in (select 'x' from pre_delete);

\copy (select * from pre_delete) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/checkDeletedEc2go' with delimiter as '|' null as '';

--rollback work;
commit work;
