--liquibase formatted sql
--changeset rtaylor:zfin-8392

ALTER TABLE foreign_db_contains_display_group
    ADD CONSTRAINT fdbcdg_unique_name UNIQUE (fdbcdg_name);

INSERT INTO foreign_db_contains_display_group (fdbcdg_name, fdbcdg_definition)
    VALUES ('str edit page', 'a group of records available to select when adding external page link on str edit page');

INSERT INTO foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
    SELECT
        fdbcont_zdb_id,
        fdbcdg_pk_id
    FROM
        foreign_db_contains,
        foreign_db_contains_display_group
    WHERE
          fdbcdg_name = 'str edit page'
      AND fdbcont_zdb_id IN
        (
         'ZDB-FDBCONT-040412-1',  -- [ foreign_db.fdb_db_name = Gene ]
         'ZDB-FDBCONT-040412-14', -- [ foreign_db.fdb_db_name = VEGA ]
         'ZDB-FDBCONT-040412-41', -- [ foreign_db.fdb_db_name = Sanger_Clone ]
         'ZDB-FDBCONT-040826-2',  -- [ foreign_db.fdb_db_name = VEGA_Clone ]
         'ZDB-FDBCONT-060417-1',  -- [ foreign_db.fdb_db_name = Vega_Trans ]
         'ZDB-FDBCONT-060626-2',  -- [ foreign_db.fdb_db_name = Ensembl_SNP ]
         'ZDB-FDBCONT-060626-3',  -- [ foreign_db.fdb_db_name = dbSNP ]
         'ZDB-FDBCONT-061004-1',  -- [ foreign_db.fdb_db_name = Ensembl_Clone ]
         'ZDB-FDBCONT-061018-1',  -- [ foreign_db.fdb_db_name = Ensembl(GRCz11) ]
         'ZDB-FDBCONT-061129-1',  -- [ foreign_db.fdb_db_name = UniProtKB-KW ]
         'ZDB-FDBCONT-070718-1',  -- [ foreign_db.fdb_db_name = PreEnsembl(Zv7) ]
         'ZDB-FDBCONT-071009-1',  -- [ foreign_db.fdb_db_name = MODB ]
         'ZDB-FDBCONT-071128-4',  -- [ foreign_db.fdb_db_name = NovelGene ]
         'ZDB-FDBCONT-071228-1',  -- [ foreign_db.fdb_db_name = ZF-Espresso ]
         'ZDB-FDBCONT-090929-3',  -- [ foreign_db.fdb_db_name = miRBASE Mature ]
         'ZDB-FDBCONT-110301-1',  -- [ foreign_db.fdb_db_name = Ensembl_Trans ]
         'ZDB-FDBCONT-120213-1',  -- [ foreign_db.fdb_db_name = zfishbook ]
         'ZDB-FDBCONT-120411-1',  -- [ foreign_db.fdb_db_name = ZMP ]
         'ZDB-FDBCONT-130516-2',  -- [ foreign_db.fdb_db_name = zfishbook-constructs ]
         'ZDB-FDBCONT-140217-1',  -- [ foreign_db.fdb_db_name = CreZoo ]
         'ZDB-FDBCONT-160128-1',  -- [ foreign_db.fdb_db_name = CRISPRz ]
         'ZDB-FDBCONT-160215-1',  -- [ foreign_db.fdb_db_name = RRID ]
         'ZDB-FDBCONT-171115-1',  -- [ foreign_db.fdb_db_name = CZRC ]
         'ZDB-FDBCONT-131021-1',  -- [ foreign_db.fdb_db_name = Ensembl ]
         'ZDB-FDBCONT-190723-1',  -- [ foreign_db.fdb_db_name = ABRegistry ]
         'ZDB-FDBCONT-220301-1'   -- [ foreign_db.fdb_db_name = FishMiRNA ]
        );