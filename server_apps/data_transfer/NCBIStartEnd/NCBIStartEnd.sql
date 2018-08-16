begin work ;

create temp table tmp_ncbi (zdbID text,
                            accnum text,
       	    	  	    existingChrom text,
                            ncbiChrom text,
			    existingStart integer,
                            ncbiStart integer,
			    existingEnd integer,
                            ncbiEnd integer
                           );

\copy tmp_ncbi from updateList (delimiter '|');

select * from tmp_ncbi limit 5;

create index tmp_ncbi_ottdarg_index
  on tmp_ncbi(accnum);

update sequence_feature_chromosome_location_generated
   set sfclg_chromosome = (select ncbiChrom from tmp_ncbi
                            where zdbID = sfclg_data_zdb_id
                              and accnum = sfclg_acc_num),
       sfclg_start = (select ncbiStart from tmp_ncbi
                       where zdbID = sfclg_data_zdb_id 
                         and accnum = sfclg_acc_num),
       sfclg_end = (select ncbiEnd from tmp_ncbi
                     where zdbID = sfclg_data_zdb_id 
                       and accnum = sfclg_acc_num),
       sfclg_assembly = 'GRCz11'
 where sfclg_location_source = 'NCBIStartEndLoader'
   and sfclg_data_zdb_id like 'ZDB-GENE%'
   and sfclg_fdb_db_id = 10
   and exists(select 'x' from tmp_ncbi
               where zdbID = sfclg_data_zdb_id       
                 and accnum = sfclg_acc_num);


commit work;

--rollback work ;
