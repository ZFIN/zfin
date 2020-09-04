--liquibase formatted sql
--changeset pm:DLOAD-6736

insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct dblink_linked_recid, recattrib_source_zdb_id from db_link, record_attribution where dblink_zdb_id=recattrib_data_zdb_id and dblink_acc_num like 'ENSDART%' and dblink_linked_recid like 'ZDB-TSCRIPT%' and dblink_linked_recid not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-190221-12');

insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct dblink_linked_recid, recattrib_source_zdb_id from db_link, record_attribution where dblink_zdb_id=recattrib_data_zdb_id and dblink_acc_num like 'ENSDARG%' and recattrib_source_zdb_id='ZDB-PUB-190221-12' and dblink_linked_recid like 'ZDB-GENE%' and dblink_linked_recid not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-190221-12');

