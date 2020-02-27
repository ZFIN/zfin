--liquibase formatted sql
--changeset kschaper:SRCH-1154.sql

update controlled_vocabulary
set cv_foreign_species = 'pufferfish',
    cv_name_definition = 'Takifugu rubripes'
where cv_zdb_id = 'ZDB-CV-150506-26';

update controlled_vocabulary
set cv_foreign_species = 'pufferfish',
    cv_name_definition = 'Tetraodon nigroviridis'
where cv_zdb_id = 'ZDB-CV-150506-20';

update controlled_vocabulary
set cv_foreign_species = 'yellow catfish',
    cv_name_definition = 'Pelteobagrus fulvidraco'
where cv_zdb_id = 'ZDB-CV-150506-52';
