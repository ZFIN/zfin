--liquibase formatted sql
--changeset cmpich:indexer-tuning.sql

CREATE INDEX term_phenotype_display_term ON UI.TERM_PHENOTYPE_DISPLAY
    (
     tpd_term_zdb_id
        );

CREATE INDEX term_phenotype_display_fish ON UI.TERM_PHENOTYPE_DISPLAY
    (
     tpd_fish_zdb_id
        );

CREATE INDEX term_phenotype_display_fig ON UI.TERM_PHENOTYPE_DISPLAY
    (
     tpd_fig_zdb_id
        );

CREATE INDEX term_phenotype_display_pub ON UI.TERM_PHENOTYPE_DISPLAY
    (
     tpd_pub_zdb_id
        );

CREATE INDEX phenotype_warehouse_association_warehouse ON UI.PHENOTYPE_WAREHOUSE_ASSOCIATION
    (
     pwa_phenotype_warehouse_id
    );

CREATE INDEX phenotype_warehouse_association_phenotype ON UI.PHENOTYPE_WAREHOUSE_ASSOCIATION
    (
     pwa_phenotype_id
    );

CREATE INDEX phenotype_zfin_association_gene ON UI.PHENOTYPE_ZFIN_ASSOCIATION
    (
     pza_gene_zdb_id
    );

CREATE INDEX phenotype_zfin_association_phenotype ON UI.PHENOTYPE_ZFIN_ASSOCIATION
    (
     pza_phenotype_id
    );


