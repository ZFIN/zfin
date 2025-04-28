--liquibase formatted sql
--changeset rtaylor:ZFIN-9663

update updates set rec_id = 'ZDB-COMPANY-120808-1' where rec_id = 'ZDB-COMPANY-181130-1';
update int_data_supplier set idsup_supplier_zdb_id = 'ZDB-COMPANY-120808-1' where idsup_supplier_zdb_id = 'ZDB-COMPANY-181130-1';

delete from company where zdb_id = 'ZDB-COMPANY-181130-1';
delete from zdb_active_source where zactvs_zdb_id = 'ZDB-COMPANY-181130-1';

INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when) VALUES
    ('ZDB-PERS-210917-1', 'ZDB-COMPANY-120808-1', 'zdb_id',  'ZDB-COMPANY-120808-1', 'ZDB-COMPANY-181130-1', 'Merged duplicate company ZDB-COMPANY-181130-1 (ZFIN-9663)', 'Taylor, Ryan', now());

