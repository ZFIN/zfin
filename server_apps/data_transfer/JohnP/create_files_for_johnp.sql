UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/JohnP/accession.txt'
  DELIMITER "	" select distinct mrkr_zdb_id, mrkr_abbrev, dblink_acc_num  from marker, OUTER (db_link, foreign_db_contains)  where ((mrkr_type = 'EST') or (mrkr_type = 'GENE'))  and dblink_linked_recid = mrkr_zdb_id and dblink_fdbcont_zdb_id = fdbcont_zdb_id and fdbcont_fdb_db_name = 'Genbank' order by 1;


UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/JohnP/mappings.txt' 
  DELIMITER "	" select distinct  marker_id, p.abbrev, or_lg, lg_location, mm.metric from mapped_marker mm, panels p where mm.refcross_id = p.zdb_id and ((marker_id like 'ZDB-EST-%') or (marker_id like 'ZDB-GENE%')) order by 1;
