begin work;

-------------------- Preparing ---------------------

	create temp table pre_delete(
		rec_data_zdb_id		varchar(50)
		)with no log;
		
	insert into pre_delete
		select recattrib_data_zdb_id from record_attribution
		where recattrib_source_zdb_id in(
		  'ZDB-PUB-020723-2','ZDB-PUB-020723-1','ZDB-PUB-020724-1');
		
--!echo '//Delete from record_attribution records from SP load'
	delete from record_attribution
		where recattrib_source_zdb_id in(
		  'ZDB-PUB-020723-2','ZDB-PUB-020723-1','ZDB-PUB-020724-1');
		  
--!echo '//Take the records that have other sources from the delete list' 
	delete from pre_delete
		where rec_data_zdb_id in (
			select recattrib_data_zdb_id 
			from record_attribution);

			
--!echo '//Delete from zdb_active_data and cause delete cascade'
	delete from zdb_active_data
		where zactvd_zdb_id in (
			select * 
			from pre_delete
			where get_obj_type(rec_data_zdb_id) <> 'GOTERM'
			);
				
--!echo '//Delete GOTERM from zdb_active_data'
	delete from zdb_active_data
		where zactvd_zdb_id in (
			select * 
			from pre_delete
		--	where get_obj_type(zactvd_zdb_id) = 'GOTERM'
			);

commit work;
