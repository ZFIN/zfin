--liquibase formatted sql
--changeset pm:ZFIN-6881


update so_zfin_mapping set szm_term_ont_id='SO:0001023' where szm_object_type ='COMPLEX_SUBSTITUTION';
update so_zfin_mapping set szm_term_ont_id='SO:0001059' where szm_object_type ='SEQUENCE_VARIANT';














