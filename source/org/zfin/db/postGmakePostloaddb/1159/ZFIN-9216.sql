--liquibase formatted sql
--changeset cmpich:ZFIN-9216.sql

-- make ensembl_zfin blast database primary blast database for ensembl transcripts
update foreign_db_contains
set fdbcont_primary_blastdb_zdb_id = 'ZDB-BLASTDB-071128-2'
where fdbcont_zdb_id = 'ZDB-FDBCONT-240304-1';

-- make ensembl transcript blast database be external origination to be included in the
-- blast UI page
update blast_database
set blastdb_origination_id = 3,
    blastdb_public         = 't',
    blastdb_name           = 'Ensembl Transcripts',
    blastdb_abbrev         = 'ensembl_zf',
    blastdb_description    = 'Ensembl Transcripts for Zebrafish'
where blastdb_zdb_id = 'ZDB-BLASTDB-071128-2';

-- mark the old ensembl transcript database as non-public
update blast_database
set blastdb_public = 'f'
where blastdb_zdb_id = 'ZDB-BLASTDB-130708-1';

-- correct sorting of the new blast database
insert into blastdb_order (bdborder_parent_blastdb_zdb_id, bdborder_child_blastdb_zdb_id, bdborder_order)
VALUES ('ZDB-BLASTDB-090929-27','ZDB-BLASTDB-071128-2', 310);

delete from blastdb_order where bdborder_pk_id = 45;