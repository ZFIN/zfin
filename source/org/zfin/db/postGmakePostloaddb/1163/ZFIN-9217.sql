--liquibase formatted sql
--changeset cmpich:ZFIN-9217.sql

create temp table id
(
    id varchar
);

insert into id
values (get_id('BLASTDB'));

insert into blast_database (blastdb_zdb_id,
                            blastdb_name,
                            blastdb_abbrev,
                            blastdb_description,
                            blastdb_public,
                            blastdb_type,
                            blastdb_tool_display_name,
                            blastdb_is_being_processed,
                            blastdb_origination_id)

select id,
       'Ensembl GRCz11 ZFIN Transcripts',
       'ensembl_zf_only',
       'Ensembl Transcripts that exist in ZFIN',
       false,
       'nucleotide',
       'Ensembl GRCz11 ZFIN Transcripts',
       false,
       3
from id;

insert into blastdb_order (bdborder_parent_blastdb_zdb_id, bdborder_child_blastdb_zdb_id, bdborder_order)
select 'ZDB-BLASTDB-090929-27', id, 120
from id;

delete
from blastdb_order
where bdborder_parent_blastdb_zdb_id = 'ZDB-BLASTDB-090929-27'
  AND bdborder_child_blastdb_zdb_id = 'ZDB-BLASTDB-071128-2';

insert into int_fdbcont_analysis_tool (ifat_fdbcont_zdb_id, ifat_blastdb_zdb_id)
VALUES ('ZDB-FDBCONT-110301-1','ZDB-BLASTDB-090929-27');
