--liquibase formatted sql
--changeset rtaylor:ZFIN-9230.sql

INSERT INTO organism ("organism_species", "organism_common_name", "organism_display_order",
                                 "organism_is_ab_immun", "organism_is_ab_host", "organism_taxid")
VALUES ('Serinus canaria', 'Common canary', 176, 't', 't', 9135);