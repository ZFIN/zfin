--liquibase formatted sql
--changeset pm:CUR-745

update organism set organism_is_ab_immun='t' where organism_common_name like 'Yeast';
update antibody set atb_immun_organism='Yeast' where atb_zdb_id='ZDB-ATB-180226-1';
