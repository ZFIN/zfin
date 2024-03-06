--liquibase formatted sql
--changeset cmpich:ZFIN-transcript-ensembl.sql

delete from transcript where tscript_mrkr_zdb_id = 'ZDB-TSCRIPT-240305-1';
delete from marker where mrkr_zdb_id = 'ZDB-TSCRIPT-240305-1';
delete from marker_relationship where mrel_mrkr_2_zdb_id = 'ZDB-TSCRIPT-240305-1';
delete from db_link where dblink_linked_recid = 'ZDB-TSCRIPT-240305-1';
delete from foreign_db_contains_display_group_member where fdbcdgm_fdbcont_zdb_id = 'ZDB-FDBCONT-240304-1';
delete from foreign_db_contains where fdbcont_zdb_id = 'ZDB-FDBCONT-240304-1';



-- add ensemb_trans to Nucleotide_sequence display group
insert into foreign_db_contains (fdbcont_organism_common_name, fdbcont_zdb_id, fdbcont_fdbdt_id, fdbcont_fdb_db_id, fdbcont_primary_blastdb_zdb_id)
values ('Zebrafish', 'ZDB-FDBCONT-240304-1', 3, 61, 'ZDB-BLASTDB-130708-1' );

alter table transcript add column tscript_genotype_zdb_id text;

alter table transcript
    add constraint transcript_genotype_foreign_key
    Foreign key (tscript_genotype_zdb_id)
        references genotype (geno_zdb_id) ;


insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
VALUES ('ZDB-FDBCONT-240304-1',9);

insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
VALUES ('ZDB-FDBCONT-240304-1',12);

