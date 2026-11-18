--liquibase formatted sql

-- ZFIN-10602: Remove Ensembl from the sequence-table analysis tools and point UCSC BLAT at GRCz12tu.
--
-- The analysis-tools menu of a sequence lists the blast_database rows linked to its reference
-- database through int_fdbcont_analysis_tool. Only those links are removed here; the Ensembl
-- blast_database rows stay because ZDB-BLASTDB-130401-2 (Ensembl_P) is the primary blast database
-- of foreign_db_contains ZDB-FDBCONT-131021-1 (Ensembl).
--
-- UCSC has no native GRCz12 database (no danRer12); GRCz12tu is served as the GenArk assembly hub
-- GCF_049306965.2 (same sequence, GCA_049306965.1), which supports BLAT.

--changeset cmpich:ZFIN-10602-remove-ensembl-analysis-tools
delete from int_fdbcont_analysis_tool
 where ifat_blastdb_zdb_id in ('ZDB-BLASTDB-090929-19',  -- ENSEMBL (Ensembl BLAST)
                               'ZDB-BLASTDB-130401-2'); -- Ensembl_P (Ensembl Protein Blast)
--rollback insert into int_fdbcont_analysis_tool (ifat_fdbcont_zdb_id, ifat_blastdb_zdb_id) values ('ZDB-FDBCONT-040412-36', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-040412-37', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-040412-38', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-050210-1', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-071128-1', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-071128-2', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-071128-3', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-090929-10', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-090929-4', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-100114-1', 'ZDB-BLASTDB-090929-19'), ('ZDB-FDBCONT-040412-39', 'ZDB-BLASTDB-130401-2'), ('ZDB-FDBCONT-040412-47', 'ZDB-BLASTDB-130401-2');

--changeset cmpich:ZFIN-10602-ucsc-grcz12
update blast_database
   set blastdb_path = 'https://genome.ucsc.edu/cgi-bin/hgBlat?db=GCF_049306965.2',
       blastdb_description = 'blat at UCSC (GRCz12tu)'
 where blastdb_zdb_id = 'ZDB-BLASTDB-090929-18'; -- UCSC BLAT
update blast_database
   set blastdb_path = 'https://genome.ucsc.edu/cgi-bin/hgTracks?db=GCF_049306965.2&position=',
       blastdb_description = 'blat at UCSC (GRCz12tu)'
 where blastdb_zdb_id = 'ZDB-BLASTDB-090929-17'; -- BLAT
--rollback update blast_database set blastdb_path = 'https://genome.ucsc.edu/cgi-bin/hgBlat?db=danRer11', blastdb_description = 'blat at UCSC' where blastdb_zdb_id = 'ZDB-BLASTDB-090929-18';
--rollback update blast_database set blastdb_path = 'https://genome.ucsc.edu/cgi-bin/hgTracks?org=Zebrafish&db=danRer11&position=', blastdb_description = 'blat at UCSC' where blastdb_zdb_id = 'ZDB-BLASTDB-090929-17';
