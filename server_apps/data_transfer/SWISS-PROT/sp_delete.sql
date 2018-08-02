begin work;

    --    set pdqpriority 50;

	create temporary table pre_delete(
		rec_data_zdb_id		text
		) ;

	
        create index pd_data_id_index on pre_delete(rec_data_zdb_id);
		
	insert into pre_delete
		select recattrib_data_zdb_id from record_attribution
		where recattrib_source_zdb_id in 
		   ('ZDB-PUB-020723-2','ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3');


--!echo '//Delete from record_attribution records from SP load'
	delete from record_attribution
		where recattrib_source_zdb_id in 
		  ('ZDB-PUB-020723-2','ZDB-PUB-020723-1','ZDB-PUB-020724-1','ZDB-PUB-031118-3');
		  
--!echo '//Take the records that have other sources from the delete list' 

	delete from pre_delete
	       where exists (select 'x' from record_attribution where recattrib_data_zdb_id = rec_data_zdb_id);

--update statistics high for table pre_delete;

--!echo '//Delete from zdb_active_data and cause delete cascades on DB link, MRKRGOEV and EXT note records'
	delete from zdb_active_data
		where exists (select 'x' from pre_delete where rec_data_zdb_id = zactvd_zdb_id);

--rollback work;
commit work;
