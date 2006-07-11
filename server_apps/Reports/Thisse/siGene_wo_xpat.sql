-- si: gene 
-- no est, cDNA
-- no GenBank cDNA sequence, no RefSeq cDNA sequence
-- there is OTTDART accession
-- no Thisse xpat 

select dblink_acc_num
  from marker, db_link
 where mrkr_name like "si:%"
   and mrkr_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-060417-1"
   and not exists (
	select "t"
          from marker_relationship
         where mrel_type in ("gene encodes small segment",
 			     "gene constains small segment")
           and mrel_mrkr_1_zdb_id = mrkr_zdb_id
	          )
   and not exists (
	 select "t"
           from db_link
          where dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-37",
				          "ZDB-FDBCONT-040412-38")
            and mrkr_zdb_id = dblink_linked_recid
		)
   and not exists (
	  select "t"
            from expression_experiment
           where xpatex_gene_zdb_id = mrkr_zdb_id 
             and xpatex_source_zdb_id in ("ZDB-PUB-010810-1",
				       "ZDB-PUB-040907-1",
				       "ZDB-PUB-051025-1")
		 );
         