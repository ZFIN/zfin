--liquibase formatted sql
--changeset christian:ZFIN-7845

-- associate blastable database to fish mi RNA records
insert into int_fdbcont_analysis_tool
select fdbcont_zdb_id, 'ZDB-BLASTDB-071128-27'
from foreign_db,
     foreign_db_contains
where fdb_db_pk_id = fdbcont_fdb_db_id
  AND fdb_db_name = 'FishMiRNA';

-- create new blast database for Fish mi RNA sequences
insert into blast_database
select get_id('BLASTDB'), 'Loaded FishmiRNA Stem Loop', 'LoadedFishMicroRNAStemLoop', 'The loaded sequences from FishmiRNA database',
       'f','nucleotide',null,'Loaded FishmiRNA Stem Loop',null,'f',bdot_pk_id,null,null,null,null from blast_database_origination_type
where bdot_type = 'LOADED';

-- associate foreign_db_contains with this new blast database
update foreign_db_contains set fdbcont_primary_blastdb_zdb_id =
                                   (select blastdb_zdb_id from blast_database where blastdb_name = 'Loaded FishmiRNA Stem Loop')
where fdbcont_zdb_id = (select f.fdbcont_zdb_id from foreign_db_contains f, foreign_db where f.fdbcont_fdb_db_id = fdb_db_pk_id
    and fdb_db_name = 'FishMiRNA');

-- set the correct category for the main blast selection box
insert into blastdb_order (bdborder_parent_blastdb_zdb_id,bdborder_child_blastdb_zdb_id, bdborder_order)
select 'ZDB-BLASTDB-090929-6',blastdb_zdb_id, 30 from blast_database where  blastdb_name = 'Loaded FishmiRNA Stem Loop';
