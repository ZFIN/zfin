--liquibase formatted sql
--changeset cmpich:ZFIN-1014

insert into organism (organism_species, organism_common_name, organism_display_order, organism_is_ab_immun, organism_taxid)
values ('Pacific electric ray', 'Torpedo californica', '225', 't', '7787');


