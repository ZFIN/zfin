begin work ; 

delete from zdb_active_data
where exists
(select dbl.dblink_zdb_id From db_link dbl 
 join foreign_db_contains fdbc on fdbc.fdbcont_zdb_id=dbl.dblink_fdbcont_zdb_id
 join foreign_db fdb on fdb.fdb_db_pk_id=fdbc.fdbcont_fdb_db_id
where fdb.fdb_db_name='GEO'
);

commit work ; 
