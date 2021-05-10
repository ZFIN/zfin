--liquibase formatted sql
--changeset prita:INF-3169

create temp table ftrtemp (ftrid varchar(50), ftrabrev varchar(50), pub varchar(50));
insert into ftrtemp select distinct dblink_linked_recid, feature_abbrev,recattrib_source_zdb_id from feature , record_attribution, db_link where feature_zdb_id=dblink_linked_recid and dblink_zdb_id=recattrib_data_zdb_id;
create temp table recatt (recftr varchar(50),  recpub varchar(50));
insert into recatt select ftrid,pub from ftrtemp where ftrid not in (select recattrib_data_zdb_id from record_attribution,ftrtemp where recattrib_data_zdb_id=ftrid and recattrib_source_zdb_id=pub);
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select * from recatt;


