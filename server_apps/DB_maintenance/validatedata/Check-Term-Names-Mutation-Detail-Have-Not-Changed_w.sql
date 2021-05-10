select term_zdb_id, term_name
 from term
 where ((term_zdb_id = 'ZDB-TERM-130401-166'
         and term_name != 'five_prime_cis_splice_site') or
	(term_Zdb_id = 'ZDB-TERM-130401-167'
	 and term_name != 'three_prime_cis_splice_site') or
        (term_zdb_id = 'ZDB-TERM-130401-1417'
	 and term_name != 'splice_junction'));
