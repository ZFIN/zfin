--liquibase formatted sql
--changeset pm:ZFIN-6778


insert into  expression_pattern_assay (xpatassay_name, xpatassay_display_order, xpatassay_abbrev, xpatassay_mmo_id)
values ('RNA Seq', 6, 'RNA-Seq', 'MMO:0000659');