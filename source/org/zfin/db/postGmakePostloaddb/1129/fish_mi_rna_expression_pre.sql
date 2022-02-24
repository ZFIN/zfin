--liquibase formatted sql
--changeset cmpich:ZFIN-7733

-- create new blast database

--insert into blast_database values (get_id('BLASTDB'), 'Curated NTR / Regions', 'CuratedNtrRegions', 'The curated NTR/Regions sequence db at ZFIN',
--                                   'f','nucleotide',null,'Curated NTR/Regions',null,'f',2,null,null,null,null);

-- create Fish miRNA FDB
insert into foreign_db (fdb_db_name, fdb_db_query, fdb_db_display_name, fdb_db_significance)
values ('FishMiRNA-Expression', 'http://fishmirna.org/index.html?fishmirna_mature_id=', 'Fish miRNA Expression', 3);

-- create Fish miRNA foreign_db_contains
insert into foreign_db_contains (fdbcont_organism_common_name, fdbcont_zdb_id, fdbcont_fdbdt_id,
                                 fdbcont_primary_blastdb_zdb_id, fdbcont_fdb_db_id)
SELECT 'Zebrafish', get_id('FDBCONT'), fdbdt_pk_id, null, fdb_db_pk_id
from foreign_db,
     foreign_db_data_type
where fdbdt_data_type = 'RNA'
  AND fdbdt_super_type = 'sequence'
order by fdb_db_pk_id desc
limit 1;

insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
select fdbcont_zdb_id, 18
from foreign_db,
     foreign_db_contains
where fdb_db_pk_id = fdbcont_fdb_db_id
  AND fdb_db_name = 'FishMiRNA-Expression';


create table fishmir_temp
(
    mir_gene_id VARCHAR(100)  NOT NULL,
    gene_zdb_id VARCHAR(100)  NOT NULL,
    sequence    VARCHAR(5000)
);


