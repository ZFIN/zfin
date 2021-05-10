SELECT fdb_db_name
FROM   foreign_db
WHERE  fdb_db_pk_id NOT IN (SELECT fdbcont_fdb_db_id
                            FROM   foreign_db_contains)
       AND fdb_db_name NOT IN ( 'HAMAP', 'UniProtKB-SubCell', 'SP_SL', 'PANTHER',
                                'Ensembl', 'HTTP', 'MESH', 'ISBN', 'ZFIN' );