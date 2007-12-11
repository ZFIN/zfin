begin work;

create temp table tmp_acc_bk (
	tp_acc_num	     varchar(30) not null,
	tp_length            integer,
	tp_fdbcont_zdb_id    varchar(50)
	)with no log;

create unique index tmp_acc_bk_primary_key 
	on tmp_acc_bk(tp_acc_num);

load from nc_zf_acc.unl insert into tmp_acc_bk;

update statistics high for table tmp_acc_bk;

!echo "---update the accession_bank---"
!echo "some accessions get length synchronized with daily file..."

update accession_bank
   set accbk_length = (select tp_length
                         from tmp_acc_bk
                        where accbk_acc_num = tp_acc_num
                          and accbk_fdbcont_zdb_id = tp_fdbcont_zdb_id)
 where accbk_acc_num in (select tp_acc_num
                           from tmp_acc_bk)
   and accbk_fdbcont_zdb_id in 
		(select fdbcont_zdb_id
		   from foreign_db_contains
		  where fdbcont_fdb_db_name= "GenBank"
		);

!echo "Add new accessions into accession_bank"

delete from tmp_acc_bk
      where exists (select 't' 
                      from accession_bank
                     where accbk_acc_num = tp_acc_num
                       and accbk_fdbcont_zdb_id = tp_fdbcont_zdb_id);

insert into accession_bank (accbk_acc_num, accbk_length, accbk_fdbcont_zdb_id)
     select * from tmp_acc_bk;


!echo "---update the db_link---"

update db_link 
   set dblink_length = (select tp_length
		       from tmp_acc_bk
		      where tp_acc_num = dblink_acc_num
		        and tp_fdbcont_zdb_id = dblink_fdbcont_zdb_id )
 where dblink_fdbcont_zdb_id in 
		(select fdbcont_zdb_id
		   from foreign_db_contains
		  where fdbcont_fdb_db_name= "GenBank"
		)
   and exists 
		(select 't'
		   from tmp_acc_bk
		  where tp_acc_num = db_link.dblink_acc_num
		    and (tp_length <> db_link.dblink_length
			or db_link.dblink_length is null)
		 );
!echo "-- we assumed that the data type is stable... -- " 
commit work;