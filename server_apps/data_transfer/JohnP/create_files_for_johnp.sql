UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/JohnP/accession.txt'
  DELIMITER "	" select distinct zdb_id, abbrev, acc_num  from all_markers, OUTER db_link  where ((mtype = 'EST') or (mtype = 'GENE'))  and linked_recid = zdb_id and db_name = 'Genbank' order by 1;


UNLOAD to '<!--|FTP_ROOT|-->/pub/transfer/JohnP/mappings.txt' 
  DELIMITER "	" select distinct  zdb_id, target_abbrev, OR_lg, lg_location, metric from public_paneled_markers  where ((zdb_id like '%EST%') or (zdb_id like '%GENE%')) order by 1;
