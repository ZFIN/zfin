begin work ; 

delete from db_link 
where 
exists (
select 't'
from foreign_db fdb
join foreign_db_contains fdbc on fdb.fdb_db_pk_id=fdbc.fdbcont_fdb_db_id
where
fdb.fdb_db_name='ZFIN_PROT'
and 
fdbc.fdbcont_zdb_id=dblink_fdbcont_zdb_id
) ;

commit work ; 
