-- cleanupGBTfeatureNotes.sql
-- one time use script to clean up the feature_comments of feature table for notes of "visit zfishbook.org."
-- and insert new db_link records for them

begin work;


create table pre_db_link (
        predblink_data_zdb_id text not null,
        predblink_acc_num varchar(50) not null,
        predblink_acc_num_display varchar(50) not null,
        predblink_fdbcont_zdb_id text not null
);

insert into pre_db_link (
        predblink_data_zdb_id,
        predblink_acc_num,
        predblink_acc_num_display,
        predblink_fdbcont_zdb_id)
  select distinct feature_zdb_id, dalias_alias, dalias_alias, fdbcont_zdb_id 
    from feature, data_alias, foreign_db, foreign_db_contains 
   where feature_comments like '%zfishbook.org%' 
     and dalias_data_zdb_id = feature_zdb_id 
     and dalias_alias like 'GBT%'
     and fdbcont_fdb_db_id = fdb_db_pk_id 
     and fdb_db_name = 'zfishbook';
     


alter table pre_db_link add predblink_dblink_zdb_id text;

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');

\copy (select * from pre_db_link order by predblink_acc_num) to 'pre_db_link_cleanupZfishbook' ;
 
insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;



insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id) 
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id 
    from pre_db_link; 




insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)  
  select predblink_dblink_zdb_id,'ZDB-PUB-120111-1' from pre_db_link;


create temp table tmp_updateComments as   select feature_abbrev, feature_name, feature_zdb_id 
     from feature
    where feature_comments like '%zfishbook.org%' 
      and exists (select "x" from data_alias where dalias_data_zdb_id = feature_zdb_id and dalias_alias like 'GBT%')
 order by feature_abbrev;

update feature set feature_comments = '' 
             where feature_comments like '%zfishbook.org%' 
               and exists (select "x" from data_alias where dalias_data_zdb_id = feature_zdb_id and dalias_alias like 'GBT%');

\copy (select * from tmp_updateComments) to 'updatedFeaturesToCleanUpZfishbookComments'; 


drop table pre_db_link;

                                 
--rollback work;

commit work;
