begin work;

create temp table tmp_acc_bk (
	tp_acc_num	varchar(30) not null,
	tp_length	integer,
	tp_data_type	varchar(30),
	tp_db_name	varchar(30)
	)with no log;

create unique index tmp_acc_bk_primary_key 
	on tmp_acc_bk(tp_acc_num);

load from nc_zf_acc.unl insert into tmp_acc_bk;

update statistics high for table tmp_acc_bk;

!echo "---update the accession_bank---"
!echo "# of acc get length updated ..."

select count(*) 
  from accession_bank, tmp_acc_bk
 where accbk_acc_num = tp_acc_num
   and accbk_length <> tp_length;

!echo "delete and insert..."
delete from accession_bank 
	where accbk_acc_num in 
		(select tp_acc_num from tmp_acc_bk);

insert into accession_bank 
	select * from tmp_acc_bk;


!echo "---update the db_link---"

update db_link set dblink_length = 
		(select tp_length
		   from tmp_acc_bk
		  where tp_acc_num = db_link.dblink_acc_num)
	where dblink_fdbcont_zdb_id in 
		(select fdbcont_zdb_id
		   from foreign_db_contains
		  where fdbcont_fdb_db_name= "Genbank"
		)
	 and  exists 
		(select tp_length
		   from tmp_acc_bk
		  where tp_acc_num = db_link.dblink_acc_num
		    and (tp_length <> db_link.dblink_length
			or db_link.dblink_length is null)
		 );
!echo "-- we assumed that the data type is stable... -- " 
commit work;