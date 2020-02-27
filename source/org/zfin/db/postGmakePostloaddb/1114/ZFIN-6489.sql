--liquibase formatted sql
--changeset pm:ZFIN-6489

update so_zfin_mapping
set szm_term_ont_id='SO:0000715'
where szm_object_type='RNAMO';