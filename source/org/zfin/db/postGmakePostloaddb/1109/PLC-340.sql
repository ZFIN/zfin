--liquibase formatted sql
--changeset xshao:PLC-340

update pub_correspondence_recipient 
                                set pubcr_recipient_person_zdb_id = 'ZDB-PERS-160503-1'
                              where pubcr_recipient_person_zdb_id = 'ZDB-PERS-120309-1';

delete from withdrawn_data where wd_old_zdb_id = 'ZDB-PERS-120309-1';

update withdrawn_data set wd_new_zdb_id = 'ZDB-PERS-160503-1' where wd_new_zdb_id = 'ZDB-PERS-120309-1';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PERS-120309-1';

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id, wd_display_note) values ('ZDB-PERS-120309-1', 'ZDB-PERS-160503-1', 'merged');

