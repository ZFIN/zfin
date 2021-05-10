begin work;

create temp table tmp_acc_bk (
	tp_acc_num	     varchar(30) not null,
	tp_length            integer,
	tp_fdbcont_zdb_id    varchar(50)
	);

create unique index tmp_acc_bk_primary_key 
	on tmp_acc_bk(tp_acc_num);

\copy tmp_acc_bk from ./nc_zf_acc.unl;

update accession_bank
   set accbk_length = (select tp_length
                         from tmp_acc_bk
                        where accbk_acc_num = tp_acc_num
                          and accbk_fdbcont_zdb_id = tp_fdbcont_zdb_id)
 where accbk_acc_num in (select tp_acc_num
                           from tmp_acc_bk)
   and accbk_fdbcont_zdb_id in 
		(select fdbcont_zdb_id
		   from foreign_db_contains, foreign_db
		  where fdb_db_name= 'GenBank'
		  and fdbcont_fdb_db_id = fdb_db_pk_id
		);

delete from tmp_acc_bk
      where exists (select 't' 
                      from accession_bank
                     where accbk_acc_num = tp_acc_num
                       and accbk_fdbcont_zdb_id = tp_fdbcont_zdb_id);

insert into accession_bank (accbk_acc_num, accbk_length, accbk_fdbcont_zdb_id)
     select * from tmp_acc_bk;


update db_link 
   set dblink_length = (select tp_length
		       from tmp_acc_bk
		      where tp_acc_num = dblink_acc_num
		        and tp_fdbcont_zdb_id = dblink_fdbcont_zdb_id )
 where dblink_fdbcont_zdb_id in 
		(select fdbcont_zdb_id
		   from foreign_db_contains, foreign_db
		  where fdb_db_name= 'GenBank'
		  and fdbcont_fdb_db_id = fdb_db_pk_id
		)
   and exists 
		(select 't'
		   from tmp_acc_bk
		  where tp_acc_num = db_link.dblink_acc_num
		    and (tp_length <> db_link.dblink_length
			or db_link.dblink_length is null)
		 );

commit work;
