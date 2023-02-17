--liquibase formatted sql
--changeset cmpich:zfin-8416a

drop table IF EXISTS UI.PHENOTYPE_ZFIN_ASSOCIATION;
drop table IF EXISTS UI.PHENOTYPE_WAREHOUSE_ASSOCIATION;
Drop table IF EXISTS UI.TERM_PHENOTYPE_DISPLAY;

create table UI.TERM_PHENOTYPE_DISPLAY
(
    tpd_id               serial8 not null,
    tpd_term_zdb_id      text,
    tpd_fish_zdb_id      text,
    tpd_fig_zdb_id       text,
    tpd_pub_zdb_id       text,
    tpd_pub_count        integer,
    tpd_fig_count        integer,
    tpd_fish_search      text,
    tpd_gene_search      text,
    tpd_phenotype_search text
);

ALTER TABLE UI.TERM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT TERM_PHENOTYPE_DISPLAY_un UNIQUE (tpd_id);

ALTER TABLE UI.TERM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (tpd_fig_zdb_id)
            REFERENCES figure (fig_zdb_id);

ALTER TABLE UI.TERM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk3
        FOREIGN KEY (tpd_fish_zdb_id)
            REFERENCES fish (fish_zdb_id);

ALTER TABLE UI.TERM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk4
        FOREIGN KEY (tpd_pub_zdb_id)
            REFERENCES publication (zdb_id);

ALTER TABLE UI.TERM_PHENOTYPE_DISPLAY
    ADD CONSTRAINT constraint_fk5
        FOREIGN KEY (tpd_term_zdb_id)
            REFERENCES term (term_zdb_id);

create table UI.PHENOTYPE_ZFIN_ASSOCIATION
(
    pza_gene_zdb_id  text,
    pza_phenotype_id integer
);

ALTER TABLE UI.PHENOTYPE_ZFIN_ASSOCIATION
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (pza_gene_zdb_id)
            REFERENCES MARKER (mrkr_zdb_id);

ALTER TABLE UI.PHENOTYPE_ZFIN_ASSOCIATION
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (pza_phenotype_id)
            REFERENCES UI.TERM_PHENOTYPE_DISPLAY (tpd_id);

create table UI.PHENOTYPE_WAREHOUSE_ASSOCIATION
(
    pwa_phenotype_warehouse_id integer,
    pwa_phenotype_id           integer
);

/*ALTER TABLE UI.PHENOTYPE_WAREHOUSE_ASSOCIATION
    ADD CONSTRAINT constraint_fk1
        FOREIGN KEY (pwa_phenotype_warehouse_id)
            REFERENCES PHENOTYPE_OBSERVATION_GENERATED (psg_id);
*/
ALTER TABLE UI.PHENOTYPE_WAREHOUSE_ASSOCIATION
    ADD CONSTRAINT constraint_fk2
        FOREIGN KEY (pwa_phenotype_id)
            REFERENCES UI.TERM_PHENOTYPE_DISPLAY (tpd_id);

