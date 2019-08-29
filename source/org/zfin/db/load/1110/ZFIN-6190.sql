--liquibase formatted sql
--changeset kschaper:ZFIN-6190.sql

update so_zfin_mapping set (szm_term_ont_id, szm_term_name) = ('SO:0001217', 'protein_coding_gene') where szm_object_type = 'GENE';

