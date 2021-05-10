--liquibase formatted sql
--changeset sierra:ZFIN-6560.sq

delete from db_link 
where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-090929-8';

delete from accession_bank
 where accbk_fdbcont_zdb_id = 'ZDB-FDBCONT-090929-8';

delete from foreign_db_contains where fdbcont_zdb_id = 'ZDB-FDBCONT-090929-8';

delete from foreign_db where fdb_db_pk_id = 55;

delete from blastdb_order where bdborder_parent_blastdb_zdb_id ='ZDB-BLASTDB-090929-24' or bdborder_child_blastdb_zdb_id = 'ZDB-BLASTDB-090929-24';

delete from blastdb_regen_content where brc_blastdb_zdb_id = 'ZDB-BLASTDB-090929-24';

delete from blast_database where blastdb_zdb_id = 'ZDB-BLASTDB-090929-24';


