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
