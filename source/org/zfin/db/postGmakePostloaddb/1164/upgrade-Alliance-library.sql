--liquibase formatted sql
--changeset cmpich:upgrade-alliance

delete
from marker_history
where mhist_dalias_zdb_id in (select dalias_zdb_id from data_alias where trim(dalias_alias) = '');
delete
from data_alias
where trim(dalias_alias) = '';

alter table data_alias
    add constraint check_not_empty check (length(trim(dalias_alias)) > 0);

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_alias_lower, dalias_group_id)
VALUES ('ZDB-DALIAS-241123-2', 'ZDB-GENE-000427-3', 'd', 'd  ', 1);
