--liquibase formatted sql
--changeset xshao:CUR-888

insert into organism (organism_species, organism_common_name, organism_display_order, organism_is_ab_immun, organism_taxid)
  values ('Coturnix coturnix', 'Common quail', '220', 't', '9091');


