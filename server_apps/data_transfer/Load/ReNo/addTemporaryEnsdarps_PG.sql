begin work ;

create temp table tmp_id (id2 varchar(50))
with no log;

insert into tmp_id (id2)
select get_id('FDBCONT') as id2 from single;

insert into foreign_db_contains (fdbcont_zdb_id, fdbcont_fdb_db_id,
       	    			fdbcont_fdbdt_id,fdbcont_organism_common_name,
				fdbcont_primary_blastdb_zdb_id)
 select id2,7,2,'Zebrafish','ZDB-BLASTDB-130401-2'
   from tmp_id;

create temp table tmp_dblink (id varchar(50), dblink_linked_Recid varchar(50), prot_acc varchar(50));

insert into tmp_dblink 
  select get_id('DBLINK'), dblink_linked_recid, ensp_ensdarp_id
    from db_link, ensdarg_ensdarp_mapping
 where dblink_acc_num = ensp_ensdarg_id;

insert into zdb_active_data
 select id from tmp_dblink;

insert into record_Attribution (recattrib_datA_zdb_id, recattrib_Source_zdb_id)
 select id, 'ZDB-PUB-130213-1'
  from tmp_dblink;

insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_acc_num, dblink_length, dblink_fdbcont_zdb_id)
  select id, dblink_linked_recid, prot_acc, ensp_length, id2
    from tmp_dblink, ensdarg_ensdarp_mapping, tmp_id
    where prot_acc = ensp_ensdarp_id;


--rollback work ;
commit work ;
