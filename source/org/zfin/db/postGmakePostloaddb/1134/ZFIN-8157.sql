--liquibase formatted sql
--changeset cmpich:ZFIN-8157

delete
from zdb_active_data
where zactvd_zdb_id = 'ZDB-FISH-150901-13769';

delete
from zdb_active_data
where zactvd_zdb_id = 'ZDB-FISH-150901-22853';

select *
from record_attribution
where recattrib_data_zdb_id = 'ZDB-FISH-150901-13769';

select *
from record_attribution
where recattrib_data_zdb_id = 'ZDB-FISH-150901-22853';

select count(*) from fish where fish_zdb_id in ('ZDB-FISH-150901-22853', 'ZDB-FISH-150901-13769');

