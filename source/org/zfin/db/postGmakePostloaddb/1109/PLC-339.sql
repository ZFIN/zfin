--liquibase formatted sql
--changeset xshao:PUB-527

update int_person_pub 
                                set source_id = 'ZDB-PERS-090406-8'
                              where source_id = 'ZDB-PERS-110815-2';

delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PERS-110815-2';

update withdrawn_data set wd_new_zdb_id = 'ZDB-PERS-090406-8' where wd_new_zdb_id = 'ZDB-PERS-110815-2';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PERS-110815-2';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PERS-110815-2', 'ZDB-PERS-090406-8', 'merged');

update person set email = null where zdb_id in ('ZDB-PERS-170918-1', 'ZDB-PERS-170918-2');

update person set email = null where email = '';

alter table person add constraint email_index unique (email);

