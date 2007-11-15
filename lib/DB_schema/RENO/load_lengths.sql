begin work ;

create temp table tmp_protein_lengths (id varchar(15), barf varchar(200),
       lengther int)
with no log ;

load from length
  insert into tmp_protein_lengths ;

select first 10 * from tmp_protein_lengths;

select * from tmp_protein_lengths
  where id = 'Q6IQ95';

update accession_bank
  set accbk_length = (Select lengther
      		     	     from tmp_protein_lengths
			     where accbk_pk_id = id
			     )
  where exists (Select 'x'
  	       	       from foreign_db_contains
		       where fdbcont_zdb_id = accbk_fdbcont_zdb_id
		       and fdbcont_fdb_db_name = 'UniProt');


select first 10 * from accession_bank
  where exists (Select 'x'
  	       	       from foreign_db_contains
		       where fdbcont_zdb_id = accbk_fdbcont_zdb_id
		       and fdbcont_fdb_db_name = 'UniProt');
--rollback work ;

commit work ;