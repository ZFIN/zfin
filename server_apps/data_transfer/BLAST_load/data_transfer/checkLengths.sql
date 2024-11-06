begin work ;


create temp table tmp_lengths (lengther varchar(30), db_abbrev varchar(20))
 with no log;

load from @BLASTSERVER_BLAST_DATABASE_PATH@/Current/lengths.txt
 insert into tmp_lengths;

delete from tmp_lengths
 where db_abbrev = 'total';

update blast_database
  set blastdb_old_num_seqs = blastdb_num_seqs
  where blastdb_abbrev in (select db_abbrev from tmp_lengths);


unload to @TARGET_PATH@/missingSequencesReport.txt
select * from tmp_lengths
 where exists (Select 'x' from blast_database
       	      	      where db_abbrev = blastdb_abbrev
		      and blastdb_num_seqs > lengther);

update blast_database
 set blastdb_num_seqs = (select lengther from tmp_lengths
     		      		where db_abbrev = blastdb_abbrev)
 where exists (Select 'x' from tmp_lengths
       	      	      where blastdb_abbrev = db_abbrev);

commit work ;