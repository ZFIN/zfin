
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select dblink_zdb_id,'ZDB-PUB-160316-6' from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-141007-1';

