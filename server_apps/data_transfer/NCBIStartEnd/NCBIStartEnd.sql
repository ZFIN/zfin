begin work ;

create temp table tmp_ncbi_update (zdbID text,
                                   accnum text,
                                   ncbiChrom text,
                                   ncbiStart integer,
                                   ncbiEnd integer
                                  );

\copy tmp_ncbi_update from updateList (delimiter '|');

create index tmp_ncbi_update_index
  on tmp_ncbi_update(accnum);

update sequence_feature_chromosome_location_generated
   set sfclg_chromosome = (select ncbiChrom from tmp_ncbi_update
                            where zdbID = sfclg_data_zdb_id
                              and accnum = sfclg_acc_num),
       sfclg_start = (select ncbiStart from tmp_ncbi_update
                       where zdbID = sfclg_data_zdb_id 
                         and accnum = sfclg_acc_num),
       sfclg_end = (select ncbiEnd from tmp_ncbi_update
                     where zdbID = sfclg_data_zdb_id 
                       and accnum = sfclg_acc_num),
       sfclg_assembly = 'GRCz11'
 where sfclg_location_source = 'NCBIStartEndLoader'
   and sfclg_fdb_db_id = 10
   and exists(select 'x' from tmp_ncbi_update
               where zdbID = sfclg_data_zdb_id       
                 and accnum = sfclg_acc_num);

create temp table tmp_ncbi_add (zdbID text,
                                accnum text,
                                ncbiChrom text,
                                ncbiStart integer,
                                ncbiEnd integer
                               );

\copy tmp_ncbi_add from addList (delimiter '|');

create index tmp_ncbi_add_index
  on tmp_ncbi_add(accnum);


insert into sequence_feature_chromosome_location_generated (sfclg_data_zdb_id, 
                                                            sfclg_acc_num,
       	    			                            sfclg_chromosome,
				                            sfclg_start,
				                            sfclg_end,				                
				                            sfclg_location_source,
				                            sfclg_fdb_db_id, 
				                            sfclg_evidence_code)
select distinct zdbID,
       		accnum,
       		ncbiChrom,
		ncbiStart,
		ncbiEnd,
		'NCBIStartEndLoader',
		10, 
		'ZDB-TERM-170419-250'
  from tmp_ncbi_add;

commit work;

