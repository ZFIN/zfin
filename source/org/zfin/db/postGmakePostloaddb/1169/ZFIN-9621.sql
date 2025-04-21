--liquibase formatted sql
--changeset cmpich:ZFIN-9621

delete from accession_bank  where exists (select trim(id) from zfinnucl where trim(id) = accbk_acc_num);
delete from db_link  where exists (select trim(id) from zfinnucl where lower(trim(id)) = dblink_acc_num);

drop table if exists zfinnucl cascade;
