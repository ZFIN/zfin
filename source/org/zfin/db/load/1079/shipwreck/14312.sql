--liquibase formatted sql
--changeset pm:14312

insert into withdrawn_Data (wd_old_zdb_id,wd_new_zdb_id,wd_display_note) values ('ZDB-GENO-120130-866', 'ZDB-PUB-121121-2',' deleted b/c not submitted in 2012-10 B/L load');
insert into withdrawn_Data (wd_old_zdb_id,wd_new_zdb_id,wd_display_note) values ('ZDB-GENO-120130-721', 'ZDB-PUB-121121-2',' deleted b/c not submitted in 2012-10 B/L load');
insert into withdrawn_Data (wd_old_zdb_id,wd_new_zdb_id,wd_display_note) values ('ZDB-GENO-120806-107', 'ZDB-PUB-121121-2',' deleted b/c not submitted in 2012-10 B/L load');
delete from zdb_active_data where zactvd_zdb_id in ('ZDB-GENO-120130-866','ZDB-GENO-120130-721','ZDB-GENO-120806-107');


