--liquibase formatted sql
--changeset prita:CUR-721

INSERT into organism (organism_species, organism_common_name,organism_display_order,organism_is_ab_immun,organism_taxid) values ('Canis lupus familiaris','Dog',210,'t',9615);
UPDATE antibody
SET atb_immun_organism='Dog'
where atb_zdb_id='ZDB-ATB-171102-1';
