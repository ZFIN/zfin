--liquibase formatted sql
--changeset cmpich:ZFIN-7770

-- create new blast database

--insert into blast_database values (get_id('BLASTDB'), 'Curated NTR / Regions', 'CuratedNtrRegions', 'The curated NTR/Regions sequence db at ZFIN',
--                                   'f','nucleotide',null,'Curated NTR/Regions',null,'f',2,null,null,null,null);

-- create blast database , curated, pointing to 'CuratedNtrRegions' blast database file (also AvailabelAbbrev enum)
insert into blast_database
select get_id('BLASTDB'), 'Curated NTR / Regions', 'CuratedNtrRegions', 'The curated NTR/Regions sequence db at ZFIN',
                                   'f','nucleotide',null,'Curated NTR/Regions',null,'f',bdot_pk_id,null,null,null,null from blast_database_origination_type
                                       where bdot_type = 'CURATED';

-- create NTR foreign_db
insert into foreign_db (fdb_db_name, fdb_db_query, fdb_db_display_name, fdb_db_significance) values ('NTR-Region', '/action/blast/display-sequence?accession=','NTR / Region',3 );

-- create Ntr foreign_db_contains , pointing to new blast database and types other,seqyence
insert into foreign_db_contains (fdbcont_organism_common_name, fdbcont_zdb_id, fdbcont_fdbdt_id, fdbcont_primary_blastdb_zdb_id, fdbcont_fdb_db_id)
select 'Zebrafish', get_id('FDBCONT'),fdbdt_pk_id,blastdb_zdb_id , fdb_db_pk_id from foreign_db, foreign_db_data_type, blast_database
where fdbdt_data_type = 'other' AND fdbdt_super_type = 'sequence'
AND blastdb_name = 'Curated NTR / Regions'
order by fdb_db_pk_id desc limit 1;

-- set display groups
insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
select 'ZDB-FDBCONT-220124-1',fdbcdg_pk_id from foreign_db_contains_display_group where fdbcdg_name = 'displayed nucleotide sequence';
insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
select 'ZDB-FDBCONT-220124-1',fdbcdg_pk_id from foreign_db_contains_display_group where fdbcdg_name = 'transcript edit addable nucleotide sequence';
